package org.example.igirepay;

import org.example.igirepay.lab1.model.Account;
import org.example.igirepay.lab1.model.Customer;
import org.example.igirepay.lab1.model.FeeCalculator;
import org.example.igirepay.lab1.model.Language;
import org.example.igirepay.lab1.model.SavingsAccount;
import org.example.igirepay.lab1.model.Transaction;
import org.example.igirepay.lab1.model.WalletAccount;
import org.example.igirepay.lab1.exception.DuplicateTransactionException;
import org.example.igirepay.lab1.exception.InsufficientBalanceException;
import org.example.igirepay.lab1.exception.InvalidAccountException;
import java.util.Scanner;

public class Main {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        selectLanguage();

        System.out.println(Language.get("welcome"));
        System.out.println(Language.get("app_name"));
        System.out.println(Language.get("welcome") + "\n");

        Customer customer = registerCustomer();
        if (customer == null) return;

        String walletPin = getPin(Language.get("create_pin"));
        WalletAccount wallet = new WalletAccount("W-" + customer.getPhoneNumber(), 0, walletPin);
        customer.addAccount(wallet);
        System.out.println(Language.get("wallet_created") + "\n");

        System.out.print(Language.get("mokash_ask"));
        String moKashChoice = scanner.nextLine().trim().toLowerCase();
        SavingsAccount mokash = null;
        if (moKashChoice.equals("yes") || moKashChoice.equals("yego")) {
            String mokashPin = getPin(Language.get("mokash_pin"));
            mokash = new SavingsAccount("MK-" + customer.getPhoneNumber(), 0, mokashPin);
            customer.addAccount(mokash);
            System.out.println(Language.get("mokash_activated") + "\n");
        }


        System.out.println(Language.get("login"));
        boolean loggedIn = false;
        int attempts = 0;
        while (attempts < 3) {
            String pin = getPin(Language.get("enter_pin"));
            if (wallet.validatePin(pin)) {
                loggedIn = true;
                customer.setLoggedIn(true);
                System.out.println(Language.get("login_success") + customer.getFullName() + "!");
                break;
            }
            attempts++;
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
                    System.out.println("\n💰 Wallet: " + wallet.getBalance() + " RWF");
                    break;
                case "6":
                    if (mokash != null) mokashMenu(mokash, wallet, customer);
                    else System.out.println(Language.get("mokash_not_active"));
                    break;
                case "7": customer.printFailedTransactions(); break;
                case "8": selectLanguage(); break;
                case "0":
                    System.out.println(Language.get("goodbye"));
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
                    System.out.println("\n💰 MoKash: " + mokash.getBalance() + " RWF");
                    break;
                case "4": mokash.printTransactionHistory(); break;
                case "5": mokash.applyInterest(); break;
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
        System.out.println(Language.get("exit"));
        System.out.print(Language.get("choose"));
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
            if (Customer.isValidPhoneNumber(phone)) break;
            System.out.println(Language.get("invalid_phone"));
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
            String refId = "DEP-" + System.currentTimeMillis();
            wallet.deposit(amount);
            customer.markTransactionProcessed(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("DEP-ERR-" + System.currentTimeMillis(), e.getMessage());
        }
    }

    static void handleWithdraw(WalletAccount wallet, Customer customer) {
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
            String refId = "WIT-" + System.currentTimeMillis();
            wallet.withdraw(amount);
            customer.markTransactionProcessed(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("WIT-ERR-" + System.currentTimeMillis(), e.getMessage());
        }
    }

    static void handleSendLocal(WalletAccount wallet, Customer customer) {
        System.out.print(Language.get("recipient"));
        String recipient = scanner.nextLine().trim();
        if (!Customer.isValidPhoneNumber(recipient)) {
            System.out.println(Language.get("invalid_recipient"));
            customer.logFailedTransaction("SND-ERR-" + System.currentTimeMillis(),
                    "Invalid recipient number: " + recipient);
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
            String refId = "SND-" + System.currentTimeMillis();
            if (customer.isDuplicateTransaction(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.sendMoneyLocal(amount, recipient);
            customer.markTransactionProcessed(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("SND-ERR-" + System.currentTimeMillis(), e.getMessage());
        }
    }

    static void handleSendInternational(WalletAccount wallet, Customer customer) {

        System.out.println("\n=== Supported Countries ===");
        FeeCalculator.SUPPORTED_COUNTRIES.forEach((code, name) ->
                System.out.println("  +" + code + " → " + name));
        System.out.println("===========================");

        System.out.print(Language.get("country_code"));
        String countryCode = scanner.nextLine().trim();


        if (!FeeCalculator.isSupportedCountry(countryCode)) {
            String reason = "Unsupported country code: +" + countryCode +
                    ". MTN Rwanda does not support transfers to this country.";
            System.out.println("✗ " + reason);
            customer.logFailedTransaction("INT-ERR-" + System.currentTimeMillis(), reason);
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
            String refId = "INT-" + System.currentTimeMillis();
            if (customer.isDuplicateTransaction(refId)) {
                System.out.println("✗ Duplicate transaction detected.");
                return;
            }
            wallet.sendMoneyInternational(amount, countryCode, recipient);
            customer.markTransactionProcessed(refId);
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("INT-ERR-" + System.currentTimeMillis(), e.getMessage());
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
                customer.logFailedTransaction("MKD-ERR-" + System.currentTimeMillis(), reason);
                return;
            }
            System.out.println("Fee: 0 RWF | Amount to move: " + amount + " RWF");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            wallet.setBalance(wallet.getBalance() - amount);
            mokash.deposit(amount);
            System.out.println("✓ Wallet balance: " + wallet.getBalance() + " RWF");
            System.out.println("✓ MoKash balance: " + mokash.getBalance() + " RWF");
            customer.markTransactionProcessed("MKD-" + System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("MKD-ERR-" + System.currentTimeMillis(), e.getMessage());
        }
    }

    static void handleMoKashWithdraw(SavingsAccount mokash, WalletAccount wallet, Customer customer) {
        System.out.println("ℹ MoKash withdrawals go directly to your own Wallet only.");
        System.out.print(Language.get("enter_amount"));
        try {
            double amount = Double.parseDouble(scanner.nextLine().trim());
            if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
            System.out.println("Fee: 0 RWF | Amount to move: " + amount + " RWF");
            System.out.print(Language.get("confirm"));
            String confirm = scanner.nextLine().trim().toLowerCase();
            if (!confirm.equals("yes") && !confirm.equals("yego")) {
                System.out.println(Language.get("cancelled"));
                return;
            }
            mokash.withdraw(amount);
            wallet.setBalance(wallet.getBalance() + amount);
            System.out.println("✓ MoKash balance: " + mokash.getBalance() + " RWF");
            System.out.println("✓ Wallet balance: " + wallet.getBalance() + " RWF");
            customer.markTransactionProcessed("MKW-" + System.currentTimeMillis());
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
            customer.logFailedTransaction("MKW-ERR-" + System.currentTimeMillis(), e.getMessage());
        }
    }
}