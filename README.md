# IgirePay Payment Gateway

A secure desktop-based digital wallet system built with Java, JavaFX, JDBC, and PostgreSQL.

## Technologies
- Java
- JavaFX
- JDBC + PostgreSQL
- Maven
- Git & GitHub

## Branch: feature/lab1-oop-design
### What's included:
- `Account.java` — Abstract base class for all accounts
- `WalletAccount.java` — Instant transfers, inherits Account
- `SavingsAccount.java` — 2% withdrawal fee, minimum balance, inherits Account
- `Transaction.java` — Tracks all transaction details
- `Customer.java` — Manages customer and their accounts
- `DuplicateTransactionException.java`
- `InsufficientBalanceException.java`
- `InvalidAccountException.java`