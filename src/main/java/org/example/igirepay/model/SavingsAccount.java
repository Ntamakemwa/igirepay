package org.example.igirepay.model;

import org.example.igirepay.exception.InsufficientBalanceException;

public class SavingsAccount extends Account {

    private static final double WITHDRAWAL_FEE = 0.02; // 2% fee
    private static final double MIN_BALANCE = 1000.0;  // minimum balance

    public SavingsAccount(String accountId, double balance, String pin) {
        super(accountId, "SAVINGS", balance, pin);
    }

    @Override
    public void deposit(double amount) {
        setBalance(getBalance() + amount);
        System.out.println("Deposited " + amount + " to Savings. New balance: " + getBalance());
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        double fee = amount * WITHDRAWAL_FEE;
        double total = amount + fee;

        if (getBalance() - total < MIN_BALANCE) {
            throw new InsufficientBalanceException(
                    "Cannot withdraw. Minimum balance of " + MIN_BALANCE + " must be maintained.");
        }
        setBalance(getBalance() - total);
        System.out.println("Withdrawn " + amount + " (fee: " + fee + ") from Savings. New balance: " + getBalance());
    }

    @Override
    public void processTransaction(Transaction transaction) throws InsufficientBalanceException {
        switch (transaction.getTransactionType()) {
            case "DEPOSIT" -> deposit(transaction.getAmount());
            case "WITHDRAWAL" -> withdraw(transaction.getAmount());
            default -> System.out.println("Unknown transaction type.");
        }
    }

    @Override
    public String toString() {
        return "SavingsAccount{" + super.toString() + '}';
    }
}
