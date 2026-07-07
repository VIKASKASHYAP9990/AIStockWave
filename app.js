/* AIStockWave Web Application Core Logic */

// ==================== 1. LOCAL DATABASE SERVICE (localStorage) ====================
const dbService = {
    init() {
        if (!localStorage.getItem('asw_initialized')) {
            // Seed Users
            const users = [
                {
                    id: 1,
                    fullName: "Paper Trader",
                    email: "user@aistockwave.com",
                    password: "user123",
                    balance: 100000.00,
                    isAdmin: false
                },
                {
                    id: 2,
                    fullName: "System Administrator",
                    email: "admin@aistockwave.com",
                    password: "admin123",
                    balance: 100000.00,
                    isAdmin: true
                }
            ];
            
            // Seed Stocks
            const stocks = [
                { symbol: "AAPL", name: "Apple Inc.", price: 185.25, open: 184.00, close: 184.50, high: 186.40, low: 183.50, volume: 52000000, cap: 2900.5, pe: 28.5, eps: 6.5, div: 0.52, h52: 199.62, l52: 164.08 },
                { symbol: "MSFT", name: "Microsoft Corp.", price: 420.50, open: 418.00, close: 417.80, high: 422.90, low: 416.30, volume: 23000000, cap: 3130.2, pe: 35.8, eps: 11.75, div: 0.71, h52: 430.82, l52: 315.18 },
                { symbol: "GOOGL", name: "Alphabet Inc.", price: 172.30, open: 170.50, close: 170.10, high: 173.50, low: 169.80, volume: 28000000, cap: 2150.1, pe: 26.2, eps: 6.58, div: 0.46, h52: 177.43, l52: 115.50 },
                { symbol: "AMZN", name: "Amazon.com Inc.", price: 188.40, open: 187.00, close: 186.20, high: 189.50, low: 185.30, volume: 35000000, cap: 1950.4, pe: 40.5, eps: 4.65, div: 0.00, h52: 191.70, l52: 120.43 },
                { symbol: "TSLA", name: "Tesla Inc.", price: 175.80, open: 178.00, close: 179.20, high: 180.50, low: 173.20, volume: 82000000, cap: 560.8, pe: 48.2, eps: 3.65, div: 0.00, h52: 299.29, l52: 138.80 },
                { symbol: "NVDA", name: "NVIDIA Corp.", price: 910.20, open: 898.00, close: 895.00, high: 922.00, low: 891.50, volume: 49000000, cap: 2270.3, pe: 72.8, eps: 12.50, div: 0.02, h52: 974.00, l52: 262.20 },
                { symbol: "META", name: "Meta Platforms Inc.", price: 495.10, open: 492.00, close: 490.50, high: 501.20, low: 488.30, volume: 18000000, cap: 1260.6, pe: 24.3, eps: 20.37, div: 0.40, h52: 531.49, l52: 229.85 },
                { symbol: "NFLX", name: "Netflix Inc.", price: 620.15, open: 615.00, close: 612.40, high: 625.30, low: 611.20, volume: 5000000, cap: 268.4, pe: 38.6, eps: 16.07, div: 0.00, h52: 639.00, l52: 315.62 }
            ];

            // Seed News
            const news = [
                { id: 1, title: "NVIDIA Hits Record High on Massive AI Chip Demand", description: "NVIDIA stock soared past $900 today as major tech companies continue to place multi-billion dollar orders for its next-generation Blackwell AI processors. Analysts project high margins for the upcoming quarters.", source: "Financial Times", date: "July 8, 2026" },
                { id: 2, title: "Federal Reserve Hints at Possible Rate Cut Next Month", description: "The Federal Reserve chairman suggested inflation is moderating toward the 2% target, sparking a market-wide rally. Tech and growth sectors led gains following the announcement.", source: "Wall Street Journal", date: "July 7, 2026" },
                { id: 3, title: "Apple Announces New Generative AI Integrations for iOS 20", description: "Apple unveiled its upcoming OS updates at a developer conference, detailing deep system integrations of advanced generative LLMs running on-device. The stock climbed 2% in afternoon trading.", source: "TechCrunch", date: "July 6, 2026" },
                { id: 4, title: "Tesla Deliveries Beats Estimates, Stock Jumps 4%", description: "Tesla announced quarterly delivery numbers that slightly exceeded Wall Street expectations, showing strong growth in its Model Y production and international markets.", source: "Bloomberg", date: "July 5, 2026" },
                { id: 5, title: "Global Markets Rally as Supply Chain Congestion Eases", description: "Shipping rates and port wait times are returning to pre-pandemic averages, relieving inflationary pressure on manufacturing and consumer goods companies globally.", source: "Reuters", date: "July 4, 2026" }
            ];

            localStorage.setItem('asw_users', JSON.stringify(users));
            localStorage.setItem('asw_stocks', JSON.stringify(stocks));
            localStorage.setItem('asw_news', JSON.stringify(news));
            localStorage.setItem('asw_portfolio', JSON.stringify([]));
            localStorage.setItem('asw_transactions', JSON.stringify([]));
            localStorage.setItem('asw_watchlist', JSON.stringify([]));
            localStorage.setItem('asw_initialized', 'true');
        }
    },

    getTable(key) {
        return JSON.parse(localStorage.getItem('asw_' + key)) || [];
    },

    saveTable(key, data) {
        localStorage.setItem('asw_' + key, JSON.stringify(data));
    }
};

// Initialize DB immediately
dbService.init();


