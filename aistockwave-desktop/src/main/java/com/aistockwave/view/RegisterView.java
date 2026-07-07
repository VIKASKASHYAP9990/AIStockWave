package com.aistockwave.view;

import com.aistockwave.App;
import com.aistockwave.service.AuthService;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RegisterView {
    private final StackPane container;
    private final AuthService authService = new AuthService();

    public RegisterView() {
        container = new StackPane();
        container.getStyleClass().add("root");

        // Centered Card Container
        VBox card = new VBox(15);
        card.getStyleClass().add("card");
        card.setMaxSize(420, 540);
        card.setPadding(new Insets(25, 40, 30, 40));
        card.setAlignment(Pos.CENTER);

        // Header Branding
        Label brandLabel = new Label("AIStockWave");
        brandLabel.getStyleClass().add("sidebar-brand");
        brandLabel.setStyle("-fx-font-size: 28; -fx-padding: 0 0 5 0;");

        Label titleLabel = new Label("Create an Account");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Register to configure watchlists, track portfolios, and perform stock audits.");
        subtitleLabel.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 12; -fx-text-alignment: center;");
        subtitleLabel.setWrapText(true);

        // Form Fields
        VBox form = new VBox(10);
        form.setPadding(new Insets(10, 0, 10, 0));
        form.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label("Full Name");
        nameLabel.getStyleClass().add("lbl-caption");
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your full name");

        Label emailLabel = new Label("Email Address");
        emailLabel.getStyleClass().add("lbl-caption");
        TextField emailField = new TextField();
        emailField.setPromptText("Enter your email address");

        Label passLabel = new Label("Password");
        passLabel.getStyleClass().add("lbl-caption");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Create a password");

        Label confirmLabel = new Label("Confirm Password");
        confirmLabel.getStyleClass().add("lbl-caption");
        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Confirm your password");

        form.getChildren().addAll(nameLabel, nameField, emailLabel, emailField, passLabel, passField, confirmLabel, confirmField);

        // Feedback Label
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 12;");
        feedbackLabel.setWrapText(true);

        // Buttons
        Button signupBtn = new Button("Register");
        signupBtn.getStyleClass().add("btn-primary");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER);
        Label footerLabel = new Label("Already have an account?");
        footerLabel.setStyle("-fx-text-fill: #B2B5BE; -fx-font-size: 13;");
        Button loginLink = new Button("Log In");
        loginLink.setStyle("-fx-background-color: transparent; -fx-text-fill: #2962FF; -fx-font-weight: bold; -fx-padding: 0; -fx-cursor: hand;");
        loginLink.setOnAction(e -> App.showLoginView());

        footer.getChildren().addAll(footerLabel, loginLink);

        // Action Handlers
        signupBtn.setOnAction(e -> {
            feedbackLabel.setText("");
            feedbackLabel.setStyle("-fx-text-fill: #EF5350; -fx-font-size: 12;");

            String name = nameField.getText();
            String email = emailField.getText();
            String password = passField.getText();
            String confirm = confirmField.getText();

            if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty() || confirm.trim().isEmpty()) {
                feedbackLabel.setText("Please fill in all input fields.");
                return;
            }

            if (!password.equals(confirm)) {
                feedbackLabel.setText("Passwords do not match.");
                return;
            }

            if (password.length() < 6) {
                feedbackLabel.setText("Password must be at least 6 characters.");
                return;
            }

            boolean registered = authService.register(name, email, password);
            if (registered) {
                feedbackLabel.setStyle("-fx-text-fill: #26A69A; -fx-font-size: 12;");
                feedbackLabel.setText("Registration successful! Redirecting to login...");
                
                // Disable inputs
                signupBtn.setDisable(true);
                nameField.setDisable(true);
                emailField.setDisable(true);
                passField.setDisable(true);
                confirmField.setDisable(true);

                PauseTransition delay = new PauseTransition(Duration.seconds(2));
                delay.setOnFinished(event -> App.showLoginView());
                delay.play();
            } else {
                feedbackLabel.setText("Registration failed. Email may already be in use.");
            }
        });

        card.getChildren().addAll(brandLabel, titleLabel, subtitleLabel, form, feedbackLabel, signupBtn, footer);
        container.getChildren().add(card);
    }

    public StackPane getView() {
        return container;
    }
}
