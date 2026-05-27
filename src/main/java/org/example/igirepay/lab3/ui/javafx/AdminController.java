package org.example.igirepay.lab3.ui.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.igirepay.lab2.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminController {

    @FXML private Label totalCustomersLabel;
    @FXML private Label totalTxLabel;
    @FXML private Label failedTxLabel;
    @FXML private TableView<ObservableList<String>> customersTable;
    @FXML private TableColumn<ObservableList<String>, String> idCol;
    @FXML private TableColumn<ObservableList<String>, String> nameCol;
    @FXML private TableColumn<ObservableList<String>, String> phoneCol;
    @FXML private TableColumn<ObservableList<String>, String> walletCol;
    @FXML private TableColumn<ObservableList<String>, String> statusCol;
    @FXML private TableColumn<ObservableList<String>, String> actionCol;
    @FXML private TextField searchField;

    @FXML
    public void initialize() {
        loadStats();
        loadCustomers(null);
    }

    private void loadStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement s1 = conn.prepareStatement("SELECT COUNT(*) FROM customers");
            ResultSet r1 = s1.executeQuery();
            if (r1.next()) totalCustomersLabel.setText(String.valueOf(r1.getInt(1)));

            PreparedStatement s2 = conn.prepareStatement("SELECT COUNT(*) FROM transactions");
            ResultSet r2 = s2.executeQuery();
            if (r2.next()) totalTxLabel.setText(String.valueOf(r2.getInt(1)));

            PreparedStatement s3 = conn.prepareStatement("SELECT COUNT(*) FROM transactions WHERE status = 'FAILED'");
            ResultSet r3 = s3.executeQuery();
            if (r3.next()) failedTxLabel.setText(String.valueOf(r3.getInt(1)));
        } catch (Exception e) {
            showError("Error loading stats: " + e.getMessage());
        }
    }

    private void loadCustomers(String keyword) {
        idCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        phoneCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        walletCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        actionCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        String sql = keyword == null ?
                "SELECT c.id, c.full_name, c.phone_number, COALESCE(a.balance, 0), a.is_locked " +
                "FROM customers c LEFT JOIN accounts a ON a.customer_id = c.id AND a.account_type = 'WALLET'" :
                "SELECT c.id, c.full_name, c.phone_number, COALESCE(a.balance, 0), a.is_locked " +
                "FROM customers c LEFT JOIN accounts a ON a.customer_id = c.id AND a.account_type = 'WALLET' " +
                "WHERE c.full_name ILIKE ? OR c.phone_number ILIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (keyword != null) {
                stmt.setString(1, "%" + keyword + "%");
                stmt.setString(2, "%" + keyword + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("id"));
                row.add(rs.getString("full_name"));
                row.add(rs.getString("phone_number"));
                row.add(String.format("%.0f RWF", rs.getDouble(4)));
                row.add(rs.getBoolean(5) ? "🔒 Locked" : "✅ Active");
                row.add("Manage");
                data.add(row);
            }
        } catch (Exception e) {
            showError("Error loading customers: " + e.getMessage());
        }
        customersTable.setItems(data);
    }

    @FXML public void showCustomers() { loadCustomers(null); }
    @FXML public void showAccounts() { loadCustomers(null); }
    @FXML public void showTransactions() { loadStats(); }
    @FXML public void showReports() { showInfo("Reports feature coming soon!"); }
    @FXML public void handleSearch() { loadCustomers(searchField.getText().trim()); }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/igirepay/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) totalCustomersLabel.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("IgirePay - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
