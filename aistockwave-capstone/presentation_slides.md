# Slide Deck: AIStockWave Data Analyst Capstone Project

This document represents the presentation slides for the final evaluation of the AIStockWave project, following the standard format of the IBM Capstone project slides.

---

## Slide 1: Title Slide
### AIStockWave: AI-Powered Stock Market Analysis & Paper Trading Platform
* **Subtitle**: IBM Data Analyst Capstone Final Project
* **Author**: Lead Data Analyst / Software Engineer
* **Contact Email**: user@aistockwave.com
* **GitHub Repository**: https://github.com/VIKASKASHYAP9990/AIStockWave (Branch: `capstone`)

---

## Slide 2: Project Objective & Architecture
### Overarching Platform Goals
1. **Financial Literacy**: Create a risk-free paper trading platform for learning.
2. **Automated Analysis**: Integrate an AI evaluation rule engine to audit stock valuations.
3. **Data Pipeline**: Design an end-to-end data processing workflow:
   - **Data Collection** (Finnhub API & Web Scraping news)
   - **Data Cleaning** (Pandas Wrangling)
   - **Database Storage** (SQLite & SQL Query analytics)
   - **Data Visualization** (Matplotlib & Plotly interactive candlestick wicks)
   - **Interactive Frontend Application** (Python Streamlit Web App)

---

## Slide 3: Module 1-3: Data Collection & Cleaning
### Wrangling Checklist & Outcomes
* **Raw Extracted Datasets**:
  - `raw_stocks_data.xlsx`: Raw quote metrics (high, low, open, close, volumes, valuations).
  - `raw_news_data.xlsx`: Crawled financial articles and publisher sources.
* **Cleaning Actions**:
  - Dropped duplicate symbol keys.
  - Trimmed whitespaces and capitalized stock ticker symbols.
  - Imputed missing P/E metrics using mean-value calculations.
  - Exported normalized dataset files: `cleaned_stocks_data.csv` and `cleaned_news_data.csv`.

---

## Slide 4: Module 4: SQL Database Schema Design
### Entity-Relationship Architecture
* **SQLite Database**: `aistockwave.db`
* **Schema Entities**:
  1. `users`: Stores user names, credentials, balances (default: ₹100,000.00), and Admin roles.
  2. `stocks`: Stores live price metrics, EPS, P/E ratios, volumes, and annual limits.
  3. `portfolio`: Tracks virtual assets held (quantity, average buy price).
  4. `transactions`: Historical trade logs (BUY/SELL, timestamp, quantity, price).
  5. `watchlist`: Tickers bookmarked by traders.
  6. `news`: Feeds of published headlines.

---

## Slide 5: Module 4: Analytics Queries
### SQL Code Examples
```sql
-- Query: Find Overvalued Stocks
SELECT symbol, company_name, pe_ratio 
FROM stocks 
WHERE pe_ratio > 30.0;

-- Query: Active User Portfolio Assets
SELECT p.symbol, s.company_name, p.quantity, p.avg_buy_price, s.current_price 
FROM portfolio p 
JOIN stocks s ON p.symbol = s.symbol 
WHERE p.user_id = 1;
```

---

## Slide 6: Module 5: Exploratory Visualizations
### Core Visual Discoveries
* **Market Capitalization Breakdown**:
  - Bar charts reveal major tech holdings like MSFT and AAPL dominating capitalization brackets.
* **Valuation (P/E Ratio) Distribution**:
  - Right-skewed distribution highlights that most values fall inside reasonable ranges (20x-40x), with high-growth assets located as outliers.
* **52-Week Price Channels**:
  - Scatter plots map live prices against annual bands, making it easy to identify momentum breakouts or support channels.

---

## Slide 7: Module 6: Interactive Web Terminal (Streamlit App)
### The Paper Trading Solution
* **Local Python Server**: Served locally via Streamlit at `http://localhost:8501`.
* **Trading Terminal Interface**:
  - Renders live price ticker tables updating automatically on interface interaction.
  - Renders interactive candles with zoom sliders via Plotly.
  - Buy/Sell interface deducts cash, calculates average share costs, updates portfolios, and registers transaction logs in SQLite.

---

## Slide 8: AI-Powered Stock Auditor
### Value-Added Analytics
* **AI Evaluation Engine**: Renders instant recommendations (STRONG BUY, BUY, HOLD, UNDERPERFORM).
* **Detailed Audit Cards**:
  - Highlights specific **Growth Strengths** (high EPS, low P/E, stable yields).
  - Highlights specific **Risk Vulnerabilities** (P/E overvaluation, annual low bounds).
  - Generates clear financial lessons explaining the P/E multiplier, EPS, and 52W range concepts.

---

## Slide 9: Connect & Project References
### References & Links
* **Repository Link**: https://github.com/VIKASKASHYAP9990/AIStockWave
* **Documentation**: [Analyst Report](aistockwave_report.md)
* **API Providers**: Finnhub, Alpha Vantage, Yahoo Finance
* **Python Libraries**: Streamlit, SQLite3, Pandas, Matplotlib, Seaborn, Plotly
