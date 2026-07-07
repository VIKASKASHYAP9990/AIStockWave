# AIStockWave Capstone Streamlit Web Platform
import streamlit as st
import sqlite3
import pandas as pd
import numpy as np
import plotly.graph_objects as go
import os
from datetime import datetime

# ==================== PAGE SETTINGS & CONFIG ====================
st.set_page_config(
    page_title="AIStockWave – AI-Powered Paper Trading & Analytics Terminal",
    layout="wide",
    page_icon="📈"
)

# Custom Obsidian Fintech Stylesheet injection
st.markdown("""
<style>
    /* Styling overrides for premium fintech look */
    .stApp {
        background-color: #0B0E14;
        color: #FFFFFF;
    }
    .css-1d391kg {
        background-color: #131722 !important;
    }
    div[data-testid="stMetricValue"] {
        font-size: 24px !important;
        font-weight: 700 !important;
    }
    div[data-testid="stMetricLabel"] {
        color: #B2B5BE !important;
        font-size: 13px !important;
        font-weight: 600 !important;
        text-transform: uppercase;
    }
</style>
""", unsafe_allow_html=True)

DB_PATH = "aistockwave.db"

def get_db_connection():
    return sqlite3.connect(DB_PATH)

# ==================== PRICE FLUCTUATION SIMULATOR ====================
def simulate_market_ticks():
    """Applies a minor random-walk fluctuation to database stock prices on reload."""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute("SELECT symbol, current_price, high_price, low_price, open_price, volume FROM stocks")
    rows = cursor.fetchall()
    
    for row in rows:
        sym, price, high, low, op, vol = row
        # Fluctuation pct between -0.8% and +0.9%
        change_pct = (np.random.rand() * 1.7 - 0.8) / 100.0
        new_price = round(price * (1.0 + change_pct), 2)
        
        new_high = max(high, new_price)
        new_low = min(low, new_price)
        new_vol = vol + np.random.randint(150, 4000)
        
        cursor.execute("""
            UPDATE stocks 
            SET current_price = ?, high_price = ?, low_price = ?, volume = ? 
            WHERE symbol = ?
        """, (new_price, new_high, new_low, new_vol, sym))
        
    conn.commit()
    conn.close()

# Simulate minor changes on boot/refresh
simulate_market_ticks()

# ==================== AI ANALYTICS ENGINE ====================
def get_ai_analysis(s):
    pe = s['pe_ratio']
    div = s['dividend_yield']
    price = s['current_price']
    op = s['open_price']
    high52 = s['high_52week']
    low52 = s['low_52week']
    eps = s['eps']
    change = ((price - op) / op) * 100
    
    rating = "HOLD"
    summary = ""
    strengths = []
    risks = []
    
    if change > 2.0 and pe < 30.0 and eps > 0:
        rating = "STRONG BUY"
        summary = f"{s['company_name']} is displaying exceptionally strong bullish momentum. Rising {change:.2f}% to ₹{price}, the stock is trading under heavy buying volume. Stable valuation margins and a positive chart trend suggest high investor confidence."
    elif change >= 0 and pe < 40.0:
        rating = "BUY"
        summary = f"{s['company_name']} exhibits a stable market profile. Moderate valuation levels coupled with positive earnings support a constructive forecast over the short to medium term."
    elif pe > 60.0:
        rating = "UNDERPERFORM / SELL"
        summary = f"{s['company_name']} appears highly valued at the current level of ₹{price}. An elevated P/E ratio of {pe} implies that the stock price is reflecting aggressive growth expectations, exposing it to downward corrections if earnings disappoint."
    else:
        rating = "HOLD"
        summary = f"{s['company_name']} is consolidating within its standard trading range. With current price at ₹{price}, the market is awaiting catalyst news or earnings reports. Proximity to support levels indicates a balanced risk-to-reward ratio."

    # Strengths
    if pe < 25.0 and pe > 0:
        strengths.append(f"• Attractive Valuation: The P/E ratio is {pe:.2f}, which is below the tech sector average, offering a margin of safety.")
    if eps > 5.0:
        strengths.append(f"• Exceptional Profitability: An Earnings Per Share (EPS) of ₹{eps:.2f} demonstrates high core profitability.")
    if div > 0:
        strengths.append(f"• Yield Generosity: Provides a dividend yield of {div:.2f}%, securing consistent cash return for passive income portfolios.")
    if price > (high52 + low52) / 2:
        strengths.append("• Momentum Strength: Stock is trading in the upper half of its 52-week range, reflecting a long-term uptrend.")
    if not strengths:
        strengths.append("• High Liquidity: Large daily volume supports easy transaction entries and exits with low slippage.")

    # Risks
    if pe > 45.0:
        risks.append(f"• Premium Valuation: The P/E ratio is elevated at {pe:.2f}. This high multiple requires sustained high growth rate to justify.")
    if change < -2.0:
        risks.append(f"• Bearish Momentum: Heavy short-term selling pressure indicates near-term volatility.")
    if price < low52 * 1.15:
        risks.append(f"• Near 52-Week Lows: Trading close to its annual low (₹{low52}). Suggests operational challenges or weak industry sentiments.")
    if div == 0:
        risks.append("• No Yield: The company pays no dividends, requiring the investor to rely solely on capital appreciation.")
    if not risks:
        risks.append("• General Market Beta: Subject to systematic macro interest rate shifts and tech sector volatility.")

    education = f"1. P/E Ratio (Price-to-Earnings): {s['company_name']} has a P/E of {pe:.2f}. This means investors are paying ₹{pe:.2f} for every ₹1 of company earnings. High P/E stocks are considered 'growth' stocks; low P/E are 'value' stocks.\n\n2. EPS (Earnings Per Share): With an EPS of ₹{eps:.2f}, this is the portion of profit allocated to each share of stock. Higher EPS is generally a sign of financial strength.\n\n3. 52-Week Range: The stock is currently trading at ₹{price} against a 52-week low of ₹{low52} and a high of ₹{high52}. Buying near the high implies momentum trading, while buying near the low is contrarian/value trading."

    return {
        "rating": rating,
        "summary": summary,
        "strengths": "\n".join(strengths),
        "risks": "\n".join(risks),
        "education": education
    }