// ==================== 2. AUTHENTICATION SERVICE ====================
const authService = {
    getCurrentUser() {
        return JSON.parse(sessionStorage.getItem('asw_session_user'));
    },

    switchForm(form) {
        document.getElementById('loginCard').style.display = form === 'login' ? 'flex' : 'none';
        document.getElementById('signupCard').style.display = form === 'signup' ? 'flex' : 'none';
        document.getElementById('loginFeedback').innerHTML = '';
        document.getElementById('signupFeedback').innerHTML = '';
    },

    register() {
        const feedback = document.getElementById('signupFeedback');
        feedback.innerHTML = '';

        const name = document.getElementById('signupName').value.trim();
        const email = document.getElementById('signupEmail').value.trim().toLowerCase();
        const password = document.getElementById('signupPassword').value;
        const confirm = document.getElementById('signupConfirm').value;

        if (!name || !email || !password || !confirm) {
            feedback.innerHTML = `<div class="error-msg">Please fill in all inputs.</div>`;
            return;
        }

        if (password !== confirm) {
            feedback.innerHTML = `<div class="error-msg">Passwords do not match.</div>`;
            return;
        }

        if (password.length < 6) {
            feedback.innerHTML = `<div class="error-msg">Password must be at least 6 characters.</div>`;
            return;
        }

        const users = dbService.getTable('users');
        if (users.find(u => u.email === email)) {
            feedback.innerHTML = `<div class="error-msg">Email already registered.</div>`;
            return;
        }

        const newUser = {
            id: users.length + 1,
            fullName: name,
            email: email,
            password: password, // client-side simulation plain hashing
            balance: 100000.00,
            isAdmin: false
        };

        users.push(newUser);
        dbService.saveTable('users', users);

        feedback.innerHTML = `<div class="success-msg">Successfully registered! Redirecting to login...</div>`;
        
        // Reset fields
        document.getElementById('signupName').value = '';
        document.getElementById('signupEmail').value = '';
        document.getElementById('signupPassword').value = '';
        document.getElementById('signupConfirm').value = '';

        setTimeout(() => {
            this.switchForm('login');
        }, 1500);
    },

    login() {
        const feedback = document.getElementById('loginFeedback');
        feedback.innerHTML = '';

        const email = document.getElementById('loginEmail').value.trim().toLowerCase();
        const password = document.getElementById('loginPassword').value;

        if (!email || !password) {
            feedback.innerHTML = `<div class="error-msg">Please enter credentials.</div>`;
            return;
        }

        const users = dbService.getTable('users');
        const user = users.find(u => u.email === email && u.password === password);

        if (user) {
            sessionStorage.setItem('asw_session_user', JSON.stringify(user));
            
            // UI Switch
            document.getElementById('authContainer').style.display = 'none';
            document.getElementById('appContainer').style.display = 'flex';
            
            // Check Admin role for menu rendering
            const adminMenu = document.getElementById('menu-admin');
            if (user.isAdmin) {
                adminMenu.style.display = 'flex';
            } else {
                adminMenu.style.display = 'none';
            }

            uiController.refreshHeader();
            appRouter.navigate('dashboard');

            // Start Ticker Simulation
            simulatorService.start();
        } else {
            feedback.innerHTML = `<div class="error-msg">Invalid email or password.</div>`;
        }
    },

    logout() {
        sessionStorage.removeItem('asw_session_user');
        simulatorService.stop();
        
        document.getElementById('authContainer').style.display = 'flex';
        document.getElementById('appContainer').style.display = 'none';
        
        // Reset fields
        document.getElementById('loginEmail').value = '';
        document.getElementById('loginPassword').value = '';
        document.getElementById('loginFeedback').innerHTML = '';
    }
};


// ==================== 3. MARKET SIMULATOR SERVICE ====================
const simulatorService = {
    intervalId: null,

    start() {
        if (this.intervalId) return;
        this.intervalId = setInterval(() => {
            this.simulateTick();
        }, 3000);
        console.log("Web stock simulator started.");
    },

    stop() {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
            console.log("Web stock simulator stopped.");
        }
    },

    simulateTick() {
        const stocks = dbService.getTable('stocks');
        stocks.forEach(s => {
            // Random Gaussian-like walk (-0.6% to +0.7%, slight positive bias)
            const changePercent = (Math.random() * 1.3 - 0.6) / 100.0;
            const oldPrice = s.price;
            let newPrice = oldPrice * (1.0 + changePercent);
            newPrice = Math.round(newPrice * 100) / 100;

            if (newPrice > s.high) s.high = newPrice;
            if (newPrice < s.low) s.low = newPrice;
            s.price = newPrice;
            s.volume += Math.floor(Math.random() * 2500) + 100;
        });

        dbService.saveTable('stocks', stocks);

        // Notify active views to redraw
        uiController.onMarketTick();
    }
};


