package com.aistockwave.database;

import com.aistockwave.config.DatabaseConfig;
import com.aistockwave.model.PortfolioItem;
import com.aistockwave.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO {

    private final UserDAO userDAO = new UserDAO();

    public List<PortfolioItem> getPortfolioForUser(int userId) {
        List<PortfolioItem> list = new ArrayList<>();
        // Join portfolio with stocks to fetch company_name and live current_price
        String sql = "SELECT p.symbol, s.company_name, p.quantity, p.avg_buy_price, s.current_price "
                + "FROM portfolio p "
                + "JOIN stocks s ON p.symbol = s.symbol "
                + "WHERE p.user_id = ? AND p.quantity > 0 "
                + "ORDER BY p.symbol ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new PortfolioItem(
                            rs.getString("symbol"),
                            rs.getString("company_name"),
                            rs.getInt("quantity"),
                            rs.getDouble("avg_buy_price"),
                            rs.getDouble("current_price")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean executeBuy(int userId, String symbol, int quantity, double price) throws SQLException {
        Connection conn = null;
        PreparedStatement checkCashPstmt = null;
        PreparedStatement updateCashPstmt = null;
        PreparedStatement checkHoldingPstmt = null;
        PreparedStatement insertHoldingPstmt = null;
        PreparedStatement updateHoldingPstmt = null;
        PreparedStatement logTxPstmt = null;

        symbol = symbol.toUpperCase();
        double cost = quantity * price;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Transaction block

            // 1. Verify User Cash Balance
            String checkCashSql = "SELECT balance FROM users WHERE id = ?";
            checkCashPstmt = conn.prepareStatement(checkCashSql);
            checkCashPstmt.setInt(1, userId);
            double currentBalance = 0;
            try (ResultSet rs = checkCashPstmt.executeQuery()) {
                if (rs.next()) {
                    currentBalance = rs.getDouble("balance");
                } else {
                    throw new SQLException("User not found.");
                }
            }

            if (currentBalance < cost) {
                throw new SQLException("Insufficient funds for purchase. Required: ₹" + cost + ", Available: ₹" + currentBalance);
            }

            // 2. Deduct Cash
            String updateCashSql = "UPDATE users SET balance = balance - ? WHERE id = ?";
            updateCashPstmt = conn.prepareStatement(updateCashSql);
            updateCashPstmt.setDouble(1, cost);
            updateCashPstmt.setInt(2, userId);
            updateCashPstmt.executeUpdate();

            // 3. Insert or Update Portfolio
            String checkHoldingSql = "SELECT quantity, avg_buy_price FROM portfolio WHERE user_id = ? AND symbol = ?";
            checkHoldingPstmt = conn.prepareStatement(checkHoldingSql);
            checkHoldingPstmt.setInt(1, userId);
            checkHoldingPstmt.setString(2, symbol);

            int oldQuantity = 0;
            double oldAvgPrice = 0.0;
            boolean alreadyHolds = false;

            try (ResultSet rs = checkHoldingPstmt.executeQuery()) {
                if (rs.next()) {
                    oldQuantity = rs.getInt("quantity");
                    oldAvgPrice = rs.getDouble("avg_buy_price");
                    alreadyHolds = true;
                }
            }

            if (alreadyHolds) {
                // Recalculate average buy price
                int newQuantity = oldQuantity + quantity;
                double newAvgPrice = ((oldQuantity * oldAvgPrice) + cost) / newQuantity;

                String updateHoldingSql = "UPDATE portfolio SET quantity = ?, avg_buy_price = ? WHERE user_id = ? AND symbol = ?";
                updateHoldingPstmt = conn.prepareStatement(updateHoldingSql);
                updateHoldingPstmt.setInt(1, newQuantity);
                updateHoldingPstmt.setDouble(2, newAvgPrice);
                updateHoldingPstmt.setInt(3, userId);
                updateHoldingPstmt.setString(4, symbol);
                updateHoldingPstmt.executeUpdate();
            } else {
                String insertHoldingSql = "INSERT INTO portfolio (user_id, symbol, quantity, avg_buy_price) VALUES (?, ?, ?, ?)";
                insertHoldingPstmt = conn.prepareStatement(insertHoldingSql);
                insertHoldingPstmt.setInt(1, userId);
                insertHoldingPstmt.setString(2, symbol);
                insertHoldingPstmt.setInt(3, quantity);
                insertHoldingPstmt.setDouble(4, price);
                insertHoldingPstmt.executeUpdate();
            }

            // 4. Log Transaction History
            String logTxSql = "INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'BUY', ?, ?, ?)";
            logTxPstmt = conn.prepareStatement(logTxSql);
            logTxPstmt.setInt(1, userId);
            logTxPstmt.setString(2, symbol);
            logTxPstmt.setInt(3, quantity);
            logTxPstmt.setDouble(4, price);
            logTxPstmt.executeUpdate();

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            closeQuietly(checkCashPstmt);
            closeQuietly(updateCashPstmt);
            closeQuietly(checkHoldingPstmt);
            closeQuietly(insertHoldingPstmt);
            closeQuietly(updateHoldingPstmt);
            closeQuietly(logTxPstmt);
            closeQuietly(conn);
        }
    }

    public boolean executeSell(int userId, String symbol, int quantity, double price) throws SQLException {
        Connection conn = null;
        PreparedStatement checkHoldingPstmt = null;
        PreparedStatement updateHoldingPstmt = null;
        PreparedStatement deleteHoldingPstmt = null;
        PreparedStatement updateCashPstmt = null;
        PreparedStatement logTxPstmt = null;

        symbol = symbol.toUpperCase();
        double profit = quantity * price;

        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false); // Transaction block

            // 1. Verify User Holds Stock in sufficient quantity
            String checkHoldingSql = "SELECT quantity FROM portfolio WHERE user_id = ? AND symbol = ?";
            checkHoldingPstmt = conn.prepareStatement(checkHoldingSql);
            checkHoldingPstmt.setInt(1, userId);
            checkHoldingPstmt.setString(2, symbol);

            int currentHolding = 0;
            try (ResultSet rs = checkHoldingPstmt.executeQuery()) {
                if (rs.next()) {
                    currentHolding = rs.getInt("quantity");
                }
            }

            if (currentHolding < quantity) {
                throw new SQLException("Insufficient stock holdings to sell. Requested: " + quantity + ", Available: " + currentHolding);
            }

            // 2. Update Portfolio
            if (currentHolding == quantity) {
                // Delete holding if fully sold
                String deleteHoldingSql = "DELETE FROM portfolio WHERE user_id = ? AND symbol = ?";
                deleteHoldingPstmt = conn.prepareStatement(deleteHoldingSql);
                deleteHoldingPstmt.setInt(1, userId);
                deleteHoldingPstmt.setString(2, symbol);
                deleteHoldingPstmt.executeUpdate();
            } else {
                // Deduct quantity
                String updateHoldingSql = "UPDATE portfolio SET quantity = quantity - ? WHERE user_id = ? AND symbol = ?";
                updateHoldingPstmt = conn.prepareStatement(updateHoldingSql);
                updateHoldingPstmt.setInt(1, quantity);
                updateHoldingPstmt.setInt(2, userId);
                updateHoldingPstmt.setString(3, symbol);
                updateHoldingPstmt.executeUpdate();
            }

            // 3. Credit Cash
            String updateCashSql = "UPDATE users SET balance = balance + ? WHERE id = ?";
            updateCashPstmt = conn.prepareStatement(updateCashSql);
            updateCashPstmt.setDouble(1, profit);
            updateCashPstmt.setInt(2, userId);
            updateCashPstmt.executeUpdate();

            // 4. Log Transaction History
            String logTxSql = "INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'SELL', ?, ?, ?)";
            logTxPstmt = conn.prepareStatement(logTxSql);
            logTxPstmt.setInt(1, userId);
            logTxPstmt.setString(2, symbol);
            logTxPstmt.setInt(3, quantity);
            logTxPstmt.setDouble(4, price);
            logTxPstmt.executeUpdate();

            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            closeQuietly(checkHoldingPstmt);
            closeQuietly(updateHoldingPstmt);
            closeQuietly(deleteHoldingPstmt);
            closeQuietly(updateCashPstmt);
            closeQuietly(logTxPstmt);
            closeQuietly(conn);
        }
    }

    public List<Transaction> getTransactionsForUser(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("type"),
                            rs.getString("symbol"),
                            rs.getInt("quantity"),
                            rs.getDouble("price"),
                            rs.getTimestamp("timestamp")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY timestamp DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Transaction(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("type"),
                        rs.getString("symbol"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getTimestamp("timestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Ignore
            }
        }
    }
}
