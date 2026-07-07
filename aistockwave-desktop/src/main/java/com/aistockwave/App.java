package com.aistockwave;

import com.aistockwave.database.DatabaseInitializer;
import com.aistockwave.service.FinancialApiService;
import com.aistockwave.view.LoginView;
import com.aistockwave.view.MainDashboardView;
import com.aistockwave.view.RegisterView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static Stage primaryStage;
    private static final FinancialApiService apiService = new FinancialApiService();

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("AIStockWave – AI-Powered Paper Trading Platform");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);

        // 1. Setup Database
        DatabaseInitializer.initializeDatabase();

        // 2. Start Stock Price Generator Thread
        apiService.startSimulator();

        // 3. Render Login Screen
        showLoginView();
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Shutdown simulator cleanly
        apiService.stopSimulator();
    }

    public static FinancialApiService getApiService() {
        return apiService;
    }

    public static void showLoginView() {
        LoginView loginView = new LoginView();
        Scene scene = new Scene(loginView.getView(), 1050, 680);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public static void showRegisterView() {
        RegisterView registerView = new RegisterView();
        Scene scene = new Scene(registerView.getView(), 1050, 680);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
    }

    public static void showMainDashboardView() {
        MainDashboardView dashboardView = new MainDashboardView();
        Scene scene = new Scene(dashboardView.getView(), 1200, 750);
        applyStylesheet(scene);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    private static void applyStylesheet(Scene scene) {
        String css = App.class.getClassLoader().getResource("css/styles.css").toExternalForm();
        scene.getStylesheets().add(css);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
