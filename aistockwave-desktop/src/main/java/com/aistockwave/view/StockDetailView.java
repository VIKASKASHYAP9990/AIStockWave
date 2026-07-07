package com.aistockwave.view;

import com.aistockwave.App;
import com.aistockwave.database.PortfolioDAO;
import com.aistockwave.database.StockDAO;
import com.aistockwave.database.UserDAO;
import com.aistockwave.database.WatchlistDAO;
import com.aistockwave.model.Stock;
import com.aistockwave.model.User;
import com.aistockwave.service.AiAnalysisService;
import com.aistockwave.service.AuthService;
import com.aistockwave.service.FinancialApiService.ChartDataPoint;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.util.List;

public class StockDetailView {
    private final ScrollPane scrollPane;
    private final VBox layout;
    private final String symbol;

    private final StockDAO stockDAO = new StockDAO();
    private final UserDAO userDAO = new UserDAO();
    private final WatchlistDAO watchlistDAO = new WatchlistDAO();
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();
    private final AiAnalysisService aiService = new AiAnalysisService();

    // Live updating UI nodes
    private Label priceLabel;
    private Label changeLabel;
    private Label availableCashLabel;
    private Label estCostValLabel;

    // Stat board nodes
    private Label statOpen, statClose, statHigh, statLow, statVol, statCap, statPE, statEPS, statDiv, stat52H, stat52L;

    // AI summary card nodes
    private Label aiRatingLabel;
    private Label aiSummaryText;
    private Label aiStrengthsText;
    private Label aiRisksText;
    private Label aiEducationText;

    private Button watchlistBtn;
    private CandlestickChartComponent chartComponent;
    private String selectedInterval = "1M"; // default timeframe

    private Timeline liveUpdateTimeline;

    public StockDetailView(String symbol) {
        this.symbol = symbol.toUpperCase();
        
        layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0B0E14;");

        scrollPane = new ScrollPane(layout);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        Stock stock = stockDAO.getStockBySymbol(this.symbol);
        if (stock == null) {
            layout.getChildren().add(new Label("Error: Stock " + this.symbol + " not found in database."));
            return;
        }

        // 1. Stock Header Widget (Price, Name, Watchlist toggle)
        HBox headerWidget = createHeaderWidget(stock);

        // 2. Chart Section with time interval buttons
        VBox chartSection = createChartSection();

        // 3. Middle split: Stats panel & Paper Trading panel
        HBox statsAndTradingRow = createStatsAndTradingRow(stock);

        // 4. AI Analysis Card
        VBox aiAnalysisCard = createAiAnalysisSection(stock);

        layout.getChildren().addAll(headerWidget, chartSection, statsAndTradingRow, aiAnalysisCard);

        // Load default chart
        refreshChart();

        // Start live quotes polling
        startLivePolling();
    }

    public ScrollPane getView() {
        return scrollPane;
    }

    private HBox createHeaderWidget(Stock stock) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");

        VBox titleBox = new VBox(4);
        Label symbolLbl = new Label(stock.getSymbol());
        symbolLbl.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");
        Label nameLbl = new Label(stock.getCompanyName());
        nameLbl.getStyleClass().add("lbl-caption");
        nameLbl.setStyle("-fx-font-size: 14;");
        titleBox.getChildren().addAll(symbolLbl, nameLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox priceBox = new VBox(4);
        priceBox.setAlignment(Pos.CENTER_RIGHT);
        priceLabel = new Label();
        priceLabel.setStyle("-fx-font-size: 26; -fx-font-weight: bold; -fx-text-fill: white;");
        changeLabel = new Label();
        changeLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        priceBox.getChildren().addAll(priceLabel, changeLabel);

        updatePriceLabels(stock);

        // Watchlist Toggle Button
        User current = AuthService.getCurrentUser();
        watchlistBtn = new Button();
        updateWatchlistButtonText();
        watchlistBtn.setOnAction(e -> {
            if (current != null) {
                if (watchlistDAO.isInWatchlist(current.getId(), symbol)) {
                    watchlistDAO.removeFromWatchlist(current.getId(), symbol);
                } else {
                    watchlistDAO.addToWatchlist(current.getId(), symbol);
                }
                updateWatchlistButtonText();
            }
        });

        row.getChildren().addAll(titleBox, spacer, priceBox, watchlistBtn);
        return row;
    }