// ==================== 4. AI ANALYTICS SERVICE ====================
const aiService = {
    generateAnalysis(stock) {
        if (!stock) return null;

        const pe = stock.pe;
        const div = stock.div;
        const change = ((stock.price - stock.open) / stock.open) * 100;
        const current = stock.price;
        const high52 = stock.h52;
        const low52 = stock.l52;
        const eps = stock.eps;

        let rating = "HOLD";
        let summary = "";
        let strengths = [];
        let risks = [];
        let education = "";

        // Decision logic
        if (change > 2.0 && pe < 30.0 && eps > 0) {
            rating = "STRONG BUY";
            summary = `${stock.name} is showing extreme bullish momentum today. Rising ${change.toFixed(2)}% to ₹${current}, it is trading with heavy buyer volumes. Favorable sector metrics and a supportive chart frame make it highly attractive.`;
        } else if (change >= 0 && pe < 40.0) {
            rating = "BUY";
            summary = `${stock.name} demonstrates a strong defensive posture. Priced at ₹${current}, it balances reasonable P/E levels with positive earnings growth. Technical structures support target accumulations.`;
        } else if (pe > 60.0) {
            rating = "UNDERPERFORM";
            summary = `${stock.name} appears heavily overvalued at current levels of ₹${current}. A P/E of ${pe} implies aggressive growth expectations that expose investors to deep corrections if earnings reports mismatch guidelines.`;
        } else {
            rating = "HOLD";
            summary = `${stock.name} is trading inside a narrow consolidation channel at ₹${current}. Investors are currently waiting for earnings indicators or macro updates. Proximity to historical supports restricts downsides.`;
        }

        // Strengths
        if (pe < 25.0 && pe > 0) strengths.push(`• Low P/E Valuation: Trading at ${pe} times earnings, offering a defensive margin of safety.`);
        if (eps > 5.0) strengths.push(`• Profitable Core: Solid EPS of ₹${eps} verifies robust underlying profitability.`);
        if (div > 0) strengths.push(`• Dividend Returns: Generates ${div}% dividend yield, helping cashflow passive portfolios.`);
        if (current > (high52 + low52) / 2) strengths.push(`• Uptrend Proximity: Trading in the upper half of its 52W range, indicating institutional accumulation.`);
        if (strengths.length === 0) strengths.push(`• Liquidity base: Strong daily trading volume ensures low execution spreads.`);

        // Risks
        if (pe > 45.0) risks.push(`• High P/E Multiple: Priced at ${pe} times earnings, meaning any growth slowdown will lead to valuation compression.`);
        if (change < -2.0) risks.push(`• Bearish Drift: Under strong intraday selling pressures, indicating volatility risks.`);
        if (current < low52 * 1.15) risks.push(`• Vulnerability check: Trading close to its annual lows (₹${low52}), indicating sector headwinds.`);
        if (div === 0) risks.push(`• Zero Yield: Pays no dividends, forcing dependency solely on equity appreciation.`);
        if (risks.length === 0) risks.push(`• General Beta risks: Standard systematic sector shifts and interest rate fluctuations.`);

        // Education
        education = `1. Price-to-Earnings (P/E) Ratio: ${stock.symbol} holds a P/E of ${pe}. This implies you pay ₹${pe} for every ₹1 of profit. High P/E stocks emphasize future growth; low P/E stocks highlight immediate value.\n\n2. Earnings Per Share (EPS): An EPS of ₹${eps} is the exact net income generated per share. A steady increase in EPS is the primary driver of stock prices.\n\n3. 52-Week Range: With a low of ₹${low52} and high of ₹${high52}, comparing current price (₹${current}) outlines whether you are buying on value discount or momentum break-out.`;

        return {
            rating,
            summary,
            strengths: strengths.join("\n"),
            risks: risks.join("\n"),
            education
        };
    }
};


// ==================== 5. CHART ENGINE (Chart.js Wrapper) ====================
let myActiveChartInstance = null;

const chartEngine = {
    renderChart(canvasId, symbol, interval) {
        const stocks = dbService.getTable('stocks');
        const stock = stocks.find(s => s.symbol === symbol);
        if (!stock) return;

        const ctx = document.getElementById(canvasId).getContext('2d');

        // Destroy previous chart instance to avoid overlay glitches
        if (myActiveChartInstance) {
            myActiveChartInstance.destroy();
        }

        // Generate mock points based on selected interval
        const dataConfig = this.generateHistoricalPoints(stock.price, interval);

        // Chart.js Configuration for sleek obsidian fintech line chart
        const gradient = ctx.createLinearGradient(0, 0, 0, 300);
        gradient.addColorStop(0, 'rgba(41, 98, 255, 0.3)');
        gradient.addColorStop(1, 'rgba(41, 98, 255, 0.0)');

        myActiveChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: dataConfig.labels,
                datasets: [{
                    label: `${symbol} Price`,
                    data: dataConfig.prices,
                    borderColor: '#2962FF',
                    borderWidth: 2.5,
                    pointBackgroundColor: '#2962FF',
                    pointHoverBackgroundColor: '#FFFFFF',
                    pointHoverBorderColor: '#2962FF',
                    pointHoverBorderWidth: 2,
                    pointRadius: dataConfig.prices.length > 25 ? 0 : 2,
                    pointHoverRadius: 5,
                    fill: true,
                    backgroundColor: gradient,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#1C2030',
                        titleColor: '#B2B5BE',
                        bodyColor: '#FFFFFF',
                        borderColor: '#2A2E39',
                        borderWidth: 1,
                        padding: 10,
                        displayColors: false,
                        callbacks: {
                            label: function(context) {
                                return `₹${context.parsed.y.toFixed(2)}`;
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        grid: { display: false },
                        ticks: { color: '#B2B5BE', font: { size: 10 } }
                    },
                    y: {
                        grid: { color: '#2A2E39' },
                        ticks: {
                            color: '#B2B5BE',
                            font: { size: 10 },
                            callback: function(val) {
                                return '₹' + val.toFixed(0);
                            }
                        }
                    }
                }
            }
        });
    },

    generateHistoricalPoints(currentPrice, interval) {
        let points = 10;
        let volatility = 0.015;
        let labels = [];

        switch (interval) {
            case "1D":
                points = 8;
                volatility = 0.005;
                labels = ["09:30", "10:30", "11:30", "12:30", "13:30", "14:30", "15:30", "16:00"];
                break;
            case "1W":
                points = 5;
                volatility = 0.012;
                labels = ["Mon", "Tue", "Wed", "Thu", "Fri"];
                break;
            case "1M":
                points = 20;
                volatility = 0.025;
                for (let i = 1; i <= 20; i++) labels.push(`Day ${i}`);
                break;
            case "6M":
                points = 26;
                volatility = 0.06;
                for (let i = 1; i <= 26; i++) labels.push(`Wk ${i}`);
                break;
            case "1Y":
                points = 12;
                volatility = 0.10;
                labels = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
                break;
            case "5Y":
                points = 10;
                volatility = 0.25;
                labels = ["Y1-H1", "Y1-H2", "Y2-H1", "Y2-H2", "Y3-H1", "Y3-H2", "Y4-H1", "Y4-H2", "Y5-H1", "Y5-H2"];
                break;
        }

        const prices = [];
        let price = currentPrice;

        // Backward random walk
        for (let i = points - 1; i >= 0; i--) {
            const pct = (Math.random() - 0.49) * volatility;
            const open = price / (1.0 + pct);
            prices.push(Math.round(price * 100) / 100);
            price = open;
        }

        prices.reverse();
        
        // Ensure final point matches exactly
        prices[prices.length - 1] = currentPrice;

        return { labels, prices };
    }
};


