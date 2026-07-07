package com.aistockwave.view;

import com.aistockwave.database.WatchlistDAO;
import com.aistockwave.model.Stock;
import com.aistockwave.model.User;
import com.aistockwave.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class WatchlistView {
    private final ScrollPane scrollPane;
    private final VBox container;
    private final FlowPane grid;
    private final WatchlistDAO watchlistDAO = new WatchlistDAO();
    private Timeline updateTimeline;

    public WatchlistView() {
        container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #0B0E14;");

        scrollPane = new ScrollPane(container);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        Label headline = new Label("⭐ Saved Ticker Watchlist");
        headline.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        grid = new FlowPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPrefWrapLength(900); // wrap row on overflow

        container.getChildren().addAll(headline, grid);

        // Load data
        refreshWatchlistCards();

        // Start periodic updates
        startGuiUpdates();
    }

    public ScrollPane getView() {
        return scrollPane;
    }

    private void refreshWatchlistCards() {
        grid.getChildren().clear();
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        List<Stock> watchlist = watchlistDAO.getWatchlistForUser(user.getId());
        if (watchlist.isEmpty()) {
            Label emptyLbl = new Label("Your watchlist is currently empty.\nGo to the Dashboard or Search for a stock to add it here.");
            emptyLbl.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 14; -fx-line-spacing: 1.4;");
            grid.getChildren().add(emptyLbl);
            return;
        }

        for (Stock stock : watchlist) {
            VBox card = createWatchlistCard(stock);
            grid.getChildren().add(card);
        }
    }

    private VBox createWatchlistCard(Stock stock) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMinWidth(205);
        card.setMaxWidth(205);
        card.setPadding(new Insets(15));

        // Header info
        HBox header = new HBox(5);
        header.setAlignment(Pos.CENTER_LEFT);
        Label symbolLbl = new Label(stock.getSymbol());
        symbolLbl.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button deleteBtn = new Button("✕");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 0 4; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                watchlistDAO.removeFromWatchlist(user.getId(), stock.getSymbol());
                refreshWatchlistCards();
            }
        });
        header.getChildren().addAll(symbolLbl, spacer, deleteBtn);

        Label nameLbl = new Label(stock.getCompanyName());
        nameLbl.getStyleClass().add("lbl-caption");
        nameLbl.setStyle("-fx-font-size: 11;");

        // Price details
        Label priceLbl = new Label(String.format("₹%,.2f", stock.getCurrentPrice()));
        priceLbl.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");

        double change = stock.getChangePercent();
        Label changeLbl = new Label();
        if (change >= 0) {
            changeLbl.setText(String.format("+%.2f%%", change));
            changeLbl.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-font-size: 12;");
        } else {
            changeLbl.setText(String.format("%.2f%%", change));
            changeLbl.setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-font-size: 12;");
        }

        HBox priceRow = new HBox(8);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        priceRow.getChildren().addAll(priceLbl, changeLbl);

        // Click to view detailed analysis
        Button analyzeBtn = new Button("Analyze Stock");
        analyzeBtn.getStyleClass().add("btn-primary");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setStyle("-fx-padding: 6 12; -fx-font-size: 11;");
        analyzeBtn.setOnAction(e -> MainDashboardView.getInstance().loadStockDetailView(stock.getSymbol()));

        card.getChildren().addAll(header, nameLbl, priceRow, analyzeBtn);
        return card;
    }

    private void updateLivePricesOnly() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        List<Stock> watchlist = watchlistDAO.getWatchlistForUser(user.getId());
        // If counts differ, reload full grid; otherwise just update price strings to avoid re-rendering layout jump
        if (watchlist.size() != grid.getChildren().size()) {
            refreshWatchlistCards();
            return;
        }

        for (int i = 0; i < watchlist.size(); i++) {
            Stock currentQuote = watchlist.get(i);
            VBox card = (VBox) grid.getChildren().get(i);
            
            // Find labels within children of card
            HBox priceRow = null;
            for (javafx.scene.Node node : card.getChildren()) {
                if (node instanceof HBox && node != card.getChildren().get(0)) {
                    priceRow = (HBox) node;
                    break;
                }
            }

            if (priceRow != null && priceRow.getChildren().size() >= 2) {
                Label priceLbl = (Label) priceRow.getChildren().get(0);
                Label changeLbl = (Label) priceRow.getChildren().get(1);

                priceLbl.setText(String.format("₹%,.2f", currentQuote.getCurrentPrice()));
                double change = currentQuote.getChangePercent();
                if (change >= 0) {
                    changeLbl.setText(String.format("+%.2f%%", change));
                    changeLbl.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-font-size: 12;");
                } else {
                    changeLbl.setText(String.format("%.2f%%", change));
                    changeLbl.setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold; -fx-font-size: 12;");
                }
            }
        }
    }

    private void startGuiUpdates() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            updateLivePricesOnly();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        container.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && updateTimeline != null) {
                updateTimeline.stop();
            }
        });
    }
}
