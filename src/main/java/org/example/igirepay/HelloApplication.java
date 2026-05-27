package org.example.igirepay;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.igirepay.lab2.db.DatabaseConnection;

public class HelloApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        if (!DatabaseConnection.testConnection()) {
            System.out.println("✗ Cannot connect to database. Check DB_URL, DB_USER, DB_PASSWORD env vars.");
        }
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/org/example/igirepay/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 900, 600);
        stage.setTitle("IgirePay - Digital Wallet System");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}