    private void updateWatchlistButtonText() {
        User current = AuthService.getCurrentUser();
        if (current != null) {
            boolean inWatch = watchlistDAO.isInWatchlist(current.getId(), symbol);
            if (inWatch) {
                watchlistBtn.setText("⭐ Saved in Watchlist");
                watchlistBtn.getStyleClass().setAll("btn-secondary");
            } else {
                watchlistBtn.setText("☆ Add to Watchlist");
                watchlistBtn.getStyleClass().setAll("btn-primary");
            }
        }
    }

    private void updatePriceLabels(Stock stock) {
        priceLabel.setText(String.format("₹%,.2f", stock.getCurrentPrice()));
        double chg = stock.getChangePercent();
        if (chg >= 0) {
            changeLabel.setText(String.format("+%.2f%% Today", chg));
            changeLabel.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-font-size: 14;");
        } else {
            changeLabel.setText(String.format("%.2f%% Today", chg));
            changeLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-font-size: 14;");
        }
    }

    private VBox createChartSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");

        HBox chartHeader = new HBox(10);
        chartHeader.setAlignment(Pos.CENTER_LEFT);
        Label chartTitle = new Label("📈 Technical Candlestick Chart");
        chartTitle.getStyleClass().add("card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Timeframe toggles
        HBox toggleBox = new HBox(5);
        ToggleGroup group = new ToggleGroup();

        String[] timeframes = {"1D", "1W", "1M", "6M", "1Y", "5Y"};
        for (String tf : timeframes) {
            ToggleButton btn = new ToggleButton(tf);
            btn.getStyleClass().add("btn-secondary");
            btn.setStyle("-fx-padding: 5 12; -fx-font-size: 11; -fx-cursor: hand;");
            btn.setToggleGroup(group);
            if (tf.equals(selectedInterval)) {
                btn.setSelected(true);
            }
            btn.setOnAction(e -> {
                selectedInterval = tf;
                refreshChart();
            });
            toggleBox.getChildren().add(btn);
        }

        chartHeader.getChildren().addAll(chartTitle, spacer, toggleBox);

        // Chart Component drawing pane
        chartComponent = new CandlestickChartComponent();

        // Listen to width changes to trigger charts redraw for responsiveness
        chartComponent.widthProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::refreshChart);
        });

        container.getChildren().addAll(chartHeader, chartComponent);
        return container;
    }

    private void refreshChart() {
        List<ChartDataPoint> data = App.getApiService().getHistoricalData(symbol, selectedInterval);
        chartComponent.setChartData(data);
    }

    private HBox createStatsAndTradingRow(Stock stock) {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        // 1. Stats Board Card
        VBox statsCard = createStatsBoardCard(stock);
        HBox.setHgrow(statsCard, Priority.ALWAYS);

        // 2. Paper Trading Card
        VBox tradeCard = createTradingCard(stock);
        tradeCard.setMinWidth(360);
        tradeCard.setMaxWidth(360);

        row.getChildren().addAll(statsCard, tradeCard);
        return row;
    }

    private VBox createStatsBoardCard(Stock s) {
        VBox container = new VBox(15);
        container.getStyleClass().add("card");

        Label title = new Label("📊 Key Financial Statistics");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(35);
        grid.setVgap(12);

        // Row 0
        grid.add(createStatCell("Open Price", statOpen = new Label()), 0, 0);
        grid.add(createStatCell("Previous Close", statClose = new Label()), 1, 0);
        grid.add(createStatCell("Market Capitalization", statCap = new Label()), 2, 0);

        // Row 1
        grid.add(createStatCell("Today's High", statHigh = new Label()), 0, 1);
        grid.add(createStatCell("Today's Low", statLow = new Label()), 1, 1);
        grid.add(createStatCell("Trading Volume", statVol = new Label()), 2, 1);

        // Row 2
        grid.add(createStatCell("P/E Ratio", statPE = new Label()), 0, 2);
        grid.add(createStatCell("EPS (Earnings Per Share)", statEPS = new Label()), 1, 2);
        grid.add(createStatCell("Dividend Yield", statDiv = new Label()), 2, 2);

        // Row 3
        grid.add(createStatCell("52-Week High", stat52H = new Label()), 0, 3);
        grid.add(createStatCell("52-Week Low", stat52L = new Label()), 1, 3);

        updateStatsValues(s);

        container.getChildren().addAll(title, grid);
        return container;
    }

    private VBox createStatCell(String title, Label valueLabel) {
        VBox cell = new VBox(2);
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("lbl-caption");
        titleLbl.setStyle("-fx-font-size: 11;");
        valueLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        cell.getChildren().addAll(titleLbl, valueLabel);
        return cell;
    }

    private void updateStatsValues(Stock s) {
        statOpen.setText(String.format("₹%,.2f", s.getOpenPrice()));
        statClose.setText(String.format("₹%,.2f", s.getClosePrice()));
        statHigh.setText(String.format("₹%,.2f", s.getHighPrice()));
        statLow.setText(String.format("₹%,.2f", s.getLowPrice()));
        statVol.setText(String.format("%,d", s.getVolume()));
        statCap.setText(String.format("₹%,.1fB", s.getMarketCap()));
        statPE.setText(s.getPeRatio() == 0 ? "N/A" : String.format("%.2f", s.getPeRatio()));
        statEPS.setText(String.format("₹%.2f", s.getEps()));
        statDiv.setText(s.getDividendYield() == 0 ? "0.00%" : String.format("%.2f%%", s.getDividendYield()));
        stat52H.setText(String.format("₹%,.2f", s.getHigh52Week()));
        stat52L.setText(String.format("₹%,.2f", s.getLow52Week()));
    }

    private VBox createTradingCard(Stock stock) {
        VBox container = new VBox(12);
        container.getStyleClass().add("card");

        Label title = new Label("💼 Paper Trading Desk");
        title.getStyleClass().add("card-title");

        User user = AuthService.getCurrentUser();
        double balance = (user != null) ? user.getBalance() : 0.0;
        availableCashLabel = new Label(String.format("Available Cash: ₹%,.2f", balance));
        availableCashLabel.getStyleClass().add("lbl-caption");
        availableCashLabel.setStyle("-fx-font-size: 12;");

        // Action Toggles (BUY vs SELL)
        HBox toggleRow = new HBox(8);
        toggleRow.setAlignment(Pos.CENTER);
        
        ToggleButton buyToggle = new ToggleButton("BUY SHARES");
        buyToggle.getStyleClass().addAll("btn-secondary");
        buyToggle.setStyle("-fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        buyToggle.setSelected(true);

        ToggleButton sellToggle = new ToggleButton("SELL SHARES");
        sellToggle.getStyleClass().addAll("btn-secondary");
        sellToggle.setStyle("-fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");

        ToggleGroup buySellGroup = new ToggleGroup();
        buyToggle.setToggleGroup(buySellGroup);
        sellToggle.setToggleGroup(buySellGroup);

        // Force styling changes
        buyToggle.setOnAction(e -> {
            buyToggle.setStyle("-fx-background-color: #26A69A; -fx-text-fill: white; -fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold;");
            sellToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #B2B5BE; -fx-border-color: #2A2E39; -fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold;");
        });
        sellToggle.setOnAction(e -> {
            sellToggle.setStyle("-fx-background-color: #EF5350; -fx-text-fill: white; -fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold;");
            buyToggle.setStyle("-fx-background-color: transparent; -fx-text-fill: #B2B5BE; -fx-border-color: #2A2E39; -fx-padding: 8 16; -fx-font-size: 12; -fx-font-weight: bold;");
        });
        // Initial style trigger
        buyToggle.fire();

        HBox.setHgrow(buyToggle, Priority.ALWAYS);
        HBox.setHgrow(sellToggle, Priority.ALWAYS);
        buyToggle.setMaxWidth(Double.MAX_VALUE);
        sellToggle.setMaxWidth(Double.MAX_VALUE);
        toggleRow.getChildren().addAll(buyToggle, sellToggle);

        // Quantity Entry
        Label qtyLbl = new Label("Order Quantity");
        qtyLbl.getStyleClass().add("lbl-caption");
        TextField qtyField = new TextField();
        qtyField.setPromptText("Enter shares count");

        // Estimated Valuation
        HBox costRow = new HBox(5);
        Label estLbl = new Label("Estimated Value:");
        estLbl.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 13;");
        estCostValLabel = new Label("₹0.00");
        estCostValLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13;");
        costRow.getChildren().addAll(estLbl, estCostValLabel);

        // Dynamic cost recalculation on typing
        qtyField.textProperty().addListener((observable, oldVal, newVal) -> {
            try {
                if (newVal.trim().isEmpty()) {
                    estCostValLabel.setText("₹0.00");
                    return;
                }
                int qty = Integer.parseInt(newVal.trim());
                double price = stockDAO.getStockBySymbol(symbol).getCurrentPrice();
                estCostValLabel.setText(String.format("₹%,.2f", qty * price));
            } catch (NumberFormatException e) {
                estCostValLabel.setText("Invalid count");
            }
        });

        // Banner labels for feedback
        Label tradeFeedback = new Label();
        tradeFeedback.setWrapText(true);
        tradeFeedback.setStyle("-fx-font-size: 12;");

        Button submitBtn = new Button("EXECUTE TRADE");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setStyle("-fx-padding: 12 20; -fx-font-weight: bold;");

        submitBtn.setOnAction(e -> {
            tradeFeedback.setText("");
            tradeFeedback.setStyle("");
            String qtyStr = qtyField.getText().trim();
            if (qtyStr.isEmpty()) {
                tradeFeedback.setText("Enter a quantity to trade.");
                tradeFeedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            try {
                int qty = Integer.parseInt(qtyStr);
                if (qty <= 0) {
                    tradeFeedback.setText("Quantity must be greater than zero.");
                    tradeFeedback.setStyle("-fx-text-fill: #EF5350;");
                    return;
                }

                double livePrice = stockDAO.getStockBySymbol(symbol).getCurrentPrice();
                boolean isBuy = buyToggle.isSelected();
                boolean success;

                if (isBuy) {
                    success = portfolioDAO.executeBuy(user.getId(), symbol, qty, livePrice);
                    if (success) {
                        tradeFeedback.setText("SUCCESS: Purchased " + qty + " shares of " + symbol + "!");
                        tradeFeedback.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold;");
                    }
                } else {
                    success = portfolioDAO.executeSell(user.getId(), symbol, qty, livePrice);
                    if (success) {
                        tradeFeedback.setText("SUCCESS: Sold " + qty + " shares of " + symbol + "!");
                        tradeFeedback.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold;");
                    }
                }

                if (success) {
                    qtyField.setText("");
                    estCostValLabel.setText("₹0.00");
                    // Refresh Header & wallet balances
                    MainDashboardView.getInstance().refreshHeader();
                    // Reload cash display
                    double newBal = userDAO.getUserByEmail(user.getEmail()).getBalance();
                    availableCashLabel.setText(String.format("Available Cash: ₹%,.2f", newBal));
                }

            } catch (NumberFormatException ex) {
                tradeFeedback.setText("Invalid quantity integer.");
                tradeFeedback.setStyle("-fx-text-fill: #EF5350;");
            } catch (SQLException sqlEx) {
                tradeFeedback.setText(sqlEx.getMessage());
                tradeFeedback.setStyle("-fx-text-fill: #EF5350;");
            }
        });

        container.getChildren().addAll(title, availableCashLabel, toggleRow, qtyLbl, qtyField, costRow, tradeFeedback, submitBtn);
        return container;
    }

    private VBox createAiAnalysisSection(Stock stock) {
        VBox container = new VBox(15);
        container.getStyleClass().add("card");

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("🤖 AI-Powered Stock Auditor");
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 16;");

        aiRatingLabel = new Label();
        aiRatingLabel.setStyle("-fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-size: 12;");
        titleRow.getChildren().addAll(title, aiRatingLabel);

        aiSummaryText = new Label();
        aiSummaryText.setWrapText(true);
        aiSummaryText.setStyle("-fx-text-fill: white; -fx-font-size: 13.5; -fx-line-spacing: 1.4;");

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(30);
        detailsGrid.setVgap(15);

        // Core Strengths
        VBox strengthsBox = new VBox(5);
        Label strHeader = new Label("🟢 Growth Strengths");
        strHeader.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-font-size: 13;");
        aiStrengthsText = new Label();
        aiStrengthsText.setWrapText(true);
        aiStrengthsText.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 12.5; -fx-line-spacing: 1.3;");
        strengthsBox.getChildren().addAll(strHeader, aiStrengthsText);
        detailsGrid.add(strengthsBox, 0, 0);
        GridPane.setHgrow(strengthsBox, Priority.ALWAYS);

        // Core Risks
        VBox risksBox = new VBox(5);
        Label riskHeader = new Label("🔴 Risk Factors");
        riskHeader.setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-font-size: 13;");
        aiRisksText = new Label();
        aiRisksText.setWrapText(true);
        aiRisksText.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 12.5; -fx-line-spacing: 1.3;");
        risksBox.getChildren().addAll(riskHeader, aiRisksText);
        detailsGrid.add(risksBox, 1, 0);
        GridPane.setHgrow(risksBox, Priority.ALWAYS);

        // Educational Insights
        VBox educationBox = new VBox(5);
        Label eduHeader = new Label("📚 Investor Educational Concept");
        eduHeader.setStyle("-fx-text-fill: #2962FF; -fx-font-weight: bold; -fx-font-size: 13;");
        aiEducationText = new Label();
        aiEducationText.setWrapText(true);
        aiEducationText.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 12.5; -fx-line-spacing: 1.3;");
        educationBox.getChildren().addAll(eduHeader, aiEducationText);

        updateAiValues(stock);

        container.getChildren().addAll(titleRow, aiSummaryText, detailsGrid, educationBox);
        return container;
    }

    private void updateAiValues(Stock stock) {
        AiAnalysisService.StockAnalysisReport r = aiService.generateAnalysis(stock);
        
        aiRatingLabel.setText("RATING: " + r.getRating());
        if (r.getRating().contains("BUY")) {
            aiRatingLabel.setStyle("-fx-background-color: rgba(38, 166, 154, 0.15); -fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-size: 12;");
        } else if (r.getRating().contains("SELL")) {
            aiRatingLabel.setStyle("-fx-background-color: rgba(239, 83, 80, 0.15); -fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-size: 12;");
        } else {
            aiRatingLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 4 10; -fx-background-radius: 4; -fx-font-size: 12;");
        }

        aiSummaryText.setText(r.getSummary());
        aiStrengthsText.setText(r.getStrengths());
        aiRisksText.setText(r.getRisks());
        aiEducationText.setText(r.getEducationalInsight());
    }

    private void startLivePolling() {
        liveUpdateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            Stock currentQuote = stockDAO.getStockBySymbol(symbol);
            if (currentQuote != null) {
                // Update live pricing labels
                updatePriceLabels(currentQuote);
                // Update financial metrics board
                updateStatsValues(currentQuote);
                // Redraw AI Analysis (so that it updates to match new price movements)
                updateAiValues(currentQuote);
                
                // If the selected interval is 1D, let's refresh the chart so the last candle wiggles with the live tick!
                if ("1D".equals(selectedInterval)) {
                    refreshChart();
                }
            }
        }));
        liveUpdateTimeline.setCycleCount(Timeline.INDEFINITE);
        liveUpdateTimeline.play();

        // Stop polling once view detached from window
        scrollPane.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && liveUpdateTimeline != null) {
                liveUpdateTimeline.stop();
            }
        });
    }
}
