package org.uniupo.it.dao;

/**
 * Classe contenente tutte le query SQL utilizzate nell'applicazione.
 * Le query sono organizzate per entità e tipo di operazione.
 */
public final class SQLQueries {
    private SQLQueries() {

    }

    public static String getSchemaName(String instituteId, String machineId) {
        return String.format("machine_%s_%s",
                instituteId.toLowerCase().replace("-", "_"),
                machineId.toLowerCase().replace("-", "_"));
    }


    public static final class Balance {

        private static final String GET_TOTAL_BALANCE =
                "SELECT \"totalBalance\" FROM %s.\"Machine\"";

        public static String getGetTotalBalance(String instituteId, String machineId) {
            return String.format(GET_TOTAL_BALANCE, getSchemaName(instituteId, machineId));
        }

        private static final String RESET_CREDIT = "UPDATE %s.\"Machine\" SET \"totalCredit\" = 0";

        public static String getResetCredit(String instituteId, String machineId) {
            return String.format(RESET_CREDIT, getSchemaName(instituteId, machineId));
        }


        private static final String GET_MAX_BALANCE =
                "SELECT \"maxBalance\" FROM %s.\"Machine\"";

        public static String getGetMaxBalance(String instituteId, String machineId) {
            return String.format(GET_MAX_BALANCE, getSchemaName(instituteId, machineId));
        }


        private static final String UPDATE_TOTAL_BALANCE =
                "UPDATE %s.\"Machine\" SET \"totalBalance\" = ?";

        public static String getUpdateTotalBalance(String instituteId, String machineId) {
            return String.format(UPDATE_TOTAL_BALANCE, getSchemaName(instituteId, machineId));
        }


        private static final String GET_CURRENT_CREDIT =
                "SELECT \"totalCredit\" FROM %s.\"Machine\" LIMIT 1";

        public static String getGetCurrentCredit(String instituteId, String machineId) {
            return String.format(GET_CURRENT_CREDIT, getSchemaName(instituteId, machineId));
        }

        private static final String CHECK_CASH_BOX = """
            SELECT "totalBalance", "maxBalance"
            FROM %s."Machine"
            WHERE "totalBalance" >= "maxBalance" * 0.8;""";

        public static String getCheckCashBox(String instituteId, String machineId) {
            return String.format(CHECK_CASH_BOX, getSchemaName(instituteId, machineId));
        }



        private static final String CHECK_CASH_BOX_FULL = """
            SELECT "totalBalance", "maxBalance"
            FROM %s."Machine"
            WHERE "totalBalance" >= "maxBalance";""";

        public static String getCheckCashBoxFull(String instituteId, String machineId) {
            return String.format(CHECK_CASH_BOX_FULL, getSchemaName(instituteId, machineId));
        }

        public static final String INSERT_FAULTS = """
                INSERT INTO %s."Fault" (description, id_fault, timestamp, fault_type)\s
                VALUES (?, ?, ?, ?)""";

        public static String getInsertFaults(String instituteId, String machineId) {
            return String.format(INSERT_FAULTS, getSchemaName(instituteId, machineId));
        }

        private static final String EMPTY_CASH_BOX =
                "UPDATE %s.\"Machine\" SET \"totalBalance\" = 0 RETURNING \"totalBalance\"";

        public static String getEmptyCashBox(String instituteId, String machineId) {
            return String.format(EMPTY_CASH_BOX, getSchemaName(instituteId, machineId));
        }

        private static final String GET_CASH_FULL_FAULTS =
                "SELECT id_fault FROM %s.\"Fault\" WHERE fault_type = ?::%s.\"fault_type\" AND risolto = false;";

        public static String getGetCashFullFaults(String instituteId, String machineId) {
            String schemaName = getSchemaName(instituteId, machineId);
            return String.format(GET_CASH_FULL_FAULTS, schemaName, schemaName);
        }

        private static final String UPDATE_CASH_FULL_FAULTS =
                "UPDATE %s.\"Fault\" SET risolto = true WHERE fault_type = ?::%s.\"fault_type\" AND risolto = false;";

        public static String getUpdateCashFullFaults(String instituteId, String machineId) {
            String schemaName = getSchemaName(instituteId, machineId);
            return String.format(UPDATE_CASH_FULL_FAULTS, schemaName, schemaName);
        }

        private static final String CHECK_UNRESOLVED_FAULTS =
                "SELECT COUNT(*) as count FROM %s.\"Fault\" WHERE risolto = false;";

        public static String getCheckUnresolvedFaults(String instituteId, String machineId) {
            return String.format(CHECK_UNRESOLVED_FAULTS, getSchemaName(instituteId, machineId));
        }

    }


    public static final class Drink {

        private static final String GET_DRINK_PRICE =
                "SELECT price FROM %s.\"Drink\" WHERE code = ?";

        public static String getGetDrinkPrice(String instituteId, String machineId) {
            return String.format(GET_DRINK_PRICE, getSchemaName(instituteId, machineId));
        }
    }
}