package org.example.igirepay.lab3.ui.javafx;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.igirepay.lab1.model.Account;
import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab1.model.FeeCalculator;
import org.example.igirepay.lab1.model.SavingsAccount;
import org.example.igirepay.lab1.model.WalletAccount;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.CustomerDAO;
import org.example.igirepay.lab2.dao.ProcessedRequestDAO;
import org.example.igirepay.lab2.dao.TransactionDAO;
import org.example.igirepay.lab2.db.DatabaseConnection;

import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import javafx.scene.control.ButtonType;

public class DashboardController {

    @FXML private Label userNameLabel;
    @FXML private Label userPhoneLabel;
    @FXML private Label walletBalanceLabel;
    @FXML private Label mokashBalanceLabel;
    @FXML private Label totalTxLabel;
    @FXML private TableView<ObservableList<String>> transactionsTable;
    @FXML private TableColumn<ObservableList<String>, String> typeCol;
    @FXML private TableColumn<ObservableList<String>, String> amountCol;
    @FXML private TableColumn<ObservableList<String>, String> feeCol;
    @FXML private TableColumn<ObservableList<String>, String> statusCol;
    @FXML private TableColumn<ObservableList<String>, String> descCol;
    @FXML private TableColumn<ObservableList<String>, String> dateCol;

    private Customer customer;
    private WalletAccount wallet;
    private SavingsAccount mokash;

    private final AccountDAO accountDAO = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();

    public void initData(Customer customer, WalletAccount wallet, SavingsAccount mokash) {
        this.customer = customer;
        this.wallet = wallet;
        this.mokash = mokash;
        refreshDashboard();
    }

    private void refreshDashboard() {
        userNameLabel.setText(customer.getFullName());
        userPhoneLabel.setText(customer.getPhoneNumber());
        double walletBal = accountDAO.getBalance(wallet.getAccountId());
        walletBalanceLabel.setText(String.format("%.0f RWF", walletBal));
        if (mokash != null) {
            double mokashBal = accountDAO.getBalance(mokash.getAccountId());
            mokashBalanceLabel.setText(String.format("%.0f RWF", mokashBal));
        } else {
            mokashBalanceLabel.setText("Not activated");
        }
        loadTransactions();
    }

