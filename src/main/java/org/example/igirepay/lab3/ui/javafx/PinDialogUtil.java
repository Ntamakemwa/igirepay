package org.example.igirepay.lab3.ui.javafx;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PinDialogUtil {

    public static String showPinDialog(String title, String prompt) {
        final String[] result = {null};
        
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setWidth(400);
        dialog.setHeight(250);

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1;");
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label promptLabel = new Label(prompt);
        promptLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #34495e; -fx-wrap-text: true;");

        PasswordField pinField = new PasswordField();
        pinField.setStyle("-fx-font-size: 14; -fx-padding: 8; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 3; -fx-font-family: 'Monospaced';");
        pinField.setPromptText("Enter 5-digit PIN");
        pinField.setPrefWidth(300);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        Button okButton = new Button("OK");
        okButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #229954; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));
        okButton.setOnMouseExited(e -> okButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));

        Button cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #c0392b; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));
        cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));

        okButton.setOnAction(e -> {
            result[0] = pinField.getText();
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        pinField.setOnKeyPressed(e -> {
            if (e.getCode().toString().equals("ENTER")) {
                result[0] = pinField.getText();
                dialog.close();
            }
        });

        buttonBox.getChildren().addAll(okButton, cancelButton);
        root.getChildren().addAll(titleLabel, promptLabel, pinField, buttonBox);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }

    public static boolean showConfirmDialog(String title, String message) {
        final boolean[] result = {false};
        
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(title);
        dialog.setResizable(false);
        dialog.setWidth(400);
        dialog.setHeight(200);

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 20;");
        root.setAlignment(Pos.CENTER);

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #34495e; -fx-wrap-text: true;");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button yesButton = new Button("Yes");
        yesButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        yesButton.setOnMouseEntered(e -> yesButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #229954; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));
        yesButton.setOnMouseExited(e -> yesButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));

        Button noButton = new Button("No");
        noButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;");
        noButton.setOnMouseEntered(e -> noButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #c0392b; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));
        noButton.setOnMouseExited(e -> noButton.setStyle("-fx-font-size: 12; -fx-padding: 8 30; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 3; -fx-cursor: hand;"));

        yesButton.setOnAction(e -> {
            result[0] = true;
            dialog.close();
        });

        noButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(yesButton, noButton);
        root.getChildren().addAll(messageLabel, buttonBox);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();

        return result[0];
    }
}
