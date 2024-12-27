package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.dao.DaoBalanceImpl;
import org.uniupo.it.model.TransactionRequest;
import org.uniupo.it.util.Topics;

public class BalanceService {
    final private String machineId;
    final private MqttClient mqttClient;
    final private String baseTopic;
    final private Gson gson;
    final private DaoBalanceImpl balanceDao;

    public BalanceService(String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.baseTopic = "macchina/" + machineId + "/balance";
        this.gson = new Gson();
        this.balanceDao = new DaoBalanceImpl();
        this.mqttClient.subscribe(String.format(Topics.BALANCE_CHECK_TOPIC, machineId), this::balanceRequestHandler);
        this.mqttClient.subscribe(String.format(Topics.PROGRESS_TRANSACTION_TOPIC, machineId), this::handleTransaction);
    }

    private void balanceRequestHandler(String topic, MqttMessage mqttMessage) {
        String drinkCode = new String(mqttMessage.getPayload());
        System.out.println("Checking balance for drink code: " + drinkCode);
        boolean isBalanceOk = checkBalance(drinkCode);
        System.out.println("Balance check result: " + isBalanceOk);
        try {
            mqttClient.publish(String.format(Topics.BALANCE_CHECK_TOPIC_RESPONSE, machineId), new MqttMessage(gson.toJson(isBalanceOk).getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException("Error publishing balance response", e);
        }
    }

    public boolean checkBalance(String drinkCode) {
        double currentBalance = balanceDao.getTotalBalance();
        double maxBalance = balanceDao.getMaxBalance();
        double drinkPrice = balanceDao.getDrinkPrice(drinkCode);

        System.out.println("Current balance: " + currentBalance);
        System.out.println("Max balance: " + maxBalance);
        System.out.println("Drink price: " + drinkPrice);

        return (currentBalance + drinkPrice) <= maxBalance;
    }

    private void handleTransaction(String topic, MqttMessage message) {
        TransactionRequest request = gson.fromJson(new String(message.getPayload()), TransactionRequest.class);
        double change = request.currentCredit() - request.drinkPrice();

        try {
            updateBalanceAfterSale(request.drinkPrice());
            if (change >= 0) {
                mqttClient.publish(String.format(Topics.CHANGE_TOPIC, machineId), new MqttMessage(String.valueOf(change).getBytes()));
                mqttClient.publish(String.format(Topics.DISPLAY_TOPIC, machineId), new MqttMessage(String.format("Total change: %.2f€", change).getBytes()));
            }
            else{
                mqttClient.publish(String.format(Topics.DISPLAY_TOPIC, machineId), new MqttMessage("Total change: 0€".getBytes()));
            }
        } catch (MqttException e) {
            throw new RuntimeException("Error processing transaction", e);
        }
    }

    private void updateBalanceAfterSale(double credit) {
        double currentBalance = balanceDao.getTotalBalance();
        double newBalance = currentBalance + credit;
        balanceDao.updateTotalBalance(newBalance);
    }
}
