package com.aistockwave.view;

import com.aistockwave.database.StockDAO;
import com.aistockwave.model.Stock;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;

public class DashboardOverviewView {
    private final VBox layout;
    private final StockDAO stockDAO = new StockDAO();
    private final ObservableList<Stock> stockList = FXCollections.observableArrayList();
    private TableView<Stock> tableView;
    private Timeline updateTimeline;

    // Simulated market indices
    private Label niftyPriceLabel;
    private Label niftyChangeLabel;
    private Label sensexPriceLabel;
    private Label sensexChangeLabel;
    private Label spPriceLabel;
    private Label spChangeLabel;

    private double niftyVal = 23450.25;
    private double sensexVal = 76820.40;
    private double spVal = 5420.10;

    public DashboardOverviewView() {
        layout = new VBox(20);
        layout.setPadding(new Insets(20));

        // 1. Search Bar Header
        HBox searchRow = createSearchBar();

        // 2. Index Overview Cards
        HBox indicesRow = createIndicesRow();

        // 3. Stock List Table
        VBox tableContainer = createStockTableSection();

        layout.getChildren().addAll(searchRow, indicesRow, tableContainer);

        // Load Initial Data
        refreshData();

        // Start Periodic GUI update matching simulator frequency
        startGuiUpdates();
    }

    public VBox getView() {
        return layout;
    }

    private HBox createSearchBar() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");
        row.setPadding(new Insets(12, 20, 12, 20));

        Label searchLbl = new Label("Search Tickers:");
        searchLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14;");

        TextField searchField = new TextField();
        searchField.setPromptText("Enter stock symbol (e.g. AAPL, TSLA) or company name");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        Button searchBtn = new Button("🔍 Search");
        searchBtn.getStyleClass().add("btn-primary");

        Label searchFeedback = new Label();
        searchFeedback.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13;");

        searchBtn.setOnAction(e -> {
            searchFeedback.setText("");
            String query = searchField.getText().trim().toUpperCase();
            if (query.isEmpty()) return;

            // Search by symbol
            Stock stock = stockDAO.getStockBySymbol(query);
            if (stock != null) {
                MainDashboardView.getInstance().loadStockDetailView(stock.getSymbol());
                return;
            }

            // Search by company name (simple substring)
            List<Stock> all = stockDAO.getAllStocks();
            for (Stock s : all) {
                if (s.getCompanyName().toUpperCase().contains(query)) {
                    MainDashboardView.getInstance().loadStockDetailView(s.getSymbol());
                    return;
                }
            }
            searchFeedback.setText("No matching stock symbol or company name found.");
        });

        searchField.setOnAction(e -> searchBtn.fire());

