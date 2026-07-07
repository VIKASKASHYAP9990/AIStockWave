# AIStockWave – Complete Stock Market Analysis & Paper Trading Monorepo

Welcome to the official repository of **AIStockWave**, a premium educational stock market analysis and paper trading platform. This repository is a unified monorepo containing three distinct implementations of the platform to suit different execution and testing environments.

🔗 **GitHub Repository Link**: [https://github.com/VIKASKASHYAP9990/AIStockWave](https://github.com/VIKASKASHYAP9990/AIStockWave)

---

## 📂 Repository Structure

The project is structured into three self-contained folders:

### 1. [📈 Data Analyst Capstone (Python & Streamlit)](./aistockwave-capstone)
* **Goal**: Formulates an end-to-end data analyst Capstone project matching the Stack Overflow Job Survey Capstone architecture.
* **Tech Stack**: Jupyter Notebooks (`.ipynb`), SQLite database (`aistockwave.db`), Pandas cleaning, Plotly graphs, and a Streamlit interactive web server.
* **Key Features**: 5 pipeline notebooks, interactive Plotly Candlestick charts, simulated OTP verification registers, UPI payment simulators, and an interactive **AI Chatbot Portfolio Auditor**.
* **Launch Instructions**:
  1. Navigate to the folder in PowerShell:
     ```powershell
     cd aistockwave-capstone
     ```
  2. Run the data cleaning and database seed pipeline:
     ```powershell
     python run_pipeline.py
     ```
  3. Start the Streamlit server:
     ```powershell
     .\start_app.ps1
     ```
  4. Access in your browser: `http://localhost:8501`

### 2. [💻 HTML5 & CSS3 Web Client (Vibrant ES6 Single Page App)](./aistockwave-web)
* **Goal**: A lightweight front-end website client utilizing browser localStorage for database simulation (bypassing Node/MongoDB installations).
* **Tech Stack**: HTML5 semantic shell, Custom CSS stylesheet, Vanilla JS router, Chart.js CDN, and Python HTTP server.
* **Launch Instructions**:
  1. Navigate to the folder in PowerShell:
     ```powershell
     cd aistockwave-web
     ```
  2. Start local web server:
     ```powershell
     .\start_server.ps1
     ```
  3. Access in your browser: `http://localhost:8000`

### 3. [🖥️ JavaFX Desktop Terminal (Java & MySQL)](./aistockwave-desktop)
* **Goal**: A high-performance Java GUI terminal mimicking institutional trading dashboards.
* **Tech Stack**: Java 8 (JDK 1.8), JavaFX GUI, MySQL local server, JDBC drivers, Maven.
* **Launch Instructions**:
  1. Update credentials in `config.properties`.
  2. Compile and run:
     ```powershell
     cd aistockwave-desktop
     .\build_and_run.ps1
     ```

---

## 🔑 Pre-Seeded Accounts (Shared Credentials)

The databases across all three variations are seeded with the following accounts for immediate inspection:

### 1. General Paper Trader
* **Email**: `user@gmail.com` (or `user@aistockwave.com` for local-web)
* **Password**: `User@123` (or `user123` for local-web)
* **Paper Balance**: ₹100,000.00
* **Privileges**: Normal dashboard access, buy/sell paper trades, watchlist edits.

### 2. System Administrator
* **Email**: `admin@gmail.com` (or `admin@aistockwave.com` for local-web)
* **Password**: `Admin@123` (or `admin123` for local-web)
* **Privileges**: Access to the **Admin Dashboard** (manages traders, blocks accounts, registers stock tickers, publishes market news events).
