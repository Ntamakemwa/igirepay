package org.example.igirepay.lab1.model;

import org.example.igirepay.lab1.exception.InsufficientBalanceException;

public class WalletAccount extends Account {

    public WalletAccount(String accountId, double balance, String pin) {
        super(accountId, "WALLET", balance, pin);
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be greater than 0.");
        setBalance(getBalance() + amount);
        System.out.println("✓ Deposit successful!");
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  Fee: 0 RWF");
        System.out.println("  New balance: " + getBalance() + " RWF");
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than 0.");
        double fee = FeeCalculator.getWithdrawFee(amount);
        double total = amount + fee;
        if (total > getBalance()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. You need " + total + " RWF (amount + fee: " + fee + " RWF) but have " + getBalance() + " RWF");
        }
        setBalance(getBalance() - total);
        System.out.println("✓ Withdrawal successful!");
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  Fee: " + fee + " RWF");
        System.out.println("  Total deducted: " + total + " RWF");
        System.out.println("  New balance: " + getBalance() + " RWF");
    }

    public void sendMoneyLocal(double amount, String recipientNumber) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        boolean isOnNet = recipientNumber.startsWith("078") || recipientNumber.startsWith("079");
        double fee = isOnNet ? FeeCalculator.getSendOnNetFee(amount) : FeeCalculator.getSendOffNetFee(amount);
        double total = amount + fee;
        if (total > getBalance()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. You need " + total + " RWF (amount + fee: " + fee + " RWF) but have " + getBalance() + " RWF");
        }
        setBalance(getBalance() - total);
        System.out.println("✓ Money sent successfully!");
        System.out.println("  Recipient: " + recipientNumber);
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  Fee: " + fee + " RWF (" + (isOnNet ? "On-net" : "Off-net") + ")");
        System.out.println("  Total deducted: " + total + " RWF");
        System.out.println("  New balance: " + getBalance() + " RWF");
    }

    public void sendMoneyInternational(double amount, String countryCode, String recipientNumber) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than 0.");
        double fee = FeeCalculator.getInternationalFee(amount);
        double total = amount + fee;
        if (total > getBalance()) {
            throw new InsufficientBalanceException(
                    "Insufficient balance. You need " + total + " RWF (amount + fee: " + fee + " RWF) but have " + getBalance() + " RWF");
        }
        setBalance(getBalance() - total);
        System.out.println("✓ International transfer successful!");
        System.out.println("  Recipient: +" + countryCode + recipientNumber);
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  International fee: " + fee + " RWF");
        System.out.println("  Total deducted: " + total + " RWF");
        System.out.println("  New balance: " + getBalance() + " RWF");
    }

    @Override
    public void processTransaction(Transaction transaction) throws InsufficientBalanceException {
        switch (transaction.getTransactionType()) {
            case "DEPOSIT":
                deposit(transaction.getAmount());
                break;
            case "WITHDRAWAL":
                withdraw(transaction.getAmount());
                break;
            default:
                System.out.println("Unknown transaction type.");
        }
    }

    @Override
    public String toString() {
        return "WalletAccount{" + super.toString() + '}';
    }
}