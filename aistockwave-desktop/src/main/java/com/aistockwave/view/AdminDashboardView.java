package com.aistockwave.view;

import com.aistockwave.database.NewsDAO;
import com.aistockwave.database.PortfolioDAO;
import com.aistockwave.database.StockDAO;
import com.aistockwave.database.UserDAO;
import com.aistockwave.model.NewsItem;
import com.aistockwave.model.Stock;
import com.aistockwave.model.Transaction;
import com.aistockwave.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminDashboardView {
    private final ScrollPane scrollPane;
    private final VBox container;

    private final UserDAO userDAO = new UserDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();
    private final NewsDAO newsDAO = new NewsDAO();

    // Data lists
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    // Stats labels
    private Label usersCountLabel;
    private Label txCountLabel;
    private Label grossVolumeLabel;

    public AdminDashboardView() {
        container = new VBox(20);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #0B0E14;");

        scrollPane = new ScrollPane(container);
        scrollPane.getStyleClass().add("scroll-pane");
        scrollPane.setFitToWidth(true);

        Label headline = new Label("🛠️ Platform Administration Console");
        headline.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        // 1. Stats row
        HBox statsRow = createMetricsCardsRow();

        // 2. User accounts table
        VBox usersSection = createUsersSection();

        // 3. Side-by-side forms for Adding Stock & Publishing News
        HBox formsRow = createFormsSplitRow();

        container.getChildren().addAll(headline, statsRow, usersSection, formsRow);

        // Load data
        refreshAdminData();
    }

    public ScrollPane getView() {
        return scrollPane;
    }

    private HBox createMetricsCardsRow() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        VBox usersCard = createStatsCard("👥 Registered Accounts", usersCountLabel = new Label("0"));
        VBox txCard = createStatsCard("💳 Total Trade Transactions", txCountLabel = new Label("0"));
        VBox grossCard = createStatsCard("💰 Gross Traded Volume", grossVolumeLabel = new Label("₹0.00"));

        HBox.setHgrow(usersCard, Priority.ALWAYS);
        HBox.setHgrow(txCard, Priority.ALWAYS);
        HBox.setHgrow(grossCard, Priority.ALWAYS);

        row.getChildren().addAll(usersCard, txCard, grossCard);
        return row;
    }

    private VBox createStatsCard(String title, Label valLbl) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPadding(new Insets(15, 20, 15, 20));

        Label t = new Label(title);
        t.getStyleClass().add("lbl-caption");
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

        valLbl.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-text-fill: white;");
        card.getChildren().addAll(t, valLbl);
        return card;
    }

    private VBox createUsersSection() {
        VBox container = new VBox(10);
        container.getStyleClass().add("card");
        container.setPrefHeight(240);
        container.setMinHeight(200);

        Label title = new Label("👤 Active User Account Management");
        title.getStyleClass().add("card-title");

        TableView<User> tableView = new TableView<>();
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(55);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(180);

        TableColumn<User, String> emailCol = new TableColumn<>("Email Address");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(220);

        TableColumn<User, Double> balCol = new TableColumn<>("Virtual Cash Balance");
        balCol.setCellValueFactory(new PropertyValueFactory<>("balance"));
        balCol.setPrefWidth(160);
        balCol.setCellFactory(col -> new TableCell<User, Double>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(String.format("₹%,.2f", item));
            }
        });

        TableColumn<User, Boolean> roleCol = new TableColumn<>("Is Admin");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("admin"));
        roleCol.setPrefWidth(90);
        roleCol.setCellFactory(col -> new TableCell<User, Boolean>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item ? "Admin" : "Trader");
            }
        });

        TableColumn<User, Void> actionCol = new TableColumn<>("Wallet Action");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<User, Void>() {
            private final Button btn = new Button("Reset ₹100k");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setStyle("-fx-padding: 4 10; -fx-font-size: 11;");
                btn.setOnAction(e -> {
                    User target = getTableView().getItems().get(getIndex());
                    userDAO.updateBalance(target.getId(), 100000.0);
                    refreshAdminData();
                    // Also refresh current top panel wallet stats if active
                    MainDashboardView.getInstance().refreshHeader();
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else setGraphic(btn);
            }
        });

        tableView.getColumns().addAll(idCol, nameCol, emailCol, balCol, roleCol, actionCol);
        tableView.setItems(userList);

        container.getChildren().addAll(title, tableView);
        return container;
    }

    private HBox createFormsSplitRow() {
        HBox row = new HBox(20);
        row.setAlignment(Pos.TOP_LEFT);

        VBox addStockCard = createAddStockCard();
        VBox publishNewsCard = createPublishNewsCard();

        HBox.setHgrow(addStockCard, Priority.ALWAYS);
        HBox.setHgrow(publishNewsCard, Priority.ALWAYS);

        row.getChildren().addAll(addStockCard, publishNewsCard);
        return row;
    }

    private VBox createAddStockCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label title = new Label("📈 Register New Ticker Symbol");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField symbolField = new TextField();
        symbolField.setPromptText("e.g. INFY");
        TextField nameField = new TextField();
        nameField.setPromptText("e.g. Infosys Ltd");
        TextField priceField = new TextField();
        priceField.setPromptText("Initial price (e.g. 1450.00)");
        TextField capField = new TextField();
        capField.setPromptText("Market Cap (e.g. 75.5 Billions)");

        grid.add(new Label("Symbol:"), 0, 0);
        grid.add(symbolField, 1, 0);
        grid.add(new Label("Company Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Initial Price (₹):"), 0, 2);
        grid.add(priceField, 1, 2);
        grid.add(new Label("Market Cap ($B):"), 0, 3);
        grid.add(capField, 1, 3);

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-font-size: 12;");

        Button submitBtn = new Button("Register Stock");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            feedback.setText("");
            feedback.setStyle("");

            String symbol = symbolField.getText().trim().toUpperCase();
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            String capStr = capField.getText().trim();

            if (symbol.isEmpty() || name.isEmpty() || priceStr.isEmpty() || capStr.isEmpty()) {
                feedback.setText("Fill in all stock details.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                double cap = Double.parseDouble(capStr);

                if (price <= 0 || cap <= 0) {
                    feedback.setText("Price and Market Cap must be positive.");
                    feedback.setStyle("-fx-text-fill: #EF5350;");
                    return;
                }

                // Check duplicate
                if (stockDAO.getStockBySymbol(symbol) != null) {
                    feedback.setText("Stock symbol " + symbol + " already registered.");
                    feedback.setStyle("-fx-text-fill: #EF5350;");
                    return;
                }

                // Standard PE / EPS mock parameters for newly registered stock
                double eps = price * 0.04;
                double pe = price / eps;
                double div = 1.15;

                Stock s = new Stock(
                        symbol, name, price, price, price, price, price,
                        250000L, cap, pe, eps, div, price, price
                );

                boolean success = stockDAO.createStock(s);
                if (success) {
                    feedback.setText("SUCCESS: Registered " + symbol + "! It will now begin trading.");
                    feedback.setStyle("-fx-text-fill: #26A69A;");
                    
                    symbolField.setText("");
                    nameField.setText("");
                    priceField.setText("");
                    capField.setText("");
                } else {
                    feedback.setText("Failed to save stock registry.");
                    feedback.setStyle("-fx-text-fill: #EF5350;");
                }

            } catch (NumberFormatException ex) {
                feedback.setText("Price and Market Cap must be numeric.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
            }
        });

        card.getChildren().addAll(title, grid, feedback, submitBtn);
        return card;
    }

    private VBox createPublishNewsCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        Label title = new Label("📰 Publish Financial Market Event");
        title.getStyleClass().add("card-title");

        VBox form = new VBox(8);
        TextField titleField = new TextField();
        titleField.setPromptText("News Headline Title");
        TextField sourceField = new TextField();
        sourceField.setPromptText("Source (e.g. Wall Street Journal)");
        TextArea descField = new TextArea();
        descField.setPromptText("Detailed content writeup...");
        descField.setPrefRowCount(4);
        descField.setWrapText(true);

        form.getChildren().addAll(new Label("Headline:"), titleField, new Label("Publisher:"), sourceField, new Label("Content Description:"), descField);

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-font-size: 12;");

        Button submitBtn = new Button("Publish News Event");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            feedback.setText("");
            feedback.setStyle("");

            String headlineText = titleField.getText().trim();
            String source = sourceField.getText().trim();
            String description = descField.getText().trim();

            if (headlineText.isEmpty() || source.isEmpty() || description.isEmpty()) {
                feedback.setText("Fill in all news details.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
            String todayStr = sdf.format(new Date());

            NewsItem news = new NewsItem(0, headlineText, description, source, todayStr);
            boolean success = newsDAO.createNews(news);
            if (success) {
                feedback.setText("SUCCESS: Market event published to general feed.");
                feedback.setStyle("-fx-text-fill: #26A69A;");

                titleField.setText("");
                sourceField.setText("");
                descField.setText("");
            } else {
                feedback.setText("Failed to save news article.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
            }
        });

        card.getChildren().addAll(title, form, feedback, submitBtn);
        return card;
    }

    private void refreshAdminData() {
        // 1. Refresh Users accounts table
        List<User> list = userDAO.getAllUsers();
        userList.clear();
        userList.addAll(list);

        // 2. Count registered accounts
        usersCountLabel.setText(String.valueOf(list.size()));

        // 3. Transactions analytics
        List<Transaction> txs = portfolioDAO.getAllTransactions();
        txCountLabel.setText(String.valueOf(txs.size()));

        double grossVal = 0;
        for (Transaction t : txs) {
            grossVal += t.getQuantity() * t.getPrice();
        }
        grossVolumeLabel.setText(String.format("₹%,.2f", grossVal));
    }
}
