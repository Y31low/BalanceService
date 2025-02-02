package org.uniupo.it.dao;

import org.uniupo.it.model.FaultMessage;
import org.uniupo.it.model.FaultType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DaoBalanceImpl implements DaoBalance{
    @Override
    public double getTotalBalance() {
        try(Connection conn= DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    SQLQueries.Balance.GET_TOTAL_BALANCE)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("totalBalance");
                }
                return 0.0;
            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving total balance", e);
            }

        }
        catch (SQLException e) {
            throw new RuntimeException("Error getting total balance", e);
        }
    }

    @Override
    public double getMaxBalance() {
        try(Connection conn= DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    SQLQueries.Balance.GET_MAX_BALANCE)) {
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("maxBalance");
                }
                return 0.0;
            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving max balance", e);
            }

        }
        catch (SQLException e) {
            throw new RuntimeException("Error getting max balance", e);
        }
    }

    @Override
    public double getDrinkPrice(String drinkCode) {
        try(Connection conn= DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    SQLQueries.Drink.GET_DRINK_PRICE)) {
                stmt.setString(1, drinkCode);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getDouble("price");
                }
                return 0.0;
            } catch (SQLException e) {
                throw new RuntimeException("Error retrieving drink price", e);
            }

        }
        catch (SQLException e) {
            throw new RuntimeException("Error getting drink price", e);
        }
    }

    @Override
    public boolean updateTotalBalance(double newBalance) {
        try(Connection conn= DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Balance.UPDATE_TOTAL_BALANCE)) {
                stmt.setDouble(1, newBalance);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Error updating total balance", e);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException("Error updating total balance", e);
        }
    }

    @Override
    public void updateBalanceAfterSale(double drinkPrice) {
        try(Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                double currentBalance;
                try (PreparedStatement stmtGetBalance = conn.prepareStatement(SQLQueries.Balance.GET_TOTAL_BALANCE)) {
                    ResultSet rs = stmtGetBalance.executeQuery();
                    currentBalance = rs.next() ? rs.getDouble("totalBalance") : 0.0;
                }

                try (PreparedStatement stmtBalance = conn.prepareStatement(SQLQueries.Balance.UPDATE_TOTAL_BALANCE)) {
                    System.out.println("Updating balance after sale");
                    stmtBalance.setDouble(1, currentBalance + drinkPrice);
                    stmtBalance.executeUpdate();
                    System.out.println("Updated balance after sale");
                }

                try (PreparedStatement stmtCredit = conn.prepareStatement(SQLQueries.Balance.RESET_CREDIT)) {
                    System.out.println("Resetting credit");
                    stmtCredit.executeUpdate();
                    System.out.println("Credit reset");
                }
                System.out.println("Committing transaction");
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error updating balance after sale", e);
            } finally {
                System.out.println("Ripristino autocommit");
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating balance after sale", e);
        }
    }

    @Override
    public double getCurrentCredit() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Balance.GET_CURRENT_CREDIT)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("totalCredit");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0.0;
    }

    @Override
    public boolean checkCashBox() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Balance.CHECK_CASH_BOX)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check cash box status", e);
        }
    }

    @Override
    public boolean checkCashBoxFull() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQLQueries.Balance.CHECK_CASH_BOX_FULL)) {
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {

            throw new RuntimeException("Failed to check if cash box is full", e);
        }
    }

    @Override
    public void insertFaults(List<FaultMessage> faults) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQLQueries.Balance.INSERT_FAULTS)) {

            for (FaultMessage fault : faults) {
                pstmt.setString(1, fault.getDescription());
                pstmt.setObject(2, fault.getIdFault());
                pstmt.setTimestamp(3, fault.getTimestamp());
                pstmt.setObject(4, fault.getFaultType().toString(), Types.OTHER);
                pstmt.addBatch();
            }

            pstmt.executeBatch();

        } catch (SQLException e) {
            System.out.println("Failed to insert faults" + e.getMessage());
            throw new RuntimeException("Failed to insert faults", e);
        }
    }

    @Override
    public double returnMoney() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                double creditToReturn;
                try (PreparedStatement stmtGetCredit = conn.prepareStatement(SQLQueries.Balance.GET_CURRENT_CREDIT)) {
                    ResultSet rs = stmtGetCredit.executeQuery();
                    creditToReturn = rs.next() ? rs.getDouble("totalCredit") : 0.0;
                }

                try (PreparedStatement stmtResetCredit = conn.prepareStatement(SQLQueries.Balance.RESET_CREDIT)) {
                    stmtResetCredit.executeUpdate();
                }

                conn.commit();
                return creditToReturn;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Error returning money", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error returning money", e);
        }
    }

    @Override
    public double emptyCashBox() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                double oldBalance;
                try (PreparedStatement stmt = conn.prepareStatement(SQLQueries.Balance.EMPTY_CASH_BOX)) {
                    ResultSet rs = stmt.executeQuery();
                    oldBalance = rs.next() ? rs.getDouble("totalBalance") : 0.0;
                }

                conn.commit();
                return oldBalance;

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error emptying cash box"+e.getMessage());
                throw new RuntimeException("Error emptying cash box", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Error emptying cash box"+e.getMessage());
            throw new RuntimeException("Error emptying cash box", e);
        }
    }

    @Override
    public List<UUID> solveCashFullFaults() {
        Connection conn = null;
        List<UUID> resolvedFaultIds = new ArrayList<>();

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement getFaultsStmt = conn.prepareStatement(SQLQueries.Balance.GET_CASH_FULL_FAULTS)) {
                getFaultsStmt.setObject(1, FaultType.CASSA_PIENA.name(), Types.OTHER);
                ResultSet rs = getFaultsStmt.executeQuery();

                while (rs.next()) {
                    resolvedFaultIds.add(rs.getObject("id_fault", UUID.class));
                }
            }

            if (!resolvedFaultIds.isEmpty()) {
                try (PreparedStatement updateFaultsStmt = conn.prepareStatement(SQLQueries.Balance.UPDATE_CASH_FULL_FAULTS)) {
                    updateFaultsStmt.setString(1, FaultType.CASSA_PIENA.name());
                    updateFaultsStmt.executeUpdate();
                }
            }

            conn.commit();
            return resolvedFaultIds;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Error solving cash full faults", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
}
