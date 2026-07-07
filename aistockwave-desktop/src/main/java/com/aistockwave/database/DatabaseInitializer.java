package com.aistockwave.database;

import com.aistockwave.config.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("Initializing Database schema...");

            // 1. Create Users Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "full_name VARCHAR(100) NOT NULL,"
                    + "email VARCHAR(100) UNIQUE NOT NULL,"
                    + "password_hash VARCHAR(255) NOT NULL,"
                    + "balance DOUBLE DEFAULT 100000.0,"
                    + "is_admin BOOLEAN DEFAULT FALSE"
                    + ")");

            // 2. Create Stocks Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS stocks ("
                    + "symbol VARCHAR(10) PRIMARY KEY,"
                    + "company_name VARCHAR(100) NOT NULL,"
                    + "current_price DOUBLE NOT NULL,"
                    + "open_price DOUBLE NOT NULL,"
                    + "close_price DOUBLE NOT NULL,"
                    + "high_price DOUBLE NOT NULL,"
                    + "low_price DOUBLE NOT NULL,"
                    + "volume BIGINT DEFAULT 0,"
                    + "market_cap DOUBLE DEFAULT 0.0,"
                    + "pe_ratio DOUBLE DEFAULT 0.0,"
                    + "eps DOUBLE DEFAULT 0.0,"
                    + "dividend_yield DOUBLE DEFAULT 0.0,"
                    + "high_52week DOUBLE DEFAULT 0.0,"
                    + "low_52week DOUBLE DEFAULT 0.0"
                    + ")");

            // 3. Create Portfolio Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS portfolio ("
                    + "user_id INT NOT NULL,"
                    + "symbol VARCHAR(10) NOT NULL,"
                    + "quantity INT NOT NULL DEFAULT 0,"
                    + "avg_buy_price DOUBLE NOT NULL DEFAULT 0.0,"
                    + "PRIMARY KEY (user_id, symbol),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ")");

            // 4. Create Transactions Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_id INT NOT NULL,"
                    + "type VARCHAR(10) NOT NULL," // BUY or SELL
                    + "symbol VARCHAR(10) NOT NULL,"
                    + "quantity INT NOT NULL,"
                    + "price DOUBLE NOT NULL,"
                    + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ")");

            // 5. Create Watchlist Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS watchlist ("
                    + "user_id INT NOT NULL,"
                    + "symbol VARCHAR(10) NOT NULL,"
                    + "PRIMARY KEY (user_id, symbol),"
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ")");

            // 6. Create News Table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS news ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "title VARCHAR(255) NOT NULL,"
                    + "description TEXT,"
                    + "source VARCHAR(100),"
                    + "date VARCHAR(50)"
                    + ")");

            System.out.println("Tables verified/created successfully.");

            // Seed Users if empty
            seedUsers(conn);

            // Seed Stocks if empty
            seedStocks(conn);

            // Seed News if empty
            seedNews(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedUsers(Connection conn) throws SQLException {
        String checkUsers = "SELECT COUNT(*) FROM users";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkUsers)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding users...");
                String insertUser = "INSERT INTO users (full_name, email, password_hash, balance, is_admin) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertUser)) {
                    // Regular user
                    pstmt.setString(1, "Paper Trader");
                    pstmt.setString(2, "user@aistockwave.com");
                    pstmt.setString(3, BCrypt.hashpw("user123", BCrypt.gensalt()));
                    pstmt.setDouble(4, 100000.0);
                    pstmt.setBoolean(5, false);
                    pstmt.executeUpdate();

                    // Admin user
                    pstmt.setString(1, "System Administrator");
                    pstmt.setString(2, "admin@aistockwave.com");
                    pstmt.setString(3, BCrypt.hashpw("admin123", BCrypt.gensalt()));
                    pstmt.setDouble(4, 100000.0);
                    pstmt.setBoolean(5, true);
                    pstmt.executeUpdate();
                    System.out.println("User seeding complete.");
                }
            }
        }
    }

    private static void seedStocks(Connection conn) throws SQLException {
        String checkStocks = "SELECT COUNT(*) FROM stocks";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkStocks)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding stocks...");
                String insertStock = "INSERT INTO stocks (symbol, company_name, current_price, open_price, close_price, "
                        + "high_price, low_price, volume, market_cap, pe_ratio, eps, dividend_yield, high_52week, low_52week) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertStock)) {
                    // Seed Apple (AAPL)
                    setStockParams(pstmt, "AAPL", "Apple Inc.", 185.25, 184.00, 184.50, 186.40, 183.50, 52000000L, 2900.5, 28.5, 6.5, 0.52, 199.62, 164.08);
                    pstmt.executeUpdate();

                    // Seed Microsoft (MSFT)
                    setStockParams(pstmt, "MSFT", "Microsoft Corp.", 420.50, 418.00, 417.80, 422.90, 416.30, 23000000L, 3130.2, 35.8, 11.75, 0.71, 430.82, 315.18);
                    pstmt.executeUpdate();

                    // Seed Google (GOOGL)
                    setStockParams(pstmt, "GOOGL", "Alphabet Inc.", 172.30, 170.50, 170.10, 173.50, 169.80, 28000000L, 2150.1, 26.2, 6.58, 0.46, 177.43, 115.50);
                    pstmt.executeUpdate();

                    // Seed Amazon (AMZN)
                    setStockParams(pstmt, "AMZN", "Amazon.com Inc.", 188.40, 187.00, 186.20, 189.50, 185.30, 35000000L, 1950.4, 40.5, 4.65, 0.00, 191.70, 120.43);
                    pstmt.executeUpdate();

                    // Seed Tesla (TSLA)
                    setStockParams(pstmt, "TSLA", "Tesla Inc.", 175.80, 178.00, 179.20, 180.50, 173.20, 82000000L, 560.8, 48.2, 3.65, 0.00, 299.29, 138.80);
                    pstmt.executeUpdate();

                    // Seed Nvidia (NVDA)
                    setStockParams(pstmt, "NVDA", "NVIDIA Corp.", 910.20, 898.00, 895.00, 922.00, 891.50, 49000000L, 2270.3, 72.8, 12.50, 0.02, 974.00, 262.20);
                    pstmt.executeUpdate();

                    // Seed Meta (META)
                    setStockParams(pstmt, "META", "Meta Platforms Inc.", 495.10, 492.00, 490.50, 501.20, 488.30, 18000000L, 1260.6, 24.3, 20.37, 0.40, 531.49, 229.85);
                    pstmt.executeUpdate();

                    // Seed Netflix (NFLX)
                    setStockParams(pstmt, "NFLX", "Netflix Inc.", 620.15, 615.00, 612.40, 625.30, 611.20, 5000000L, 268.4, 38.6, 16.07, 0.00, 639.00, 315.62);
                    pstmt.executeUpdate();

                    System.out.println("Stock seeding complete.");
                }
            }
        }
    }

    private static void setStockParams(PreparedStatement pstmt, String symbol, String name, double price, double open,
                                       double close, double high, double low, long volume, double cap, double pe,
                                       double eps, double div, double h52, double l52) throws SQLException {
        pstmt.setString(1, symbol);
        pstmt.setString(2, name);
        pstmt.setDouble(3, price);
        pstmt.setDouble(4, open);
        pstmt.setDouble(5, close);
        pstmt.setDouble(6, high);
        pstmt.setDouble(7, low);
        pstmt.setLong(8, volume);
        pstmt.setDouble(9, cap);
        pstmt.setDouble(10, pe);
        pstmt.setDouble(11, eps);
        pstmt.setDouble(12, div);
        pstmt.setDouble(13, h52);
        pstmt.setDouble(14, l52);
    }

    private static void seedNews(Connection conn) throws SQLException {
        String checkNews = "SELECT COUNT(*) FROM news";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkNews)) {
            if (rs.next() && rs.getInt(1) == 0) {
                System.out.println("Seeding news...");
                String insertNews = "INSERT INTO news (title, description, source, date) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertNews)) {
                    pstmt.setString(1, "NVIDIA Hits Record High on Massive AI Chip Demand");
                    pstmt.setString(2, "NVIDIA stock soared past $900 today as major tech companies continue to place multi-billion dollar orders for its next-generation Blackwell AI processors. Analysts project high margins for the upcoming quarters.");
                    pstmt.setString(3, "Financial Times");
                    pstmt.setString(4, "July 8, 2026");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Federal Reserve Hints at Possible Rate Cut Next Month");
                    pstmt.setString(2, "The Federal Reserve chairman suggested inflation is moderating toward the 2% target, sparking a market-wide rally. Tech and growth sectors led gains following the announcement.");
                    pstmt.setString(3, "Wall Street Journal");
                    pstmt.setString(4, "July 7, 2026");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Apple Announces New Generative AI Integrations for iOS 20");
                    pstmt.setString(2, "Apple unveiled its upcoming OS updates at a developer conference, detailing deep system integrations of advanced generative LLMs running on-device. The stock climbed 2% in afternoon trading.");
                    pstmt.setString(3, "TechCrunch");
                    pstmt.setString(4, "July 6, 2026");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Tesla Deliveries Beats Estimates, Stock Jumps 4%");
                    pstmt.setString(2, "Tesla announced quarterly delivery numbers that slightly exceeded Wall Street expectations, showing strong growth in its Model Y production and international markets.");
                    pstmt.setString(3, "Bloomberg");
                    pstmt.setString(4, "July 5, 2026");
                    pstmt.executeUpdate();

                    pstmt.setString(1, "Global Markets Rally as Supply Chain Congestion Eases");
                    pstmt.setString(2, "Shipping rates and port wait times are returning to pre-pandemic averages, relieving inflationary pressure on manufacturing and consumer goods companies globally.");
                    pstmt.setString(3, "Reuters");
                    pstmt.setString(4, "July 4, 2026");
                    pstmt.executeUpdate();

                    System.out.println("News seeding complete.");
                }
            }
        }
    }
}
