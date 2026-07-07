# AIStockWave – Complete Capstone Stock Market Web Application
import streamlit as st
import sqlite3
import pandas as pd
import numpy as np
import plotly.graph_objects as go
import os
import json
import re
from datetime import datetime

# ==================== PAGE CONFIG ====================
st.set_page_config(
    page_title="AIStockWave – AI-Powered Paper Trading & Analytics Terminal",
    layout="wide",
    page_icon="📈"
)

DB_PATH = "aistockwave.db"

def get_db_connection():
    return sqlite3.connect(DB_PATH)

# ==================== COLOR SYSTEM & CUSTOM CSS ====================
if "theme" not in st.session_state:
    st.session_state["theme"] = "Dark Mode"

def inject_theme_styles():
    theme = st.session_state["theme"]
    if theme == "Light Mode":
        st.markdown("""
        <style>
            .stApp {
                background-color: #F8F9FA !important;
                color: #0F172A !important;
            }
            div[data-testid="stSidebar"] {
                background-color: #FFFFFF !important;
                border-right: 1px solid #E2E8F0 !important;
            }
            /* Text elements forced color */
            h1, h2, h3, h4, h5, h6, p, label, li, span {
                color: #0F172A !important;
            }
            div[data-testid="stMarkdownContainer"] p {
                color: #0F172A !important;
            }
            div[data-testid="stMetricValue"] {
                color: #2962FF !important;
                font-weight: 700 !important;
            }
            div[data-testid="stMetricLabel"] {
                color: #475569 !important;
                font-weight: 600 !important;
            }
            .stDataFrame {
                background-color: #FFFFFF !important;
            }
            .card-wrapper {
                background-color: #FFFFFF;
                border: 1px solid #E2E8F0;
                border-radius: 10px;
                padding: 20px;
                margin-bottom: 15px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.02);
            }
            /* Custom Emerald Green button actions */
            div.stButton > button {
                background-color: #26A69A !important;
                color: #FFFFFF !important;
                border-radius: 8px !important;
                border: 1px solid #26A69A !important;
                font-weight: 600 !important;
                transition: all 0.2s ease !important;
            }
            div.stButton > button:hover {
                background-color: #1f8a7e !important;
                border-color: #1f8a7e !important;
            }
        </style>
        """, unsafe_allow_html=True)
    else: # Dark Mode
        st.markdown("""
        <style>
            .stApp {
                background-color: #0B0E14 !important;
                color: #FFFFFF !important;
            }
            div[data-testid="stSidebar"] {
                background-color: #131722 !important;
                border-right: 1px solid #2A2E39 !important;
            }
            h1, h2, h3, h4, h5, h6, p, label, li, span {
                color: #FFFFFF !important;
            }
            div[data-testid="stMarkdownContainer"] p {
                color: #FFFFFF !important;
            }
            div[data-testid="stMetricValue"] {
                color: #00E676 !important;
                font-weight: 700 !important;
            }
            div[data-testid="stMetricLabel"] {
                color: #B2B5BE !important;
                font-weight: 600 !important;
            }
            .stDataFrame {
                background-color: #131722 !important;
            }
            .card-wrapper {
                background-color: #131722;
                border: 1px solid #2A2E39;
                border-radius: 10px;
                padding: 20px;
                margin-bottom: 15px;
                box-shadow: 0 4px 6px rgba(0,0,0,0.3);
            }
            /* Custom Emerald Green button actions */
            div.stButton > button {
                background-color: #26A69A !important;
                color: #FFFFFF !important;
                border-radius: 8px !important;
                border: 1px solid #26A69A !important;
                font-weight: 600 !important;
                transition: all 0.2s ease !important;
            }
            div.stButton > button:hover {
                background-color: #1f8a7e !important;
                border-color: #1f8a7e !important;
            }
        </style>
        """, unsafe_allow_html=True)

inject_theme_styles()

# ==================== LIVE PRICE FLUCTUATION & ALERT BREACH CHECKS ====================
def simulate_ticks_and_check_alerts():
    conn = get_db_connection()
    cursor = conn.cursor()
    
    # 1. Simulate Price Fluctuation (Geometric Brownian Walk)
    cursor.execute("SELECT symbol, current_price, high_price, low_price, open_price, volume FROM stocks")
    stocks = cursor.fetchall()
    
    updated_quotes = {}
    for row in stocks:
        sym, price, high, low, op, vol = row
        change_pct = (np.random.rand() * 1.6 - 0.75) / 100.0
        new_price = round(price * (1.0 + change_pct), 2)
        new_high = max(high, new_price)
        new_low = min(low, new_price)
        new_vol = vol + np.random.randint(100, 3000)
        
        cursor.execute("""
            UPDATE stocks 
            SET current_price = ?, high_price = ?, low_price = ?, volume = ? 
            WHERE symbol = ?
        """, (new_price, new_high, new_low, new_vol, sym))
        
        updated_quotes[sym] = new_price
        
    # 2. Check Price Alerts triggers
    cursor.execute("SELECT user_id, symbol, target_price, condition FROM price_alerts WHERE is_triggered = 0")
    alerts = cursor.fetchall()
    
    for alert in alerts:
        usr_id, sym, target, cond = alert
        if sym in updated_quotes:
            curr = updated_quotes[sym]
            triggered = False
            if cond == "ABOVE" and curr >= target:
                triggered = True
            elif cond == "BELOW" and curr <= target:
                triggered = True
                
            if triggered:
                cursor.execute("UPDATE price_alerts SET is_triggered = 1 WHERE user_id = ? AND symbol = ? AND target_price = ?", (usr_id, sym, target))
                cursor.execute("""
                    INSERT INTO notifications (user_id, title, message) 
                    VALUES (?, '🚨 Price Alert Triggered!', 'Stock symbol ' || ? || ' crossed your target limit of ₹' || ? || ' (Current: ₹' || ? || ')')
                """, (usr_id, sym, target, curr))
                
    conn.commit()
    conn.close()

simulate_ticks_and_check_alerts()

# ==================== INPUT VAL & SECURITY HELPER UTILITIES ====================
def validate_email_gmail(email):
    return email.endswith("@gmail.com")

def check_password_complexity(pwd):
    # Criteria checklist
    has_len = len(pwd) >= 8
    has_upper = re.search(r"[A-Z]", pwd) is not None
    has_lower = re.search(r"[a-z]", pwd) is not None
    has_digit = re.search(r"\d", pwd) is not None
    has_spec = re.search(r"[!@#$%^&*(),.?\":{}|<>]", pwd) is not None
    
    score = sum([has_len, has_upper, has_lower, has_digit, has_spec])
    strength = "Weak"
    if score == 5:
        strength = "Strong"
    elif score >= 3:
        strength = "Medium"
        
    return {
        "score": score,
        "strength": strength,
        "valid": score == 5,
        "has_len": has_len,
        "has_upper": has_upper,
        "has_lower": has_lower,
        "has_digit": has_digit,
        "has_spec": has_spec
    }

