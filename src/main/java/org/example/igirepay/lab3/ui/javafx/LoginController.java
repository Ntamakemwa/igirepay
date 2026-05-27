package org.example.igirepay.lab3.ui.javafx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab1.model.Language;
import org.example.igirepay.lab1.model.SavingsAccount;
import org.example.igirepay.lab1.model.WalletAccount;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.CustomerDAO;
import org.example.igirepay.lab3.auth.AuthService;
import org.example.igirepay.lab3.auth.PinHasher;
import org.example.igirepay.lab3.auth.Role;

public class LoginController {

    @FXML private TextField phoneField;
    @FXML private TextField nameField;
    @FXML private PasswordField pinField;
    @FXML private Label errorLabel;
    @FXML private Label titleLabel;
    @FXML private Label nameLabel;
    @FXML private Label phoneLabel;
    @FXML private Label pinLabel;
    @FXML private ComboBox<String> languageCombo;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Button switchButton;
    @FXML private VBox nameBox;

    private final AuthService authService = new AuthService();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountDAO accountDAO = new AccountDAO();

    private boolean isRegisterMode = false;

    @FXML
    public void initialize() {
        languageCombo.getItems().addAll("English", "Kinyarwanda");
        languageCombo.setValue("English");
        languageCombo.setOnAction(e -> applyLanguage());
        errorLabel.setText("");
        nameBox.setVisible(false);
        nameBox.setManaged(false);
        registerButton.setVisible(false);
        registerButton.setManaged(false);
        switchButton.setText("Don't have an account? Register");
    }

    private void applyLanguage() {
        if (languageCombo.getValue().equals("Kinyarwanda")) {
            Language.setLanguage(Language.Lang.RW);
            titleLabel.setText("Murakaza neza IgirePay");
            phoneLabel.setText("Nimero ya Telefoni");
            pinLabel.setText("PIN");
            nameLabel.setText("Amazina Yose");
            loginButton.setText("Injira");
            registerButton.setText("Fungura Konti");
            phoneField.setPromptText("078XXXXXXX");
            pinField.setPromptText("Injiza PIN y'imibare 5");
            nameField.setPromptText("Injiza amazina yose");
            switchButton.setText(isRegisterMode ?
                    "Usanzwe ufite konti? Injira" :
                    "Nta konti? Fungura konti");
        } else {
            Language.setLanguage(Language.Lang.EN);
            titleLabel.setText("Welcome to IgirePay");
            phoneLabel.setText("Phone Number");
            pinLabel.setText("PIN");
            nameLabel.setText("Full Name");
            loginButton.setText("Login");
            registerButton.setText("Create Account");
            phoneField.setPromptText("078XXXXXXX");
            pinField.setPromptText("Enter 5-digit PIN");
            nameField.setPromptText("Enter your full name");
            switchButton.setText(isRegisterMode ?
                    "Already have an account? Login" :
                    "Don't have an account? Register");
        }
    }

    @FXML
    public void handleSwitch() {
        isRegisterMode = !isRegisterMode;
        errorLabel.setText("");
        if (isRegisterMode) {
            nameBox.setVisible(true);
            nameBox.setManaged(true);
            registerButton.setVisible(true);
            registerButton.setManaged(true);
            loginButton.setVisible(false);
            loginButton.setManaged(false);
            titleLabel.setText(languageCombo.getValue().equals("Kinyarwanda") ?
                    "Fungura Konti Nshya" : "Create New Account");
            switchButton.setText(languageCombo.getValue().equals("Kinyarwanda") ?
                    "Usanzwe ufite konti? Injira" : "Already have an account? Login");
        } else {
            nameBox.setVisible(false);
            nameBox.setManaged(false);
            registerButton.setVisible(false);
            registerButton.setManaged(false);
            loginButton.setVisible(true);
            loginButton.setManaged(true);
            titleLabel.setText(languageCombo.getValue().equals("Kinyarwanda") ?
                    "Murakaza neza IgirePay" : "Welcome to IgirePay");
            switchButton.setText(languageCombo.getValue().equals("Kinyarwanda") ?
                    "Nta konti? Fungura konti" : "Don't have an account? Register");
        }
    }

