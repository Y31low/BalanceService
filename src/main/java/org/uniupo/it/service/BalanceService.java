package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.dao.DaoBalanceImpl;
import org.uniupo.it.model.DisplayMessageFormat;
import org.uniupo.it.model.FaultMessage;
import org.uniupo.it.model.FaultType;
import org.uniupo.it.model.Selection;
import org.uniupo.it.util.Topics;

import java.sql.Timestamp;
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
        this.balanceDao = new DaoBalanceImpl();
        this.mqttClient.subscribe(String.format(Topics.BALANCE_CHECK_TOPIC, instituteId, machineId), this::balanceRequestHandler);
        this.mqttClient.subscribe(String.format(Topics.DISPENSE_COMPLETED_TOPIC, instituteId, machineId), this::handleBalanceAfterSale);
        this.mqttClient.subscribe(String.format(Topics.BALANCE_RETURN_MONEY_TOPIC, instituteId, machineId), this::returnMoney);
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
        System.out.printf("Change: %.2f\n", change);
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
        FaultMessage fault = new FaultMessage(machineId, "Cash box nearly full", 1, new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), FaultType.CASSA_QUASI_PIENA);
        try {
            mqttClient.publish(Topics.MANAGEMENT_SERVER_CASHBOX_TOPIC, new MqttMessage(gson.toJson(fault).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing cash box nearly full message", e);
        }
    }

    private void notifyCashBoxFull() {
        try {
            FaultMessage fault = new FaultMessage(machineId, "Cash box full", 1, new Timestamp(System.currentTimeMillis()), UUID.randomUUID(), FaultType.CASSA_PIENA);
            mqttClient.publish(Topics.MANAGEMENT_SERVER_CASHBOX_TOPIC, new MqttMessage(gson.toJson(fault).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing cash box full message", e);
        }
    }

}
