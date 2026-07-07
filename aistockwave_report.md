# AIStockWave – Data Analyst Capstone Project Report

**Author**: Lead Data Analyst  
**Course**: Capstone Final Project Portfolio  
**Target Platform**: AIStockWave Paper Trading and Analytics Terminal

---

## 1. Executive Summary & Project Goal
AIStockWave is an educational paper trading and stock market analysis platform built to guide retail investors in understanding valuations, market dynamics, and portfolio allocations. 

Following the structure of the **IBM Data Analyst Capstone Project**, this analysis outlines a complete end-to-end data engineering pipeline:
1. **API Collection**: Retrieving raw stock metrics from financial data REST interfaces.
2. **Web Scraping**: Crowling corporate news feeds and media headlines.
3. **Data Wrangling**: Cleaning missing value elements, normalising structures, and checking for anomalies.
4. **Database Integration**: Organizing an SQLite database containing structured tables for users, transactions, portfolios, and quotes.
5. **Exploratory Data Analysis (EDA)**: Visualizing valuation multipliers and trading channel bands.
6. **Dashboard Deployments**: Launching a fully interactive paper trading website built on **Streamlit** (Python).

---

## 2. Data Engineering & Cleaning Pipeline

### Data Extraction (API & Scraping)
- **Stock Quote Metrics**: Gathered raw coordinate sets for major technology stocks (AAPL, MSFT, TSLA, NVDA, etc.) detailing current bid rates, open margins, volumes, and 52-week channels. Saved to [raw_stocks_data.xlsx](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/raw_stocks_data.xlsx).
- **Market News Feed**: Simulated web-crawling scripts extracted headline news and metadata dates. Saved to [raw_news_data.xlsx](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/raw_news_data.xlsx).

### Data Wrangling & Cleaning Checklists
Wrangling was executed using Python `pandas` inside [3_Data_Wrangling.ipynb](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/3_Data_Wrangling.ipynb):
- **Missing Valuations**: Any missing P/E ratio parameters were imputed with the historical sector average.
- **Symbol Case Normalization**: Stock tickers were stripped of whitespace and forced to uppercase.
- **Duplicate Tickers**: Filtered duplicates to ensure that the stock symbol serves as a unique Primary Key in the SQLite schema.
- **Outputs**: Wrangled clean records saved in CSV format ready for bulk database load.

---

## 3. Database Schema Design (SQLite)
The relational database [aistockwave.db](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/aistockwave.db) contains six interconnected entities:

```mermaid
erDiagram
    USERS ||--o{ PORTFOLIO : owns
    USERS ||--o{ TRANSACTIONS : executes
    USERS ||--o{ WATCHLIST : favorited
    STOCKS ||--o{ PORTFOLIO : contains
    STOCKS ||--o{ TRANSACTIONS : traded
    STOCKS ||--o{ WATCHLIST : favorited
    NEWS ||--|| STOCKS : affects

    USERS {
        int id PK
        string full_name
        string email UNIQUE
        string password
        real balance
        int is_admin
    }
    STOCKS {
        string symbol PK
        string company_name
        real current_price
        real open_price
        real close_price
        real high_price
        real low_price
        int volume
        real market_cap
        real pe_ratio
        real eps
        real dividend_yield
        real high_52week
        real low_52week
    }
    PORTFOLIO {
        int user_id PK
        string symbol PK
        int quantity
        real avg_buy_price
    }
    TRANSACTIONS {
        int id PK
        int user_id
        string type
        string symbol
        int quantity
        real price
        datetime timestamp
    }
    WATCHLIST {
        int user_id PK
        string symbol PK
    }
    NEWS {
        int id PK
        string title
        string description
        string source
        string date
    }
```

---

## 4. SQL Analytics Queries
Several data audits were executed in [4_Database_Integration_SQL.ipynb](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/4_Database_Integration_SQL.ipynb) to inspect market statistics:
1. **P/E Overvaluation Audit**: Identifying tickers trading at valuation multiples above 30.0 (aggressive pricing zones).
2. **Market Cap Standing**: Ranking symbols based on overall market capitalization to map equity capitalization categories.

---

## 5. Visual Insights & Analytical Discoveries
We generated three analytical visualizations during EDA in [5_Exploratory_Data_Analysis.ipynb](file:///C:/Users/vikas/.gemini/antigravity-ide/scratch/aistockwave-capstone/5_Exploratory_Data_Analysis.ipynb):

### Chart 1: Market Cap Rankings
Shows company valuations in $ Billions. Tech giants like Microsoft (MSFT) and Apple (AAPL) lead the rankings above the $2.9T mark, while Tesla (TSLA) and Netflix (NFLX) represent mid-high cap growth channels.
*(Saved as `market_cap_chart.png`)*

### Chart 2: P/E Ratio Distributions
A right-skewed histogram revealing that the majority of stable equities trade within P/E zones of 20x-40x. Tickers above 70x (like NVDA) highlight premium pricing expectations.
*(Saved as `pe_distribution_chart.png`)*

### Chart 3: 52-Week Price Channels
Plots the live current price as a blue coordinate against green annual high limits and red annual low boundaries. This clearly indicates to investors whether a stock is consolidating, trending near supports, or breaking out to record highs.
*(Saved as `52w_channels_chart.png`)*

---

## 6. The Interactive Web Terminal (Streamlit app)
To convert these insights into a working fintech utility, an interactive dashboard app is built using Python's `streamlit` framework.
- **Login Portal**: Standard traders (`user@aistockwave.com`) start with ₹100,000.00 in virtual paper cash.
- **Plotly Candlesticks**: Financial charts with real-time wicks and price bounds.
- **Paper Trading Desk**: Buying or selling shares validates transaction cash limits, computes average buy costs, updates portfolio spreadsheets, and writes transaction logs in SQLite.
- **AI stock Auditor**: Real-time evaluation checks that calculate buy/sell sentiments based on growth indicators, volatility rates, and risk factor reports.
