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