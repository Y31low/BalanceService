package org.uniupo.it.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