        row.getChildren().addAll(searchLbl, searchField, searchBtn, searchFeedback);
        return row;
    }

    private HBox createIndicesRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        // NIFTY 50 Card
        VBox niftyCard = createIndexCard("🇮🇳 NIFTY 50", niftyPriceLabel = new Label(), niftyChangeLabel = new Label());
        // SENSEX Card
        VBox sensexCard = createIndexCard("🇮🇳 SENSEX", sensexPriceLabel = new Label(), sensexChangeLabel = new Label());
        // S&P 500 Card
        VBox spCard = createIndexCard("🇺🇸 S&P 500", spPriceLabel = new Label(), spChangeLabel = new Label());

        HBox.setHgrow(niftyCard, Priority.ALWAYS);
        HBox.setHgrow(sensexCard, Priority.ALWAYS);
        HBox.setHgrow(spCard, Priority.ALWAYS);

        row.getChildren().addAll(niftyCard, sensexCard, spCard);
        updateIndicesText(0, 0, 0); // initial load
        return row;
    }

    private VBox createIndexCard(String indexName, Label priceLbl, Label changeLbl) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15, 20, 15, 20));

        Label name = new Label(indexName);
        name.getStyleClass().add("lbl-caption");
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        priceLbl.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        changeLbl.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        HBox priceRow = new HBox(10);
        priceRow.setAlignment(Pos.BASELINE_LEFT);
        priceRow.getChildren().addAll(priceLbl, changeLbl);

        card.getChildren().addAll(name, priceRow);
        return card;
    }

    private void updateIndicesText(double niftyPct, double sensexPct, double spPct) {
        niftyVal = niftyVal * (1.0 + niftyPct);
        sensexVal = sensexVal * (1.0 + sensexPct);
        spVal = spVal * (1.0 + spPct);

        niftyPriceLabel.setText(String.format("%,.2f", niftyVal));
        sensexPriceLabel.setText(String.format("%,.2f", sensexVal));
        spPriceLabel.setText(String.format("%,.2f", spVal));

        setIndexChangeLabel(niftyChangeLabel, niftyPct * 100);
        setIndexChangeLabel(sensexChangeLabel, sensexPct * 100);
        setIndexChangeLabel(spChangeLabel, spPct * 100);
    }

    private void setIndexChangeLabel(Label lbl, double pct) {
        if (pct >= 0) {
            lbl.setText(String.format("+%.2f%%", pct));
            lbl.setStyle("-fx-text-fill: #26A69A; -fx-font-size: 13; -fx-font-weight: bold;");
        } else {
            lbl.setText(String.format("%.2f%%", pct));
            lbl.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 13; -fx-font-weight: bold;");
        }
    }

    private VBox createStockTableSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        VBox.setVgrow(container, Priority.ALWAYS);

        Label heading = new Label("🔥 Active Market Watch");
        heading.getStyleClass().add("card-title");
        heading.setStyle("-fx-font-size: 16;");

        tableView = new TableView<>();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Stock, String> symCol = new TableColumn<>("Symbol");
        symCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        symCol.setPrefWidth(90);
        symCol.setStyle("-fx-font-weight: bold;");

        TableColumn<Stock, String> nameCol = new TableColumn<>("Company Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        nameCol.setPrefWidth(220);

        TableColumn<Stock, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(column -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("₹%,.2f", item));
                }
            }
        });

        TableColumn<Stock, Double> changeCol = new TableColumn<>("Change (%)");
        changeCol.setPrefWidth(110);
        changeCol.setCellValueFactory(new PropertyValueFactory<>("changePercent"));
        changeCol.setCellFactory(column -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%+.2f%%", item));
                    if (item >= 0) {
                        setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Stock, Double> highCol = new TableColumn<>("Today's High");
        highCol.setCellValueFactory(new PropertyValueFactory<>("highPrice"));
        highCol.setPrefWidth(110);
        highCol.setCellFactory(column -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<Stock, Double> lowCol = new TableColumn<>("Today's Low");
        lowCol.setCellValueFactory(new PropertyValueFactory<>("lowPrice"));
        lowCol.setPrefWidth(110);
        lowCol.setCellFactory(column -> new TableCell<Stock, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<Stock, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(110);
        actionCol.setCellFactory(col -> new TableCell<Stock, Void>() {
            private final Button btn = new Button("Analyze");
            {
                btn.getStyleClass().add("btn-primary");
                btn.setStyle("-fx-padding: 5 10; -fx-font-size: 11;");
                btn.setOnAction(e -> {
                    Stock stock = getTableView().getItems().get(getIndex());
                    MainDashboardView.getInstance().loadStockDetailView(stock.getSymbol());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        tableView.getColumns().addAll(symCol, nameCol, priceCol, changeCol, highCol, lowCol, actionCol);
        tableView.setItems(stockList);

        container.getChildren().addAll(heading, tableView);
        return container;
    }

    private void refreshData() {
        List<Stock> stocks = stockDAO.getAllStocks();
        stockList.clear();
        stockList.addAll(stocks);
        
        // Simulating minor indices fluctuations as well
        java.util.Random r = new java.util.Random();
        double niftyChg = (r.nextDouble() * 0.2 - 0.09) / 100.0;
        double sensexChg = (r.nextDouble() * 0.2 - 0.09) / 100.0;
        double spChg = (r.nextDouble() * 0.25 - 0.12) / 100.0;
        updateIndicesText(niftyChg, sensexChg, spChg);
    }

    private void startGuiUpdates() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            refreshData();
            // Also refresh wallet balance in header
            MainDashboardView.getInstance().refreshHeader();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        // Stop timeline updates when view is detached to save CPU cycles
        layout.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && updateTimeline != null) {
                updateTimeline.stop();
            }
        });
    }
}