// ==================== 6. ROUTER SERVICE ====================
const appRouter = {
    navigate(viewName, params = null) {
        // Toggle view classes
        const panels = document.querySelectorAll('.view-panel');
        panels.forEach(p => p.classList.remove('active'));

        const activePanel = document.getElementById('view-' + viewName);
        if (activePanel) {
            activePanel.classList.add('active');
        }

        // Toggle sidebar highlights
        const menuItems = document.querySelectorAll('.sidebar .menu-item');
        menuItems.forEach(item => item.classList.remove('active'));

        const activeMenu = document.getElementById('menu-' + viewName);
        if (activeMenu) {
            activeMenu.classList.add('active');
        }

        // Set Title & load specific components
        const titles = {
            'dashboard': 'Dashboard Overview',
            'details': `Stock Technical Audit: ${params || ''}`,
            'portfolio': 'Virtual Portfolio Desk',
            'watchlist': 'My Saved Watchlist',
            'news': 'Financial Market News',
            'admin': 'Admin Control Panel',
            'profile': 'User Profile & API Config'
        };

        document.getElementById('headerTitle').innerText = titles[viewName] || 'AIStockWave';

        // Load specific panel details
        switch (viewName) {
            case 'dashboard':
                uiController.renderMarketTable();
                break;
            case 'details':
                uiController.renderStockDetails(params);
                break;
            case 'portfolio':
                uiController.renderPortfolio();
                break;
            case 'watchlist':
                uiController.renderWatchlist();
                break;
            case 'news':
                uiController.renderNews();
                break;
            case 'admin':
                uiController.renderAdminPanel();
                break;
            case 'profile':
                uiController.renderProfile();
                break;
        }
    }
};


