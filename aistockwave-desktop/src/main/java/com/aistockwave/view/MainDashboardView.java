package com.aistockwave.view;

import com.aistockwave.App;
import com.aistockwave.database.UserDAO;
import com.aistockwave.model.User;
import com.aistockwave.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainDashboardView {
    private static MainDashboardView instance;
    
    private final BorderPane rootLayout;
    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();

    // Header Components
    private Label pageTitleLabel;
    private Label usernameLabel;
    private Label balanceValLabel;

    // Sidebar buttons
    private Button dashboardBtn;
    private Button portfolioBtn;
    private Button watchlistBtn;
    private Button newsBtn;
    private Button adminBtn;
    private Button profileBtn;

    public static MainDashboardView getInstance() {
        return instance;
    }

    public MainDashboardView() {
        instance = this;
        rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("root");

        // 1. Sidebar Left Panel
        VBox sidebar = createSidebar();
        rootLayout.setLeft(sidebar);

        // 2. Header Top Panel
        HBox header = createHeader();
        rootLayout.setTop(header);

        // 3. Default Center Panel
        loadDashboardOverview();
    }

    public BorderPane getView() {
        return rootLayout;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        Label brand = new Label("AIStockWave");
        brand.getStyleClass().add("sidebar-brand");

        dashboardBtn = new Button("📊 Dashboard");
        dashboardBtn.getStyleClass().addAll("sidebar-btn", "sidebar-btn-active");
        dashboardBtn.setOnAction(e -> loadDashboardOverview());

        portfolioBtn = new Button("💼 Portfolio");
        portfolioBtn.getStyleClass().add("sidebar-btn");
        portfolioBtn.setOnAction(e -> loadPortfolioView());

        watchlistBtn = new Button("⭐ Watchlist");
        watchlistBtn.getStyleClass().add("sidebar-btn");
        watchlistBtn.setOnAction(e -> loadWatchlistView());

        newsBtn = new Button("📰 Market News");
        newsBtn.getStyleClass().add("sidebar-btn");
        newsBtn.setOnAction(e -> loadNewsView());

        adminBtn = new Button("🛠️ Admin Panel");
        adminBtn.getStyleClass().add("sidebar-btn");
        adminBtn.setOnAction(e -> loadAdminView());

        // Hide admin panel for non-admins
        User current = AuthService.getCurrentUser();
        if (current == null || !current.isAdmin()) {
            adminBtn.setVisible(false);
            adminBtn.setManaged(false);
        }

        profileBtn = new Button("👤 Profile & Keys");
        profileBtn.getStyleClass().add("sidebar-btn");
        profileBtn.setOnAction(e -> loadProfileView());

        Button logoutBtn = new Button("🚪 Logout");
        logoutBtn.getStyleClass().add("sidebar-btn");
        logoutBtn.setStyle("-fx-text-fill: #EF5350;");
        logoutBtn.setOnAction(e -> {
            authService.logout();
            App.showLoginView();
        });

        // Spacers and layout alignment
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(brand, dashboardBtn, portfolioBtn, watchlistBtn, newsBtn, adminBtn, profileBtn, spacer, logoutBtn);
        return sidebar;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        pageTitleLabel = new Label("Dashboard Overview");
        pageTitleLabel.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox userDetails = new VBox(2);
        userDetails.setAlignment(Pos.CENTER_RIGHT);

        User current = AuthService.getCurrentUser();
        String nameStr = (current != null) ? current.getFullName() : "Paper Trader";
        usernameLabel = new Label("Welcome, " + nameStr);
        usernameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");

        HBox balanceBox = new HBox(4);
        balanceBox.setAlignment(Pos.CENTER_RIGHT);
        Label balanceLbl = new Label("Virtual Balance: ");
        balanceLbl.getStyleClass().add("header-wallet-lbl");
        balanceValLabel = new Label("₹0.00");
        balanceValLabel.getStyleClass().add("header-wallet-val");
        balanceBox.getChildren().addAll(balanceLbl, balanceValLabel);

        userDetails.getChildren().addAll(usernameLabel, balanceBox);
        header.getChildren().addAll(pageTitleLabel, spacer, userDetails);

        refreshHeader();
        return header;
    }

    public void refreshHeader() {
        User current = AuthService.getCurrentUser();
        if (current != null) {
            // Fetch updated user stats from Database
            User updatedUser = userDAO.getUserByEmail(current.getEmail());
            if (updatedUser != null) {
                // Update session state
                AuthService.setCurrentUser(updatedUser);
                current = updatedUser;
            }
            usernameLabel.setText("Welcome, " + current.getFullName());
            balanceValLabel.setText(String.format("₹%,.2f", current.getBalance()));
        }
    }

    public void setCenterView(Node viewNode, String title) {
        rootLayout.setCenter(viewNode);
        pageTitleLabel.setText(title);
    }

    // Navigation Routers
    public void loadDashboardOverview() {
        clearActiveStates();
        dashboardBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new DashboardOverviewView().getView(), "Dashboard Overview");
    }

    public void loadPortfolioView() {
        clearActiveStates();
        portfolioBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new PortfolioView().getView(), "Virtual Portfolio");
    }

    public void loadWatchlistView() {
        clearActiveStates();
        watchlistBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new WatchlistView().getView(), "Saved Watchlist");
    }

    public void loadNewsView() {
        clearActiveStates();
        newsBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new MarketNewsView().getView(), "Financial Market News");
    }

    public void loadAdminView() {
        clearActiveStates();
        adminBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new AdminDashboardView().getView(), "Admin Administration Panel");
    }

    public void loadProfileView() {
        clearActiveStates();
        profileBtn.getStyleClass().add("sidebar-btn-active");
        setCenterView(new ProfileView().getView(), "User Profile & API Config");
    }

    public void loadStockDetailView(String symbol) {
        clearActiveStates(); // Deselect sidebar as we are deep-linking to stock detail page
        setCenterView(new StockDetailView(symbol).getView(), "Stock Analysis: " + symbol.toUpperCase());
    }

    private void clearActiveStates() {
        dashboardBtn.getStyleClass().remove("sidebar-btn-active");
        portfolioBtn.getStyleClass().remove("sidebar-btn-active");
        watchlistBtn.getStyleClass().remove("sidebar-btn-active");
        newsBtn.getStyleClass().remove("sidebar-btn-active");
        adminBtn.getStyleClass().remove("sidebar-btn-active");
        profileBtn.getStyleClass().remove("sidebar-btn-active");
    }
}
