package org.uniupo.it.dao;

import org.uniupo.it.model.FaultMessage;

import java.util.List;
import java.util.UUID;

public interface DaoBalance {
    /**
     * Recupera il totale presente in cassa
     * @return il totale presente in cassa
     */
    double getTotalBalance();

    /**
     * Recupera la capacità massima della cassa
     * @return la capacità massima della cassa
     */
    double getMaxBalance();

    /**
     * Recupera il prezzo di una bevanda
     * @param drinkCode codice della bevanda
     * @return il prezzo della bevanda
     */
    double getDrinkPrice(String drinkCode);

    /**
     * Aggiorna il totale in cassa
     * @param newBalance nuovo totale
     * @return true se l'operazione è andata a buon fine, false altrimenti
     */
    boolean updateTotalBalance(double newBalance);

    double getCurrentCredit();

    void updateBalanceAfterSale(double drinkPrice);

    boolean checkCashBox();
    boolean checkCashBoxFull();
    void insertFaults(List<FaultMessage> faults);

    double returnMoney();

    double emptyCashBox();

    List<UUID> solveCashFullFaults();
}
