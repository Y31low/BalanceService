package org.uniupo.it.service;

import com.google.gson.Gson;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.uniupo.it.util.Topics;

public class BalanceService {
    final private String machineId;
    final private MqttClient mqttClient;
    final private String baseTopic;
    final private Gson gson;

    public BalanceService(String machineId, MqttClient mqttClient) throws MqttException {
        this.machineId = machineId;
        this.mqttClient = mqttClient;
        this.baseTopic = "macchina/" + machineId + "/balance";
        this.gson = new Gson();
        this.mqttClient.subscribe(baseTopic + "/balanceRequest", this::balanceRequestHandler);
    }

    private void balanceRequestHandler(String s, MqttMessage mqttMessage) {
    }
}
