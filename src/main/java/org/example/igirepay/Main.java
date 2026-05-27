package org.example.igirepay;

import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab1.model.FeeCalculator;
import org.example.igirepay.lab1.model.Language;
import org.example.igirepay.lab1.model.SavingsAccount;
import org.example.igirepay.lab1.model.WalletAccount;
import org.example.igirepay.lab2.dao.AccountDAO;
import org.example.igirepay.lab2.dao.CustomerDAO;
import org.example.igirepay.lab2.dao.ProcessedRequestDAO;
import org.example.igirepay.lab2.dao.TransactionDAO;
import org.example.igirepay.lab2.db.DatabaseConnection;
import org.example.igirepay.lab3.reports.ReportGenerator;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    static Scanner scanner = new Scanner(System.in);
    static CustomerDAO customerDAO = new CustomerDAO();
    static AccountDAO accountDAO = new AccountDAO();
    static TransactionDAO transactionDAO = new TransactionDAO();
    static ProcessedRequestDAO processedRequestDAO = new ProcessedRequestDAO();
    static ReportGenerator reportGenerator = new ReportGenerator();

    public static void main(String[] args) {

        try {
            DatabaseConnection.getConnection();
        } catch (SQLException e) {
            System.out.println("✗ Cannot connect to database. Exiting.");
            return;
        }

        selectLanguage();

        System.out.println(Language.get("welcome"));
        System.out.println(Language.get("app_name"));
        System.out.println(Language.get("welcome") + "\n");

        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.print(Language.get("choose"));
        String startChoice = scanner.nextLine().trim();

        Customer customer = null;
        WalletAccount wallet = null;
        SavingsAccount mokash = null;

        if (startChoice.equals("1")) {
            customer = registerCustomer();
            if (customer == null) return;

            String walletPin = getPin(Language.get("create_pin"));
            wallet = new WalletAccount("W-" + customer.getPhoneNumber(), 0, walletPin);
            customer.addAccount(wallet);

            customerDAO.createCustomer(customer);
            accountDAO.createAccount(
                    wallet.getAccountId(),
                    customer.getCustomerId(),
                    "WALLET", 0, walletPin
            );
            System.out.println(Language.get("wallet_created") + "\n");

            System.out.print(Language.get("mokash_ask"));
            String moKashChoice = scanner.nextLine().trim().toLowerCase();
            if (moKashChoice.equals("yes") || moKashChoice.equals("yego")) {
                String mokashPin = getPin(Language.get("mokash_pin"));
                mokash = new SavingsAccount("MK-" + customer.getPhoneNumber(), 0, mokashPin);
                customer.addAccount(mokash);
                accountDAO.createAccount(
                        mokash.getAccountId(),
                        customer.getCustomerId(),
                        "MOKASH", 0, mokashPin
                );
                System.out.println(Language.get("mokash_activated") + "\n");
            }

        } else if (startChoice.equals("2")) {
            System.out.print(Language.get("phone_prompt"));
            String phone = scanner.nextLine().trim();
            if (!Customer.isValidPhoneNumber(phone)) {
                System.out.println(Language.get("invalid_phone"));
                return;
            }
            customer = customerDAO.getCustomerByPhone(phone);
            if (customer == null) {
                System.out.println("✗ Account not found.");
                System.out.println("1. Register now");
                System.out.println("0. Exit");
                System.out.print(Language.get("choose"));
                String retry = scanner.nextLine().trim();
                if (retry.equals("1")) {
                    customer = registerCustomer();
                    if (customer == null) return;
                    String walletPin = getPin(Language.get("create_pin"));
                    wallet = new WalletAccount("W-" + customer.getPhoneNumber(), 0, walletPin);
                    customer.addAccount(wallet);
                    customerDAO.createCustomer(customer);
                    accountDAO.createAccount(wallet.getAccountId(), customer.getCustomerId(), "WALLET", 0, walletPin);
                    System.out.println(Language.get("wallet_created") + "\n");
                    System.out.print(Language.get("mokash_ask"));
                    String moKashChoice = scanner.nextLine().trim().toLowerCase();
                    if (moKashChoice.equals("yes") || moKashChoice.equals("yego")) {
                        String mokashPin = getPin(Language.get("mokash_pin"));
                        mokash = new SavingsAccount("MK-" + customer.getPhoneNumber(), 0, mokashPin);
                        customer.addAccount(mokash);
                        accountDAO.createAccount(mokash.getAccountId(), customer.getCustomerId(), "MOKASH", 0, mokashPin);
                        System.out.println(Language.get("mokash_activated") + "\n");
                    }
                } else {
                    return;
                }
            } else {
                double walletBalance = accountDAO.getBalance("W-" + phone);
                String walletPin = accountDAO.getPin("W-" + phone);
                wallet = new WalletAccount("W-" + phone, walletBalance, walletPin);
                customer.addAccount(wallet);

                if (accountDAO.accountExists("MK-" + phone)) {
                    double mokashBalance = accountDAO.getBalance("MK-" + phone);
                    String mokashPin = accountDAO.getPin("MK-" + phone);
                    mokash = new SavingsAccount("MK-" + phone, mokashBalance, mokashPin);
                    customer.addAccount(mokash);
                }
            }
        } else {
            System.out.println(Language.get("invalid_option"));
            return;
        }

        System.out.println(Language.get("login"));
        boolean loggedIn = false;
        int attempts = 0;
        while (attempts < 3) {
            String pin = getPin(Language.get("enter_pin"));
            if (wallet.validatePin(pin)) {
                loggedIn = true;
                customer.setLoggedIn(true);
                accountDAO.updateLockStatus(wallet.getAccountId(), false, 0);
                System.out.println(Language.get("login_success") + customer.getFullName() + "!");
                break;
            }
            attempts++;
            accountDAO.updateLockStatus(wallet.getAccountId(), attempts >= 3, attempts);
            System.out.println(Language.get("wrong_pin") + attempts + "/3");
        }

        if (!loggedIn) {
            System.out.println(Language.get("account_locked"));
            return;
        }

        boolean running = true;
        while (running) {
            printMainMenu(mokash != null);
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": handleDeposit(wallet, customer); break;
                case "2": handleWithdraw(wallet, customer); break;
                case "3": handleSendLocal(wallet, customer); break;
                case "4": handleSendInternational(wallet, customer); break;
                case "5":
                    double bal = accountDAO.getBalance(wallet.getAccountId());
                    System.out.println("\n💰 Wallet: " + bal + " RWF");
                    break;
                case "6":
                    if (mokash != null) mokashMenu(mokash, wallet, customer);
                    else System.out.println(Language.get("mokash_not_active"));
                    break;
                case "7":
                    transactionDAO.printFailedTransactions(wallet.getAccountId());
                    break;
                case "8": selectLanguage(); break;
                case "9": printReportsMenu(wallet, customer); break;
                case "0":
                    System.out.println(Language.get("goodbye"));
                    DatabaseConnection.closeConnection();
                    running = false;
                    break;
                default:
                    System.out.println(Language.get("invalid_option"));
            }
        }
    }

    static void mokashMenu(SavingsAccount mokash, WalletAccount wallet, Customer customer) {
        boolean inMoKash = true;
        while (inMoKash) {
            System.out.println("\n" + Language.get("mokash_menu_title"));
            System.out.println(Language.get("mokash_deposit"));
            System.out.println(Language.get("mokash_withdraw"));
            System.out.println(Language.get("mokash_balance"));
            System.out.println(Language.get("mokash_history"));
            System.out.println(Language.get("mokash_interest"));
            System.out.println(Language.get("back"));
            System.out.print(Language.get("choose"));

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": handleMoKashDeposit(wallet, mokash, customer); break;
                case "2": handleMoKashWithdraw(mokash, wallet, customer); break;
                case "3":
                    double bal = accountDAO.getBalance(mokash.getAccountId());
                    System.out.println("\n💰 MoKash: " + bal + " RWF");
                    break;
                case "4":
                    transactionDAO.printTransactionHistory(mokash.getAccountId());
                    break;
                case "5":
                    mokash.applyInterest();
                    accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
                    break;
                case "0": inMoKash = false; break;
                default: System.out.println(Language.get("invalid_option"));
            }
        }
    }

    static void printMainMenu(boolean hasMoKash) {
        System.out.println("\n" + Language.get("main_menu"));
        System.out.println(Language.get("deposit"));
        System.out.println(Language.get("withdraw"));
        System.out.println(Language.get("send_local"));
        System.out.println(Language.get("send_intl"));
        System.out.println(Language.get("check_balance"));
        if (hasMoKash) System.out.println(Language.get("mokash_menu"));
        System.out.println(Language.get("failed_tx"));
        System.out.println(Language.get("change_lang"));
        System.out.println("9. Reports");
        System.out.println(Language.get("exit"));
        System.out.print(Language.get("choose"));
    }

    static void printReportsMenu(WalletAccount wallet, Customer customer) {
        boolean inReports = true;
        while (inReports) {
            System.out.println("\n========= REPORTS MENU =========");
            System.out.println("1. Export Transaction History to CSV");
            System.out.println("2. View Daily Summary");
            System.out.println("3. Export Daily Summary to CSV");
            System.out.println("4. View Customer Statement");
            System.out.println("5. Search Transactions");
            System.out.println("6. Filter by Status (SUCCESS/FAILED)");
            System.out.println("0. Back");
            System.out.print(Language.get("choose"));
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    reportGenerator.exportTransactionHistoryToCSV(wallet.getAccountId());
                    break;
                case "2":
                    reportGenerator.viewDailyTransactionSummary(wallet.getAccountId());
                    break;
                case "3":
                    reportGenerator.exportDailySummaryToCSV(wallet.getAccountId());
                    break;
                case "4":
                    reportGenerator.viewCustomerStatement(wallet.getAccountId(), customer.getFullName());
                    break;
                case "5":
                    System.out.print("Search keyword: ");
                    String keyword = scanner.nextLine().trim();
                    reportGenerator.searchTransactions(wallet.getAccountId(), keyword);
                    break;
                case "6":
                    System.out.print("Status (SUCCESS/FAILED): ");
                    String status = scanner.nextLine().trim();
                    reportGenerator.filterByStatus(wallet.getAccountId(), status);
                    break;
                case "0":
                    inReports = false;
                    break;
                default:
                    System.out.println(Language.get("invalid_option"));
            }
        }
    }

    static void selectLanguage() {
        System.out.println("\n" + Language.get("select_lang"));
        System.out.println(Language.get("lang_en"));
        System.out.println(Language.get("lang_rw"));
        System.out.print(Language.get("choose"));
        String choice = scanner.nextLine().trim();
        if (choice.equals("2")) {
            Language.setLanguage(Language.Lang.RW);
            System.out.println("✓ Ururimi rwahinduwe: Kinyarwanda");
        } else {
            Language.setLanguage(Language.Lang.EN);
            System.out.println("✓ Language set to: English");
        }
    }

    static Customer registerCustomer() {
        System.out.println(Language.get("register"));
        System.out.print(Language.get("full_name"));
        String name = scanner.nextLine().trim();
        String phone;
        while (true) {
            System.out.print(Language.get("phone_prompt"));
            phone = scanner.nextLine().trim();
            if (!Customer.isValidPhoneNumber(phone)) {
                System.out.println(Language.get("invalid_phone"));
                continue;
            }
            if (customerDAO.customerExists(phone)) {
                System.out.println("✗ Account already exists. Please login.");
                return null;
            }
            break;
        }
        return new Customer("C-" + phone, name, phone);
    }

    static String getPin(String prompt) {
        while (true) {
            System.out.print(prompt);
            String pin = scanner.nextLine().trim();
            if (Customer.isValidPin(pin)) return pin;
            System.out.println(Language.get("invalid_pin"));
        }
    }

    static void handleDeposit(WalletAccount wallet, Customer customer) {
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            String refId = "DEP-" + UUID.randomUUID();
            if (processedRequestDAO.isAlreadyProcessed(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.deposit(amount);
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    refId, "DEPOSIT", amount, 0, "SUCCESS",
                    "Deposit to wallet"
            );
            processedRequestDAO.saveProcessedRequest(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "DEP-ERR-" + UUID.randomUUID(), "DEPOSIT",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }

    static void handleWithdraw(WalletAccount wallet, Customer customer) {
        System.out.print(Language.get("enter_pin"));
        String pin = scanner.nextLine().trim();
        if (!wallet.validatePin(pin)) {
            System.out.println("✗ Incorrect PIN. Transaction cancelled.");
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "WIT-ERR-" + UUID.randomUUID(), "WITHDRAWAL",
                    0, 0, "FAILED", "Wrong PIN entered"
            );
            return;
        }
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            double fee = FeeCalculator.getWithdrawFee(amount);
            System.out.println(Language.get("fee_info") + fee + Language.get("total_info") + (amount + fee) + " RWF");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            String refId = "WIT-" + UUID.randomUUID();
            if (processedRequestDAO.isAlreadyProcessed(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.withdraw(amount);
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    refId, "WITHDRAWAL", amount, fee, "SUCCESS",
                    "Wallet withdrawal"
            );
            processedRequestDAO.saveProcessedRequest(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "WIT-ERR-" + UUID.randomUUID(), "WITHDRAWAL",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }

    static void handleSendLocal(WalletAccount wallet, Customer customer) {
        System.out.print(Language.get("enter_pin"));
        String pin = scanner.nextLine().trim();
        if (!wallet.validatePin(pin)) {
            System.out.println("✗ Incorrect PIN. Transaction cancelled.");
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "SND-ERR-" + UUID.randomUUID(), "SEND_LOCAL",
                    0, 0, "FAILED", "Wrong PIN entered"
            );
            return;
        }
        System.out.print(Language.get("recipient"));
        String recipient = scanner.nextLine().trim();
        if (!Customer.isValidPhoneNumber(recipient)) {
            System.out.println(Language.get("invalid_recipient"));
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "SND-ERR-" + UUID.randomUUID(), "SEND_LOCAL",
                    0, 0, "FAILED", "Invalid recipient: " + recipient
            );
            return;
        }
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            boolean isOnNet = recipient.startsWith("078") || recipient.startsWith("079");
            double fee = isOnNet ?
                    FeeCalculator.getSendOnNetFee(amount) :
                    FeeCalculator.getSendOffNetFee(amount);
            System.out.println(Language.get("fee_info") + fee +
                    Language.get("total_info") + (amount + fee) + " RWF (" +
                    (isOnNet ? "On-net" : "Off-net") + ")");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            String refId = "SND-" + UUID.randomUUID();
            if (processedRequestDAO.isAlreadyProcessed(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.sendMoneyLocal(amount, recipient);
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    refId, "SEND_LOCAL", amount, fee, "SUCCESS",
                    "Sent to " + recipient
            );
            processedRequestDAO.saveProcessedRequest(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "SND-ERR-" + UUID.randomUUID(), "SEND_LOCAL",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }

    static void handleSendInternational(WalletAccount wallet, Customer customer) {
        System.out.print(Language.get("enter_pin"));
        String pin = scanner.nextLine().trim();
        if (!wallet.validatePin(pin)) {
            System.out.println("✗ Incorrect PIN. Transaction cancelled.");
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "INT-ERR-" + UUID.randomUUID(), "SEND_INTL",
                    0, 0, "FAILED", "Wrong PIN entered"
            );
            return;
        }
        System.out.println("\n=== Supported Countries ===");
        FeeCalculator.SUPPORTED_COUNTRIES.forEach((code, name) ->
                System.out.println("  +" + code + " → " + name));
        System.out.println("===========================");
        System.out.print(Language.get("country_code"));
        String countryCode = scanner.nextLine().trim();
        if (!FeeCalculator.isSupportedCountry(countryCode)) {
            String reason = "Unsupported country code: +" + countryCode;
            System.out.println("✗ " + reason);
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "INT-ERR-" + UUID.randomUUID(), "SEND_INTL",
                    0, 0, "FAILED", reason
            );
            return;
        }
        System.out.println("✓ Sending to: " + FeeCalculator.getCountryName(countryCode));
        System.out.print(Language.get("recipient_number"));
        String recipient = scanner.nextLine().trim();
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            double fee = FeeCalculator.getInternationalFee(amount);
            System.out.println(Language.get("fee_info") + fee +
                    Language.get("total_info") + (amount + fee) + " RWF");
            System.out.println("  Destination: +" + countryCode + " " + recipient +
                    " (" + FeeCalculator.getCountryName(countryCode) + ")");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            String refId = "INT-" + UUID.randomUUID();
            if (processedRequestDAO.isAlreadyProcessed(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.sendMoneyInternational(amount, countryCode, recipient);
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    refId, "SEND_INTL", amount, fee, "SUCCESS",
                    "Sent to +" + countryCode + " " + recipient
            );
            processedRequestDAO.saveProcessedRequest(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), wallet.getAccountId(),
                    "INT-ERR-" + UUID.randomUUID(), "SEND_INTL",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }

    static void handleMoKashDeposit(WalletAccount wallet, SavingsAccount mokash, Customer customer) {
        System.out.println("ℹ MoKash only accepts transfers from your own Wallet.");
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            if (amount > wallet.getBalance()) {
                String reason = "Insufficient wallet balance. Have: " +
                        wallet.getBalance() + " RWF, Need: " + amount + " RWF";
                System.out.println("✗ " + reason);
                transactionDAO.createTransaction(
                        "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                        "MKD-ERR-" + UUID.randomUUID(), "MOKASH_DEPOSIT",
                        amount, 0, "FAILED", reason
                );
                return;
            }
            System.out.println("Fee: 0 RWF | Amount: " + amount + " RWF");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            String refId = "MKD-" + UUID.randomUUID();
            wallet.setBalance(wallet.getBalance() - amount);
            mokash.deposit(amount);
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                    refId, "MOKASH_DEPOSIT", amount, 0, "SUCCESS",
                    "Deposit from wallet to MoKash"
            );
            processedRequestDAO.saveProcessedRequest(refId);
            System.out.println("✓ Wallet: " + wallet.getBalance() + " RWF");
            System.out.println("✓ MoKash: " + mokash.getBalance() + " RWF");
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                    "MKD-ERR-" + UUID.randomUUID(), "MOKASH_DEPOSIT",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }

    static void handleMoKashWithdraw(SavingsAccount mokash, WalletAccount wallet, Customer customer) {
        System.out.println("ℹ MoKash withdrawals go to your own Wallet only.");
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            System.out.println("Fee: 0 RWF | Amount: " + amount + " RWF");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            String refId = "MKW-" + UUID.randomUUID();
            mokash.withdraw(amount);
            wallet.setBalance(wallet.getBalance() + amount);
            accountDAO.updateBalance(mokash.getAccountId(), mokash.getBalance());
            accountDAO.updateBalance(wallet.getAccountId(), wallet.getBalance());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                    refId, "MOKASH_WITHDRAWAL", amount, 0, "SUCCESS",
                    "Withdrawal from MoKash to wallet"
            );
            processedRequestDAO.saveProcessedRequest(refId);
            System.out.println("✓ MoKash: " + mokash.getBalance() + " RWF");
            System.out.println("✓ Wallet: " + wallet.getBalance() + " RWF");
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            transactionDAO.createTransaction(
                    "TXN-" + UUID.randomUUID(), mokash.getAccountId(),
                    "MKW-ERR-" + UUID.randomUUID(), "MOKASH_WITHDRAWAL",
                    0, 0, "FAILED", e.getMessage()
            );
        }
    }
}