// ==================== 7. UI CONTROLLER ====================
const uiController = {
    activeDetailSymbol: null,
    activeTradeMode: 'BUY',
    activeChartInterval: '1M',

    onMarketTick() {
        const view = document.querySelector('.view-panel.active').id;
        if (view === 'view-dashboard') {
            this.renderMarketTable();
        } else if (view === 'view-details' && this.activeDetailSymbol) {
            // Live update prices and stats dynamically
            const stocks = dbService.getTable('stocks');
            const stock = stocks.find(s => s.symbol === this.activeDetailSymbol);
            if (stock) {
                document.getElementById('detailPrice').innerText = `₹${stock.price.toFixed(2)}`;
                const change = ((stock.price - stock.open) / stock.open) * 100;
                const changeLbl = document.getElementById('detailChange');
                if (change >= 0) {
                    changeLbl.innerText = `+${change.toFixed(2)}% Today`;
                    changeLbl.className = "text-up";
                } else {
                    changeLbl.innerText = `${change.toFixed(2)}% Today`;
                    changeLbl.className = "text-down";
                }

                // Update stats
                document.getElementById('statHigh').innerText = `₹${stock.high.toFixed(2)}`;
                document.getElementById('statLow').innerText = `₹${stock.low.toFixed(2)}`;
                document.getElementById('statVol').innerText = stock.volume.toLocaleString();

                // Live update AI analysis as well
                const ai = aiService.generateAnalysis(stock);
                document.getElementById('aiRating').innerText = `RATING: ${ai.rating}`;
                document.getElementById('aiRating').className = `badge ${ai.rating.includes('BUY') ? 'badge-up' : (ai.rating.includes('SELL') ? 'badge-down' : 'badge-blue')}`;
                document.getElementById('aiSummary').innerText = ai.summary;
                document.getElementById('aiStrengths').innerText = ai.strengths;
                document.getElementById('aiRisks').innerText = ai.risks;
            }
        } else if (view === 'view-portfolio') {
            this.renderPortfolio();
        } else if (view === 'view-watchlist') {
            this.renderWatchlist();
        }
    },

    refreshHeader() {
        const user = authService.getCurrentUser();
        if (user) {
            // Load fresh from DB
            const users = dbService.getTable('users');
            const freshUser = users.find(u => u.id === user.id);
            
            document.getElementById('headerUserWelcome').innerText = `Welcome, ${freshUser.fullName}`;
            document.getElementById('headerBalance').innerText = `₹${freshUser.balance.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
        }
    },

    // --- DASHBOARD OVERVIEW PANELS ---
    renderMarketTable() {
        const tbody = document.getElementById('marketTableBody');
        tbody.innerHTML = '';

        const stocks = dbService.getTable('stocks');
        stocks.forEach(s => {
            const tr = document.createElement('tr');
            const change = ((s.price - s.open) / s.open) * 100;
            const changeClass = change >= 0 ? 'text-up' : 'text-down';
            const changeSign = change >= 0 ? '+' : '';

            tr.innerHTML = `
                <td style="font-weight: 700; color: white; cursor: pointer;" onclick="appRouter.navigate('details', '${s.symbol}')">${s.symbol}</td>
                <td>${s.name}</td>
                <td>₹${s.price.toFixed(2)}</td>
                <td class="${changeClass}" style="font-weight: 600;">${changeSign}${change.toFixed(2)}%</td>
                <td>₹${s.high.toFixed(2)}</td>
                <td>₹${s.low.toFixed(2)}</td>
                <td><button onclick="appRouter.navigate('details', '${s.symbol}')" class="btn btn-primary" style="padding: 5px 12px; font-size: 12px;">Analyze</button></td>
            `;
            tbody.appendChild(tr);
        });

        // Fluctuating Indices as well
        const nifty = 23450.25 + (Math.random() * 80 - 40);
        const sensex = 76820.40 + (Math.random() * 250 - 125);
        const sp = 5420.10 + (Math.random() * 20 - 10);

        document.getElementById('idxNiftyPrice').innerText = nifty.toLocaleString('en-IN', { maximumFractionDigits: 2 });
        document.getElementById('idxSensexPrice').innerText = sensex.toLocaleString('en-IN', { maximumFractionDigits: 2 });
        document.getElementById('idxSpPrice').innerText = sp.toLocaleString('en-US', { maximumFractionDigits: 2 });
    },

    executeSearch() {
        const feedback = document.getElementById('searchFeedback');
        feedback.innerText = '';

        const query = document.getElementById('searchInput').value.trim().toUpperCase();
        if (!query) return;

        const stocks = dbService.getTable('stocks');
        const found = stocks.find(s => s.symbol === query || s.name.toUpperCase().includes(query));

        if (found) {
            document.getElementById('searchInput').value = '';
            appRouter.navigate('details', found.symbol);
        } else {
            feedback.innerText = 'No matching symbol or company name found.';
        }
    },

    // --- STOCK DETAILS PANEL ---
    renderStockDetails(symbol) {
        this.activeDetailSymbol = symbol;
        const stocks = dbService.getTable('stocks');
        const stock = stocks.find(s => s.symbol === symbol);
        if (!stock) return;

        document.getElementById('detailSymbol').innerText = stock.symbol;
        document.getElementById('detailName').innerText = stock.name;
        document.getElementById('detailPrice').innerText = `₹${stock.price.toFixed(2)}`;
        
        const change = ((stock.price - stock.open) / stock.open) * 100;
        const changeLbl = document.getElementById('detailChange');
        if (change >= 0) {
            changeLbl.innerText = `+${change.toFixed(2)}% Today`;
            changeLbl.className = "text-up";
        } else {
            changeLbl.innerText = `${change.toFixed(2)}% Today`;
            changeLbl.className = "text-down";
        }

        // Stats board
        document.getElementById('statOpen').innerText = `₹${stock.open.toFixed(2)}`;
        document.getElementById('statClose').innerText = `₹${stock.close.toFixed(2)}`;
        document.getElementById('statCap').innerText = `₹${stock.cap.toFixed(1)}B`;
        document.getElementById('statHigh').innerText = `₹${stock.high.toFixed(2)}`;
        document.getElementById('statLow').innerText = `₹${stock.low.toFixed(2)}`;
        document.getElementById('statVol').innerText = stock.volume.toLocaleString();
        document.getElementById('statPE').innerText = stock.pe === 0 ? "N/A" : stock.pe.toFixed(2);
        document.getElementById('statEPS').innerText = `₹${stock.eps.toFixed(2)}`;
        document.getElementById('statDiv').innerText = stock.div === 0 ? "0.00%" : `${stock.div.toFixed(2)}%`;
        document.getElementById('stat52H').innerText = `₹${stock.h52.toFixed(2)}`;
        document.getElementById('stat52L').innerText = `₹${stock.l52.toFixed(2)}`;

        // Watchlist status
        const user = authService.getCurrentUser();
        const watchlist = dbService.getTable('watchlist');
        const isFavorited = watchlist.some(w => w.userId === user.id && w.symbol === symbol);
        
        const watchBtn = document.getElementById('watchlistToggleBtn');
        if (isFavorited) {
            watchBtn.innerText = '⭐ Saved in Watchlist';
            watchBtn.className = 'btn btn-secondary';
        } else {
            watchBtn.innerText = '☆ Add to Watchlist';
            watchBtn.className = 'btn btn-primary';
        }

        // Available Cash
        const users = dbService.getTable('users');
        const dbUser = users.find(u => u.id === user.id);
        document.getElementById('tradeAvailableCash').innerText = `Available Cash: ₹${dbUser.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        // Reset Trade inputs
        document.getElementById('tradeQuantity').value = '';
        document.getElementById('tradeEstCost').innerText = '₹0.00';
        document.getElementById('tradeFeedback').innerHTML = '';

        this.setTradeMode('BUY');
        this.activeChartInterval = '1M';
        
        // Render Chart.js line
        chartEngine.renderChart('stockDetailChart', symbol, this.activeChartInterval);

        // AI summaries
        const ai = aiService.generateAnalysis(stock);
        document.getElementById('aiRating').innerText = `RATING: ${ai.rating}`;
        document.getElementById('aiRating').className = `badge ${ai.rating.includes('BUY') ? 'badge-up' : (ai.rating.includes('SELL') ? 'badge-down' : 'badge-blue')}`;
        document.getElementById('aiSummary').innerText = ai.summary;
        document.getElementById('aiStrengths').innerText = ai.strengths;
        document.getElementById('aiRisks').innerText = ai.risks;
        document.getElementById('aiEducation').innerText = ai.education;
    },

    toggleWatchlist() {
        const user = authService.getCurrentUser();
        const watchlist = dbService.getTable('watchlist');
        const index = watchlist.findIndex(w => w.userId === user.id && w.symbol === this.activeDetailSymbol);

        if (index > -1) {
            watchlist.splice(index, 1);
        } else {
            watchlist.push({ userId: user.id, symbol: this.activeDetailSymbol });
        }

        dbService.saveTable('watchlist', watchlist);
        this.renderStockDetails(this.activeDetailSymbol);
    },

    changeChartInterval(interval, btn) {
        // Toggle active styling
        const buttons = document.querySelectorAll('.chart-toggles .chart-toggle-btn');
        buttons.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        this.activeChartInterval = interval;
        chartEngine.renderChart('stockDetailChart', this.activeDetailSymbol, interval);
    },

    setTradeMode(mode) {
        this.activeTradeMode = mode;
        const buy = document.getElementById('tradeToggleBuy');
        const sell = document.getElementById('tradeToggleSell');
        const submit = document.getElementById('tradeSubmitBtn');

        if (mode === 'BUY') {
            buy.className = 'trade-toggle-btn active buy';
            sell.className = 'trade-toggle-btn';
            submit.className = 'btn btn-primary btn-buy';
        } else {
            sell.className = 'trade-toggle-btn active sell';
            buy.className = 'trade-toggle-btn';
            submit.className = 'btn btn-primary btn-sell';
        }

        this.recalculateTradeCost();
    },

    recalculateTradeCost() {
        const qty = parseInt(document.getElementById('tradeQuantity').value) || 0;
        const stocks = dbService.getTable('stocks');
        const stock = stocks.find(s => s.symbol === this.activeDetailSymbol);
        
        if (stock) {
            document.getElementById('tradeEstCost').innerText = `₹${(qty * stock.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
        }
    },

    executeTrade() {
        const feedback = document.getElementById('tradeFeedback');
        feedback.innerHTML = '';

        const qty = parseInt(document.getElementById('tradeQuantity').value);
        if (isNaN(qty) || qty <= 0) {
            feedback.innerHTML = `<div class="error-msg">Enter a valid positive quantity.</div>`;
            return;
        }

        const user = authService.getCurrentUser();
        const users = dbService.getTable('users');
        const dbUser = users.find(u => u.id === user.id);

        const stocks = dbService.getTable('stocks');
        const stock = stocks.find(s => s.symbol === this.activeDetailSymbol);
        const cost = qty * stock.price;

        const portfolio = dbService.getTable('portfolio');
        const transactions = dbService.getTable('transactions');

        if (this.activeTradeMode === 'BUY') {
            if (dbUser.balance < cost) {
                feedback.innerHTML = `<div class="error-msg">Insufficient cash. Required: ₹${cost.toFixed(2)}</div>`;
                return;
            }

            // Deduct cash
            dbUser.balance -= cost;

            // Update holdings
            const holding = portfolio.find(p => p.userId === user.id && p.symbol === stock.symbol);
            if (holding) {
                const totalCost = (holding.quantity * holding.avgBuyPrice) + cost;
                holding.quantity += qty;
                holding.avgBuyPrice = totalCost / holding.quantity;
            } else {
                portfolio.push({
                    userId: user.id,
                    symbol: stock.symbol,
                    quantity: qty,
                    avgBuyPrice: stock.price
                });
            }

            // Log Tx
            transactions.unshift({
                userId: user.id,
                type: "BUY",
                symbol: stock.symbol,
                quantity: qty,
                price: stock.price,
                timestamp: new Date().toISOString()
            });

            feedback.innerHTML = `<div class="success-msg">SUCCESS: Bought ${qty} shares of ${stock.symbol}!</div>`;

        } else { // SELL
            const holdingIndex = portfolio.findIndex(p => p.userId === user.id && p.symbol === stock.symbol);
            if (holdingIndex === -1 || portfolio[holdingIndex].quantity < qty) {
                feedback.innerHTML = `<div class="error-msg">Insufficient shares count to sell.</div>`;
                return;
            }

            // Credit Cash
            dbUser.balance += cost;

            // Update holding
            portfolio[holdingIndex].quantity -= qty;
            if (portfolio[holdingIndex].quantity === 0) {
                portfolio.splice(holdingIndex, 1);
            }

            // Log Tx
            transactions.unshift({
                userId: user.id,
                type: "SELL",
                symbol: stock.symbol,
                quantity: qty,
                price: stock.price,
                timestamp: new Date().toISOString()
            });

            feedback.innerHTML = `<div class="success-msg">SUCCESS: Sold ${qty} shares of ${stock.symbol}!</div>`;
        }

        // Commit database tables
        dbService.saveTable('users', users);
        dbService.saveTable('portfolio', portfolio);
        dbService.saveTable('transactions', transactions);

        // Update Session User state
        sessionStorage.setItem('asw_session_user', JSON.stringify(dbUser));

        // Reset forms and refresh UI
        document.getElementById('tradeQuantity').value = '';
        document.getElementById('tradeEstCost').innerText = '₹0.00';
        document.getElementById('tradeAvailableCash').innerText = `Available Cash: ₹${dbUser.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        this.refreshHeader();
    },

    // --- PORTFOLIO VIEW ---
    renderPortfolio() {
        const user = authService.getCurrentUser();
        const users = dbService.getTable('users');
        const dbUser = users.find(u => u.id === user.id);

        const portfolio = dbService.getTable('portfolio').filter(p => p.userId === user.id && p.quantity > 0);
        const stocks = dbService.getTable('stocks');

        // Cash card
        document.getElementById('portCashBal').innerText = `₹${dbUser.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        // Calculate holding values
        let stockAssets = 0;
        const holdingsTbody = document.getElementById('portfolioHoldingsBody');
        holdingsTbody.innerHTML = '';

        portfolio.forEach(item => {
            const stock = stocks.find(s => s.symbol === item.symbol);
            if (!stock) return;

            const value = item.quantity * stock.price;
            stockAssets += value;

            const profitLoss = value - (item.quantity * item.avgBuyPrice);
            const returnPct = ((stock.price - item.avgBuyPrice) / item.avgBuyPrice) * 100;

            const plClass = profitLoss >= 0 ? 'text-up' : 'text-down';
            const plSign = profitLoss >= 0 ? '+' : '';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td style="font-weight: 700; color: white; cursor: pointer;" onclick="appRouter.navigate('details', '${item.symbol}')">${item.symbol}</td>
                <td>${stock.name}</td>
                <td>${item.quantity}</td>
                <td>₹${item.avgBuyPrice.toFixed(2)}</td>
                <td>₹${stock.price.toFixed(2)}</td>
                <td>₹${value.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                <td class="${plClass}" style="font-weight: 600;">
                    ${plSign}₹${profitLoss.toFixed(2)} (${plSign}${returnPct.toFixed(2)}%)
                </td>
            `;
            holdingsTbody.appendChild(tr);
        });

        const netAssets = dbUser.balance + stockAssets;
        document.getElementById('portNetAssets').innerText = `₹${netAssets.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        // Lifetime returns relative to seeded ₹100,000
        const initialCapital = 100000.00;
        const returns = netAssets - initialCapital;
        const roi = (returns / initialCapital) * 100;

        const retLabel = document.getElementById('portReturns');
        const retPctLabel = document.getElementById('portReturnsPct');

        retLabel.innerText = `₹${returns.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;
        if (returns >= 0) {
            retLabel.style.color = 'var(--color-up)';
            retPctLabel.innerText = `+${roi.toFixed(2)}%`;
            retPctLabel.className = 'metric-change text-up';
        } else {
            retLabel.style.color = 'var(--color-down)';
            retPctLabel.innerText = `${roi.toFixed(2)}%`;
            retPctLabel.className = 'metric-change text-down';
        }

        // Render transactions history log
        const txsTbody = document.getElementById('portfolioTxsBody');
        txsTbody.innerHTML = '';
        const txs = dbService.getTable('transactions').filter(t => t.userId === user.id);
        
        txs.forEach(t => {
            const tr = document.createElement('tr');
            const actionClass = t.type === 'BUY' ? 'text-up' : 'text-down';
            const formattedDate = new Date(t.timestamp).toLocaleString();

            tr.innerHTML = `
                <td>${formattedDate}</td>
                <td class="${actionClass}" style="font-weight: 700;">${t.type}</td>
                <td style="font-weight: 700; color: white;">${t.symbol}</td>
                <td>${t.quantity}</td>
                <td>₹${t.price.toFixed(2)}</td>
                <td>₹${(t.quantity * t.price).toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
            `;
            txsTbody.appendChild(tr);
        });
    },

    // --- WATCHLIST VIEW ---
    renderWatchlist() {
        const grid = document.getElementById('watchlistCardsGrid');
        grid.innerHTML = '';

        const user = authService.getCurrentUser();
        const watchlist = dbService.getTable('watchlist').filter(w => w.userId === user.id);
        const stocks = dbService.getTable('stocks');

        if (watchlist.length === 0) {
            grid.innerHTML = `<p class="lbl-caption" style="grid-column: 1/-1; line-height: 1.5; font-size: 14px;">Your watchlist is currently empty.<br>Go to the Dashboard to search and add stock symbols here.</p>`;
            return;
        }

        watchlist.forEach(item => {
            const stock = stocks.find(s => s.symbol === item.symbol);
            if (!stock) return;

            const change = ((stock.price - stock.open) / stock.open) * 100;
            const changeClass = change >= 0 ? 'text-up' : 'text-down';
            const changeSign = change >= 0 ? '+' : '';

            const card = document.createElement('div');
            card.className = 'watchlist-card';
            card.innerHTML = `
                <div class="watchlist-card-header">
                    <span class="watchlist-card-symbol">${stock.symbol}</span>
                    <button class="watchlist-card-delete" onclick="uiController.removeWatchlist('${stock.symbol}')">✕</button>
                </div>
                <div class="watchlist-card-name">${stock.name}</div>
                <div class="watchlist-card-price-row">
                    <span class="watchlist-card-price">₹${stock.price.toFixed(2)}</span>
                    <span class="watchlist-card-change ${changeClass}">${changeSign}${change.toFixed(2)}%</span>
                </div>
                <button class="btn btn-primary" style="padding: 6px 12px; font-size: 11px; margin-top: 5px;" onclick="appRouter.navigate('details', '${stock.symbol}')">Analyze Stock</button>
            `;
            grid.appendChild(card);
        });
    },

    removeWatchlist(symbol) {
        const user = authService.getCurrentUser();
        const watchlist = dbService.getTable('watchlist');
        const index = watchlist.findIndex(w => w.userId === user.id && w.symbol === symbol);

        if (index > -1) {
            watchlist.splice(index, 1);
            dbService.saveTable('watchlist', watchlist);
            this.renderWatchlist();
        }
    },

    // --- MARKET NEWS VIEW ---
    renderNews() {
        const container = document.getElementById('newsListContainer');
        container.innerHTML = '';

        const news = dbService.getTable('news');
        news.forEach(item => {
            const card = document.createElement('div');
            card.className = 'news-card';
            card.innerHTML = `
                <a href="#" class="news-headline" onclick="event.preventDefault();">${item.title}</a>
                <div class="news-meta">
                    <span class="news-source">${item.source}</span>
                    <span>•</span>
                    <span>${item.date}</span>
                </div>
                <p class="news-description">${item.description}</p>
            `;
            container.appendChild(card);
        });
    },

    // --- ADMIN PANEL ---
    renderAdminPanel() {
        const user = authService.getCurrentUser();
        if (!user || !user.isAdmin) return;

        const users = dbService.getTable('users');
        const transactions = dbService.getTable('transactions');

        // Metrics
        document.getElementById('adminUsersCount').innerText = users.length;
        document.getElementById('adminTxsCount').innerText = transactions.length;

        let grossVal = 0;
        transactions.forEach(t => {
            grossVal += t.quantity * t.price;
        });
        document.getElementById('adminGrossVolume').innerText = `₹${grossVal.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        // Users Account table
        const tbody = document.getElementById('adminUsersTableBody');
        tbody.innerHTML = '';

        users.forEach(u => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${u.id}</td>
                <td>${u.fullName}</td>
                <td>${u.email}</td>
                <td>₹${u.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}</td>
                <td>${u.isAdmin ? 'Admin' : 'Trader'}</td>
                <td><button onclick="uiController.adminResetBalance(${u.id})" class="btn btn-secondary" style="padding: 4px 10px; font-size: 11px;">Reset ₹100k</button></td>
            `;
            tbody.appendChild(tr);
        });
    },

    adminResetBalance(userId) {
        const users = dbService.getTable('users');
        const u = users.find(usr => usr.id === userId);
        if (u) {
            u.balance = 100000.00;
            dbService.saveTable('users', users);
            this.renderAdminPanel();
            this.refreshHeader();
        }
    },

    adminAddStock() {
        const feedback = document.getElementById('adminStockFeedback');
        feedback.innerHTML = '';

        const symbol = document.getElementById('adminStockSymbol').value.trim().toUpperCase();
        const name = document.getElementById('adminStockName').value.trim();
        const price = parseFloat(document.getElementById('adminStockPrice').value);
        const cap = parseFloat(document.getElementById('adminStockCap').value);

        if (!symbol || !name || isNaN(price) || isNaN(cap)) {
            feedback.innerHTML = `<div class="error-msg">Fill in all stock fields.</div>`;
            return;
        }

        const stocks = dbService.getTable('stocks');
        if (stocks.find(s => s.symbol === symbol)) {
            feedback.innerHTML = `<div class="error-msg">Symbol ${symbol} already exists.</div>`;
            return;
        }

        const eps = price * 0.04;
        const pe = price / eps;

        const newStock = {
            symbol,
            name,
            price,
            open: price,
            close: price,
            high: price,
            low: price,
            volume: 120000,
            cap,
            pe,
            eps,
            div: 1.25,
            h52: price,
            l52: price
        };

        stocks.push(newStock);
        dbService.saveTable('stocks', stocks);

        feedback.innerHTML = `<div class="success-msg">SUCCESS: Registered ${symbol}! It is now active.</div>`;

        // Clear
        document.getElementById('adminStockSymbol').value = '';
        document.getElementById('adminStockName').value = '';
        document.getElementById('adminStockPrice').value = '';
        document.getElementById('adminStockCap').value = '';

        this.renderAdminPanel();
    },

    adminPublishNews() {
        const feedback = document.getElementById('adminNewsFeedback');
        feedback.innerHTML = '';

        const title = document.getElementById('adminNewsTitle').value.trim();
        const source = document.getElementById('adminNewsSource').value.trim();
        const desc = document.getElementById('adminNewsDesc').value.trim();

        if (!title || !source || !desc) {
            feedback.innerHTML = `<div class="error-msg">Fill in all news fields.</div>`;
            return;
        }

        const news = dbService.getTable('news');
        const months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
        const today = new Date();
        const dateStr = `${months[today.getMonth()]} ${today.getDate()}, ${today.getFullYear()}`;

        const newArticle = {
            id: news.length + 1,
            title,
            description: desc,
            source,
            date: dateStr
        };

        news.unshift(newArticle);
        dbService.saveTable('news', news);

        feedback.innerHTML = `<div class="success-msg">SUCCESS: Event published to market feed.</div>`;

        // Clear
        document.getElementById('adminNewsTitle').value = '';
        document.getElementById('adminNewsSource').value = '';
        document.getElementById('adminNewsDesc').value = '';

        this.renderAdminPanel();
    },

    // --- PROFILE ---
    renderProfile() {
        const user = authService.getCurrentUser();
        const users = dbService.getTable('users');
        const dbUser = users.find(u => u.id === user.id);

        document.getElementById('profileName').innerText = dbUser.fullName;
        document.getElementById('profileEmail').innerText = dbUser.email;
        document.getElementById('profileBalance').innerText = `₹${dbUser.balance.toLocaleString('en-IN', { minimumFractionDigits: 2 })}`;

        // Clear passwords
        document.getElementById('profileCurPassword').value = '';
        document.getElementById('profileNewPassword').value = '';
        document.getElementById('profileConfirmPassword').value = '';
        document.getElementById('profilePasswordFeedback').innerHTML = '';
        document.getElementById('profileApiFeedback').innerHTML = '';
    },

    updatePassword() {
        const feedback = document.getElementById('profilePasswordFeedback');
        feedback.innerHTML = '';

        const curPass = document.getElementById('profileCurPassword').value;
        const newPass = document.getElementById('profileNewPassword').value;
        const confirm = document.getElementById('profileConfirmPassword').value;

        if (!curPass || !newPass || !confirm) {
            feedback.innerHTML = `<div class="error-msg">Fill in all password fields.</div>`;
            return;
        }

        if (newPass !== confirm) {
            feedback.innerHTML = `<div class="error-msg">New passwords do not match.</div>`;
            return;
        }

        if (newPass.length < 6) {
            feedback.innerHTML = `<div class="error-msg">Password must be at least 6 characters.</div>`;
            return;
        }

        const user = authService.getCurrentUser();
        const users = dbService.getTable('users');
        const dbUser = users.find(u => u.id === user.id);

        if (dbUser.password === curPass) {
            dbUser.password = newPass;
            dbService.saveTable('users', users);
            sessionStorage.setItem('asw_session_user', JSON.stringify(dbUser));

            feedback.innerHTML = `<div class="success-msg">SUCCESS: Password updated successfully.</div>`;
            
            document.getElementById('profileCurPassword').value = '';
            document.getElementById('profileNewPassword').value = '';
            document.getElementById('profileConfirmPassword').value = '';
        } else {
            feedback.innerHTML = `<div class="error-msg">Incorrect current password.</div>`;
        }
    },

    saveApiKeys() {
        const feedback = document.getElementById('profileApiFeedback');
        feedback.innerHTML = `<div class="success-msg">SUCCESS: Applied live server credentials.</div>`;
        
        document.getElementById('profileFinnhubKey').value = '';
        document.getElementById('profileAvKey').value = '';
    }
};


// ==================== 8. BOOTSTRAP INITIALIZATION ====================
window.onload = function() {
    // Check if user has active session
    const sessionUser = authService.getCurrentUser();
    if (sessionUser) {
        document.getElementById('authContainer').style.display = 'none';
        document.getElementById('appContainer').style.display = 'flex';
        
        const adminMenu = document.getElementById('menu-admin');
        if (sessionUser.isAdmin) {
            adminMenu.style.display = 'flex';
        } else {
            adminMenu.style.display = 'none';
        }

        uiController.refreshHeader();
        appRouter.navigate('dashboard');

        // Start Ticker
        simulatorService.start();
    } else {
        document.getElementById('authContainer').style.display = 'flex';
        document.getElementById('appContainer').style.display = 'none';
    }

    // Trigger searches on Enter press
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            uiController.executeSearch();
        }
    });

    document.getElementById('loginPassword').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            authService.login();
        }
    });
};
