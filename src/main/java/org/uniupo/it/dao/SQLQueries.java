package org.uniupo.it.dao;

/**
 * Classe contenente tutte le query SQL utilizzate nell'applicazione.
 * Le query sono organizzate per entità e tipo di operazione.
 */
public final class SQLQueries {
    private SQLQueries() {
        // Costruttore privato per impedire l'istanziazione
    }

    /**
     * Query relative alla gestione del bilancio e delle transazioni monetarie
     */
    public static final class Balance {
        /**
         * Query per ottenere il bilancio totale della macchina
         */
        public static final String GET_TOTAL_BALANCE =
                "SELECT \"totalBalance\" FROM machine.\"Machine\"";

        public static final String RESET_CREDIT = "UPDATE machine.\"Machine\" SET \"totalCredit\" = 0";

        /**
         * Query per ottenere il bilancio massimo della macchina
         */
        public static final String GET_MAX_BALANCE =
                "SELECT \"maxBalance\" FROM machine.\"Machine\"";

        /**
         * Query per aggiornare il bilancio totale della macchina
         */
        public static final String UPDATE_TOTAL_BALANCE =
                "UPDATE machine.\"Machine\" SET \"totalBalance\" = ?";


        public static final String GET_CURRENT_CREDIT =
                "SELECT \"totalCredit\" FROM machine.\"Machine\" LIMIT 1";

        public static final String CHECK_CASH_BOX = """
            SELECT "totalBalance", "maxBalance"
            FROM machine."Machine"
            WHERE "totalBalance" >= "maxBalance" * 0.8;""";

        public static final String CHECK_CASH_BOX_FULL = """
            SELECT "totalBalance", "maxBalance"
            FROM machine."Machine"
            WHERE "totalBalance" >= "maxBalance";""";

        public static final String INSERT_FAULTS = """
                INSERT INTO machine."Fault" (description, id_fault, timestamp, fault_type)\s
                VALUES (?, ?, ?, ?)""";

    }

    /**
     * Query relative alla gestione delle bevande
     */
    public static final class Drink {
        /**
         * Query per ottenere il prezzo di una bevanda
         */
        public static final String GET_DRINK_PRICE =
                "SELECT price FROM machine.\"Drink\" WHERE code = ?";
    }
}