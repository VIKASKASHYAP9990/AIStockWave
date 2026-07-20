# Data Analyst pipeline runner script for AIStockWave (Expanded Schema)
import pandas as pd
import sqlite3
import matplotlib.pyplot as plt
import seaborn as sns
import os

print("--- Starting AIStockWave Capstone Data Pipeline (Expanded Schema) ---")

# 1. DATA WRANGLING
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
print("Wrangling data from excel sources...")
df_stocks = pd.read_excel(os.path.join(BASE_DIR, "raw_stocks_data.xlsx"))
df_news = pd.read_excel(os.path.join(BASE_DIR, "raw_news_data.xlsx"))

# Clean and normalize strings
df_stocks['Symbol'] = df_stocks['Symbol'].str.upper().str.strip()
df_stocks['PE Ratio'] = df_stocks['PE Ratio'].fillna(df_stocks['PE Ratio'].mean())
df_stocks.to_csv(os.path.join(BASE_DIR, "cleaned_stocks_data.csv"), index=False)

df_news['Title'] = df_news['Title'].str.strip()
df_news['Source'] = df_news['Source'].str.strip()
df_news.to_csv(os.path.join(BASE_DIR, "cleaned_news_data.csv"), index=False)
print("Saved clean CSV datasets.")

# 2. DATABASE INTEGRATION
print("Building sqlite database schema 'aistockwave.db'...")
conn = sqlite3.connect(os.path.join(BASE_DIR, "aistockwave.db"))
cursor = conn.cursor()

# DDL Create Statements (Expanded Schema)
cursor.execute("DROP TABLE IF EXISTS users")
cursor.execute("""
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    mobile TEXT,
    is_verified INTEGER DEFAULT 0,
    otp_code TEXT,
    subscription_tier TEXT DEFAULT 'Free',
    is_blocked INTEGER DEFAULT 0,
    login_history TEXT DEFAULT '[]',
    balance REAL DEFAULT 100000.0,
    is_admin INTEGER DEFAULT 0
)""")

cursor.execute("DROP TABLE IF EXISTS stocks")
cursor.execute("""
CREATE TABLE stocks (
    symbol TEXT PRIMARY KEY,
    company_name TEXT NOT NULL,
    current_price REAL NOT NULL,
    open_price REAL,
    close_price REAL,
    high_price REAL,
    low_price REAL,
    volume INTEGER,
    market_cap REAL,
    pe_ratio REAL,
    eps REAL,
    dividend_yield REAL,
    high_52week REAL,
    low_52week REAL
)""")

cursor.execute("DROP TABLE IF EXISTS portfolio")
cursor.execute("""
CREATE TABLE portfolio (
    user_id INTEGER,
    symbol TEXT,
    quantity INTEGER NOT NULL DEFAULT 0,
    avg_buy_price REAL NOT NULL DEFAULT 0.0,
    PRIMARY KEY (user_id, symbol)
)""")

cursor.execute("DROP TABLE IF EXISTS transactions")
cursor.execute("""
CREATE TABLE transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    symbol TEXT NOT NULL,
    quantity INTEGER NOT NULL,
    price REAL NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
)""")

cursor.execute("DROP TABLE IF EXISTS watchlist")
cursor.execute("""
CREATE TABLE watchlist (
    user_id INTEGER,
    symbol TEXT,
    watchlist_name TEXT DEFAULT 'Default',
    PRIMARY KEY (user_id, symbol, watchlist_name)
)""")

cursor.execute("DROP TABLE IF EXISTS news")
cursor.execute("""
CREATE TABLE news (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    source TEXT,
    date TEXT
)""")

cursor.execute("DROP TABLE IF EXISTS price_alerts")
cursor.execute("""
CREATE TABLE price_alerts (
    user_id INTEGER,
    symbol TEXT,
    target_price REAL,
    condition TEXT,
    is_triggered INTEGER DEFAULT 0,
    PRIMARY KEY (user_id, symbol, target_price)
)""")

