package org.example.igirepay.lab1.model;

import org.example.igirepay.lab1.exception.InsufficientBalanceException;
import java.util.ArrayList;
import java.util.List;

public class SavingsAccount extends Account {

    private static final double MIN_BALANCE = 100.0;
    private List<String> transactionHistory;

    public SavingsAccount(String accountId, double balance, String pin) {
        super(accountId, "MOKASH", balance, pin);
        this.transactionHistory = new ArrayList<>();
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be greater than 0.");
        setBalance(getBalance() + amount);
        String record = "DEPOSIT | +" + amount + " RWF | Balance: " + getBalance() + " RWF";
        transactionHistory.add(record);
        System.out.println("✓ MoKash deposit successful!");
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  Fee: 0 RWF");
        System.out.println("  New MoKash balance: " + getBalance() + " RWF");
    }

    @Override
    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be greater than 0.");
        if (getBalance() - amount < MIN_BALANCE) {
            throw new InsufficientBalanceException(
                    "Cannot withdraw. Minimum MoKash balance of " + MIN_BALANCE + " RWF must be maintained.");
        }
        setBalance(getBalance() - amount);
        String record = "WITHDRAWAL | -" + amount + " RWF | Balance: " + getBalance() + " RWF";
        transactionHistory.add(record);
        System.out.println("✓ MoKash withdrawal successful!");
        System.out.println("  Amount: " + amount + " RWF");
        System.out.println("  Fee: 0 RWF");
        System.out.println("  New MoKash balance: " + getBalance() + " RWF");
    }

    public void applyInterest() {
        double rate = FeeCalculator.getMoKashInterestRate(getBalance());
        double interest = getBalance() * rate / 12; // monthly interest
        setBalance(getBalance() + interest);
        String record = "INTEREST | +" + interest + " RWF | Rate: " + (rate * 100) + "% p.a | Balance: " + getBalance() + " RWF";
        transactionHistory.add(record);
        System.out.println("✓ Monthly interest applied: " + interest + " RWF");
        System.out.println("  New MoKash balance: " + getBalance() + " RWF");
    }

    public void printTransactionHistory() {
        System.out.println("\n=== MoKash Transaction History ===");
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions yet.");
        } else {
            for (int i = 0; i < transactionHistory.size(); i++) {
                System.out.println((i + 1) + ". " + transactionHistory.get(i));
            }
        }
        System.out.println("==================================");
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
        return "SavingsAccount(MoKash){" + super.toString() + '}';
    }
}