# ==================== INITIAL SYSTEM SESSION VARIABLES ====================
if "user" not in st.session_state:
    st.session_state["user"] = None
if "auth_mode" not in st.session_state:
    st.session_state["auth_mode"] = "Landing"
if "temp_otp" not in st.session_state:
    st.session_state["temp_otp"] = None
if "temp_signup_data" not in st.session_state:
    st.session_state["temp_signup_data"] = None
if "chat_messages" not in st.session_state:
    st.session_state["chat_messages"] = []
if "otp_time" not in st.session_state:
    st.session_state["otp_time"] = None
if "forgot_pass_email" not in st.session_state:
    st.session_state["forgot_pass_email"] = None

# ==================== LANDING PAGE VIEW ====================
def show_landing_page():
    # Navbar representation
    col_nav1, col_nav2 = st.columns([4, 1])
    with col_nav1:
        st.markdown("<h2 style='color:#2962FF; font-family:Outfit; margin-top:0;'>🌊 AIStockWave</h2>", unsafe_allow_html=True)
    with col_nav2:
        col_btn1, col_btn2 = st.columns(2)
        if col_btn1.button("Log In", use_container_width=True, key="landing_login_btn"):
            st.session_state["auth_mode"] = "Login"
            st.rerun()
        if col_btn2.button("Sign Up", use_container_width=True, key="landing_signup_btn"):
            st.session_state["auth_mode"] = "Signup"
            st.rerun()
            
    # Hero Section
    st.markdown("""
    <div style='background: linear-gradient(135deg, rgba(41,98,255,0.15) 0%, rgba(38,166,154,0.05) 100%); padding: 60px 40px; border-radius:15px; margin: 20px 0; border: 1px dashed rgba(255,255,255,0.1); text-align:center;'>
        <h1 style='font-family: Outfit; font-size:45px; font-weight:700; margin-bottom:10px;'>Analyze Markets & Trade Smart with AI</h1>
        <p style='color:#B2B5BE; font-size:16px; max-width:800px; margin: 0 auto 25px auto;'>AIStockWave is a premium educational paper trading terminal and stock auditor. Explaining complex metrics, auditing company stats, and calculating portfolio risk limits entirely risk-free.</p>
    </div>
    """, unsafe_allow_html=True)
    
    # Live market ticker
    st.markdown("##### ⚡ Live Simulated Quotes")
    conn = get_db_connection()
    df_tk = pd.read_sql_query("SELECT symbol, current_price, open_price FROM stocks LIMIT 6", conn)
    conn.close()
    
    cols_tk = st.columns(6)
    for idx, row in df_tk.iterrows():
        chg = ((row['current_price'] - row['open_price']) / row['open_price']) * 100
        cols_tk[idx].markdown(f"""
        <div style='background-color:#131722; padding:10px; border-radius:6px; border:1px solid #2A2E39; text-align:center;'>
            <span style='font-weight:700; font-size:14px; color:white;'>{row['symbol']}</span><br>
            <span style='font-weight:600; font-size:15px; color:#2962FF;'>₹{row['current_price']:.2f}</span>
            <span style='font-size:12px; color:{'#00E676' if chg>=0 else '#EF5350'};'> {chg:+.2f}%</span>
        </div>
        """, unsafe_allow_html=True)

    # Features Cards Grid
    st.write("---")
    st.markdown("<h3 style='text-align:center;'>🛠️ Built-in Investor Modules</h3>", unsafe_allow_html=True)
    col_f1, col_f2, col_f3 = st.columns(3)
    
    col_f1.markdown("""
    <div class='card-wrapper' style='text-align:center;'>
        <span style='font-size:35px;'>💼</span>
        <h4 style='margin-top:10px;'>Paper Trading Desk</h4>
        <p style='font-size:13px; color:#B2B5BE;'>Trade with ₹100,000 in paper cash. Practice order entries, compute average costs, and review lifetime P/L results.</p>
    </div>
    """, unsafe_allow_html=True)
    
    col_f2.markdown("""
    <div class='card-wrapper' style='text-align:center;'>
        <span style='font-size:35px;'>🤖</span>
        <h4 style='margin-top:10px;'>AI Stock Auditor</h4>
        <p style='font-size:13px; color:#B2B5BE;'>Generates automated valuations reports checking P/E metrics, EPS growths, 52W margins, and financial lesson insights.</p>
    </div>
    """, unsafe_allow_html=True)
    
    col_f3.markdown("""
    <div class='card-wrapper' style='text-align:center;'>
        <span style='font-size:35px;'>📈</span>
        <h4 style='margin-top:10px;'>Premium Charting</h4>
        <p style='font-size:13px; color:#B2B5BE;'>Explore interactive financial candlestick grids and lines to evaluate volume channels and index resistances.</p>
    </div>
    """, unsafe_allow_html=True)

    # Pricing Grid Section
    st.write("---")
    st.markdown("<h3 style='text-align:center;'>💰 Fair Subscription Tiers</h3>", unsafe_allow_html=True)
    col_pr1, col_pr2 = st.columns(2)
    
    col_pr1.markdown("""
    <div class='card-wrapper' style='border-top: 4px solid #B2B5BE;'>
        <h3 style='text-align:center;'>Free Plan</h3>
        <h2 style='text-align:center; color:#2962FF;'>₹0 <span style='font-size:14px; color:#B2B5BE;'>/ Month</span></h2>
        <hr style='border-color:#2A2E39;'>
        <ul style='font-size:13px; line-height:1.8; color:#B2B5BE;'>
            <li>✓ ₹100,000 Simulated Balance</li>
            <li>✓ Basic Watchlists (1 default list)</li>
            <li>✓ Financial News Feeds</li>
            <li>✗ Advanced AI Valuations & Chatbot</li>
            <li>✗ Customized Price Alert thresholds</li>
        </ul>
    </div>
    """, unsafe_allow_html=True)
    
    col_pr2.markdown("""
    <div class='card-wrapper' style='border-top: 4px solid #2962FF;'>
        <h3 style='text-align:center;'>Pro Analyst Plan</h3>
        <h2 style='text-align:center; color:#00E676;'>₹499 <span style='font-size:14px; color:#B2B5BE;'>/ Month</span></h2>
        <hr style='border-color:#2A2E39;'>
        <ul style='font-size:13px; line-height:1.8; color:#B2B5BE;'>
            <li>✓ Everything in Free Plan</li>
            <li>✓ <b>Advanced Interactive AI chatbot</b></li>
            <li>✓ Multi-watchlist directories creation</li>
            <li>✓ Real-time <b>Price Alarm breach emails</b></li>
            <li>✓ Detailed Portfolio health audit reports</li>
            <li>✓ Priority support channel</li>
        </ul>
    </div>
    """, unsafe_allow_html=True)

    # FAQ Accordion
    st.write("---")
    st.markdown("<h3 style='text-align:center;'>🙋 Frequently Asked Questions</h3>", unsafe_allow_html=True)
    with st.expander("Is AIStockWave using real brokerage cash?"):
        st.write("No. AIStockWave is purely educational. All trade calculations utilize virtual cash balances of ₹100,000.00.")
    with st.expander("How does the stock simulation function?"):
        st.write("The platform utilizes standard Geometric Brownian Motion (random walk) algorithm loops to update prices on interface clicks.")

    # Contact Section
    st.write("---")
    st.markdown("<h3 style='text-align:center;'>✉️ Get in Touch</h3>", unsafe_allow_html=True)
    col_c1, col_c2 = st.columns([1, 1])
    with col_c1:
        st.write("Have feedback or experiencing platform issues? Write to our Support Desk directly.")
        st.markdown("**Email**: support@aistockwave.com")
        st.markdown("**Phone**: +91-98765-43210")
    with col_c2:
        st.text_input("Name", placeholder="Your name")
        st.text_input("Email", placeholder="your.email@gmail.com")
        st.text_area("Message", placeholder="Write message description...")
        if st.button("Send Inquiry"):
            st.success("Your message has been sent successfully!")