    @FXML
    public void handleLogin() {
        String phone = phoneField.getText().trim();
        String pin = pinField.getText().trim();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("");

        boolean isLocalNumber = Customer.isLocalNumber(phone);
        boolean isInternational = Customer.isValidInternationalNumber(phone);

        if (!isLocalNumber && !isInternational) {
            errorLabel.setText("✗ Invalid phone number. Use local (078/079) or international (+country code)");
            return;
        }
        if (!Customer.isValidPin(pin)) {
            errorLabel.setText(Language.get("invalid_pin"));
            return;
        }

        if (authService.isAdmin(phone)) {
            if (authService.authenticateAdmin(phone, pin)) {
                openAdminDashboard();
            } else {
                errorLabel.setText("✗ Incorrect admin PIN.");
            }
            return;
        }

        if (!customerDAO.customerExists(phone)) {
            errorLabel.setText("✗ Account not found. Please register.");
            return;
        }

        if (authService.authenticateUser(phone, pin)) {
            Customer customer = customerDAO.getCustomerByPhone(phone);
            double walletBalance = accountDAO.getBalance("W-" + phone);
            String walletPin = accountDAO.getPin("W-" + phone);
            WalletAccount wallet = new WalletAccount("W-" + phone, walletBalance, walletPin);
            customer.addAccount(wallet);
            SavingsAccount mokash = null;
            if (accountDAO.accountExists("MK-" + phone)) {
                double mokashBalance = accountDAO.getBalance("MK-" + phone);
                String mokashPin = accountDAO.getPin("MK-" + phone);
                mokash = new SavingsAccount("MK-" + phone, mokashBalance, mokashPin);
                customer.addAccount(mokash);
            }
            openUserDashboard(customer, wallet, mokash);
        } else {
            errorLabel.setText("✗ Incorrect PIN. Please try again.");
        }
    }

    @FXML
    public void handleRegister() {
        String phone = phoneField.getText().trim();
        String pin = pinField.getText().trim();
        String name = nameField.getText().trim();
        errorLabel.setStyle("-fx-text-fill: #e74c3c;");
        errorLabel.setText("");

        if (name.isEmpty()) {
            errorLabel.setText("✗ Please enter your full name.");
            return;
        }

        boolean isLocalNumber = Customer.isLocalNumber(phone);
        boolean isInternational = Customer.isValidInternationalNumber(phone);

        if (!isLocalNumber && !isInternational) {
            errorLabel.setText("✗ Invalid phone number. Use local (078/079) or international (+country code)");
            return;
        }
        if (!Customer.isValidPin(pin)) {
            errorLabel.setText(Language.get("invalid_pin"));
            return;
        }
        if (customerDAO.customerExists(phone)) {
            errorLabel.setText("✗ Account already exists. Please login.");
            return;
        }

        Customer customer = new Customer("C-" + phone, name, phone);
        customerDAO.createCustomer(customer);
        String hashedPin = PinHasher.hash(pin);
        accountDAO.createAccount("W-" + phone, "C-" + phone, "WALLET", 0, hashedPin);
        
        // Ask about MoKash activation only for local numbers
        if (isLocalNumber) {
            boolean activateMokash = PinDialogUtil.showConfirmDialog("MoKash Activation", 
                    "Would you like to activate MoKash (Savings Account)?");
            
            if (activateMokash) {
                String mokashPin = PinDialogUtil.showPinDialog("MoKash PIN", 
                        "Enter a 5-digit PIN for your MoKash account");
                if (mokashPin != null && Customer.isValidPin(mokashPin)) {
                    String hashedMokashPin = PinHasher.hash(mokashPin);
                    accountDAO.createAccount("MK-" + phone, "C-" + phone, "MOKASH", 0, hashedMokashPin);
                    errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13;");
                    errorLabel.setText("✓ Account created with MoKash activated! Please login.");
                } else {
                    errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13;");
                    errorLabel.setText("✓ Account created without MoKash. Please login.");
                }
            } else {
                errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13;");
                errorLabel.setText("✓ Account created successfully! Please login.");
            }
        } else {
            errorLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 13;");
            errorLabel.setText("✓ International account created (Wallet only). Please login.");
        }
        
        handleSwitch();
    }

    private void openUserDashboard(Customer customer, WalletAccount wallet, SavingsAccount mokash) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/igirepay/dashboard-view.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.initData(customer, wallet, mokash);
            Stage stage = (Stage) phoneField.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("IgirePay - Dashboard");
        } catch (Exception e) {
            errorLabel.setText("✗ Error: " + e.getMessage());
        }
    }

    private void openAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/igirepay/admin-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) phoneField.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(new Scene(root, 1100, 700));
            stage.setTitle("IgirePay - Admin Panel");
        } catch (Exception e) {
            errorLabel.setText("✗ Error: " + e.getMessage());
        }
    }
}