package com.aistockwave.view;

import com.aistockwave.database.PortfolioDAO;
import com.aistockwave.database.UserDAO;
import com.aistockwave.model.PortfolioItem;
import com.aistockwave.model.Transaction;
import com.aistockwave.model.User;
import com.aistockwave.service.AuthService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class PortfolioView {
    private final VBox layout;
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();
    private final UserDAO userDAO = new UserDAO();

    // Observable data lists
    private final ObservableList<PortfolioItem> holdingsList = FXCollections.observableArrayList();
    private final ObservableList<Transaction> transactionsList = FXCollections.observableArrayList();

    // Metric Summary Labels
    private Label totalPortfolioValLabel;
    private Label cashBalLabel;
    private Label totalReturnLabel;
    private Label totalReturnPctLabel;

    private Timeline updateTimeline;

    public PortfolioView() {
        layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0B0E14;");

        // 1. Portfolio summary metrics cards
        HBox metricsRow = createMetricsRow();

        // 2. Portfolio holdings table
        VBox holdingsSection = createHoldingsSection();

        // 3. Transactions history table
        VBox transactionsSection = createTransactionsSection();

        layout.getChildren().addAll(metricsRow, holdingsSection, transactionsSection);

        // Load data
        refreshPortfolioData();

        // Periodic UI updates
        startGuiUpdates();
    }

    public VBox getView() {
        return layout;
    }

    private HBox createMetricsRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        VBox cashCard = createMetricCard("💸 Virtual Cash Available", cashBalLabel = new Label("₹0.00"));
        VBox valCard = createMetricCard("📈 Net Assets (Cash + Stock)", totalPortfolioValLabel = new Label("₹0.00"));
        VBox profitCard = createReturnCard("📊 Lifetime Net Returns", totalReturnLabel = new Label("₹0.00"), totalReturnPctLabel = new Label("0.00%"));

        HBox.setHgrow(cashCard, Priority.ALWAYS);
        HBox.setHgrow(valCard, Priority.ALWAYS);
        HBox.setHgrow(profitCard, Priority.ALWAYS);

        row.getChildren().addAll(cashCard, valCard, profitCard);
        return row;
    }

    private VBox createMetricCard(String title, Label valueLabel) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15, 20, 15, 20));

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("lbl-caption");
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        valueLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        card.getChildren().addAll(titleLbl, valueLabel);
        return card;
    }

    private VBox createReturnCard(String title, Label valueLabel, Label pctLabel) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15, 20, 15, 20));

        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("lbl-caption");
        titleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        valueLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: white;");
        pctLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

        HBox valueRow = new HBox(10);
        valueRow.setAlignment(Pos.BASELINE_LEFT);
        valueRow.getChildren().addAll(valueLabel, pctLabel);

        card.getChildren().addAll(titleLbl, valueRow);
        return card;
    }

    private VBox createHoldingsSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        VBox.setVgrow(container, Priority.ALWAYS);

        Label title = new Label("💼 Current Stock Holdings");
        title.getStyleClass().add("card-title");

        TableView<PortfolioItem> tableView = new TableView<>();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<PortfolioItem, String> symCol = new TableColumn<>("Symbol");
        symCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        symCol.setPrefWidth(90);
        symCol.setStyle("-fx-font-weight: bold;");

        TableColumn<PortfolioItem, String> nameCol = new TableColumn<>("Company Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("companyName"));
        nameCol.setPrefWidth(200);

        TableColumn<PortfolioItem, Integer> qtyCol = new TableColumn<>("Shares");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(90);

        TableColumn<PortfolioItem, Double> buyCol = new TableColumn<>("Avg Buy Price");
        buyCol.setCellValueFactory(new PropertyValueFactory<>("avgBuyPrice"));
        buyCol.setPrefWidth(120);
        buyCol.setCellFactory(col -> new TableCell<PortfolioItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<PortfolioItem, Double> priceCol = new TableColumn<>("Live Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        priceCol.setPrefWidth(120);
        priceCol.setCellFactory(col -> new TableCell<PortfolioItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<PortfolioItem, Double> valCol = new TableColumn<>("Current Value");
        valCol.setPrefWidth(130);
        valCol.setCellFactory(col -> new TableCell<PortfolioItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    PortfolioItem rowItem = getTableView().getItems().get(getIndex());
                    setText(String.format("₹%,.2f", rowItem.getCurrentValue()));
                }
            }
        });

        TableColumn<PortfolioItem, Double> retCol = new TableColumn<>("Profit/Loss");
        retCol.setPrefWidth(130);
        retCol.setCellValueFactory(new PropertyValueFactory<>("profitLoss"));
        retCol.setCellFactory(col -> new TableCell<PortfolioItem, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    PortfolioItem rowItem = getTableView().getItems().get(getIndex());
                    double pct = rowItem.getReturnPercent();
                    setText(String.format("%+.2f (%+.2f%%)", item, pct));
                    if (item >= 0) {
                        setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableView.getColumns().addAll(symCol, nameCol, qtyCol, buyCol, priceCol, valCol, retCol);
        tableView.setItems(holdingsList);

        // Add double click on row to open detailed analysis view
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !tableView.getSelectionModel().isEmpty()) {
                PortfolioItem item = tableView.getSelectionModel().getSelectedItem();
                MainDashboardView.getInstance().loadStockDetailView(item.getSymbol());
            }
        });

        container.getChildren().addAll(title, tableView);
        return container;
    }

    private VBox createTransactionsSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        container.setPrefHeight(230);
        container.setMinHeight(200);

        Label title = new Label("📜 Transaction Execution History");
        title.getStyleClass().add("card-title");

        TableView<Transaction> tableView = new TableView<>();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Transaction, Timestamp> timeCol = new TableColumn<>("Date & Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        timeCol.setPrefWidth(180);
        timeCol.setCellFactory(col -> new TableCell<Transaction, Timestamp>() {
            private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @Override protected void updateItem(Timestamp item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(formatter.format(item));
            }
        });

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Action");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(90);
        typeCol.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("BUY")) {
                        setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #EF5350; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<Transaction, String> symCol = new TableColumn<>("Symbol");
        symCol.setCellValueFactory(new PropertyValueFactory<>("symbol"));
        symCol.setPrefWidth(90);
        symCol.setStyle("-fx-font-weight: bold;");

        TableColumn<Transaction, Integer> qtyCol = new TableColumn<>("Shares");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setPrefWidth(100);

        TableColumn<Transaction, Double> priceCol = new TableColumn<>("Trade Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(130);
        priceCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<Transaction, Double> totalCol = new TableColumn<>("Gross Value");
        totalCol.setPrefWidth(140);
        totalCol.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    Transaction rowItem = getTableView().getItems().get(getIndex());
                    setText(String.format("₹%,.2f", rowItem.getQuantity() * rowItem.getPrice()));
                }
            }
        });

        tableView.getColumns().addAll(timeCol, typeCol, symCol, qtyCol, priceCol, totalCol);
        tableView.setItems(transactionsList);

        container.getChildren().addAll(title, tableView);
        return container;
    }

    private void refreshPortfolioData() {
        User user = AuthService.getCurrentUser();
        if (user == null) return;

        // 1. Fetch updated cash balance from Database
        User dbUser = userDAO.getUserByEmail(user.getEmail());
        double cash = (dbUser != null) ? dbUser.getBalance() : user.getBalance();
        cashBalLabel.setText(String.format("₹%,.2f", cash));

        // 2. Fetch current holdings
        List<PortfolioItem> items = portfolioDAO.getPortfolioForUser(user.getId());
        holdingsList.clear();
        holdingsList.addAll(items);

        // 3. Compute net asset valuation (Cash + stock value)
        double totalStockValue = 0;
        for (PortfolioItem item : items) {
            totalStockValue += item.getCurrentValue();
        }
        double netAssets = cash + totalStockValue;
        totalPortfolioValLabel.setText(String.format("₹%,.2f", netAssets));

        // 4. Calculate Net profit/losses relative to original ₹100,000 paper balance
        double originalCapital = 100000.0;
        double netProfitLoss = netAssets - originalCapital;
        double roi = (netProfitLoss / originalCapital) * 100;

        totalReturnLabel.setText(String.format("₹%,.2f", netProfitLoss));
        if (netProfitLoss >= 0) {
            totalReturnLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #26A69A;");
            totalReturnPctLabel.setText(String.format("+%.2f%%", roi));
            totalReturnPctLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #26A69A;");
        } else {
            totalReturnLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #EF5350;");
            totalReturnPctLabel.setText(String.format("%.2f%%", roi));
            totalReturnPctLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #EF5350;");
        }

        // 5. Fetch transactions log (only fetch on initial display, no need to refresh every 3 seconds unless dynamic changes occur)
        if (transactionsList.isEmpty()) {
            List<Transaction> txs = portfolioDAO.getTransactionsForUser(user.getId());
            transactionsList.addAll(txs);
        }
    }

    private void startGuiUpdates() {
        updateTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> {
            refreshPortfolioData();
        }));
        updateTimeline.setCycleCount(Timeline.INDEFINITE);
        updateTimeline.play();

        layout.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null && updateTimeline != null) {
                updateTimeline.stop();
            }
        });
    }
}
