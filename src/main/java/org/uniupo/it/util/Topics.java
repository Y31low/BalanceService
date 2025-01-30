package org.uniupo.it.util;

public class Topics {

    private final static String BASE_TOPIC = "istituto/%s/macchina/%s";
    public static final String DISPLAY_TOPIC_UPDATE = BASE_TOPIC+"/frontend/screen/update";

    public static final String BALANCE_CHECK_TOPIC = BASE_TOPIC+"/balance/checkBalance";
    public static final String BALANCE_CHECK_TOPIC_RESPONSE = BASE_TOPIC+"/transaction/checkBalanceResponse";
    public static final String DISPENSE_COMPLETED_TOPIC = BASE_TOPIC+"/dispenser/dispenseCompleted";
    public static final String BALANCE_RETURN_MONEY_TOPIC = BASE_TOPIC+"/balance/returnMoney";

    public static final String MANAGEMENT_SERVER_CASHBOX_TOPIC = "managementServer/faults/cashbox";
}
