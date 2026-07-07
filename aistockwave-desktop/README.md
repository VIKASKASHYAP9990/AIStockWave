# AIStockWave – AI-Powered Stock Market Analysis & Paper Trading Platform

AIStockWave is a full-featured JavaFX desktop trading terminal and paper portfolio simulation application built for Java 8 and MySQL. 

---

## 🌟 Core Features

- **Dynamic Side Navigation Panel**: Switch cleanly between the Dashboard, Portfolio, Watchlist, Market News, Admin Panel, and Profile screens.
- **Interactive Technical Charts**: Interactive Candlestick Charts that support toggling between 1D, 1W, 1M, 6M, 1Y, and 5Y time intervals.
- **Paper Trading Desk**: Every user gets ₹100,000 in virtual cash. Place simulated market BUY or SELL orders with instant cash balance validations and average buy price recalculations.
- **AI Stock Analysis Auditor**: Automatically parses stock metrics (P/E ratio, High/Low range, volatility, EPS) and outputs sentiment ratings (e.g. STRONG BUY, HOLD, UNDERPERFORM) along with core strengths, risk factors, and conceptual trading lessons.
- **Admin Administration Panel**: Monitor overall platform trades, users count, gross traded volumes, reset paper balances for users, register new tickers to begin active trading, and publish financial news events to the news feed.
- **Secure Password Hashing**: Passwords encrypted using blowfish-based password hashing (BCrypt) inside the database.

---

## 🛠️ Project Structure

```text
aistockwave/
├── pom.xml                        # Maven dependencies & plugins configuration
├── build_and_run.ps1              # Single-click compiler & launcher script (PowerShell)
├── README.md                      # Documentation
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── aistockwave/
        │           ├── App.java                   # Primary Application entry (JavaFX)
        │           ├── config/
        │           │   └── DatabaseConfig.java    # Loads config.properties
        │           ├── model/                     # Data Entities (User, Stock, news, etc.)
        │           │   ├── User.java
        │           │   ├── Stock.java
        │           │   ├── Transaction.java
        │           │   ├── PortfolioItem.java
        │           │   └── NewsItem.java
        │           ├── database/                  # JDBC CRUD & Schema Bootstrappers
        │           │   ├── DatabaseInitializer.java
        │           │   ├── UserDAO.java
        │           │   ├── StockDAO.java
        │           │   ├── PortfolioDAO.java
        │           │   ├── WatchlistDAO.java
        │           │   └── NewsDAO.java
        │           ├── service/                   # Core business calculations
        │           │   ├── FinancialApiService.java
        │           │   ├── AiAnalysisService.java
        │           │   └── AuthService.java
        │           └── view/                      # GUI layout screens (JavaFX)
        │               ├── LoginView.java
        │               ├── RegisterView.java
        │               ├── MainDashboardView.java
        │               ├── DashboardOverviewView.java
        │               ├── StockDetailView.java
        │               ├── CandlestickChartComponent.java # Custom candles drawing canvas
        │               ├── PortfolioView.java
        │               ├── WatchlistView.java
        │               ├── MarketNewsView.java
        │               ├── AdminDashboardView.java
        │               └── ProfileView.java
        └── resources/
            ├── config.properties          # MySQL Connection settings
            └── css/
                └── styles.css             # Obsidian dark-mode fintech stylesheet
```

---

## ⚙️ Prerequisites & Database Setup

1. **MySQL Server 8.0**: Ensure your local MySQL service is running.
2. **Database Credentials**: Open the configuration file located at:
   [src/main/resources/config.properties](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave/src/main/resources/config.properties)
   Update the database password (`db.password`) to match your local MySQL root password:
   ```properties
   db.password=YOUR_MYSQL_PASSWORD_HERE
   ```
   *Note: On first launch, the database bootstrap initializer will automatically create the database `aistockwave` and build all required tables.*

---

## 🚀 How to Compile and Run

1. Open a PowerShell terminal.
2. Navigate to the project directory:
   ```powershell
   cd C:\Users\vikas\.gemini\antigravity-ide\scratch\aistockwave
   ```
3. Run the compiler & launcher script:
   ```powershell
   .\build_and_run.ps1
   ```
   *This script sets JAVA_HOME, triggers our local Maven compiler, downloads dependencies (jBCrypt, Gson, MySQL Connector), and launches the JavaFX desktop app.*

---

## 🔑 Seed User Logins

On startup, the system seeds the database with the following demo credentials:

### 1. General Paper Trader
- **Email**: `user@aistockwave.com`
- **Password**: `user123`
- **Starting Cash**: ₹100,000.00
- **Access**: Standard trading dashboard, watchlists, portfolio tracking.

### 2. System Administrator
- **Email**: `admin@aistockwave.com`
- **Password**: `admin123`
- **Access**: Standard trading dashboard + the **Admin Panel** sidebar tab (user management, reset balances, publish news, register stocks).
