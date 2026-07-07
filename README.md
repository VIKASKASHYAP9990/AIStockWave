# AIStockWave – AI-Powered Stock Market Analysis & Paper Trading Web App

AIStockWave is a full-featured, interactive Single Page Web Application (SPA) designed to simulate a modern fintech stock trading and audit dashboard.

---

## 🌟 Core Features

- **Obsidian Dark Theme CSS Layout**: Sleek obsidian styling styled with custom responsive grid alignments, hover animations, glassmorphic fields, and growth-green/loss-red text cues.
- **Persistent Local Database**: Uses browser `localStorage` to keep registrations, watchlists, paper portfolio sheets, and transaction logs fully persistent.
- **Dynamic Price Simulation**: An active background interval loop applying Geometric Brownian Motion ticks to database stock quotes every 3 seconds.
- **Interactive Chart.js Charts**: Standardized responsive line charts styled with custom color gradients, tooltips, and time interval options: 1D, 1W, 1M, 6M, 1Y, 5Y.
- **AI Stock Audit Summary**: Formulates live sentiment audits (STRONG BUY, BUY, HOLD, UNDERPERFORM) detailing growth strengths, downside vulnerabilities, and conceptual tutorial cards.
- **Administrative Control Deck**: Platform metrics counters (users, transactions count, gross volumes), user wallet resetting, custom stock ticker registrations, and news headline publishers.

---

## 🚀 How to Run the Web Application

1. Open a PowerShell terminal.
2. Navigate to the web project directory:
   ```powershell
   cd C:\Users\vikas\.gemini\antigravity-ide\scratch\aistockwave-web
   ```
3. Run the launcher script:
   ```powershell
   .\start_server.ps1
   ```
   *This starts Python's built-in web server (`python -m http.server`) on port 8000.*
4. Open your web browser and go to:
   👉 **[http://localhost:8000](http://localhost:8000)**

---

## 🔑 Pre-Seeded Account Credentials

On first boot, the system seeds the local browser storage database with these accounts:

### 1. General Paper Trader
- **Email**: `user@aistockwave.com`
- **Password**: `user123`
- **Starting Cash**: ₹100,000.00
- **Access**: Standard trading dashboard, watchlists, portfolio tracking.

### 2. System Administrator
- **Email**: `admin@aistockwave.com`
- **Password**: `admin123`
- **Access**: Standard dashboard + **Admin Panel** tab (user balance resets, stock registrar, news publisher).
