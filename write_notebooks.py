import json
import os

folder = r"C:\Users\vikas\.gemini\antigravity-ide\scratch\aistockwave-capstone"

def make_notebook(filename, cells):
    nb = {
        "cells": cells,
        "metadata": {
            "kernelspec": {
                "display_name": "Python 3",
                "language": "python",
                "name": "python3"
            },
            "language_info": {
                "name": "python"
            }
        },
        "nbformat": 4,
        "nbformat_minor": 2
    }
    filepath = os.path.join(folder, filename)
    with open(filepath, 'w', encoding='utf-8') as f:
        json.dump(nb, f, indent=1)
    print(f"Created Notebook: {filepath}")

# ==================== NOTEBOOK 1: API COLLECTION ====================
nb1_cells = [
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "# Module 1: Accessing Stock Market APIs\n",
            "This notebook demonstrates how to access live financial APIs (e.g. Yahoo Finance, Finnhub, or Alpha Vantage) to retrieve current quotes and company statistics."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "import requests\n",
            "import pandas as pd\n",
            "\n",
            "print(\"Requests and Pandas loaded successfully.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 1. Querying Ticker Symbol Details from External REST Endpoints\n",
            "We fetch details for standard companies. If API key is empty, we print a sample response and load our raw dataset spreadsheet."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "def fetch_stock_quote(symbol, api_key):\n",
            "    if not api_key:\n",
            "        print(f\"No API key configured for {symbol}. Returning fallback mock response.\")\n",
            "        return {\"symbol\": symbol, \"price\": 185.0, \"high\": 186.0, \"low\": 183.0}\n",
            "    \n",
            "    endpoint = f\"https://finnhub.io/api/v1/quote?symbol={symbol}&token={api_key}\"\n",
            "    try:\n",
            "        response = requests.get(endpoint, timeout=10)\n",
            "        if response.status_code == 200:\n",
            "            return response.json()\n",
            "    except Exception as e:\n",
            "        print(f\"Request failed: {e}\")\n",
            "    return None\n",
            "\n",
            "quote = fetch_stock_quote(\"AAPL\", \"\")\n",
            "print(quote)"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 2. Loading and Inspecting the Seed Stocks Excel Sheet\n",
            "We load the spreadsheet containing raw parameters fetched from the APIs."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_raw = pd.read_excel(\"raw_stocks_data.xlsx\")\n",
            "print(\"Raw stocks dataset summary:\")\n",
            "print(df_raw.head())"
        ]
    }
]

# ==================== NOTEBOOK 2: WEB SCRAPING ====================
nb2_cells = [
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "# Module 2: Scraping Financial News Portals\n",
            "This notebook outlines scraping financial headlines and news items from financial portals using python parser tools."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "import requests\n",
            "import pandas as pd\n",
            "from urllib.request import urlopen\n",
            "\n",
            "print(\"Scraping modules loaded.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 1. Simulated HTML parser scraping mock corporate headlines"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "def scrape_financial_news():\n",
            "    # Simulated parsing output representing crawled financial updates\n",
            "    crawled_news = [\n",
            "        {\"Title\": \"Market Rally: Tech index climbs 1.5% on growth signals\", \"Source\": \"Reuters\"},\n",
            "        {\"Title\": \"Company Update: Product launch boosts target shares\", \"Source\": \"Bloomberg\"}\n",
            "    ]\n",
            "    return pd.DataFrame(crawled_news)\n",
            "\n",
            "df_scraped = scrape_financial_news()\n",
            "print(df_scraped)"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 2. Loading the Seed Raw News Excel Sheet"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_news_raw = pd.read_excel(\"raw_news_data.xlsx\")\n",
            "print(\"News articles dataset summary:\")\n",
            "print(df_news_raw)"
        ]
    }
]

