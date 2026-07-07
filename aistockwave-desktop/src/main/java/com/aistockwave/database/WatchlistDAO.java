package com.aistockwave.database;

import com.aistockwave.config.DatabaseConfig;
import com.aistockwave.model.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WatchlistDAO {

    public boolean addToWatchlist(int userId, String symbol) {
        String sql = "INSERT IGNORE INTO watchlist (user_id, symbol) VALUES (?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol.toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFromWatchlist(int userId, String symbol) {
        String sql = "DELETE FROM watchlist WHERE user_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol.toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isInWatchlist(int userId, String symbol) {
        String sql = "SELECT 1 FROM watchlist WHERE user_id = ? AND symbol = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, symbol.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Stock> getWatchlistForUser(int userId) {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT s.* FROM watchlist w "
                + "JOIN stocks s ON w.symbol = s.symbol "
                + "WHERE w.user_id = ? "
                + "ORDER BY w.symbol ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Stock(
                            rs.getString("symbol"),
                            rs.getString("company_name"),
                            rs.getDouble("current_price"),
                            rs.getDouble("open_price"),
                            rs.getDouble("close_price"),
                            rs.getDouble("high_price"),
                            rs.getDouble("low_price"),
                            rs.getLong("volume"),
                            rs.getDouble("market_cap"),
                            rs.getDouble("pe_ratio"),
                            rs.getDouble("eps"),
                            rs.getDouble("dividend_yield"),
                            rs.getDouble("high_52week"),
                            rs.getDouble("low_52week")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