    private void loadTransactions() {
        typeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(0)));
        amountCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(1)));
        feeCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(2)));
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(3)));
        descCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(4)));
        dateCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().get(5)));

        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        int count = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC LIMIT 20")) {
            stmt.setString(1, wallet.getAccountId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                count++;
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("transaction_type"));
                row.add(String.format("%.0f", rs.getDouble("amount")));
                row.add(String.format("%.0f", rs.getDouble("fee")));
                row.add(rs.getString("status"));
                row.add(rs.getString("description"));
                row.add(rs.getTimestamp("created_at").toString());
                data.add(row);
            }
        } catch (Exception e) {
            showError("Error loading transactions: " + e.getMessage());
        }
        transactionsTable.setItems(data);
        totalTxLabel.setText(String.valueOf(count));
    }

    @FXML
    public void handleDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Deposit");
        dialog.setHeaderText("Deposit to Wallet");
        dialog.setContentText("Amount (RWF):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
                String refId = "DEP-" + UUID.randomUUID();
                wallet.deposit(amount);
                accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                        refId, "DEPOSIT", amount, 0, "SUCCESS", "Deposit to wallet"
                );
                processedRequestDAO.saveProcessedRequest(refId);
                showSuccess("Deposited " + amount + " RWF successfully!");
                refreshDashboard();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    @FXML
    public void handleWithdraw() {
        String pin = PinDialogUtil.showPinDialog("PIN Verification", "Enter your PIN to withdraw");
        if (pin == null || pin.isEmpty()) return;
        if (!wallet.validatePin(pin)) {
            showError("✗ Incorrect PIN.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Withdraw");
        dialog.setHeaderText("Withdraw from Wallet");
        dialog.setContentText("Amount (RWF):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                double fee = FeeCalculator.getWithdrawFee(amount);
                wallet.withdraw(amount);
                accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
                String refId = "WIT-" + UUID.randomUUID();
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                        refId, "WITHDRAWAL", amount, fee, "SUCCESS", "Wallet withdrawal"
                );
                processedRequestDAO.saveProcessedRequest(refId);
                showSuccess("Withdrawn " + amount + " RWF (fee: " + fee + " RWF)");
                refreshDashboard();
            } catch (Exception e) {
                showError(e.getMessage());
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                        "WIT-ERR-" + UUID.randomUUID(), "WITHDRAWAL",
                        0, 0, "FAILED", e.getMessage()
                );
            }
        });
    }

    @FXML
    public void handleSend() {
        String pin = PinDialogUtil.showPinDialog("PIN Verification", "Enter your PIN to send money");
        if (pin == null || pin.isEmpty()) return;
        if (!wallet.validatePin(pin)) {
            showError("✗ Incorrect PIN.");
            return;
        }

        TextInputDialog recipientDialog = new TextInputDialog();
        recipientDialog.setTitle("Send Money");
        recipientDialog.setHeaderText("Local Transfer");
        recipientDialog.setContentText("Recipient phone (078/079 for MTN, 072/073 for Airtel):");
        Optional<String> recipientResult = recipientDialog.showAndWait();
        if (recipientResult.isEmpty()) return;
        String recipient = recipientResult.get().trim();


        if (!Customer.isValidPhoneNumber(recipient)) {
            showError("✗ Invalid number. Must be 078/079 (MTN) or 072/073 (Airtel).");
            return;
        }

        boolean isOnNet = recipient.startsWith("078") || recipient.startsWith("079");
        boolean isAirtel = recipient.startsWith("072") || recipient.startsWith("073");

        String recipientName;

        if (isOnNet) {

            CustomerDAO recipientDAO = new CustomerDAO();
            Customer recInfo = recipientDAO.getCustomerByPhone(recipient);
            if (recInfo == null) {
                showError("✗ Recipient " + recipient + " is not registered on IgirePay.");
                return;
            }
            recipientName = recInfo.getFullName();
        } else {

            recipientName = recipient;
        }


        double currentBalance = accountDAO.getBalance(wallet.getAccountId());
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Recipient Info");
        info.setHeaderText("Recipient and Your Balance");
        info.setContentText(
                "Recipient: " + recipientName + " (" + recipient + ")\n" +
                        "Network: " + (isOnNet ? "MTN (On-net)" : "Airtel (Off-net)") + "\n" +
                        "Your Wallet balance: " + String.format("%.0f RWF", currentBalance)
        );
        info.showAndWait();

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Send Money");
        amountDialog.setHeaderText("Amount");
        amountDialog.setContentText("Amount (RWF):");
        Optional<String> amountResult = amountDialog.showAndWait();
        amountResult.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                double fee = isOnNet ?
                        FeeCalculator.getSendOnNetFee(amount) :
                        FeeCalculator.getSendOffNetFee(amount);

                double projected = accountDAO.getBalance(wallet.getAccountId()) - (amount + fee);

                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Transfer");
                confirm.setHeaderText("Please confirm transfer details");
                confirm.setContentText(
                        "Recipient: " + recipientName + " (" + recipient + ")\n" +
                                "Network: " + (isOnNet ? "MTN On-net" : "Airtel Off-net") + "\n" +
                                "Amount: " + amount + " RWF\n" +
                                "Fee: " + fee + " RWF\n" +
                                "Total: " + (amount + fee) + " RWF\n" +
                                "Remaining balance after: " + String.format("%.0f RWF", projected)
                );
                Optional<ButtonType> conf = confirm.showAndWait();
                if (conf.isEmpty() || conf.get() != ButtonType.OK) return;

                wallet.sendMoneyLocal(amount, recipient);
                accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
                String refId = "SND-" + UUID.randomUUID();
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                        refId, "SEND_LOCAL", amount, fee, "SUCCESS",
                        "Sent to " + recipientName + " (" + recipient + ")"
                );
                processedRequestDAO.saveProcessedRequest(refId);
                showSuccess("✓ Sent " + amount + " RWF to " + recipientName +
                        " (" + recipient + ")\nFee: " + fee + " RWF");
                refreshDashboard();
            } catch (Exception e) {
                showError(e.getMessage());
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                        "SND-ERR-" + UUID.randomUUID(), "SEND_LOCAL",
                        0, 0, "FAILED", e.getMessage()
                );
            }
        });
    }

    @FXML public void showDashboard() { refreshDashboard(); }
    @FXML public void showTransactions() { loadTransactions(); }
    
    @FXML
    public void showMoKash() {
        if (mokash == null) {
            showError("MoKash not activated. Activate during account creation.");
            return;
        }
        
        Stage mokashStage = new Stage();
        mokashStage.setTitle("MoKash Savings");
        mokashStage.setWidth(500);
        mokashStage.setHeight(400);
        
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        root.setAlignment(Pos.TOP_CENTER);
        
        Label balanceLabel = new Label("Balance: " + accountDAO.getBalance(mokash.getAccountId()) + " RWF");
        balanceLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Button depositBtn = new Button("Deposit to MoKash");
        depositBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        depositBtn.setOnAction(e -> handleMoKashDeposit());
        
        Button withdrawBtn = new Button("Withdraw from MoKash");
        withdrawBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        withdrawBtn.setOnAction(e -> handleMoKashWithdraw());
        
        Button historyBtn = new Button("View Transaction History");
        historyBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        historyBtn.setOnAction(e -> handleMoKashHistory());
        
        Button interestBtn = new Button("Apply Interest");
        interestBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        interestBtn.setOnAction(e -> handleMoKashInterest());
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(depositBtn, withdrawBtn, historyBtn, interestBtn);
        
        root.getChildren().addAll(balanceLabel, buttonBox);
        
        ScrollPane scroll = new ScrollPane(root);
        mokashStage.setScene(new Scene(scroll));
        mokashStage.show();
    }
    
    private void handleMoKashDeposit() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MoKash Deposit");
        dialog.setHeaderText("Deposit from Wallet to MoKash");
        dialog.setContentText("Amount (RWF):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
                mokash.deposit(amount);
                wallet.withdraw(amount);
                accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
                accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
                String refId = "MKDEP-" + UUID.randomUUID();
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                        refId, "MOKASH_DEPOSIT", amount, 0, "SUCCESS", "Deposit from wallet"
                );
                processedRequestDAO.saveProcessedRequest(refId);
                showSuccess("Deposited " + amount + " RWF to MoKash!");
                refreshDashboard();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }
    
    private void handleMoKashWithdraw() {
        String pin = PinDialogUtil.showPinDialog("PIN Verification", "Enter your MoKash PIN");
        if (pin == null || pin.isEmpty()) return;
        if (!mokash.validatePin(pin)) {
            showError("✗ Incorrect MoKash PIN.");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MoKash Withdrawal");
        dialog.setHeaderText("Withdraw from MoKash to Wallet");
        dialog.setContentText("Amount (RWF):");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                mokash.withdraw(amount);
                wallet.deposit(amount);
                accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
                accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
                String refId = "MKWIT-" + UUID.randomUUID();
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                        refId, "MOKASH_WITHDRAWAL", amount, 0, "SUCCESS", "Withdrawal to wallet"
                );
                processedRequestDAO.saveProcessedRequest(refId);
                showSuccess("Withdrawn " + amount + " RWF from MoKash!");
                refreshDashboard();
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }
    
    private void handleMoKashHistory() {
        ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC LIMIT 50")) {
            stmt.setString(1, mokash.getAccountId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(rs.getString("transaction_type"));
                row.add(String.format("%.0f", rs.getDouble("amount")));
                row.add(rs.getString("status"));
                row.add(rs.getTimestamp("created_at").toString());
                data.add(row);
            }
        } catch (Exception e) {
            showError("Error loading history: " + e.getMessage());
        }
        
        Stage historyStage = new Stage();
        historyStage.setTitle("MoKash Transaction History");
        historyStage.setWidth(600);
        historyStage.setHeight(400);
        
        TableView<ObservableList<String>> table = new TableView<>();
        TableColumn<ObservableList<String>, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().get(0)));
        TableColumn<ObservableList<String>, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().get(1)));
        TableColumn<ObservableList<String>, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().get(2)));
        TableColumn<ObservableList<String>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(data1 -> new SimpleStringProperty(data1.getValue().get(3)));
        
        table.getColumns().addAll(typeCol, amountCol, statusCol, dateCol);
        table.setItems(data);
        
        historyStage.setScene(new Scene(table));
        historyStage.show();
    }
    
    private void handleMoKashInterest() {
        mokash.applyInterest();
        accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
        showSuccess("Interest applied successfully!");
        refreshDashboard();
    }
    
    @FXML
    public void showReports() {
        Stage reportStage = new Stage();
        reportStage.setTitle("Reports");
        reportStage.setWidth(500);
        reportStage.setHeight(300);
        
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        root.setAlignment(Pos.CENTER);
        
        Button walletReportBtn = new Button("Download Wallet Transactions (CSV)");
        walletReportBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 200;");
        walletReportBtn.setOnAction(e -> downloadReport(wallet.getAccountId(), "Wallet"));
        
        Button mokashReportBtn = new Button("Download MoKash Transactions (CSV)");
        mokashReportBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 200;");
        mokashReportBtn.setDisable(mokash == null);
        mokashReportBtn.setOnAction(e -> downloadReport(mokash.getAccountId(), "MoKash"));
        
        root.getChildren().addAll(walletReportBtn, mokashReportBtn);
        reportStage.setScene(new Scene(root));
        reportStage.show();
    }
    
    private void downloadReport(String accountId, String accountType) {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY created_at DESC";
        String fileName = accountType + "_transactions_" + accountId + "_" + LocalDate.now() + ".csv";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             FileWriter writer = new FileWriter(fileName)) {
            
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();
            writer.write("Transaction ID,Account ID,Reference ID,Type,Amount,Fee,Status,Description,Date\n");
            
            int count = 0;
            while (rs.next()) {
                count++;
                writer.write(
                        rs.getString("id") + "," +
                        rs.getString("account_id") + "," +
                        rs.getString("reference_id") + "," +
                        rs.getString("transaction_type") + "," +
                        rs.getDouble("amount") + "," +
                        rs.getDouble("fee") + "," +
                        rs.getString("status") + "," +
                        rs.getString("description") + "," +
                        rs.getTimestamp("created_at") + "\n"
                );
            }
            
            if (count == 0) {
                showError("No transactions found to export.");
                return;
            }
            
            showSuccess("✓ Report exported: " + fileName + " (" + count + " transactions)");
        } catch (Exception e) {
            showError("Error exporting report: " + e.getMessage());
        }
    }
    
    @FXML
    public void showSettings() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle("Settings");
        settingsStage.setWidth(400);
        settingsStage.setHeight(300);
        
        VBox root = new VBox(15);
        root.setStyle("-fx-padding: 20; -fx-background-color: #f5f5f5;");
        root.setAlignment(Pos.CENTER);
        
        Button changeWalletPinBtn = new Button("Change Wallet PIN");
        changeWalletPinBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        changeWalletPinBtn.setOnAction(e -> handleChangePin("Wallet", wallet));
        
        Button changeMoKashPinBtn = new Button("Change MoKash PIN");
        changeMoKashPinBtn.setStyle("-fx-font-size: 13; -fx-padding: 10; -fx-min-width: 150;");
        changeMoKashPinBtn.setDisable(mokash == null);
        changeMoKashPinBtn.setOnAction(e -> handleChangePin("MoKash", mokash));
        
        root.getChildren().addAll(changeWalletPinBtn, changeMoKashPinBtn);
        settingsStage.setScene(new Scene(root));
        settingsStage.show();
    }
    
    private void handleChangePin(String accountType, Account account) {
        String oldPin = PinDialogUtil.showPinDialog("Verify Current PIN", "Enter your current " + accountType + " PIN");
        if (oldPin == null || oldPin.isEmpty()) return;
        if (!account.validatePin(oldPin)) {
            showError("✗ Incorrect current PIN.");
            return;
        }
        String newPin = PinDialogUtil.showPinDialog("New PIN", "Enter your new 5-digit PIN");
        if (newPin == null || !Customer.isValidPin(newPin)) {
            showError("✗ PIN must be exactly 5 digits.");
            return;
        }
        account.changePin(oldPin, newPin);
        showSuccess("✓ " + accountType + " PIN changed successfully!");
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/igirepay/login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(new Scene(root, 900, 620));
            stage.setTitle("IgirePay - Login");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Logout error: " + e.getMessage());
        }
    }

    private void showSuccess(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