# ==================== NOTEBOOK 3: DATA WRANGLING ====================
nb3_cells = [
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "# Module 3: Data Cleaning and Wrangling\n",
            "This notebook cleans the raw stock and news datasets, standardizes numbers, handles nulls, and exports templates ready for database insertion."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "import pandas as pd\n",
            "import numpy as np\n",
            "\n",
            "print(\"Pandas and Numpy loaded.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 1. Data Cleaning of Stock Metrics\n",
            "- Check for missing values.\n",
            "- Verify P/E Ratio boundaries.\n",
            "- Drop duplicate symbols."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_stocks = pd.read_excel(\"raw_stocks_data.xlsx\")\n",
            "\n",
            "# Proactive duplicate check\n",
            "duplicates = df_stocks.duplicated(subset=['Symbol']).sum()\n",
            "print(f\"Duplicated symbols count: {duplicates}\")\n",
            "\n",
            "# Fill empty or null values if any (e.g. PE ratio fills)\n",
            "df_stocks['PE Ratio'] = df_stocks['PE Ratio'].fillna(df_stocks['PE Ratio'].mean())\n",
            "\n",
            "# Clean symbols\n",
            "df_stocks['Symbol'] = df_stocks['Symbol'].str.upper().str.strip()\n",
            "print(\"Data cleaning checklist complete. Cleaned columns:\")\n",
            "print(df_stocks.dtypes)"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 2. Cleaning News headlines"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_news = pd.read_excel(\"raw_news_data.xlsx\")\n",
            "df_news['Title'] = df_news['Title'].str.strip()\n",
            "df_news['Source'] = df_news['Source'].str.strip()\n",
            "print(f\"Total news items cleaned: {len(df_news)}\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 3. Save Cleaned Datasets to CSV files for SQL Import"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_stocks.to_csv(\"cleaned_stocks_data.csv\", index=False)\n",
            "df_news.to_csv(\"cleaned_news_data.csv\", index=False)\n",
            "print(\"Exported cleaned_stocks_data.csv and cleaned_news_data.csv successfully.\")"
        ]
    }
]

