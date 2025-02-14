package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.dao.DaoBalanceImpl;
import org.uniupo.it.model.*;
import org.uniupo.it.util.Topics;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BalanceService {
    final private String machineId;
    final private MqttClient mqttClient;
    final private String instituteId;
    final private Gson gson;
    final private DaoBalanceImpl balanceDao;

    public BalanceService(String instituteId, String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.instituteId = instituteId;
        this.gson = new Gson();
        this.balanceDao = new DaoBalanceImpl(instituteId, machineId);
        this.mqttClient.subscribe(String.format(Topics.BALANCE_CHECK_TOPIC, instituteId, machineId), this::balanceRequestHandler);
        this.mqttClient.subscribe(String.format(Topics.DISPENSE_COMPLETED_TOPIC, instituteId, machineId), this::handleBalanceAfterSale);
        this.mqttClient.subscribe(String.format(Topics.BALANCE_RETURN_MONEY_TOPIC, instituteId, machineId), this::returnMoney);
        this.mqttClient.subscribe(String.format(Topics.MANAGEMENT_REVENUE_TOPIC, instituteId, machineId), this::revenueRequestHandler);
        this.mqttClient.subscribe(String.format(Topics.KILL_SERVICE_TOPIC, instituteId, machineId), this::killServiceHandler);
    }

    private void killServiceHandler(String topic, MqttMessage message) {
        System.out.println("Service killed hello darkness my old friend :(");
        new Thread(()->{
            try {
                Thread.sleep(1000);
                if(mqttClient.isConnected()) {
                    mqttClient.disconnect();
                }
                mqttClient.close();
                System.exit(0);
            } catch (Exception e) {
                System.err.println("Error during shutdown: "+e.getMessage());
                Runtime.getRuntime().halt(1);
            }
        }).start();
    }


    private void revenueRequestHandler(String topic, MqttMessage message) {
        System.out.println("Received revenue request");
        String revenueRequester = new String(message.getPayload());
        double revenue = balanceDao.getTotalBalance();
        balanceDao.emptyCashBox();
        System.out.println("Revenue: " + revenue);

        Ricavo ricavoMessage = new Ricavo(machineId,Integer.parseInt(instituteId),new BigDecimal(revenue),revenueRequester);
        try {
            mqttClient.publish(Topics.MANAGEMENT_REVENUE_TOPIC_RESPONSE, new MqttMessage(gson.toJson(ricavoMessage).getBytes()));
            System.out.println("Published revenue response");
            List<UUID> resolvedFaultsUUID = balanceDao.solveCashFullFaults();
            mqttClient.publish(Topics.MANAGEMENT_RESOLVE_FAULT_TOPIC, new MqttMessage(gson.toJson(resolvedFaultsUUID).getBytes()));
            System.out.println("Published resolved faults");
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing revenue response", e);
        }
    }


    private void returnMoney(String topic, MqttMessage message) {
        double amount = balanceDao.returnMoney();
        try {
            DisplayMessageFormat displayMessage = new DisplayMessageFormat(false, "Resto: " + amount);
            mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(displayMessage).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing balance response", e);
        }
    }

    private void balanceRequestHandler(String topic, MqttMessage mqttMessage) {
        String drinkCode = new String(mqttMessage.getPayload());
        System.out.println("Checking balance for drink code: " + drinkCode);
        boolean isBalanceOk = checkBalance(drinkCode);
        System.out.println("Balance check result: " + isBalanceOk);
        try {
            mqttClient.publish(String.format(Topics.BALANCE_CHECK_TOPIC_RESPONSE, instituteId, machineId), new MqttMessage(gson.toJson(isBalanceOk).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing balance response", e);
        }
    }

    private boolean checkBalance(String drinkCode) {
        double currentBalance = balanceDao.getTotalBalance();
        double maxBalance = balanceDao.getMaxBalance();
        double drinkPrice = balanceDao.getDrinkPrice(drinkCode);

        System.out.println("Current balance: " + currentBalance);
        System.out.println("Max balance: " + maxBalance);
        System.out.println("Drink price: " + drinkPrice);

        return (currentBalance + drinkPrice) <= maxBalance;
    }

    private void handleBalanceAfterSale(String topic, MqttMessage message) {
        Selection selection = gson.fromJson(new String(message.getPayload()), Selection.class);
        double drinkPrice = balanceDao.getDrinkPrice(selection.getDrinkCode());
        System.out.println("Drink price: " + drinkPrice);
        double currentCredit = balanceDao.getCurrentCredit();
        System.out.println("Current credit: " + currentCredit);
        double change = currentCredit - drinkPrice;
        try {
            DisplayMessageFormat displayMessage = new DisplayMessageFormat(false, String.format("Resto: %.2f", change));
            mqttClient.publish(String.format(Topics.DISPLAY_TOPIC_UPDATE, instituteId, machineId), new MqttMessage(gson.toJson(displayMessage).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing balance response", e);
        }
        balanceDao.updateBalanceAfterSale(drinkPrice);

        balanceCheckUp();
    }

    private void balanceCheckUp() {
        if (balanceDao.checkCashBoxFull()) {
            notifyCashBoxFull();
        } else if (balanceDao.checkCashBox()) {
            notifyCashBoxNearlyFull();
        }

    }

    private void notifyCashBoxNearlyFull() {
        List<FaultMessage> faults = new ArrayList<>();
        faults.add(new FaultMessage(machineId, "Cash box nearly full", Integer.parseInt(instituteId), new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), FaultType.CASSA_PIENA));
        balanceDao.insertFaults(faults);
        try {
            mqttClient.publish(Topics.MANAGEMENT_SERVER_CASHBOX_TOPIC, new MqttMessage(gson.toJson(faults).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing cash box nearly full message", e);
        }
    }

    private void notifyCashBoxFull() {
        try {
            List<FaultMessage> faults = new ArrayList<>();
            faults.add(new FaultMessage(machineId, "Cash box full", Integer.parseInt(instituteId), new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), FaultType.CASSA_PIENA));
            System.out.println("Cash box full" + faults + " " + Topics.MANAGEMENT_SERVER_CASHBOX_TOPIC);
            balanceDao.insertFaults(faults);
            mqttClient.publish(Topics.MANAGEMENT_SERVER_CASHBOX_TOPIC, new MqttMessage(gson.toJson(faults).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing cash box full message", e);
        }
    }

}
