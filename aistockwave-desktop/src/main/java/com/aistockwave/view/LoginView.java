package com.aistockwave.view;

import com.aistockwave.App;
import com.aistockwave.model.User;
import com.aistockwave.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginView {
    private final StackPane container;
    private final AuthService authService = new AuthService();

    public LoginView() {
        container = new StackPane();
        container.getStyleClass().add("root");

        // Centered Card Container
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setMaxSize(420, 480);
        card.setPadding(new Insets(30, 40, 35, 40));
        card.setAlignment(Pos.CENTER);

        // Header Branding
        Label brandLabel = new Label("AIStockWave");
        brandLabel.getStyleClass().add("sidebar-brand");
        brandLabel.setStyle("-fx-font-size: 28; -fx-padding: 0 0 5 0;");

        Label titleLabel = new Label("Login to Your Account");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Simulate paper trading with ₹100,000 of virtual money.");
        subtitleLabel.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 12; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);

        // Form Fields
        VBox form = new VBox(12);
        form.setPadding(new Insets(15, 0, 10, 0));
        form.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label("Email Address");
        emailLabel.getStyleClass().add("lbl-caption");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("lbl-caption");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");

        form.getChildren().addAll(emailLabel, emailField, passLabel, passField);

        // Feedback Label
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 12;");
        feedbackLabel.setWrapText(true);

        // Buttons
        Button loginBtn = new Button("Login");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label footerLabel = new Label("Don't have an account?");
        footerLabel.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 13;");
        Button registerLink = new Button("Sign Up");
        registerLink.setStyle("-fx-background-color: transparent; -fx-text-fill: #2962FF; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
        registerLink.setOnAction(e -> App.showRegisterView());

        footer.getChildren().addAll(footerLabel, registerLink);

        // Action Handlers
        loginBtn.setOnAction(e -> {
            feedbackLabel.setText("");
            String email = emailField.getText();
            String password = passField.getText();

            if (email.trim().isEmpty() || password.trim().isEmpty()) {
                feedbackLabel.setText("Please fill in all fields.");
                return;
            }

            User loggedIn = authService.login(email, password);
            if (loggedIn != null) {
                App.showMainDashboardView();
            } else {
                feedbackLabel.setText("Invalid email address or password.");
            }
        });

        // Set default trigger on Enter key press
        passField.setOnAction(e -> loginBtn.fire());
        emailField.setOnAction(e -> loginBtn.fire());

        card.getChildren().addAll(brandLabel, titleLabel, subtitleLabel, form, feedbackLabel, loginBtn, footer);
        container.getChildren().add(card);
    }

    public StackPane getView() {
        return container;
    }
}