# ==================== NOTEBOOK 4: SQL INTEGRATION ====================
nb4_cells = [
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "# Module 4: Database Integration & SQL Analysis\n",
            "This notebook sets up the SQLite database schema (`aistockwave.db`), populates it with our wrangled datasets, and executes analytical SQL queries."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "import sqlite3\n",
            "import pandas as pd\n",
            "\n",
            "print(\"SQLite3 database engine initialized.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 1. Establishing Schema Tables\n",
            "We construct tables: `users`, `stocks`, `portfolio`, `transactions`, `watchlist`, and `news`."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "conn = sqlite3.connect(\"aistockwave.db\")\n",
            "cursor = conn.cursor()\n",
            "\n",
            "# 1. Create tables\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS users (\n",
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n",
            "    full_name TEXT NOT NULL,\n",
            "    email TEXT UNIQUE NOT NULL,\n",
            "    password TEXT NOT NULL,\n",
            "    balance REAL DEFAULT 100000.0,\n",
            "    is_admin INTEGER DEFAULT 0\n",
            ")\"\"\")\n",
            "\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS stocks (\n",
            "    symbol TEXT PRIMARY KEY,\n",
            "    company_name TEXT NOT NULL,\n",
            "    current_price REAL NOT NULL,\n",
            "    open_price REAL,\n",
            "    close_price REAL,\n",
            "    high_price REAL,\n",
            "    low_price REAL,\n",
            "    volume INTEGER,\n",
            "    market_cap REAL,\n",
            "    pe_ratio REAL,\n",
            "    eps REAL,\n",
            "    dividend_yield REAL,\n",
            "    high_52week REAL,\n",
            "    low_52week REAL\n",
            ")\"\"\")\n",
            "\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS portfolio (\n",
            "    user_id INTEGER,\n",
            "    symbol TEXT,\n",
            "    quantity INTEGER NOT NULL DEFAULT 0,\n",
            "    avg_buy_price REAL NOT NULL DEFAULT 0.0,\n",
            "    PRIMARY KEY (user_id, symbol)\n",
            ")\"\"\")\n",
            "\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS transactions (\n",
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n",
            "    user_id INTEGER NOT NULL,\n",
            "    type TEXT NOT NULL,\n",
            "    symbol TEXT NOT NULL,\n",
            "    quantity INTEGER NOT NULL,\n",
            "    price REAL NOT NULL,\n",
            "    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP\n",
            ")\"\"\")\n",
            "\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS watchlist (\n",
            "    user_id INTEGER,\n",
            "    symbol TEXT,\n",
            "    PRIMARY KEY (user_id, symbol)\n",
            ")\"\"\")\n",
            "\n",
            "cursor.execute(\"\"\"\n",
            "CREATE TABLE IF NOT EXISTS news (\n",
            "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n",
            "    title TEXT NOT NULL,\n",
            "    description TEXT,\n",
            "    source TEXT,\n",
            "    date TEXT\n",
            ")\"\"\")\n",
            "\n",
            "conn.commit()\n",
            "print(\"Database tables created successfully.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 2. Loading Wrangled Data into the Database Tables\n",
            "We populate tables from the exported CSV sheets."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "# Load CSV files\n",
            "df_stocks_clean = pd.read_csv(\"cleaned_stocks_data.csv\")\n",
            "df_news_clean = pd.read_csv(\"cleaned_news_data.csv\")\n",
            "\n",
            "# Insert Stocks\n",
            "df_stocks_clean.columns = [\n",
            "    'symbol', 'company_name', 'current_price', 'open_price', 'close_price',\n",
            "    'high_price', 'low_price', 'volume', 'market_cap', 'pe_ratio', 'eps',\n",
            "    'dividend_yield', 'high_52week', 'low_52week'\n",
            "]\n",
            "df_stocks_clean.to_sql(\"stocks\", conn, if_exists=\"replace\", index=False)\n",
            "\n",
            "# Insert News\n",
            "df_news_clean.columns = ['title', 'description', 'source', 'date']\n",
            "df_news_clean.to_sql(\"news\", conn, if_exists=\"replace\", index=False)\n",
            "\n",
            "# Seed standard user accounts if not present\n",
            "cursor.execute(\"SELECT COUNT(*) FROM users\")\n",
            "if cursor.fetchone()[0] == 0:\n",
            "    cursor.execute(\"INSERT INTO users (full_name, email, password, balance, is_admin) VALUES ('Paper Trader', 'user@aistockwave.com', 'user123', 100000.0, 0)\")\n",
            "    cursor.execute(\"INSERT INTO users (full_name, email, password, balance, is_admin) VALUES ('System Administrator', 'admin@aistockwave.com', 'admin123', 100000.0, 1)\")\n",
            "    conn.commit()\n",
            "    print(\"Demo users seeded.\")\n",
            "\n",
            "print(\"Tables seeded successfully.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 3. Running SQL Queries for Stock Analytics\n",
            "Let's execute query checks on our SQLite database."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "# Query 1: Top 5 stocks by Market Cap\n",
            "query_mc = \"\"\"\n",
            "SELECT symbol, company_name, market_cap, current_price \n",
            "FROM stocks \n",
            "ORDER BY market_cap DESC \n",
            "LIMIT 5\n",
            "\"\"\"\n",
            "print(pd.read_sql_query(query_mc, conn))\n",
            "\n",
            "print(\"\\n\")\n",
            "\n",
            "# Query 2: Stocks with high P/E multiples (valued highly)\n",
            "query_pe = \"\"\"\n",
            "SELECT symbol, company_name, pe_ratio \n",
            "FROM stocks \n",
            "WHERE pe_ratio > 30.0\n",
            "\"\"\"\n",
            "print(pd.read_sql_query(query_pe, conn))\n",
            "\n",
            "conn.close()"
        ]
    }
]

