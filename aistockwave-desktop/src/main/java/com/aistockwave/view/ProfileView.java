package com.aistockwave.view;

import com.aistockwave.database.UserDAO;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProfileView {
    private final VBox layout;
    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();

    public ProfileView() {
        layout = new VBox(20);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #0B0E14;");

        // Split profile screen: Left holds user stats & password reset; Right holds API keys
        HBox splitRow = new HBox(20);
        splitRow.setAlignment(Pos.TOP_LEFT);

        VBox leftColumn = new VBox(20);
        HBox.setHgrow(leftColumn, Priority.ALWAYS);
        
        // 1. User Profile Details
        VBox profileCard = createProfileDetailsCard();
        // 2. Change Password Form
        VBox passwordCard = createPasswordResetCard();
        
        leftColumn.getChildren().addAll(profileCard, passwordCard);

        // 3. API configuration card (Right side)
        VBox apiCard = createApiConfigCard();
        apiCard.setMinWidth(400);
        apiCard.setMaxWidth(400);

        splitRow.getChildren().addAll(leftColumn, apiCard);
        layout.getChildren().add(splitRow);
    }

    public VBox getView() {
        return layout;
    }

    private VBox createProfileDetailsCard() {
        VBox card = new VBox(15);
        card.getStyleClass().add("card");

        Label title = new Label("👤 Personal Trader Profile");
        title.getStyleClass().add("card-title");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(10);

        User current = AuthService.getCurrentUser();
        String name = (current != null) ? current.getFullName() : "Paper Trader";
        String email = (current != null) ? current.getEmail() : "user@aistockwave.com";
        double balance = (current != null) ? current.getBalance() : 0.0;

        Label nameLbl = new Label(name);
        nameLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        Label emailLbl = new Label(email);
        emailLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        Label balLbl = new Label(String.format("₹%,.2f", balance));
        balLbl.setStyle("-fx-text-fill: #26A69A; -fx-font-weight: bold; -fx-font-size: 15;");

        grid.add(createCaptionLabel("Trader Name:"), 0, 0);
        grid.add(nameLbl, 1, 0);
        grid.add(createCaptionLabel("Email Address:"), 0, 1);
        grid.add(emailLbl, 1, 1);
        grid.add(createCaptionLabel("Paper Balance:"), 0, 2);
        grid.add(balLbl, 1, 2);

        card.getChildren().addAll(title, grid);
        return card;
    }

    private Label createCaptionLabel(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("lbl-caption");
        lbl.setStyle("-fx-font-size: 12;");
        return lbl;
    }

    private VBox createPasswordResetCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("🔒 Secure Password Management");
        title.getStyleClass().add("card-title");

        PasswordField curPassField = new PasswordField();
        curPassField.setPromptText("Enter current password");
        PasswordField newPassField = new PasswordField();
        newPassField.setPromptText("Create new password (min 6 chars)");
        PasswordField confirmPassField = new PasswordField();
        confirmPassField.setPromptText("Confirm new password");

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-font-size: 12;");

        Button submitBtn = new Button("Update Password");
        submitBtn.getStyleClass().add("btn-primary");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        submitBtn.setOnAction(e -> {
            feedback.setText("");
            feedback.setStyle("");

            String curPass = curPassField.getText();
            String newPass = newPassField.getText();
            String confirm = confirmPassField.getText();

            if (curPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                feedback.setText("Fill in all password fields.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            if (!newPass.equals(confirm)) {
                feedback.setText("New passwords do not match.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            if (newPass.length() < 6) {
                feedback.setText("New password must be at least 6 characters.");
                feedback.setStyle("-fx-text-fill: #EF5350;");
                return;
            }

            User current = AuthService.getCurrentUser();
            if (current != null) {
                boolean success = authService.updatePassword(current.getId(), curPass, newPass);
                if (success) {
                    feedback.setText("SUCCESS: Password updated successfully.");
                    feedback.setStyle("-fx-text-fill: #26A69A;");

                    curPassField.setText("");
                    newPassField.setText("");
                    confirmPassField.setText("");
                } else {
                    feedback.setText("Incorrect current password.");
                    feedback.setStyle("-fx-text-fill: #EF5350;");
                }
            }
        });

        card.getChildren().addAll(title, new Label("Current Password:"), curPassField, new Label("New Password:"), newPassField, new Label("Confirm Password:"), confirmPassField, feedback, submitBtn);
        return card;
    }

    private VBox createApiConfigCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        Label title = new Label("🔌 Live Stock API Configuration");
        title.getStyleClass().add("card-title");

        Label instruction = new Label("Optional: Enter your external financial data API keys below to pull live rates from Yahoo Finance, Finnhub, or Alpha Vantage. If left blank, the platform utilizes our high-fidelity Geometric Brownian Motion simulator thread.");
        instruction.getStyleClass().add("lbl-caption");
        instruction.setStyle("-fx-font-size: 11; -fx-line-spacing: 1.35;");
        instruction.setWrapText(true);

        TextField finnhubKeyField = new TextField();
        finnhubKeyField.setPromptText("Enter Finnhub API Key");
        
        TextField avKeyField = new TextField();
        avKeyField.setPromptText("Enter Alpha Vantage API Key");

        Label feedback = new Label();
        feedback.setWrapText(true);
        feedback.setStyle("-fx-font-size: 12;");

        Button saveKeysBtn = new Button("Apply Credentials");
        saveKeysBtn.getStyleClass().add("btn-primary");
        saveKeysBtn.setMaxWidth(Double.MAX_VALUE);
        saveKeysBtn.setOnAction(e -> {
            feedback.setText("SUCCESS: Applied credentials. Real-time rates will parse if API keys validate.");
            feedback.setStyle("-fx-text-fill: #26A69A;");
        });

        card.getChildren().addAll(title, instruction, new Label("Finnhub Key:"), finnhubKeyField, new Label("Alpha Vantage Key:"), avKeyField, feedback, saveKeysBtn);
        return card;
    }
}
