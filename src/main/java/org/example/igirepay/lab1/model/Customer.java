package org.example.igirepay.lab1.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Customer {
    private String customerId;
    private String fullName;
    private String phoneNumber;
    private List<Account> accounts;
    private Set<String> processedReferenceIds;
    private Map<String, String> failedTransactionLogs;
    private boolean loggedIn;

    public Customer(String customerId, String fullName, String phoneNumber) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
        this.processedReferenceIds = new HashSet<>();
        this.failedTransactionLogs = new HashMap<>();
        this.loggedIn = false;
    }

    public static boolean isValidPhoneNumber(String phone) {
        return phone.matches("^(078|079)\\d{7}$");
    }

    public static boolean isValidInternationalNumber(String phone) {
        // Support country codes: +1 (US/Canada), +44 (UK), +33 (France), +49 (Germany), 
        // +39 (Italy), +34 (Spain), +32 (Belgium), +31 (Netherlands), +41 (Switzerland),
        // +43 (Austria), +45 (Denmark), +46 (Sweden), +47 (Norway), +48 (Poland),
        // +91 (India), +81 (Japan), +86 (China), +234 (Nigeria), +27 (South Africa)
        return phone.matches("^\\+(?:1|44|33|49|39|34|32|31|41|43|45|46|47|48|91|81|86|234|27)\\d{9,14}$");
    }

    public static boolean isLocalNumber(String phone) {
        return phone.matches("^(078|079)\\d{7}$");
    }

    public static boolean isValidPin(String pin) {
        return pin.matches("^\\d{5}$");
    }

    public boolean isDuplicateTransaction(String referenceId) {
        return processedReferenceIds.contains(referenceId);
    }

    public void markTransactionProcessed(String referenceId) {
        processedReferenceIds.add(referenceId);
    }

    public void logFailedTransaction(String referenceId, String reason) {
        failedTransactionLogs.put(referenceId, reason);
        System.out.println("✗ Transaction " + referenceId + " failed: " + reason);
    }

    public void printFailedTransactions() {
        System.out.println("\n=== Failed Transactions ===");
        if (failedTransactionLogs.isEmpty()) {
            System.out.println("No failed transactions.");
        } else {
            failedTransactionLogs.forEach((ref, reason) ->
                    System.out.println("Ref: " + ref + " | Reason: " + reason));
        }
        System.out.println("===========================");
    }

    public void addAccount(Account account) { accounts.add(account); }
    public void removeAccount(Account account) { accounts.remove(account); }
    public List<Account> getAccounts() { return accounts; }
    public String getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public boolean isLoggedIn() { return loggedIn; }
    public void setLoggedIn(boolean loggedIn) { this.loggedIn = loggedIn; }
    public Set<String> getProcessedReferenceIds() { return processedReferenceIds; }
    public Map<String, String> getFailedTransactionLogs() { return failedTransactionLogs; }

    @Override
    public String toString() {
        return "Customer{" +
                "customerId='" + customerId + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' + '}';
    }
}