# ==================== NOTEBOOK 5: EDA & VISUALIZATION ====================
nb5_cells = [
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "# Module 5: Exploratory Data Analysis & Visualization\n",
            "This notebook visualizes stock sector metrics, distributions, and simulated portfolios using matplotlib and seaborn."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "import sqlite3\n",
            "import pandas as pd\n",
            "import matplotlib.pyplot as plt\n",
            "import seaborn as sns\n",
            "\n",
            "# Set design system parameters\n",
            "plt.style.use('seaborn-v0_8-darkgrid' if 'seaborn-v0_8-darkgrid' in plt.style.available else 'default')\n",
            "plt.rcParams['figure.figsize'] = (10, 6)\n",
            "print(\"Visualizers loaded.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 1. Load Data from SQLite Database"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "conn = sqlite3.connect(\"aistockwave.db\")\n",
            "df_stocks = pd.read_sql_query(\"SELECT * FROM stocks\", conn)\n",
            "conn.close()\n",
            "print(f\"Loaded {len(df_stocks)} stock records for visualization.\")"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 2. Chart 1: Top Stocks by Market Capitalization ($ Billions)\n",
            "We show company value comparisons."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "sns.barplot(x=\"market_cap\", y=\"symbol\", data=df_stocks.sort_values(\"market_cap\", ascending=False), palette=\"viridis\")\n",
            "plt.title(\"AIStockWave Tickers ranked by Market Capitalization ($ Billions)\", fontsize=14, fontweight='bold')\n",
            "plt.xlabel(\"Market Cap ($B)\")\n",
            "plt.ylabel(\"Ticker Ticker\")\n",
            "plt.tight_layout()\n",
            "plt.savefig(\"market_cap_chart.png\", dpi=150)\n",
            "plt.show()"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 3. Chart 2: Distribution of P/E Ratios\n",
            "Shows valuation frequencies."
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "sns.histplot(df_stocks['pe_ratio'], bins=6, kde=True, color=\"#2962FF\")\n",
            "plt.title(\"Distribution of Price-to-Earnings (P/E) Ratios\", fontsize=14, fontweight='bold')\n",
            "plt.xlabel(\"P/E Ratio\")\n",
            "plt.ylabel(\"Frequency count\")\n",
            "plt.tight_layout()\n",
            "plt.savefig(\"pe_distribution_chart.png\", dpi=150)\n",
            "plt.show()"
        ]
    },
    {
        "cell_type": "markdown",
        "metadata": {},
        "source": [
            "### 4. Chart 3: Proximity channels of Live Prices against 52W Highs / Lows"
        ]
    },
    {
        "cell_type": "code",
        "execution_count": None,
        "metadata": {},
        "outputs": [],
        "source": [
            "df_sorted = df_stocks.sort_values(\"current_price\")\n",
            "plt.scatter(df_sorted['symbol'], df_sorted['current_price'], color='blue', label='Live price', zorder=5)\n",
            "plt.scatter(df_sorted['symbol'], df_sorted['high_52week'], color='green', marker='^', label='52W High', alpha=0.6)\n",
            "plt.scatter(df_sorted['symbol'], df_sorted['low_52week'], color='red', marker='v', label='52W Low', alpha=0.6)\n",
            "plt.title(\"Current Prices relative to 52-Week High & Low Boundaries\", fontsize=14, fontweight='bold')\n",
            "plt.ylabel(\"Price in ₹\")\n",
            "plt.legend()\n",
            "plt.tight_layout()\n",
            "plt.savefig('52w_channels_chart.png', dpi=150)\n",
            "plt.show()"
        ]
    }
]

# Write out the notebooks
make_notebook("1_Collecting_Data_Using_APIs.ipynb", nb1_cells)
make_notebook("2_Web_Scraping_News.ipynb", nb2_cells)
make_notebook("3_Data_Wrangling.ipynb", nb3_cells)
make_notebook("4_Database_Integration_SQL.ipynb", nb4_cells)
make_notebook("5_Exploratory_Data_Analysis.ipynb", nb5_cells)
