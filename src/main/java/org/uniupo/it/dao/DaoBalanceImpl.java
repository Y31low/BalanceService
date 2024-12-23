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
}
