package com.aistockwave.database;

import com.aistockwave.config.DatabaseConfig;
import com.aistockwave.model.Stock;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public Stock getStockBySymbol(String symbol) {
        String sql = "SELECT * FROM stocks WHERE symbol = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStock(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Stock> getAllStocks() {
        List<Stock> list = new ArrayList<>();
        String sql = "SELECT * FROM stocks ORDER BY symbol ASC";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToStock(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createStock(Stock s) {
        String sql = "INSERT INTO stocks (symbol, company_name, current_price, open_price, close_price, "
                + "high_price, low_price, volume, market_cap, pe_ratio, eps, dividend_yield, high_52week, low_52week) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getSymbol().toUpperCase());
            pstmt.setString(2, s.getCompanyName());
            pstmt.setDouble(3, s.getCurrentPrice());
            pstmt.setDouble(4, s.getOpenPrice());
            pstmt.setDouble(5, s.getClosePrice());
            pstmt.setDouble(6, s.getHighPrice());
            pstmt.setDouble(7, s.getLowPrice());
            pstmt.setLong(8, s.getVolume());
            pstmt.setDouble(9, s.getMarketCap());
            pstmt.setDouble(10, s.getPeRatio());
            pstmt.setDouble(11, s.getEps());
            pstmt.setDouble(12, s.getDividendYield());
            pstmt.setDouble(13, s.getHigh52Week());
            pstmt.setDouble(14, s.getLow52Week());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStockPrice(String symbol, double price, double open, double close, double high, double low, long volume) {
        String sql = "UPDATE stocks SET current_price = ?, open_price = ?, close_price = ?, high_price = ?, low_price = ?, volume = ? WHERE symbol = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, price);
            pstmt.setDouble(2, open);
            pstmt.setDouble(3, close);
            pstmt.setDouble(4, high);
            pstmt.setDouble(5, low);
            pstmt.setLong(6, volume);
            pstmt.setString(7, symbol.toUpperCase());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Stock mapRowToStock(ResultSet rs) throws SQLException {
        return new Stock(
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
        );
    }
}