# ==================== SECURITY AUTHENTICATION PAGES ====================
def show_auth_pages():
    mode = st.session_state["auth_mode"]
    
    if mode == "Login":
        st.markdown("<h2 style='text-align:center; color:#2962FF;'>🔑 Sign In</h2>", unsafe_allow_html=True)
        email = st.text_input("Email Address").strip().lower()
        
        # Show/Hide password toggle implementation
        show_pwd = st.checkbox("Show password characters")
        pwd_type = "text" if show_pwd else "password"
        password = st.text_input("Password", type=pwd_type)
        
        remember = st.checkbox("Remember Me")
        
        col_a1, col_a2 = st.columns(2)
        if col_a1.button("Log In", use_container_width=True):
            if not email or not password:
                st.error("Please fill in email and password credentials.")
            else:
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT * FROM users WHERE email = ?", (email,))
                user_row = cursor.fetchone()
                
                if user_row:
                    db_id, db_name, db_email, db_pwd, db_mob, db_ver, _, db_tier, db_block, db_hist, db_bal, db_adm = user_row
                    
                    if db_pwd != password:
                        st.error("Incorrect password credentials.")
                    elif db_block == 1:
                        st.error("This user account has been blocked by administrators.")
                    elif db_ver == 0:
                        st.error("Account not verified. Please register to verify.")
                    else:
                        # Append to login history
                        history = json.loads(db_hist or "[]")
                        history.append(datetime.now().strftime("%Y-%m-%d %H:%M:%S"))
                        cursor.execute("UPDATE users SET login_history = ? WHERE id = ?", (json.dumps(history), db_id))
                        conn.commit()
                        
                        st.session_state["user"] = {
                            "id": db_id,
                            "fullName": db_name,
                            "email": db_email,
                            "balance": db_bal,
                            "isAdmin": bool(db_adm),
                            "tier": db_tier,
                            "mobile": db_mob
                        }
                        st.success("Successfully logged in!")
                        st.session_state["auth_mode"] = "App"
                        st.rerun()
                else:
                    st.error("No account matching that email address found.")
                conn.close()
                
        if col_a2.button("Forgot Password?", use_container_width=True):
            st.session_state["auth_mode"] = "Forgot"
            st.rerun()
            
        st.write("---")
        if st.button("🌐 Continue with Google Mock", use_container_width=True):
            st.success("Google sign-in simulated successfully!")
            
        if st.button("← Back to Home"):
            st.session_state["auth_mode"] = "Landing"
            st.rerun()

    elif mode == "Signup":
        st.markdown("<h2 style='text-align:center; color:#2962FF;'>👤 Create Account</h2>", unsafe_allow_html=True)
        name = st.text_input("Full Name").strip()
        email = st.text_input("Email Address (Must end with @gmail.com)").strip().lower()
        mobile = st.text_input("Mobile Number (10 digits)").strip()
        
        show_pwd = st.checkbox("Show password characters")
        pwd_type = "text" if show_pwd else "password"
        password = st.text_input("Choose Password", type=pwd_type)
        confirm = st.text_input("Confirm Password", type=pwd_type)
        
        # Real-time Password Strength Meter
        if password:
            chk = check_password_complexity(password)
            if chk["strength"] == "Strong":
                st.success("Password Strength: STRONG (Meets complexity parameters)")
            elif chk["strength"] == "Medium":
                st.warning("Password Strength: MEDIUM (Consider adding digits or symbols)")
            else:
                st.error("Password Strength: WEAK (Requires uppercase, lowercase, numbers, and symbols)")
                
        if st.button("Generate Verification OTP", use_container_width=True):
            if not name or not email or not mobile or not password or not confirm:
                st.error("Please fill in all registration inputs.")
            elif not validate_email_gmail(email):
                st.error("SECURITY RULES: Registration email domain must end with @gmail.com.")
            elif not mobile.isdigit() or len(mobile) != 10:
                st.error("Mobile number must be exactly 10 numeric digits.")
            elif password != confirm:
                st.error("Confirm password mismatch.")
            elif not check_password_complexity(password)["valid"]:
                st.error("Password requires minimum 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 special symbol.")
            else:
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT id FROM users WHERE email = ?", (email,))
                if cursor.fetchone():
                    st.error("Email address is already in use.")
                    conn.close()
                else:
                    # Generate OTP and store signup data in session
                    otp = str(np.random.randint(1000, 9999))
                    st.session_state["temp_otp"] = otp
                    st.session_state["temp_signup_data"] = {
                        "name": name,
                        "email": email,
                        "mobile": mobile,
                        "password": password
                    }
                    st.session_state["otp_time"] = datetime.now()
                    st.session_state["auth_mode"] = "OTP"
                    conn.close()
                    st.rerun()

        if st.button("← Back to Login"):
            st.session_state["auth_mode"] = "Login"
            st.rerun()

    elif mode == "OTP":
        st.markdown("<h2 style='text-align:center; color:#2962FF;'>📨 OTP Verification</h2>", unsafe_allow_html=True)
        st.markdown(f"A simulated verification email has been dispatched to: **{st.session_state['temp_signup_data']['email']}**")
        
        # Display simulated email delivery
        st.info(f"📨 **[Simulated Email Alert]:** Your AIStockWave Registration OTP Verification Code is: `{st.session_state['temp_otp']}`")
        
        otp_input = st.text_input("Enter 4-Digit OTP Code").strip()
        
        col_o1, col_o2 = st.columns(2)
        if col_o1.button("Verify & Activate", use_container_width=True):
            if otp_input == st.session_state["temp_otp"]:
                data = st.session_state["temp_signup_data"]
                conn = get_db_connection()
                cursor = conn.cursor()
                try:
                    cursor.execute("""
                        INSERT INTO users (full_name, email, password, mobile, is_verified, balance, is_admin, subscription_tier) 
                        VALUES (?, ?, ?, ?, 1, 100000.0, 0, 'Free')
                    """, (data["name"], data["email"], data["password"], data["mobile"]))
                    conn.commit()
                    
                    # Create default watchlist for user
                    cursor.execute("SELECT last_insert_rowid()")
                    new_uid = cursor.fetchone()[0]
                    cursor.execute("INSERT INTO watchlist (user_id, symbol, watchlist_name) VALUES (?, 'AAPL', 'Default')", (new_uid,))
                    cursor.execute("INSERT INTO watchlist (user_id, symbol, watchlist_name) VALUES (?, 'TSLA', 'Default')", (new_uid,))
                    conn.commit()
                    
                    st.success("Verification successful! Account activated. Please sign in.")
                    st.session_state["auth_mode"] = "Login"
                    st.session_state["temp_otp"] = None
                    st.session_state["temp_signup_data"] = None
                    conn.close()
                    st.rerun()
                except Exception as ex:
                    st.error(f"Activation failure: {ex}")
                    conn.close()
            else:
                st.error("Incorrect verification code.")
                
        if col_o2.button("Resend Verification Code", use_container_width=True):
            otp = str(np.random.randint(1000, 9999))
            st.session_state["temp_otp"] = otp
            st.success("New OTP code generated and simulated.")
            st.rerun()

    elif mode == "Forgot":
        st.markdown("<h2 style='text-align:center; color:#2962FF;'>🔑 Password Recovery</h2>", unsafe_allow_html=True)
        rec_email = st.text_input("Enter your registered Gmail:").strip().lower()
        
        if st.session_state["forgot_pass_email"] is None:
            if st.button("Send Reset Link OTP", use_container_width=True):
                if not rec_email:
                    st.error("Please enter email.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("SELECT id FROM users WHERE email = ?", (rec_email,))
                    row = cursor.fetchone()
                    conn.close()
                    
                    if row:
                        otp = str(np.random.randint(1000, 9999))
                        st.session_state["temp_otp"] = otp
                        st.session_state["forgot_pass_email"] = rec_email
                        st.success("Password reset code simulated successfully.")
                        st.rerun()
                    else:
                        st.error("No account matching that email address found.")
        else:
            st.info(f"📨 **[Simulated Reset Alert]:** Your Password Recovery Reset Code is: `{st.session_state['temp_otp']}`")
            otp_val = st.text_input("Enter Reset Code:").strip()
            new_pwd = st.text_input("Enter New Password:", type="password")
            confirm_pwd = st.text_input("Confirm New Password:", type="password")
            
            if st.button("Apply New Password", use_container_width=True):
                if otp_val != st.session_state["temp_otp"]:
                    st.error("Incorrect verification code.")
                elif new_pwd != confirm_pwd:
                    st.error("Passwords do not match.")
                elif not check_password_complexity(new_pwd)["valid"]:
                    st.error("Complexity rules: Requires min 8 characters, 1 uppercase, 1 lowercase, 1 number, and 1 symbol.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("UPDATE users SET password = ? WHERE email = ?", (new_pwd, st.session_state["forgot_pass_email"]))
                    conn.commit()
                    conn.close()
                    st.success("Password updated successfully! Please log in.")
                    st.session_state["forgot_pass_email"] = None
                    st.session_state["temp_otp"] = None
                    st.session_state["auth_mode"] = "Login"
                    st.rerun()
                    
        if st.button("← Back"):
            st.session_state["forgot_pass_email"] = None
            st.session_state["temp_otp"] = None
            st.session_state["auth_mode"] = "Login"
            st.rerun()

# ==================== MAIN WORKSPACE ====================
if st.session_state["user"] is None:
    if st.session_state["auth_mode"] == "Landing":
        show_landing_page()
    else:
        show_auth_pages()
else:
    user = st.session_state["user"]
    
    # Reload user values from DB to guarantee sync
    conn = get_db_connection()
    df_fresh = pd.read_sql_query("SELECT * FROM users WHERE id = ?", conn, params=(user["id"],))
    conn.close()
    
    if not df_fresh.empty:
        user["balance"] = df_fresh.iloc[0]["balance"]
        user["fullName"] = df_fresh.iloc[0]["full_name"]
        user["tier"] = df_fresh.iloc[0]["subscription_tier"]
        user["mobile"] = df_fresh.iloc[0]["mobile"]
        st.session_state["user"] = user

    # Sidebar parameters
    st.sidebar.markdown("<h2 style='color:#2962FF;'>🌊 AIStockWave</h2>", unsafe_allow_html=True)
    st.sidebar.markdown(f"**Trader**: {user['fullName']}")
    st.sidebar.markdown(f"Cash Available: <span style='color:#00E676; font-weight:700;'>₹{user['balance']:,.2f}</span>", unsafe_allow_html=True)
    
    tier_color = "#00E676" if user['tier'] == 'Pro' else "#B2B5BE"
    st.sidebar.markdown(f"Account Tier: <span style='color:{tier_color}; font-weight:700;'>{user['tier'].upper()}</span>", unsafe_allow_html=True)
    
    # Theme Switcher
    st.sidebar.write("---")
    theme_choice = st.sidebar.selectbox("Select Theme:", ["Dark Mode", "Light Mode"], index=0 if st.session_state["theme"]=="Dark Mode" else 1)
    if theme_choice != st.session_state["theme"]:
        st.session_state["theme"] = theme_choice
        st.rerun()

    # Navigation choices
    nav_choices = ["📊 Dashboard", "📈 Stock Auditor", "💼 Portfolio", "⭐ Watchlists", "📰 Market News", "💳 Subscription Plan", "💬 AI Assistant", "👤 Profile Desk"]
    if user["isAdmin"]:
        nav_choices.append("🛠️ Admin Dashboard")
        
    choice = st.sidebar.radio("Navigation Workspace", nav_choices)
    
    if st.sidebar.button("🚪 Log Out", use_container_width=True):
        st.session_state["user"] = None
        st.session_state["auth_mode"] = "Landing"
        st.rerun()

    # Alert Breaches banner display
    conn = get_db_connection()
    df_unread = pd.read_sql_query("SELECT id, title, message FROM notifications WHERE user_id = ? ORDER BY id DESC LIMIT 1", conn, params=(user['id'],))
    conn.close()
    if not df_unread.empty:
        st.toast(f"📢 {df_unread.iloc[0]['title']}: {df_unread.iloc[0]['message']}")

    # --- 1. DASHBOARD ---
    if choice == "📊 Dashboard":
        st.subheader("📊 Financial Trader Dashboard")
        
        # Portfolio Value & profit calculations
        conn = get_db_connection()
        df_h = pd.read_sql_query("SELECT p.quantity, p.avg_buy_price, s.current_price FROM portfolio p JOIN stocks s ON p.symbol = s.symbol WHERE p.user_id = ?", conn, params=(user['id'],))
        conn.close()
        
        stock_value = 0
        original_cost = 0
        if not df_h.empty:
            df_h['value'] = df_h['quantity'] * df_h['current_price']
            df_h['cost'] = df_h['quantity'] * df_h['avg_buy_price']
            stock_value = df_h['value'].sum()
            original_cost = df_h['cost'].sum()
            
        net_wealth = user['balance'] + stock_value
        today_pl = stock_value - original_cost
        today_pct = (today_pl / original_cost * 100) if original_cost > 0 else 0.0
        
        # Visual Metrics
        m_col1, m_col2, m_col3 = st.columns(3)
        m_col1.metric("Total Asset Worth", f"₹{net_wealth:,.2f}")
        m_col2.metric("Today's Profit/Loss", f"₹{today_pl:+,.2f}", f"{today_pct:+.2f}%")
        m_col3.metric("Paper Wallet Balance", f"₹{user['balance']:,.2f}")
        
        # Top Gainers / Losers rows
        st.write("---")
        st.write("### Market Heat check")
        conn = get_db_connection()
        df_stk = pd.read_sql_query("SELECT symbol, company_name, current_price, open_price FROM stocks", conn)
        conn.close()
        df_stk['Change (%)'] = ((df_stk['current_price'] - df_stk['open_price']) / df_stk['open_price']) * 100
        
        col_g, col_l = st.columns(2)
        with col_g:
            st.write("##### 🔥 Top Gainers")
            df_gainers = df_stk.sort_values("Change (%)", ascending=False).head(3)
            for _, row in df_gainers.iterrows():
                st.markdown(f"**{row['symbol']}**: ₹{row['current_price']:.2f} (<span style='color:#00E676;'>{row['Change (%)']:+.2f}%</span>)", unsafe_allow_html=True)
                
        with col_l:
            st.write("##### ❄️ Top Losers")
            df_losers = df_stk.sort_values("Change (%)", ascending=True).head(3)
            for _, row in df_losers.iterrows():
                st.markdown(f"**{row['symbol']}**: ₹{row['current_price']:.2f} (<span style='color:#EF5350;'>{row['Change (%)']:+.2f}%</span>)", unsafe_allow_html=True)
                
        # Watchlist summary
        st.write("---")
        st.write("### Watchlist Snapshot")
        conn = get_db_connection()
        df_ws = pd.read_sql_query("SELECT w.watchlist_name, s.symbol, s.current_price FROM watchlist w JOIN stocks s ON w.symbol = s.symbol WHERE w.user_id = ?", conn, params=(user['id'],))
        conn.close()
        
        if df_ws.empty:
            st.write("No symbols favorited. Go to Watchlist tab to customize.")
        else:
            st.dataframe(df_ws, use_container_width=True, hide_index=True)

    # --- 2. STOCK AUDITOR ---
    elif choice == "📈 Stock Auditor":
        st.subheader("📈 Stock Technical Audit & Trading Desk")
        
        conn = get_db_connection()
        df_opts = pd.read_sql_query("SELECT symbol FROM stocks", conn)
        conn.close()
        
        selected_sym = st.selectbox("Choose stock to audit:", list(df_opts['symbol']))
        
        # Load Selected Stock
        conn = get_db_connection()
        stock = pd.read_sql_query("SELECT * FROM stocks WHERE symbol = ?", conn, params=(selected_sym,)).iloc[0]
        conn.close()
        
        # Stock Summary headers
        chg = ((stock['current_price'] - stock['open_price']) / stock['open_price']) * 100
        col_s1, col_s2 = st.columns(2)
        col_s1.markdown(f"## {stock['symbol']} - {stock['company_name']}")
        col_s2.markdown(f"<h2 style='text-align:right;'>₹{stock['current_price']:.2f} <span style='color:{'#00E676' if chg >= 0 else '#EF5350'}; font-size:20px;'>({chg:+.2f}%)</span></h2>", unsafe_allow_html=True)
        
        # Watchlist operations
        wl_name = st.text_input("Add/Save to Watchlist directory name:", value="Default").strip()
        if st.button("Save to Watchlist"):
            conn = get_db_connection()
            cursor = conn.cursor()
            try:
                cursor.execute("INSERT INTO watchlist (user_id, symbol, watchlist_name) VALUES (?, ?, ?)", (user['id'], selected_sym, wl_name))
                conn.commit()
                st.success(f"Saved {selected_sym} to watchlist directory '{wl_name}'!")
            except Exception:
                st.warning("Already saved in that watchlist folder.")
            conn.close()

        # Trigger Price alert form
        st.write("---")
        st.write("#### 🚨 Set Custom Price Alert Alarm")
        col_al1, col_al2, col_al3 = st.columns(3)
        alert_cond = col_al1.selectbox("Alert me when price is:", ["ABOVE", "BELOW"])
        alert_tgt = col_al2.number_input("Target Price (₹):", min_value=1.0, value=float(stock['current_price']))
        
        if col_al3.button("Apply Price Alarm", use_container_width=True):
            conn = get_db_connection()
            cursor = conn.cursor()
            try:
                cursor.execute("INSERT INTO price_alerts (user_id, symbol, target_price, condition) VALUES (?, ?, ?, ?)", (user['id'], selected_sym, alert_tgt, alert_cond))
                conn.commit()
                st.success("Price alert scheduled successfully!")
            except Exception:
                st.warning("Alert already exists for this price.")
            conn.close()

        # Chart Render
        st.write("---")
        interval = st.radio("Performance Interval:", ["1D", "1W", "1M", "6M", "1Y", "5Y"], index=2, horizontal=True)
        
        if user['tier'] == 'Free' and interval in ["6M", "1Y", "5Y"]:
            st.error("🔒 Long timeframe interval charts are locked on the Free tier. Upgrade to Pro plan to unlock.")
        else:
            df_hist = generate_historical_data(stock['current_price'], interval)
            fig = go.Figure(data=[go.Candlestick(
                x=df_hist['time'],
                open=df_hist['open'],
                high=df_hist['high'],
                low=df_hist['low'],
                close=df_hist['close'],
                increasing_line_color='#00E676',
                decreasing_line_color='#EF5350'
            )])
            fig.update_layout(xaxis_rangeslider_visible=False, height=350, margin=dict(l=10, r=10, t=10, b=10))
            st.plotly_chart(fig, use_container_width=True)

        # Trade Widget
        st.write("---")
        col_t1, col_t2 = st.columns(2)
        with col_t1:
            st.write("#### Financial Coordinates")
            st_col1, st_col2 = st.columns(2)
            st_col1.metric("Prev Close", f"₹{stock['close_price']:.2f}")
            st_col1.metric("EPS", f"₹{stock['eps']:.2f}")
            st_col2.metric("P/E Ratio", f"{stock['pe_ratio']:.2f}")
            st_col2.metric("Dividend Yield", f"{stock['dividend_yield']:.2f}%")
            
        with col_t2:
            st.write("#### Order Desk")
            trade_type = st.radio("Action:", ["BUY", "SELL"], horizontal=True)
            trade_qty = st.number_input("Order Quantity:", min_value=1, value=10)
            est_cost = trade_qty * stock['current_price']
            st.write(f"Estimated Cost: **₹{est_cost:,.2f}**")
            
            if st.button("Execute Order"):
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("SELECT balance FROM users WHERE id = ?", (user['id'],))
                cash = cursor.fetchone()[0]
                
                if trade_type == "BUY":
                    if cash < est_cost:
                        st.error("Insufficient paper cash balance.")
                    else:
                        cursor.execute("UPDATE users SET balance = balance - ? WHERE id = ?", (est_cost, user['id']))
                        cursor.execute("SELECT quantity, avg_buy_price FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                        row = cursor.fetchone()
                        if row:
                            q, a = row
                            new_q = q + trade_qty
                            new_a = ((q * a) + est_cost) / new_q
                            cursor.execute("UPDATE portfolio SET quantity = ?, avg_buy_price = ? WHERE user_id = ? AND symbol = ?", (new_q, new_a, user['id'], selected_sym))
                        else:
                            cursor.execute("INSERT INTO portfolio (user_id, symbol, quantity, avg_buy_price) VALUES (?, ?, ?, ?)", (user['id'], selected_sym, trade_qty, stock['current_price']))
                        cursor.execute("INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'BUY', ?, ?, ?)", (user['id'], selected_sym, trade_qty, stock['current_price']))
                        conn.commit()
                        st.success("Shares bought successfully!")
                        st.rerun()
                else: # SELL
                    cursor.execute("SELECT quantity FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                    row = cursor.fetchone()
                    q = row[0] if row else 0
                    if q < trade_qty:
                        st.error("Insufficient holdings quantity to sell.")
                    else:
                        cursor.execute("UPDATE users SET balance = balance + ? WHERE id = ?", (est_cost, user['id']))
                        if q == trade_qty:
                            cursor.execute("DELETE FROM portfolio WHERE user_id = ? AND symbol = ?", (user['id'], selected_sym))
                        else:
                            cursor.execute("UPDATE portfolio SET quantity = quantity - ? WHERE user_id = ? AND symbol = ?", (trade_qty, user['id'], selected_sym))
                        cursor.execute("INSERT INTO transactions (user_id, type, symbol, quantity, price) VALUES (?, 'SELL', ?, ?, ?)", (user['id'], selected_sym, trade_qty, stock['current_price']))
                        conn.commit()
                        st.success("Shares sold successfully!")
                        st.rerun()
                conn.close()

        # AI Stock Auditor Card
        st.write("---")
        st.write("#### 🤖 AI stock Auditor Analysis")
        ai = get_ai_analysis(stock)
        st.info(f"**AUDIT SENTIMENT**: `{ai['rating']}`\n\n{ai['summary']}")
        
        ai_col1, ai_col2 = st.columns(2)
        ai_col1.markdown("##### 🟢 Growth Strengths")
        ai_col1.write(ai['strengths'])
        ai_col2.markdown("##### 🔴 Vulnerability Risks")
        ai_col2.write(ai['risks'])
        
        st.write("---")
        st.markdown("**📚 Investor Educational Tutorial**")
        st.write(ai['education'])
        st.markdown("<span style='color:#EF5350; font-size:11px;'>⚠️ Disclaimer: AI insights are for educational goals only and do not establish financial advice.</span>", unsafe_allow_html=True)

    # --- 3. PORTFOLIO ---
    elif choice == "💼 Portfolio":
        st.subheader("💼 Portfolio & Analytics")
        
        conn = get_db_connection()
        df_p = pd.read_sql_query("""
            SELECT p.symbol, s.company_name, p.quantity, p.avg_buy_price, s.current_price 
            FROM portfolio p 
            JOIN stocks s ON p.symbol = s.symbol 
            WHERE p.user_id = ? AND p.quantity > 0
        """, conn, params=(user['id'],))
        conn.close()
        
        if df_p.empty:
            st.write("Your portfolio has no active stock holdings.")
        else:
            df_p['Current Value'] = df_p['quantity'] * df_p['current_price']
            df_p['Cost Basis'] = df_p['quantity'] * df_p['avg_buy_price']
            df_p['P/L (₹)'] = df_p['Current Value'] - df_p['Cost Basis']
            
            st.dataframe(df_p, use_container_width=True, hide_index=True)
            
            # Premium analytics block
            if user['tier'] == 'Free':
                st.error("🔒 Detailed Portfolio Risk Analytics (pie charts & allocations) are locked on the Free plan. Upgrade to Pro plan.")
            else:
                st.write("#### Portfolio Allocation breakdown")
                fig_pie = go.Figure(data=[go.Pie(labels=df_p['symbol'], values=df_p['Current Value'], hole=.3)])
                st.plotly_chart(fig_pie)

    # --- 4. WATCHLISTS ---
    elif choice == "⭐ Watchlists":
        st.subheader("⭐ Custom Watchlist Directories")
        
        conn = get_db_connection()
        df_w = pd.read_sql_query("SELECT DISTINCT watchlist_name FROM watchlist WHERE user_id = ?", conn, params=(user['id'],))
        conn.close()
        
        if df_w.empty:
            st.write("Watchlist empty. Save ticker symbols from Stock Auditor tab.")
        else:
            selected_wl = st.selectbox("Select Watchlist directory folder:", list(df_w['watchlist_name']))
            
            # Free tier check: Limit news and multiple watchlists actions
            if user['tier'] == 'Free' and selected_wl != "Default":
                st.error("🔒 Creating or viewing multiple custom watchlist directories is a Pro feature. Default folder is active.")
            else:
                conn = get_db_connection()
                df_items = pd.read_sql_query("""
                    SELECT s.symbol, s.company_name, s.current_price, s.open_price 
                    FROM watchlist w 
                    JOIN stocks s ON w.symbol = s.symbol 
                    WHERE w.user_id = ? AND w.watchlist_name = ?
                """, conn, params=(user['id'], selected_wl))
                conn.close()
                
                st.dataframe(df_items, use_container_width=True, hide_index=True)
                
                # Delete option
                del_ticker = st.selectbox("Delete symbol from watchlist:", [""] + list(df_items['symbol']))
                if del_ticker:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("DELETE FROM watchlist WHERE user_id = ? AND symbol = ? AND watchlist_name = ?", (user['id'], del_ticker, selected_wl))
                    conn.commit()
                    st.success("Deleted!")
                    conn.close()
                    st.rerun()

    # --- 5. MARKET NEWS ---
    elif choice == "📰 Market News":
        st.subheader("📰 Market Financial Headlines")
        
        # Display tabbed divisions
        tab_gen, tab_ipo, tab_calendar = st.tabs(["🔥 Financial Headlines", "🚀 IPO & Corporate updates", "📅 Economic Calendar"])
        
        with tab_gen:
            conn = get_db_connection()
            df_n = pd.read_sql_query("SELECT * FROM news ORDER BY rowid DESC", conn)
            conn.close()
            for _, row in df_n.iterrows():
                st.markdown(f"##### {row['title']}")
                st.markdown(f"<span style='color:#2962FF;'>{row['source'].upper()}</span> • {row['date']}", unsafe_allow_html=True)
                st.write(row['description'])
                st.write("---")
                
        with tab_ipo:
            st.write("##### Simulated Upcoming IPO Calendar (2026)")
            ipo_data = [
                {"Company": "QuantumAI Logistics", "Expected Price": "₹340-360", "Listing Date": "Aug 12, 2026"},
                {"Company": "GreenEnergy Tech Ltd", "Expected Price": "₹120-130", "Listing Date": "Sep 05, 2026"}
            ]
            st.table(ipo_data)
            
        with tab_calendar:
            st.write("##### Macroeconomic Events Calendar")
            cal_data = [
                {"Date": "July 15, 2026", "Event": "CPI Inflation Report Release", "Forecast": "3.1%", "Impact": "HIGH"},
                {"Date": "July 30, 2026", "Event": "Federal Reserve Rate Choice Decision", "Forecast": "Pause", "Impact": "CRITICAL"}
            ]
            st.table(cal_data)

    # --- 6. SUBSCRIPTION PLAN ---
    elif choice == "💳 Subscription Plan":
        st.subheader("💳 Manage Account Plan & Upgrades")
        
        if user['tier'] == 'Pro':
            st.success("✨ You hold a Pro Analyst membership. Access to advanced chatbot reviews, multiple watchlists, and premium charts is active.")
            if st.button("Cancel Subscription Plan"):
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("UPDATE users SET subscription_tier = 'Free' WHERE id = ?", (user['id'],))
                conn.commit()
                st.success("Membership downgraded back to Free tier.")
                conn.close()
                st.rerun()
        else:
            st.warning("⚠️ You are currently on the Free Tier plan.")
            st.write("### Unlock Pro Analyst Tools (₹499/mo)")
            
            # Upgrade Billing Gateway
            st.write("#### 🛡️ Secure Payment Gateway Simulator")
            pay_method = st.selectbox("Select Payment Method:", ["UPI ID", "Credit / Debit Card", "Net Banking", "Digital Wallet"])
            
            if pay_method == "UPI ID":
                upi_val = st.text_input("Enter UPI ID (e.g. name@okhdfc)").strip()
            elif pay_method == "Credit / Debit Card":
                card_num = st.text_input("Card Number (16 Digits)").strip()
                card_cvv = st.text_input("CVV Number (3 Digits)").strip()
            else:
                st.write("Simulated portal logs will handshake directly with local Net Banking portals.")
                
            if st.button("PAY ₹499 SECURELY & UPGRADE"):
                # Simulation loading bar
                my_bar = st.progress(0)
                for percent_complete in range(100):
                    my_bar.progress(percent_complete + 1)
                st.success("Handshake successful! Payment authorized.")
                
                # Update DB
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("UPDATE users SET subscription_tier = 'Pro' WHERE id = ?", (user['id'],))
                cursor.execute("INSERT INTO notifications (user_id, title, message) VALUES (?, '🎉 Subscription Upgraded!', 'Congratulations! Your Pro Analyst membership is active.')", (user['id'],))
                conn.commit()
                conn.close()
                st.rerun()

    # --- 7. AI ASSISTANT ---
    elif choice == "💬 AI Assistant":
        st.subheader("💬 AI StockWave Chatbot Assistant")
        st.write("Audit your portfolio balance, compare stock valuations, or ask financial vocabulary tutorials.")
        
        # Free vs Pro chatbot restriction
        if user['tier'] == 'Free':
            st.error("🔒 The Interactive AI Chatbot is locked on the Free tier. Upgrade to the Pro membership plan to communicate.")
        else:
            # Displays Chat History logs
            for msg in st.session_state["chat_messages"]:
                with st.chat_message(msg["role"]):
                    st.write(msg["content"])
                    
            user_msg = st.chat_input("Enter investment queries (e.g. 'explain AAPL', 'review my portfolio'):")
            if user_msg:
                st.session_state["chat_messages"].append({"role": "user", "content": user_msg})
                with st.chat_message("user"):
                    st.write(user_msg)
                    
                response = ""
                # Simple parsing chatbot rule replies
                cleaned_msg = user_msg.lower().strip()
                
                if "portfolio" in cleaned_msg:
                    conn = get_db_connection()
                    df_pa = pd.read_sql_query("SELECT symbol, quantity, avg_buy_price FROM portfolio WHERE user_id = ?", conn, params=(user['id'],))
                    conn.close()
                    if df_pa.empty:
                        response = "AI AUDIT: Your virtual portfolio has no active stock holdings at the moment. Try buying Apple (AAPL) or Tesla (TSLA) on the Stock Auditor desk first."
                    else:
                        response = "AI PORTFOLIO ANALYSIS SUMMARY:\n"
                        for _, row in df_pa.iterrows():
                            val = row['quantity'] * row['avg_buy_price']
                            response += f"- Stock: {row['symbol']}, Quantity: {row['quantity']} shares, Value Basis: ₹{val:.2f}\n"
                        response += "\nAudit Conclusion: Your asset sheet is diversified. Remember to watch your valuation margins."
                elif "explain" in cleaned_msg:
                    ticker = user_msg.split()[-1].upper()
                    conn = get_db_connection()
                    df_st = pd.read_sql_query("SELECT * FROM stocks WHERE symbol = ?", conn, params=(ticker,))
                    conn.close()
                    if df_st.empty:
                        response = f"AI ANALYSIS: Symbol {ticker} not found in database registry. Try AAPL, TSLA, or NVDA."
                    else:
                        ai_rep = get_ai_analysis(df_st.iloc[0])
                        response = f"AUDIT ANALYSIS FOR {ticker}:\n\nRating: {ai_rep['rating']}\n\nSummary: {ai_rep['summary']}\n\nStrengths:\n{ai_rep['strengths']}"
                elif "compare" in cleaned_msg:
                    response = "AI COMPARISON: Comparing TSLA (P/E: 48.2) vs NVDA (P/E: 72.8). NVDA displays superior EPS profit margins but trades at a high valuation multiple, representing momentum. TSLA shows value support bands."
                else:
                    response = "AI ASSISTANT: Hello! I can help explain stock details ('explain AAPL'), summarize portfolio parameters ('review my portfolio'), or compare tickers. What financial concept would you like to review?"

                st.session_state["chat_messages"].append({"role": "assistant", "content": response})
                with st.chat_message("assistant"):
                    st.write(response)
                    
            st.write("---")
            st.markdown("<span style='color:#EF5350; font-size:11px;'>⚠️ Disclaimer: AI insights are for educational goals only and do not establish financial advice.</span>", unsafe_allow_html=True)

    # --- 8. PROFILE DESK ---
    elif choice == "👤 Profile Desk":
        st.subheader("👤 Personal Profile Settings")
        
        # Profile details edit form
        col_ed1, col_ed2 = st.columns(2)
        with col_ed1:
            st.write("#### Edit User Profile")
            new_name = st.text_input("Name", value=user['fullName'])
            new_mob = st.text_input("Mobile Number", value=user['mobile'])
            
            # Profile Photo File Uploader
            ph_file = st.file_uploader("Upload Profile Photo (.png, .jpg)", type=["png", "jpg"])
            if ph_file:
                st.image(ph_file, width=150, caption="Profile Image Preview")
                
            if st.button("Save Profile Settings"):
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("UPDATE users SET full_name = ?, mobile = ? WHERE id = ?", (new_name, new_mob, user['id']))
                conn.commit()
                conn.close()
                st.success("Profile saved successfully!")
                st.rerun()
                
        with col_ed2:
            st.write("#### User Account Activity & Logins")
            conn = get_db_connection()
            df_hist_q = pd.read_sql_query("SELECT login_history FROM users WHERE id = ?", conn, params=(user['id'],))
            conn.close()
            if not df_hist_q.empty:
                h_list = json.loads(df_hist_q.iloc[0]['login_history'] or "[]")
                st.write("Recent Login Timestamps:")
                for log_time in reversed(h_list[-6:]):
                    st.write(f"- {log_time}")
                    
        # Account Deletion Action
        st.write("---")
        st.write("### Danger Zone")
        if st.button("⚠️ DELETE USER ACCOUNT FOREVER"):
            conn = get_db_connection()
            cursor = conn.cursor()
            cursor.execute("DELETE FROM users WHERE id = ?", (user['id'],))
            cursor.execute("DELETE FROM portfolio WHERE user_id = ?", (user['id'],))
            cursor.execute("DELETE FROM watchlist WHERE user_id = ?", (user['id']))
            cursor.execute("DELETE FROM price_alerts WHERE user_id = ?", (user['id'],))
            conn.commit()
            conn.close()
            st.session_state["user"] = None
            st.session_state["auth_mode"] = "Landing"
            st.success("User account deleted successfully.")
            st.rerun()

    # --- 9. ADMIN DASHBOARD ---
    elif choice == "🛠️ Admin Dashboard" and user["isAdmin"]:
        st.subheader("🛠️ Platform Administrative Console")
        
        tab_adm_users, tab_adm_data = st.tabs(["👥 Trader Account Control", "📁 Stock & News Database"])
        
        with tab_adm_users:
            st.write("#### Registered Users Database")
            conn = get_db_connection()
            df_all_u = pd.read_sql_query("SELECT id, full_name, email, mobile, subscription_tier, is_blocked FROM users", conn)
            conn.close()
            
            st.dataframe(df_all_u, use_container_width=True, hide_index=True)
            
            # User Block/Unblock toggle
            st.write("---")
            target_usr = st.number_input("User ID to target:", min_value=1, step=1)
            col_b1, col_b2 = st.columns(2)
            
            if col_b1.button("🚫 Block User Account"):
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("UPDATE users SET is_blocked = 1 WHERE id = ?", (target_usr,))
                conn.commit()
                st.success(f"Successfully blocked User ID {target_usr}!")
                conn.close()
                st.rerun()
                
            if col_b2.button("🟢 Unblock User Account"):
                conn = get_db_connection()
                cursor = conn.cursor()
                cursor.execute("UPDATE users SET is_blocked = 0 WHERE id = ?", (target_usr,))
                conn.commit()
                st.success(f"Successfully unblocked User ID {target_usr}!")
                conn.close()
                st.rerun()
                
        with tab_adm_data:
            # Stock management forms
            st.write("#### Register New Ticker Symbol")
            sym = st.text_input("Ticker Symbol (e.g. INFY)").strip().upper()
            comp = st.text_input("Company Name")
            init_price = st.number_input("Initial Price (₹)", min_value=1.0, value=150.0)
            init_cap = st.number_input("Market Capitalization ($B)", min_value=0.1, value=10.0)
            
            if st.button("Register Symbol"):
                if not sym or not comp:
                    st.error("Fields cannot be empty.")
                else:
                    conn = get_db_connection()
                    cursor = conn.cursor()
                    cursor.execute("SELECT symbol FROM stocks WHERE symbol = ?", (sym,))
                    if cursor.fetchone():
                        st.error("Ticker symbol already registered.")
                    else:
                        eps = init_price * 0.05
                        pe = init_price / eps
                        cursor.execute("""
                            INSERT INTO stocks (symbol, company_name, current_price, open_price, close_price, high_price, low_price, volume, market_cap, pe_ratio, eps, dividend_yield, high_52week, low_52week) 
                            VALUES (?, ?, ?, ?, ?, ?, ?, 100000, ?, ?, ?, 1.5, ?, ?)
                        """, (sym, comp, init_price, init_price, init_price, init_price, init_price, init_cap, pe, eps, init_price, init_price))
                        conn.commit()
                        st.success(f"Ticker {sym} registered successfully!")
                        st.rerun()
                    conn.close()
