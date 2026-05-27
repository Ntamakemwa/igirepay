# IgirePay Payment Gateway

A secure desktop-based digital wallet system inspired by MTN Mobile Money.
Built with Java, JavaFX, JDBC, and PostgreSQL.

## Labs
| Lab | Branch | Description |
|-----|--------|-------------|
| Lab 1 | feature/lab1-oop-design | OOP classes, inheritance, collections |
| Lab 2 | feature/lab2-database-jdbc | PostgreSQL, JDBC, DAO pattern |
| Lab 3 | feature/lab3-console-app | Full console + JavaFX app |
| - | feature/authentication | PIN auth |
| - | feature/reports-csv | CSV reports + bonus challenges |

## Technologies
- Java
- JavaFX
- JDBC + PostgreSQL
- Maven
- Git and GitHub

## Author
Olive Ntamakemwa / SheCanCode Backend Phase 1 Capstone.

## UI & Feature Notes (Important)

- MoKash activation is now available in the JavaFX registration flow (local numbers only).
- International transfers accept any properly-formatted international number (no DB record required), but the app will show the recipient name if that number is registered in the system.
- After typing a recipient number in the UI, the app shows the recipient name and your current wallet balance immediately, and shows a confirmation dialog with projected remaining balance before processing.
- Transactions and MoKash history views are scrollable and paged (TableView + ScrollPane) so you can see all rows.
- Added styled PIN verification dialogs to the JavaFX UI for better UX.
- Reports: downloadable CSV exports for Wallet and MoKash transactions are available from the UI and console.

## Quick Reset (Dev)
To wipe all data during development, run the `reset_database.sql` script or execute the following in `psql`:

```sql
TRUNCATE TABLE transactions CASCADE;
TRUNCATE TABLE processed_requests CASCADE;
TRUNCATE TABLE accounts CASCADE;
TRUNCATE TABLE customers CASCADE;
```

Then restart the app and re-register test users.