# ==================== HISTORICAL CHART POINTS GENERATOR ====================
def generate_historical_data(price, interval):
    points = 20
    volatility = 0.02
    labels = []
    
    if interval == "1D":
        points = 8
        volatility = 0.005
        labels = ["09:30", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:00"]
    elif interval == "1W":
        points = 5
        volatility = 0.012
        labels = ["Mon", "Tue", "Wed", "Thu", "Fri"]
    elif interval == "1M":
        points = 20
        volatility = 0.025
        labels = [f"Day {i}" for i in range(1, 21)]
    elif interval == "6M":
        points = 26
        volatility = 0.06
        labels = [f"Wk {i}" for i in range(1, 27)]
    elif interval == "1Y":
        points = 12
        volatility = 0.10;
        labels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
    elif interval == "5Y":
        points = 10
        volatility = 0.25
        labels = ["Y1-H1", "Y1-H2", "Y2-H1", "Y2-H2", "Y3-H1", "Y3-H2", "Y4-H1", "Y4-H2", "Y5-H1", "Y5-H2"]

    prices = []
    curr = price
    for i in range(points - 1, -1, -1):
        pct = (np.random.randn() * volatility)
        open_val = curr / (1.0 + pct)
        prices.append(round(curr, 2))
        curr = open_val
        
    prices.reverse()
    prices[-1] = price # Pin last to current price
    
    # Generate Candlestick components
    candles = []
    for i in range(len(prices)):
        o = prices[i]
        c = prices[i] * (1.0 + (np.random.rand() * 0.01 - 0.005))
        c = round(c, 2)
        h = max(o, c) * (1.0 + (np.random.rand() * 0.003))
        l = min(o, c) * (1.0 - (np.random.rand() * 0.003))
        h = round(h, 2)
        l = round(l, 2)
        lbl = labels[i] if i < len(labels) else f"Pt {i+1}"
        
        candles.append({"time": lbl, "open": o, "high": h, "low": l, "close": c})
        
    return pd.DataFrame(candles)

# ==================== SESSION CONTROLLER ====================
if "user" not in st.session_state:
    st.session_state["user"] = None

# ==================== AUTH PAGES ====================
def show_login_signup():
    st.markdown("<h1 style='text-align: center; color: #2962FF; font-family: Outfit;'>AIStockWave</h1>", unsafe_allow_html=True)
    st.markdown("<h3 style='text-align: center; color: white;'>Fintech Paper Trading & Stock Analyst Desk</h3>", unsafe_allow_html=True)
    
    tab_login, tab_register = st.tabs(["🚪 Account Log In", "👤 Create Account"])
    
    with tab_login:
        email = st.text_input("Email Address", key="login_email").strip().lower()
        password = st.password_input("Password", key="login_password")
        if st.button("Sign In", use_container_width=True):
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM users WHERE email = ? AND password = ?", (email, password))
            row = cursor.fetchone()
            conn.close()
            
            if row:
                st.session_state["user"] = {
                    "id": row[0],
                    "fullName": row[1],
                    "email": row[2],
                    "balance": row[4],
                    "isAdmin": bool(row[5])
                }
                st.success("Successfully logged in!")
                st.rerun()
            else:
                st.error("Invalid email address or password.")
                
    with tab_register:
        name = st.text_input("Full Name", key="reg_name").strip()
        email = st.text_input("Email Address", key="reg_email").strip().lower()
        password = st.password_input("Create Password", key="reg_password")
        confirm = st.password_input("Confirm Password", key="reg_confirm")
        
        if st.button("Register Trader", use_container_width=True):
            if not name or not email or not password or not confirm:
                st.error("Please fill in all inputs.")
            elif password != confirm:
                st.error("Passwords do not match.")
            elif len(password) < 6:
                st.error("Password must be at least 6 characters.")
            else:
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT id FROM users WHERE email = ?", (email,))
                if cursor.fetchone():
                    st.error("Email address is already in use.")
                    conn.close()
                else:
                    cursor.execute("INSERT INTO users (full_name, email, password, balance, is_admin) VALUES (?, ?, ?, 100000.0, 0)", (name, email, password))
                    conn.commit()
                    conn.close()
                    st.success("Account registered successfully! Please login in the first tab.")

# ==================== MAIN WORKSPACE ====================
if st.session_state["user"] is None:
    show_login_signup()
else:
    user = st.session_state["user"]
    
    # Refresh user stats
    conn = get_db_connection()
    df_usr = pd.read_sql_query("SELECT * FROM users WHERE id = ?", conn, params=(user["id"],))
    conn.close()
    if not df_usr.empty:
        user["balance"] = df_usr.iloc[0]["balance"]
        user["fullName"] = df_usr.iloc[0]["full_name"]
        st.session_state["user"] = user

    # Sidebar Nav Menu
    st.sidebar.markdown(f"<h2 style='color: #2962FF;'>AIStockWave</h2>", unsafe_allow_html=True)
    st.sidebar.markdown(f"**Hello, {user['fullName']}**")
    st.sidebar.markdown(f"Cash: <span style='color:#26A69A; font-weight:700;'>₹{user['balance']:.2f}</span>", unsafe_allow_html=True)
    
    menu = ["📊 Dashboard", "💼 Portfolio", "⭐ Watchlist", "📰 Market News", "👤 Profile Settings"]
    if user["isAdmin"]:
        menu.append("🛠️ Admin Control")
        
    choice = st.sidebar.radio("Navigation Menu", menu)
    
    if st.sidebar.button("🚪 Log Out", use_container_width=True):
        st.session_state["user"] = None
        st.rerun()

    # --- 1. DASHBOARD ---
    if choice == "📊 Dashboard":
        st.subheader("📊 Market Overview Dashboard")
        
        # Live Indices
        col1, col2, col3 = st.columns(3)
        nifty = 23450.25 + (np.random.rand() * 80 - 40)
        sensex = 76820.40 + (np.random.rand() * 250 - 125)
        sp = 5420.10 + (np.random.rand() * 20 - 10)
        
        col1.metric("🇮🇳 NIFTY 50", f"{nifty:,.2f}", f"+{np.random.rand()*0.8:.2f}%")
        col2.metric("🇮🇳 SENSEX", f"{sensex:,.2f}", f"+{np.random.rand()*0.7:.2f}%")
        col3.metric("🇺🇸 S&P 500", f"{sp:,.2f}", f"-{np.random.rand()*0.3:.2f}%")
        
        # Search Box
        st.write("---")
        query = st.text_input("🔍 Search stock symbol or company name (e.g. AAPL, TSLA):").strip().upper()
        if query:
            conn = get_db_connection()
            df_stocks = pd.read_sql_query("SELECT * FROM stocks", conn)
            conn.close()
            match = df_stocks[df_stocks['symbol'] == query]
            if match.empty:
                match = df_stocks[df_stocks['company_name'].str.upper().str.contains(query)]
            
            if not match.empty:
                sym = match.iloc[0]['symbol']
                st.success(f"Stock found! [Analyze {sym}](#stock-analysis)")
                # Redirect helper parameter
                st.session_state["active_symbol"] = sym
            else:
                st.error("No matches found.")
        
        # Stocks Table
        st.write("### Active Market Watch")
        conn = get_db_connection()
        df_stocks = pd.read_sql_query("SELECT symbol, company_name, current_price, open_price, high_price, low_price, volume FROM stocks ORDER BY symbol ASC", conn)
        conn.close()
        
        df_stocks['Change (%)'] = ((df_stocks['current_price'] - df_stocks['open_price']) / df_stocks['open_price']) * 100
        
        # Format columns for display
        df_display = df_stocks.copy()
        df_display['current_price'] = df_display['current_price'].apply(lambda x: f"₹{x:,.2f}")
        df_display['open_price'] = df_display['open_price'].apply(lambda x: f"₹{x:,.2f}")
        df_display['high_price'] = df_display['high_price'].apply(lambda x: f"₹{x:,.2f}")
        df_display['low_price'] = df_display['low_price'].apply(lambda x: f"₹{x:,.2f}")
        df_display['Change (%)'] = df_display['Change (%)'].apply(lambda x: f"{x:+.2f}%")
        df_display['volume'] = df_display['volume'].apply(lambda x: f"{x:,}")
        
        st.dataframe(df_display, use_container_width=True, hide_index=True)
        
        # Fast direct audit link
        st.write("---")
        st.markdown("<h3 id='stock-analysis'>📈 Stock Technical Audit Panel</h3>", unsafe_allow_html=True)
        
        symbol_list = list(df_stocks['symbol'].unique())
        selected_sym = st.selectbox("Select symbol to audit & trade:", symbol_list, index=0)
        
        # Load Selected Stock
        stock_data = df_stocks[df_stocks['symbol'] == selected_sym].iloc[0]
        
        # Proximity header
        col_hdr1, col_hdr2 = st.columns(2)
        chg_val = ((stock_data['current_price'] - stock_data['open_price']) / stock_data['open_price']) * 100
        col_hdr1.markdown(f"## {stock_data['symbol']} - {stock_data['company_name']}")
        col_hdr2.markdown(f"<h2 style='text-align:right;'>₹{stock_data['current_price']:.2f} <span style='color:{'#26A69A' if chg_val >= 0 else '#EF5350'}; font-size:20px;'>({chg_val:+.2f}%)</span></h2>", unsafe_allow_html=True)
        
        # Watchlist Toggle
        conn = get_db_connection()
        cursor = conn.cursor()
        cursor.execute("SELECT 1 FROM watchlist WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
        in_watch = cursor.fetchone() is not None
        
        if in_watch:
            if st.button("⭐ Saved in Watchlist (Click to remove)"):
                cursor.execute("DELETE FROM watchlist WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                conn.commit()
                st.success("Removed from watchlist.")
                st.rerun()
        else:
            if st.button("☆ Add to Watchlist"):
                cursor.execute("INSERT INTO watchlist (user_id, symbol) VALUES (?, ?)", (user['id'], selected_sym))
                conn.commit()
                st.success("Added to watchlist!")
                st.rerun()
        conn.close()

        # Chart Section
        tf_choice = st.radio("Chart Timeframe Interval:", ["1D", "1W", "1M", "6M", "1Y", "5Y"], index=2, horizontal=True)
        df_hist = generate_historical_data(stock_data['current_price'], tf_choice)
        
        # Create Plotly Candlestick Chart
        fig = go.Figure(data=[go.Candlestick(
            x=df_hist['time'],
            open=df_hist['open'],
            high=df_hist['high'],
            low=df_hist['low'],
            close=df_hist['close'],
            increasing_line_color='#26A69A',
            decreasing_line_color='#EF5350'
        )])
        fig.update_layout(
            paper_bgcolor='#131722',
            plot_bgcolor='#131722',
            xaxis_rangeslider_visible=False,
            height=360,
            margin=dict(l=20, r=20, t=10, b=10),
            yaxis=dict(gridcolor='#2A2E39'),
            xaxis=dict(gridcolor='#2A2E39')
        )
        st.plotly_chart(fig, use_container_width=True)

        # Split stats and order terminal
        col_st1, col_st2 = st.columns([2, 1])
        
        with col_st1:
            st.write("#### Key Financial Statistics")
            conn = get_db_connection()
            db_s = pd.read_sql_query("SELECT * FROM stocks WHERE symbol = ?", conn, params=(selected_sym,)).iloc[0]
            conn.close()
            
            st_col1, st_col2, st_col3 = st.columns(3)
            st_col1.metric("Open Price", f"₹{db_s['open_price']:.2f}")
            st_col1.metric("Today's High", f"₹{db_s['high_price']:.2f}")
            st_col1.metric("P/E Ratio", f"{db_s['pe_ratio']:.2f}" if db_s['pe_ratio'] > 0 else "N/A")
            
            st_col2.metric("Prev Close", f"₹{db_s['close_price']:.2f}")
            st_col2.metric("Today's Low", f"₹{db_s['low_price']:.2f}")
            st_col2.metric("EPS", f"₹{db_s['eps']:.2f}")
            
            st_col3.metric("Market Cap", f"₹{db_s['market_cap']:.1f}B")
            st_col3.metric("Volume", f"{int(db_s['volume']):,}")
            st_col3.metric("Dividend Yield", f"{db_s['dividend_yield']:.2f}%" if db_s['dividend_yield'] > 0 else "0.00%")
            
        with col_st2:
            st.write("#### Paper Trading Desk")
            st.write(f"Wallet balance: ₹{user['balance']:,.2f}")
            trade_type = st.radio("Order Action:", ["BUY", "SELL"], horizontal=True)
            trade_qty = st.number_input("Shares Quantity:", min_value=1, value=10, step=1)
            
            est_value = trade_qty * db_s['current_price']
            st.markdown(f"Estimated Cost: **₹{est_value:,.2f}**")
            
            if st.button("EXECUTE ORDER", use_container_width=True):
                conn = get_db_connection()
                cursor = conn.cursor()
                try:
                    # 1. Fetch current cash balance
                    cursor.execute("SELECT balance FROM users WHERE id = ?", (user['id'],))
                    current_cash = cursor.fetchone()[0]
                    
                    if trade_type == "BUY":
                        if current_cash < est_value:
                            st.error(f"Insufficient cash reserves. Required: ₹{est_value:.2f}, Available: ₹{current_cash:.2f}")
                        else:
                            # Deduct cash
                            cursor.execute("UPDATE users SET balance = balance - ? WHERE id = ?", (est_value, user['id']))
                            # Update portfolio
                            cursor.execute("SELECT quantity, avg_buy_price FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                            row_port = cursor.fetchone()
                            if row_port:
                                old_qty, old_avg = row_port
                                new_qty = old_qty + trade_qty
                                new_avg = ((old_qty * old_avg) + est_value) / new_qty
                                cursor.execute("UPDATE portfolio SET quantity = ?, avg_buy_price = ? WHERE user_id = ? AND symbol = ?", (new_qty, new_avg, user['id'], selected_sym))
                            else:
                                cursor.execute("INSERT INTO portfolio (user_id, symbol, quantity, avg_buy_price) VALUES (?, ?, ?, ?)", (user['id'], selected_sym, trade_qty, db_s['current_price']))
                            
                            # Log transaction
                            cursor.execute("INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'BUY', ?, ?, ?)", (user['id'], selected_sym, trade_qty, db_s['current_price']))
                            conn.commit()
                            st.success(f"SUCCESS: Bought {trade_qty} shares of {selected_sym}!")
                            st.rerun()
                    else: # SELL
                        cursor.execute("SELECT quantity FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                        row_qty = cursor.fetchone()
                        has_qty = row_qty[0] if row_qty else 0
                        
                        if has_qty < trade_qty:
                            st.error(f"Insufficient stock holdings. Available: {has_qty} shares.")
                        else:
                            # Credit cash
                            cursor.execute("UPDATE users SET balance = balance + ? WHERE id = ?", (est_value, user['id']))
                            # Update portfolio
                            if has_qty == trade_qty:
                                cursor.execute("DELETE FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                            else:
                                cursor.execute("UPDATE portfolio SET quantity = quantity - ? WHERE user_id = ? AND symbol = ?", (trade_qty, user['id'], selected_sym))
                            
                            # Log transaction
                            cursor.execute("INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'SELL', ?, ?, ?)", (user['id'], selected_sym, trade_qty, db_s['current_price']))
                            conn.commit()
                            st.success(f"SUCCESS: Sold {trade_qty} shares of {selected_sym}!")
                            st.rerun()
                except Exception as ex:
                    st.error(f"Execution failed: {ex}")
                finally:
                    conn.close()

        # AI Analyst Card
        st.write("---")
        st.write("#### 🤖 AI stock Auditor Insights")
        ai_report = get_ai_analysis(db_s)
        
        st.markdown(f"**SENTIMENT RATING:** `{ai_report['rating']}`")
        st.info(ai_report['summary'])
        
        ai_col1, ai_col2 = st.columns(2)
        ai_col1.markdown("##### 🟢 Growth Strengths")
        ai_col1.markdown(ai_report['strengths'])
        
        ai_col2.markdown("##### 🔴 Vulnerability Risks")
        ai_col2.markdown(ai_report['risks'])
        
        st.write("---")
        st.markdown("##### 📚 Investor Educational Concept")
        st.markdown(ai_report['education'])

    # --- 2. PORTFOLIO ---
    elif choice == "💼 Portfolio":
        st.subheader("💼 Virtual Trading Portfolio Desk")
        
        # Load user holdings
        conn = get_db_connection()
        df_p = pd.read_sql_query("""
            SELECT p.symbol, s.company_name, p.quantity, p.avg_buy_price, s.current_price 
            FROM portfolio p 
            JOIN stocks s ON p.symbol = s.symbol 
            WHERE p.user_id = ? AND p.quantity > 0
        """, conn, params=(user['id'],))
        conn.close()
        
        # Calculate summary values
        total_holdings_val = 0
        original_holdings_cost = 0
        
        df_p['Current Value'] = df_p['quantity'] * df_p['current_price']
        df_p['Initial Cost'] = df_p['quantity'] * df_p['avg_buy_price']
        df_p['Profit/Loss'] = df_p['Current Value'] - df_p['Initial Cost']
        df_p['Return (%)'] = (df_p['current_price'] - df_p['avg_buy_price']) / df_p['avg_buy_price'] * 100
        
        total_holdings_val = df_p['Current Value'].sum()
        original_holdings_cost = df_p['Initial Cost'].sum()
        net_assets = user['balance'] + total_holdings_val
        net_returns = net_assets - 100000.00
        roi = (net_returns / 100000.00) * 100
        
        col1, col2, col3 = st.columns(3)
        col1.metric("💸 Virtual Cash Available", f"₹{user['balance']:,.2f}")
        col2.metric("📈 Net Assets Valuation", f"₹{net_assets:,.2f}")
        col3.metric("📊 Lifetime Returns ROI", f"₹{net_returns:,.2f}", f"{roi:+.2f}%")
        
        # Holdings table
        st.write("### Current Stock Assets")
        if df_p.empty:
            st.write("You do not hold any stocks in your virtual portfolio yet.")
        else:
            df_port_disp = df_p[['symbol', 'company_name', 'quantity', 'avg_buy_price', 'current_price', 'Current Value', 'Profit/Loss', 'Return (%)']].copy()
            df_port_disp['avg_buy_price'] = df_port_disp['avg_buy_price'].apply(lambda x: f"₹{x:,.2f}")
            df_port_disp['current_price'] = df_port_disp['current_price'].apply(lambda x: f"₹{x:,.2f}")
            df_port_disp['Current Value'] = df_port_disp['Current Value'].apply(lambda x: f"₹{x:,.2f}")
            df_port_disp['Profit/Loss'] = df_port_disp['Profit/Loss'].apply(lambda x: f"₹{x:+,.2f}")
            df_port_disp['Return (%)'] = df_port_disp['Return (%)'].apply(lambda x: f"{x:+.2f}%")
            
            st.dataframe(df_port_disp, use_container_width=True, hide_index=True)
            
        # Transaction Log
        st.write("### Transaction Log History")
        conn = get_db_connection()
        df_txs = pd.read_sql_query("SELECT timestamp, type, symbol, quantity, price FROM transactions WHERE user_id = ? ORDER BY timestamp DESC", conn, params=(user['id'],))
        conn.close()
        
        if df_txs.empty:
            st.write("No transaction execution history logged.")
        else:
            df_txs['price'] = df_txs['price'].apply(lambda x: f"₹{x:,.2f}")
            df_txs['Total Value'] = df_txs.apply(lambda r: f"₹{(int(r['quantity']) * float(r['price'].replace('₹','').replace(',',''))):,.2f}", axis=1)
            st.dataframe(df_txs, use_container_width=True, hide_index=True)

    # --- 3. WATCHLIST ---
    elif choice == "⭐ Watchlist":
        st.subheader("⭐ My Saved Stock Watchlist")
        
        conn = get_db_connection()
        df_w = pd.read_sql_query("""
            SELECT s.* FROM watchlist w 
            JOIN stocks s ON w.symbol = s.symbol 
            WHERE w.user_id = ?
        """, conn, params=(user['id'],))
        conn.close()
        
        if df_w.empty:
            st.write("Your watchlist is currently empty. Go to the Dashboard tab to add stock symbols.")
        else:
            cols = st.columns(4)
            for idx, row in df_w.iterrows():
                chg = ((row['current_price'] - row['open_price']) / row['open_price']) * 100
                col_card = cols[idx % 4]
                with col_card:
                    st.markdown(f"""
                    <div style='background-color:#131722; padding:15px; border-radius:8px; border:1px solid #2A2E39; margin-bottom:10px;'>
                        <span style='font-size:18px; font-weight:700;'>{row['symbol']}</span><br>
                        <span style='font-size:11px; color:#B2B5BE;'>{row['company_name']}</span><br>
                        <span style='font-size:20px; font-weight:700;'>₹{row['current_price']:.2f}</span>
                        <span style='color:{'#26A69A' if chg >= 0 else '#EF5350'}; font-weight:700;'> {chg:+.2f}%</span>
                    </div>
                    """, unsafe_allow_html=True)
                    
                    if st.button("✕ Delete", key=f"del_watch_{row['symbol']}"):
                        conn = get_db_connection()
                        cursor = conn.cursor()
                        cursor.execute("DELETE FROM watchlist WHERE user_id = ? AND symbol = ?", (user['id'], row['symbol']))
                        conn.commit()
                        conn.close()
                        st.success("Removed from watchlist.")
                        st.rerun()

    # --- 4. MARKET NEWS ---
    elif choice == "📰 Market News":
        st.subheader("📰 Financial News Feed")
        
        conn = get_db_connection()
        df_news = pd.read_sql_query("SELECT title, description, source, date FROM news ORDER BY rowid DESC", conn)
        conn.close()
        
        for idx, row in df_news.iterrows():
            st.markdown(f"#### {row['title']}")
            st.markdown(f"<span style='color:#2962FF; font-weight:600; font-size:11px;'>{row['source'].upper()}</span> • <span style='color:#B2B5BE; font-size:11px;'>{row['date']}</span>", unsafe_allow_html=True)
            st.write(row['description'])
            st.write("---")

    # --- 5. PROFILE SETTINGS ---
    elif choice == "👤 Profile Settings":
        st.subheader("👤 Account & API Configurations")
        
        col_pr1, col_pr2 = st.columns(2)
        
        with col_pr1:
            st.write("#### Secure Password Reset")
            cur_pass = st.password_input("Current Password", key="prof_cur")
            new_pass = st.password_input("New Password", key="prof_new")
            confirm_pass = st.password_input("Confirm New Password", key="prof_confirm")
            
            if st.button("Update Password"):
                if not cur_pass or not new_pass or not confirm_pass:
                    st.error("Please fill in all password fields.")
                elif new_pass != confirm_pass:
                    st.error("New passwords do not match.")
                elif len(new_pass) < 6:
                    st.error("New password must be at least 6 characters.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("SELECT password FROM users WHERE id = ?", (user['id'],))
                    db_p = cursor.fetchone()[0]
                    if db_p != cur_pass:
                        st.error("Incorrect current password.")
                        conn.close()
                    else:
                        cursor.execute("UPDATE users SET password = ? WHERE id = ?", (new_pass, user['id']))
                        conn.commit()
                        conn.close()
                        st.success("Password updated successfully!")
                        
        with col_pr2:
            st.write("#### Financial API Configurations")
            st.write("Enter your developer API keys below. The terminal falls back to simulated geometric Brownian fluctuations if keys are unconfigured.")
            st.text_input("Finnhub Key", placeholder="Enter key...")
            st.text_input("Alpha Vantage Key", placeholder="Enter key...")
            if st.button("Apply API Credentials"):
                st.success("API configurations set successfully.")

    # --- 6. ADMIN CONTROL ---
    elif choice == "🛠️ Admin Control" and user["isAdmin"]:
        st.subheader("🛠️ Platform Administrator Controls")
        
        # Load platform statistics
        conn = get_db_connection()
        cursor = conn.cursor()
        
        cursor.execute("SELECT COUNT(*) FROM users")
        user_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT COUNT(*) FROM transactions")
        tx_count = cursor.fetchone()[0]
        
        cursor.execute("SELECT SUM(quantity * price) FROM transactions")
        gross_val = cursor.fetchone()[0] or 0.0
        conn.close()
        
        col1, col2, col3 = st.columns(3)
        col1.metric("Accounts Registered", f"{user_count}")
        col2.metric("Transactions Count", f"{tx_count}")
        col3.metric("Gross Traded Volume", f"₹{gross_val:,.2f}")
        
        # Users Table
        st.write("---")
        st.write("### User Profile Control")
        conn = get_db_connection()
        df_users_all = pd.read_sql_query("SELECT id, full_name, email, balance, is_admin FROM users", conn)
        conn.close()
        
        df_users_all['is_admin'] = df_users_all['is_admin'].apply(lambda x: "Admin" if x == 1 else "Trader")
        df_users_all['balance'] = df_users_all['balance'].apply(lambda x: f"₹{x:,.2f}")
        st.dataframe(df_users_all, use_container_width=True, hide_index=True)
        
        # Balance reset panel
        st.write("#### Reset Wallet Balance")
        reset_id = st.number_input("Enter User ID to reset balance to ₹100,000:", min_value=1, step=1)
        if st.button("Reset Wallet"):
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("SELECT id FROM users WHERE id = ?", (reset_id,))
            if cursor.fetchone() is None:
                st.error("User ID not found.")
            else:
                cursor.execute("UPDATE users SET balance = 100000.00 WHERE id = ?", (reset_id,))
                conn.commit()
                st.success(f"Successfully reset User ID {reset_id} to ₹100,000!")
                st.rerun()
            conn.close()
            
        # Forms Split Row
        st.write("---")
        st.write("### Create Platform Assets")
        col_ad1, col_ad2 = st.columns(2)
        
        with col_ad1:
            st.write("#### Register New Ticker Symbol")
            sym = st.text_input("Ticker Symbol (e.g. INF):").strip().upper()
            comp = st.text_input("Company Name:")
            price = st.number_input("Initial Price (₹):", min_value=1.0, value=100.0)
            cap = st.number_input("Market Cap ($ Billions):", min_value=0.1, value=5.0)
            
            if st.button("Register Stock"):
                if not sym or not comp:
                    st.error("Please fill in symbol and company name.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("SELECT symbol FROM stocks WHERE symbol = ?", (sym,))
                    if cursor.fetchone():
                        st.error("Ticker symbol already registered.")
                    else:
                        eps = price * 0.04
                        pe = price / eps
                        cursor.execute("""
                            INSERT INTO stocks (symbol, company_name, current_price, open_price, close_price, high_price, low_price, volume, market_cap, pe_ratio, eps, dividend_yield, high_52week, low_52week) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, 150000, ?, ?, ?, 1.25, ?, ?)
                        """, (sym, comp, price, price, price, price, price, cap, pe, eps, price, price))
                        conn.commit()
                        st.success(f"SUCCESS: Ticker {sym} registered and active in simulation!")
                        st.rerun()
                    conn.close()
                    
        with col_ad2:
            st.write("#### Publish Financial Market Event")
            title = st.text_input("Headline Title:")
            source = st.text_input("Publisher Source:")
            desc = st.text_area("Event Description Content:")
            
            if st.button("Publish News Event"):
                if not title or not source or not desc:
                    st.error("Please fill in all news fields.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    today_str = datetime.now().strftime("%B %d, %Y")
                    cursor.execute("INSERT INTO news (title, description, source, date) VALUES (?, ?, ?, ?)", (title, desc, source, today_str))
                    conn.commit()
                    conn.close()
                    st.success("SUCCESS: Headline published to financial feed!")
                    st.rerun()