cursor.execute("DROP TABLE IF EXISTS notifications")
cursor.execute("""
CREATE TABLE notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
)""")
conn.commit()

# Seed Clean Data into tables
df_stocks_clean = pd.read_csv(os.path.join(BASE_DIR, "cleaned_stocks_data.csv"))
df_stocks_clean.columns = [
    'symbol', 'company_name', 'current_price', 'open_price', 'close_price',
    'high_price', 'low_price', 'volume', 'market_cap', 'pe_ratio', 'eps',
    'dividend_yield', 'high_52week', 'low_52week'
]
df_stocks_clean.to_sql("stocks", conn, if_exists="replace", index=False)

df_news_clean = pd.read_csv(os.path.join(BASE_DIR, "cleaned_news_data.csv"))
df_news_clean.columns = ['title', 'description', 'source', 'date']
df_news_clean.to_sql("news", conn, if_exists="replace", index=False)

# Seed Users (Verified, Active subscriptions)
cursor.execute("""
INSERT INTO users (full_name, email, password, mobile, is_verified, subscription_tier, balance, is_admin) 
VALUES ('Paper Trader', 'user@gmail.com', 'User@123', '9876543210', 1, 'Free', 100000.0, 0)
""")
cursor.execute("""
INSERT INTO users (full_name, email, password, mobile, is_verified, subscription_tier, balance, is_admin) 
VALUES ('System Administrator', 'admin@gmail.com', 'Admin@123', '9999999999', 1, 'Pro', 100000.0, 1)
""")

# Seed basic notification and alert to test
cursor.execute("INSERT INTO notifications (user_id, title, message) VALUES (1, 'Welcome to AIStockWave!', 'Start paper trading with your ₹100,000 balance!')")

conn.commit()
print("SQLite Database created and populated with security extensions.")

# 3. EXPLORATORY VISUALIZATION
print("Generating visualization charts...")
plt.style.use('default')
plt.rcParams['figure.figsize'] = (10, 5)

# Chart 1: Market Caps
plt.figure()
sns.barplot(x="market_cap", y="symbol", data=df_stocks_clean.sort_values("market_cap", ascending=False), palette="viridis", hue="symbol", legend=False)
plt.title("AIStockWave Tickers ranked by Market Capitalization ($ Billions)", fontsize=12, fontweight='bold')
plt.xlabel("Market Cap ($B)")
plt.ylabel("Ticker Symbol")
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "market_cap_chart.png"), dpi=120)
plt.close()

# Chart 2: P/E Distribution
plt.figure()
sns.histplot(df_stocks_clean['pe_ratio'], bins=6, kde=True, color="#2962FF")
plt.title("Distribution of Price-to-Earnings (P/E) Ratios", fontsize=12, fontweight='bold')
plt.xlabel("P/E Ratio")
plt.ylabel("Frequency")
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, "pe_distribution_chart.png"), dpi=120)
plt.close()

# Chart 3: Proximity channels
plt.figure()
df_sorted = df_stocks_clean.sort_values("current_price")
plt.scatter(df_sorted['symbol'], df_sorted['current_price'], color='blue', label='Current Live price', zorder=5)
plt.scatter(df_sorted['symbol'], df_sorted['high_52week'], color='green', marker='^', label='52W High Limit', alpha=0.6)
plt.scatter(df_sorted['symbol'], df_sorted['low_52week'], color='red', marker='v', label='52W Low Limit', alpha=0.6)
plt.title("Live Prices relative to 52-Week High & Low Boundaries", fontsize=12, fontweight='bold')
plt.ylabel("Price (₹)")
plt.legend()
plt.tight_layout()
plt.savefig(os.path.join(BASE_DIR, '52w_channels_chart.png'), dpi=120)
plt.close()

conn.close()
print("Pipeline run successfully complete!")
