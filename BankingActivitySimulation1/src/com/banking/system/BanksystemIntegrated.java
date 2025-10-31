package com.banking.system;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.io.*;

public class BanksystemIntegrated {

    private static final String URL = "jdbc:mysql://localhost:3306/banking_simulator";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "thivagar161004";
    private static Scanner s = new Scanner(System.in);

    private static final Pattern emailP = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern phoneP = Pattern.compile("^\\d{10}$");
    private static final Pattern strongPwP = Pattern.compile("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private static AccountManager accountManager = new AccountManager();
    private static TransactionLogger transactionLogger = new TransactionLogger();
    private static AdminManager adminManager = new AdminManager();

    public static void main(String[] args) {
        System.out.println("Welcome to Banking Simulator System");

        try (Connection c = DriverManager.getConnection(URL, DB_USER, DB_PASS)) {
            System.out.println("Database connected successfully!");

            // Initialize database schema
            initializeDatabase(c);

            // Load existing accounts from database
            accountManager.loadAccountsFromDatabase(c);

            runMainMenu(c);

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            logError("Database connection error", e);
        } catch (Exception e) {
            System.out.println("System initialization failed: " + e.getMessage());
            logError("System initialization error", e);
        } finally {
            System.out.println("Thank you for using our bank. Goodbye!");
        }
    }

    private static boolean isValidHolderName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return name.matches("^[a-zA-Z ]{2,}$");
    }

    private static boolean isValidAccountType(String accountType) {
        return "SAVINGS".equalsIgnoreCase(accountType) || "CURRENT".equalsIgnoreCase(accountType);
    }

    private static void runMainMenu(Connection c) {
        int ch;
        do {
            System.out.println("\n=== Banking System Menu ===");
            System.out.println("1. Create Account");
            System.out.println("2. Login to Account");
            System.out.println("3. Admin Login");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            try {
                while (!s.hasNextInt()) {
                    System.out.print("Invalid input! Enter a number: ");
                    s.next();
                }
                ch = s.nextInt();
                s.nextLine();

                switch (ch) {
                    case 1 -> createAccount(c);
                    case 2 -> loginToAccount(c);
                    case 3 -> adminLogin(c);
                    case 4 -> { return; }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error in menu: " + e.getMessage());
                logError("Menu error", e);
                s.nextLine();
                ch = 0;
            }
        } while (ch != 4);
    }

    private static void loginToAccount(Connection c) {
        try {
            System.out.print("Enter Account Number: ");
            String accountNumber = s.nextLine().trim();

            System.out.print("Enter Password: ");
            String password = s.nextLine();

            if (verifyPassword(c, accountNumber, password)) {
                runAccountMenu(c, accountNumber);
            } else {
                System.out.println("Login failed! Invalid account number or password.");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            logError("Login error", e);
        }
    }

    private static void adminLogin(Connection c) {
        try {
            System.out.print("Enter Admin Username: ");
            String username = s.nextLine().trim();

            System.out.print("Enter Admin Password: ");
            String password = s.nextLine();

            if (adminManager.verifyAdmin(username, password)) {
                runAdminMenu(c);
            } else {
                System.out.println("Invalid admin credentials!");
            }
        } catch (Exception e) {
            System.out.println("Admin login error: " + e.getMessage());
            logError("Admin login error", e);
        }
    }

    private static void runAdminMenu(Connection c) {
        int ch;
        do {
            System.out.println("\n=== Admin Menu ===");
            System.out.println("1. List All Accounts");
            System.out.println("2. Search Account by Number");
            System.out.println("3. Search Account by Name");
            System.out.println("4. View System Statistics");
            System.out.println("5. View All Transactions");
            System.out.println("6. Manage Selected Account");
            System.out.println("7. Search Transaction by ID");
            System.out.println("8. Logout");
            System.out.print("Enter your choice: ");

            try {
                while (!s.hasNextInt()) {
                    System.out.print("Invalid input! Enter a number: ");
                    s.next();
                }
                ch = s.nextInt();
                s.nextLine();

                switch (ch) {
                    case 1 -> listAllAccounts(c);
                    case 2 -> searchAccountByNumber(c);
                    case 3 -> searchAccountByName(c);
                    case 4 -> viewSystemStatistics(c);
                    case 5 -> viewAllTransactions(c);
                    case 6 -> manageSelectedAccount(c);
                    case 7 -> searchTransactionById(c);
                    case 8 -> {
                        System.out.println("Admin logged out successfully!");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error in admin menu: " + e.getMessage());
                logError("Admin menu error", e);
                s.nextLine();
                ch = 0;
            }
        } while (ch != 8);
    }

    private static void runAccountMenu(Connection c, String accountNumber) {
        int ch;
        do {
            System.out.println("\n=== Account Menu ===");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit Money");
            System.out.println("3. Withdraw Money");
            System.out.println("4. Transfer Money");
            System.out.println("5. View My Transaction History");
            System.out.println("6. View My Account Details");
            System.out.println("7. Logout");
            System.out.print("Enter your choice: ");

            try {
                while (!s.hasNextInt()) {
                    System.out.print("Invalid input! Enter a number: ");
                    s.next();
                }
                ch = s.nextInt();
                s.nextLine();

                switch (ch) {
                    case 1 -> checkBalance(c, accountNumber);
                    case 2 -> depositMoney(c, accountNumber);
                    case 3 -> withdrawMoney(c, accountNumber);
                    case 4 -> transferMoney(c, accountNumber);
                    case 5 -> viewMyTransactionHistory(c, accountNumber);
                    case 6 -> viewAccountDetails(c, accountNumber);
                    case 7 -> {
                        System.out.println("Logged out successfully!");
                        return;
                    }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error in account menu: " + e.getMessage());
                logError("Account menu error", e);
                s.nextLine();
                ch = 0;
            }
        } while (ch != 7);
    }

    private static void searchTransactionById(Connection c) {
        try {
            System.out.print("Enter Transaction ID to search: ");
            String transactionId = s.nextLine().trim();

            TransactionRecord transaction = transactionLogger.findTransactionById(c, transactionId);
            if (transaction != null) {
                System.out.println("\n=== Transaction Details ===");
                System.out.println("Transaction ID: " + transaction.getTxId());
                System.out.println("Type: " + transaction.getTxType());
                System.out.println("From Account: " + (transaction.getFromAccount() != null ? transaction.getFromAccount() : "SYSTEM"));
                System.out.println("To Account: " + (transaction.getToAccount() != null ? transaction.getToAccount() : "SYSTEM"));
                System.out.println("Amount: " + transaction.getAmount());
                System.out.println("Description: " + transaction.getDescription());
                System.out.println("Timestamp: " + transaction.getCreatedAt());

                // Show account holder names if available
                if (transaction.getFromAccount() != null) {
                    Account fromAccount = accountManager.getAccountFromDB(c, transaction.getFromAccount());
                    if (fromAccount != null) {
                        System.out.println("From Account Holder: " + fromAccount.getHolderName());
                    }
                }
                if (transaction.getToAccount() != null) {
                    Account toAccount = accountManager.getAccountFromDB(c, transaction.getToAccount());
                    if (toAccount != null) {
                        System.out.println("To Account Holder: " + toAccount.getHolderName());
                    }
                }
            } else {
                System.out.println("Transaction not found with ID: " + transactionId);
            }
        } catch (Exception e) {
            System.out.println("Error searching transaction: " + e.getMessage());
            logError("Transaction search error", e);
        }
    }

    private static void manageSelectedAccount(Connection c) {
        try {
            System.out.print("Enter Account Number to manage: ");
            String accountNumber = s.nextLine().trim();

            Account account = accountManager.getAccount(accountNumber);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            runAccountManagementMenu(c, accountNumber);
        } catch (Exception e) {
            System.out.println("Error managing account: " + e.getMessage());
            logError("Account management error", e);
        }
    }

    private static void runAccountManagementMenu(Connection c, String accountNumber) {
        int ch;
        do {
            System.out.println("\n=== Managing Account: " + accountNumber + " ===");
            System.out.println("1. View Account Details");
            System.out.println("2. View Current Balance");
            System.out.println("3. View Transaction History");
            System.out.println("4. Force Deposit");
            System.out.println("5. Force Withdrawal");
            System.out.println("6. Update Account Information");
            System.out.println("7. Delete Account");
            System.out.println("8. Freeze/Unfreeze Account");
            System.out.println("9. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            try {
                while (!s.hasNextInt()) {
                    System.out.print("Invalid input! Enter a number: ");
                    s.next();
                }
                ch = s.nextInt();
                s.nextLine();

                switch (ch) {
                    case 1 -> viewFullAccountDetails(c, accountNumber);
                    case 2 -> viewBalanceForAdmin(c, accountNumber);
                    case 3 -> viewTransactionHistoryForAdmin(c, accountNumber);
                    case 4 -> forceDeposit(c, accountNumber);
                    case 5 -> forceWithdrawal(c, accountNumber);
                    case 6 -> updateAccountInformation(c, accountNumber);
                    case 7 -> deleteAccount(c, accountNumber);
                    case 8 -> toggleAccountStatus(c, accountNumber);
                    case 9 -> { return; }
                    default -> System.out.println("Invalid choice! Please try again.");
                }
            } catch (Exception e) {
                System.out.println("Error in account management: " + e.getMessage());
                logError("Account management error", e);
                s.nextLine();
                ch = 0;
            }
        } while (ch != 9);
    }

    private static void initializeDatabase(Connection c) throws SQLException {
        try (Statement st = c.createStatement()) {
            // Create sequence table for account numbers
            st.execute("CREATE TABLE IF NOT EXISTS account_sequence (" +
                    "last_account_number BIGINT NOT NULL DEFAULT 1000)");

            // Initialize sequence if empty
            st.execute("INSERT IGNORE INTO account_sequence (last_account_number) VALUES (1000)");

            // Create accounts table if not exists
            st.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "account_number VARCHAR(32) PRIMARY KEY, " +
                    "holder_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(255) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "ifsc VARCHAR(20) NOT NULL, " +
                    "balance DECIMAL(19,4) NOT NULL DEFAULT 0.00, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "account_type VARCHAR(20) DEFAULT 'SAVINGS', " +
                    "status VARCHAR(20) DEFAULT 'ACTIVE', " +
                    "last_activity DATETIME, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Create transactions table for database transaction logging
            st.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "transaction_id VARCHAR(36) PRIMARY KEY, " +
                    "transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL, " +
                    "from_account VARCHAR(32), " +
                    "to_account VARCHAR(32), " +
                    "amount DECIMAL(19,4) NOT NULL, " +
                    "description TEXT, " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (from_account) REFERENCES accounts(account_number), " +
                    "FOREIGN KEY (to_account) REFERENCES accounts(account_number))");

            // Create admin table
            st.execute("CREATE TABLE IF NOT EXISTS admin_users (" +
                    "username VARCHAR(50) PRIMARY KEY, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "role VARCHAR(20) DEFAULT 'ADMIN', " +
                    "created_at DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Insert default admin user if not exists
            st.execute("INSERT IGNORE INTO admin_users (username, password_hash, full_name, role) VALUES " +
                    "('admin', '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918', 'System Administrator', 'SUPER_ADMIN')");

            System.out.println("Database initialized successfully!");
        } catch (SQLException e) {
            logError("Database initialization error", e);
            throw e;
        }
    }

    private static void createAccount(Connection c) {
        try {
            // Auto-generate account number
            String accountNumber = accountManager.generateAccountNumber(c);
            System.out.println("Generated Account Number: " + accountNumber);

            // Modified holder name validation
            System.out.print("Enter Holder Name: ");
            String holderName = s.nextLine().trim();
            while (!isValidHolderName(holderName)) {
                System.out.print("Invalid name! Use only alphabets and spaces: ");
                holderName = s.nextLine().trim();
            }

            System.out.print("Enter IFSC Code: ");
            String ifsc = s.nextLine().trim();
            if (ifsc.isEmpty()) {
                throw new BankException("IFSC code cannot be empty!");
            }

            System.out.print("Enter Phone (10 digits): ");
            String phone = s.nextLine().trim();
            while (!phoneP.matcher(phone).matches()) {
                System.out.print("Invalid phone! Enter 10 digit phone: ");
                phone = s.nextLine().trim();
            }

            System.out.print("Enter Email: ");
            String email = s.nextLine().trim();
            while (!emailP.matcher(email).matches()) {
                System.out.print("Invalid email! Enter again: ");
                email = s.nextLine().trim();
            }

            System.out.print("Enter Account Type (SAVINGS/CURRENT): ");
            String accountType = s.nextLine().trim().toUpperCase();
            while (!isValidAccountType(accountType)) {
                System.out.print("Invalid account type! Enter exactly 'SAVINGS' or 'CURRENT': ");
                accountType = s.nextLine().trim().toUpperCase();
            }

            System.out.print("Enter Initial Deposit: ");
            BigDecimal balance = getValidAmount();

            String password1, password2;
            boolean passwordsMatch = false;
            int attemptCount = 0;
            final int MAX_ATTEMPTS = 3;

            do {
                System.out.print("Set Password (min8, upper, lower, digit, symbol): ");
                password1 = s.nextLine();

                if (!strongPwP.matcher(password1).matches()) {
                    System.out.println("Weak password! Must have min 8 chars, uppercase, lowercase, digit and symbol.");
                    continue;
                }

                System.out.print("Confirm Password: ");
                password2 = s.nextLine();

                if (password1.equals(password2)) {
                    passwordsMatch = true;
                } else {
                    attemptCount++;
                    if (attemptCount < MAX_ATTEMPTS) {
                        System.out.println("Passwords do not match! Please try again. (" +
                                (MAX_ATTEMPTS - attemptCount) + " attempts remaining)");
                    } else {
                        System.out.println("Maximum password confirmation attempts reached. Account creation cancelled.");
                        return;
                    }
                }
            } while (!passwordsMatch && attemptCount < MAX_ATTEMPTS);

            String passwordHash = hash(password1);

            // Create account in memory and database
            Account account = new Account(accountNumber, holderName, email, balance);
            account.setPhone(phone);
            account.setIfsc(ifsc);
            account.setAccountType(accountType);

            boolean success = accountManager.createAccount(c, account, passwordHash);
            if (success) {
                transactionLogger.logTransactionToDB(c,
                        TransactionType.DEPOSIT,
                        null,
                        accountNumber,
                        balance,
                        "Initial deposit - Account creation for " + holderName
                );
                System.out.println("Account created successfully!");
                System.out.println("Your Account Number: " + accountNumber);
                System.out.println("Holder Name: " + holderName);
            } else {
                throw new BankException("Failed to create account!");
            }

        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error creating account: " + e.getMessage());
            logError("Account creation error", e);
        }
    }

    private static void checkBalance(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account != null) {
                if ("FROZEN".equals(account.getStatus())) {
                    throw new BankException("Account is frozen. Please contact administrator.");
                }
                System.out.println("Current Balance: " + account.getBalance());
            } else {
                throw new BankException("Account not found!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error checking balance: " + e.getMessage());
            logError("Balance check error", e);
        }
    }

    private static void depositMoney(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account != null && "FROZEN".equals(account.getStatus())) {
                throw new BankException("Account is frozen. Cannot perform transactions.");
            }

            System.out.print("Enter amount to deposit: ");
            BigDecimal amount = getValidAmount();

            boolean success = accountManager.deposit(c, accountNumber, amount);
            if (success) {
                transactionLogger.logTransactionToDB(c,
                        TransactionType.DEPOSIT,
                        null,
                        accountNumber,
                        amount,
                        "Cash deposit to " + account.getHolderName()
                );
                System.out.println("Deposit successful!");
                System.out.println("New Balance: " + accountManager.getAccount(accountNumber).getBalance());
            } else {
                throw new BankException("Deposit failed!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error depositing money: " + e.getMessage());
            logError("Deposit error", e);
        }
    }

    private static void withdrawMoney(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account != null && "FROZEN".equals(account.getStatus())) {
                throw new BankException("Account is frozen. Cannot perform transactions.");
            }

            System.out.print("Enter amount to withdraw: ");
            BigDecimal amount = getValidAmount();

            boolean success = accountManager.withdraw(c, accountNumber, amount);
            if (success) {
                transactionLogger.logTransactionToDB(c,
                        TransactionType.WITHDRAW,
                        accountNumber,
                        null,
                        amount,
                        "Cash withdrawal from " + account.getHolderName()
                );
                System.out.println("Withdrawal successful!");
                System.out.println("New Balance: " + accountManager.getAccount(accountNumber).getBalance());
            } else {
                throw new BankException("Withdrawal failed! Check your balance.");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error withdrawing money: " + e.getMessage());
            logError("Withdrawal error", e);
        }
    }

    private static void transferMoney(Connection c, String fromAccount) {
        try {
            Account fromAcc = accountManager.getAccount(fromAccount);
            if (fromAcc != null && "FROZEN".equals(fromAcc.getStatus())) {
                throw new BankException("Your account is frozen. Cannot perform transactions.");
            }

            System.out.print("Enter Recipient Account Holder Name: ");
            String recipientName = s.nextLine().trim();

            // Search for account by name
            Account recipient = accountManager.findAccountByName(c, recipientName);
            if (recipient == null) {
                throw new BankException("No account found with holder name: " + recipientName);
            }
            if ("FROZEN".equals(recipient.getStatus())) {
                throw new BankException("Recipient account is frozen. Cannot transfer funds.");
            }

            System.out.println("Found Account:");
            System.out.println("Account Number: " + recipient.getAccountNumber());
            System.out.println("Holder Name: " + recipient.getHolderName());
            System.out.println("Confirm transfer to this account? (yes/no): ");
            String confirmation = s.nextLine().trim();

            if (!"yes".equalsIgnoreCase(confirmation)) {
                System.out.println("Transfer cancelled.");
                return;
            }

            String toAccount = recipient.getAccountNumber();

            System.out.print("Enter amount to transfer: ");
            BigDecimal amount = getValidAmount();

            boolean success = accountManager.transfer(c, fromAccount, toAccount, amount);
            if (success) {
                transactionLogger.logTransactionToDB(c,
                        TransactionType.TRANSFER,
                        fromAccount,
                        toAccount,
                        amount,
                        "Transfer from " + fromAcc.getHolderName() + " to " + recipient.getHolderName()
                );
                System.out.println("Transfer successful!");
                System.out.println("New Balance: " + accountManager.getAccount(fromAccount).getBalance());
            } else {
                throw new BankException("Transfer failed! Check your balance.");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error transferring money: " + e.getMessage());
            logError("Transfer error", e);
        }
    }

    private static void listAllAccounts(Connection c) {
        try {
            List<Account> accounts = accountManager.listAllAccounts(c);
            if (accounts.isEmpty()) {
                System.out.println("No accounts found!");
                return;
            }

            System.out.println("\n=== All Accounts ===");
            System.out.printf("%-15s %-20s %-25s %-12s %-15s %-10s %-10s%n",
                    "Account No", "Holder Name", "Email", "Phone", "Balance", "Type", "Status");
            System.out.println("---------------------------------------------------------------------------------------------------");

            for (Account account : accounts) {
                System.out.printf("%-15s %-20s %-25s %-12s %-15.2f %-10s %-10s%n",
                        account.getAccountNumber(),
                        account.getHolderName(),
                        account.getEmail().length() > 23 ? account.getEmail().substring(0, 20) + "..." : account.getEmail(),
                        account.getPhone(),
                        account.getBalance(),
                        account.getAccountType(),
                        account.getStatus());
            }

            System.out.println("Total Accounts: " + accounts.size());
        } catch (Exception e) {
            System.out.println("Error listing accounts: " + e.getMessage());
            logError("List accounts error", e);
        }
    }

    private static void searchAccountByNumber(Connection c) {
        try {
            System.out.print("Enter Account Number to search: ");
            String accountNumber = s.nextLine().trim();

            Account account = accountManager.getAccountFromDB(c, accountNumber);
            if (account != null) {
                viewFullAccountDetails(c, accountNumber);
            } else {
                System.out.println("Account not found!");
            }
        } catch (Exception e) {
            System.out.println("Error searching account: " + e.getMessage());
            logError("Account search error", e);
        }
    }

    private static void searchAccountByName(Connection c) {
        try {
            System.out.print("Enter Holder Name to search: ");
            String holderName = s.nextLine().trim();

            List<Account> accounts = accountManager.searchAccountsByName(c, holderName);
            if (accounts.isEmpty()) {
                System.out.println("No accounts found with that name!");
                return;
            }

            System.out.println("\n=== Search Results ===");
            for (Account account : accounts) {
                System.out.printf("Account: %s, Holder: %s, Email: %s, Balance: %.2f, Status: %s%n",
                        account.getAccountNumber(),
                        account.getHolderName(),
                        account.getEmail(),
                        account.getBalance(),
                        account.getStatus());
            }
        } catch (Exception e) {
            System.out.println("Error searching accounts: " + e.getMessage());
            logError("Account search error", e);
        }
    }

    private static void viewSystemStatistics(Connection c) {
        try {
            adminManager.viewSystemStatistics(c);
        } catch (Exception e) {
            System.out.println("Error viewing statistics: " + e.getMessage());
            logError("Statistics error", e);
        }
    }

    private static void viewAllTransactions(Connection c) {
        try {
            transactionLogger.printAllTransactionsFromDB(c);
        } catch (Exception e) {
            System.out.println("Error viewing transactions: " + e.getMessage());
            logError("Transactions view error", e);
        }
    }

    private static void viewMyTransactionHistory(Connection c, String accountNumber) {
        try {
            System.out.println("\n=== My Transaction History ===");
            transactionLogger.printUserTransactionHistoryFromDB(c, accountNumber);
        } catch (Exception e) {
            System.out.println("Error viewing transaction history: " + e.getMessage());
            logError("Transaction history error", e);
        }
    }

    private static void viewAccountDetails(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account != null) {
                System.out.println("\n=== Account Details ===");
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Holder Name: " + account.getHolderName());
                System.out.println("Email: " + account.getEmail());
                System.out.println("Phone: " + account.getPhone());
                System.out.println("IFSC Code: " + account.getIfsc());
                System.out.println("Account Type: " + account.getAccountType());
                System.out.println("Balance: " + account.getBalance());
                System.out.println("Status: " + account.getStatus());
                System.out.println("Account Created: " + account.getCreatedAt());
            } else {
                throw new BankException("Account not found!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error viewing account details: " + e.getMessage());
            logError("Account details error", e);
        }
    }

    private static void viewFullAccountDetails(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccountFromDB(c, accountNumber);
            if (account != null) {
                System.out.println("\n=== Full Account Details ===");
                System.out.println("Account Number: " + account.getAccountNumber());
                System.out.println("Holder Name: " + account.getHolderName());
                System.out.println("Email: " + account.getEmail());
                System.out.println("Phone: " + account.getPhone());
                System.out.println("IFSC Code: " + account.getIfsc());
                System.out.println("Account Type: " + account.getAccountType());
                System.out.println("Balance: " + account.getBalance());
                System.out.println("Status: " + account.getStatus());
                System.out.println("Last Activity: " + account.getLastActivity());
                System.out.println("Account Created: " + account.getCreatedAt());

                System.out.println("\n=== Recent Transactions ===");
                transactionLogger.printUserTransactionHistoryFromDB(c, accountNumber);
            } else {
                throw new BankException("Account not found!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error viewing account details: " + e.getMessage());
            logError("Account details error", e);
        }
    }

    private static void viewBalanceForAdmin(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account != null) {
                System.out.println("Current Balance for account " + accountNumber + ": " + account.getBalance());
            } else {
                throw new BankException("Account not found!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error checking balance: " + e.getMessage());
            logError("Balance check error", e);
        }
    }

    private static void viewTransactionHistoryForAdmin(Connection c, String accountNumber) {
        try {
            System.out.println("\n=== Transaction History for Account: " + accountNumber + " ===");
            transactionLogger.printUserTransactionHistoryFromDB(c, accountNumber);
        } catch (Exception e) {
            System.out.println("Error viewing transaction history: " + e.getMessage());
            logError("Transaction history error", e);
        }
    }

    private static void forceDeposit(Connection c, String accountNumber) {
        try {
            System.out.print("Enter amount to deposit: ");
            BigDecimal amount = getValidAmount();

            System.out.print("Enter description for this deposit: ");
            String description = s.nextLine().trim();

            boolean success = accountManager.deposit(c, accountNumber, amount);
            if (success) {
                Account account = accountManager.getAccount(accountNumber);
                transactionLogger.logTransactionToDB(c,
                        TransactionType.DEPOSIT,
                        null,
                        accountNumber,
                        amount,
                        "ADMIN: " + description + " for " + account.getHolderName()
                );
                System.out.println("Force deposit successful!");
                System.out.println("New Balance: " + accountManager.getAccount(accountNumber).getBalance());
            } else {
                throw new BankException("Force deposit failed!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in force deposit: " + e.getMessage());
            logError("Force deposit error", e);
        }
    }

    private static void forceWithdrawal(Connection c, String accountNumber) {
        try {
            System.out.print("Enter amount to withdraw: ");
            BigDecimal amount = getValidAmount();

            System.out.print("Enter description for this withdrawal: ");
            String description = s.nextLine().trim();

            boolean success = accountManager.withdraw(c, accountNumber, amount);
            if (success) {
                Account account = accountManager.getAccount(accountNumber);
                transactionLogger.logTransactionToDB(c,
                        TransactionType.WITHDRAW,
                        accountNumber,
                        null,
                        amount,
                        "ADMIN: " + description + " from " + account.getHolderName()
                );
                System.out.println("Force withdrawal successful!");
                System.out.println("New Balance: " + accountManager.getAccount(accountNumber).getBalance());
            } else {
                throw new BankException("Force withdrawal failed!");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error in force withdrawal: " + e.getMessage());
            logError("Force withdrawal error", e);
        }
    }

    private static void updateAccountInformation(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccountFromDB(c, accountNumber);
            if (account == null) {
                throw new BankException("Account not found!");
            }

            System.out.println("\nCurrent Information:");
            System.out.println("1. Holder Name: " + account.getHolderName());
            System.out.println("2. Email: " + account.getEmail());
            System.out.println("3. Phone: " + account.getPhone());
            System.out.println("4. IFSC Code: " + account.getIfsc());
            System.out.println("5. Account Type: " + account.getAccountType());

            System.out.print("\nEnter field number to update (1-5) or 0 to cancel: ");
            int field = s.nextInt();
            s.nextLine();

            switch (field) {
                case 1 -> {
                    System.out.print("Enter new Holder Name: ");
                    String newName = s.nextLine().trim();
                    while (!isValidHolderName(newName)) {
                        System.out.print("Invalid name! Use only alphabets and spaces: ");
                        newName = s.nextLine().trim();
                    }
                    if (adminManager.updateAccountField(c, accountNumber, "holder_name", newName)) {
                        System.out.println("Holder name updated successfully!");
                    }
                }
                case 2 -> {
                    System.out.print("Enter new Email: ");
                    String newEmail = s.nextLine().trim();
                    if (emailP.matcher(newEmail).matches()) {
                        if (adminManager.updateAccountField(c, accountNumber, "email", newEmail)) {
                            System.out.println("Email updated successfully!");
                        }
                    } else {
                        System.out.println("Invalid email format!");
                    }
                }
                case 3 -> {
                    System.out.print("Enter new Phone: ");
                    String newPhone = s.nextLine().trim();
                    if (phoneP.matcher(newPhone).matches()) {
                        if (adminManager.updateAccountField(c, accountNumber, "phone", newPhone)) {
                            System.out.println("Phone updated successfully!");
                        }
                    } else {
                        System.out.println("Invalid phone number!");
                    }
                }
                case 4 -> {
                    System.out.print("Enter new IFSC Code: ");
                    String newIfsc = s.nextLine().trim();
                    if (adminManager.updateAccountField(c, accountNumber, "ifsc", newIfsc)) {
                        System.out.println("IFSC Code updated successfully!");
                    }
                }
                case 5 -> {
                    System.out.print("Enter new Account Type (SAVINGS/CURRENT): ");
                    String newType = s.nextLine().trim().toUpperCase();
                    while (!isValidAccountType(newType)) {
                        System.out.print("Invalid account type! Enter exactly 'SAVINGS' or 'CURRENT': ");
                        newType = s.nextLine().trim().toUpperCase();
                    }
                    if (adminManager.updateAccountField(c, accountNumber, "account_type", newType)) {
                        System.out.println("Account type updated successfully!");
                    }
                }
                case 0 -> { return; }
                default -> System.out.println("Invalid field number!");
            }

            accountManager.loadAccountsFromDatabase(c);

        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error updating account information: " + e.getMessage());
            logError("Account update error", e);
        }
    }

    private static void deleteAccount(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account == null) {
                throw new BankException("Account not found!");
            }

            System.out.println("\n=== Account to be Deleted ===");
            System.out.println("Account Number: " + account.getAccountNumber());
            System.out.println("Holder Name: " + account.getHolderName());
            System.out.println("Balance: " + account.getBalance());

            System.out.print("\nAre you sure you want to delete this account? (yes/no): ");
            String confirmation = s.nextLine().trim();

            if ("yes".equalsIgnoreCase(confirmation)) {
                boolean success = adminManager.deleteAccount(c, accountNumber);
                if (success) {
                    System.out.println("Account deleted successfully!");
                    accountManager.loadAccountsFromDatabase(c);
                } else {
                    throw new BankException("Failed to delete account!");
                }
            } else {
                System.out.println("Account deletion cancelled.");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error deleting account: " + e.getMessage());
            logError("Account deletion error", e);
        }
    }

    private static void toggleAccountStatus(Connection c, String accountNumber) {
        try {
            Account account = accountManager.getAccount(accountNumber);
            if (account == null) {
                throw new BankException("Account not found!");
            }

            String newStatus = "ACTIVE".equals(account.getStatus()) ? "FROZEN" : "ACTIVE";
            String action = "ACTIVE".equals(account.getStatus()) ? "freeze" : "unfreeze";

            System.out.print("Are you sure you want to " + action + " account " + accountNumber + "? (yes/no): ");
            String confirmation = s.nextLine().trim();

            if ("yes".equalsIgnoreCase(confirmation)) {
                boolean success = adminManager.updateAccountField(c, accountNumber, "status", newStatus);
                if (success) {
                    System.out.println("Account " + action + "d successfully!");
                    accountManager.loadAccountsFromDatabase(c);
                } else {
                    throw new BankException("Failed to " + action + " account!");
                }
            } else {
                System.out.println("Operation cancelled.");
            }
        } catch (BankException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error toggling account status: " + e.getMessage());
            logError("Account status toggle error", e);
        }
    }

    private static BigDecimal getValidAmount() {
        while (true) {
            try {
                BigDecimal amount = new BigDecimal(s.nextLine().trim());
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.print("Amount must be greater than zero! Try again: ");
                    continue;
                }
                if (amount.scale() > 2) {
                    System.out.print("Amount can have maximum 2 decimal places! Try again: ");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                System.out.print("Invalid amount format! Please enter a valid number: ");
            } catch (Exception e) {
                System.out.print("Invalid amount! Please enter again: ");
            }
        }
    }

    private static boolean verifyPassword(Connection c, String accountNumber, String password) {
        try (PreparedStatement ps = c.prepareStatement("SELECT password_hash FROM accounts WHERE account_number = ?")) {
            ps.setString(1, accountNumber);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) {
                    return false;
                }
                String storedHash = r.getString("password_hash");
                String inputHash = hash(password);
                return inputHash.equals(storedHash);
            }
        } catch (SQLException e) {
            logError("Password verification error", e);
            return false;
        }
    }

    private static String hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error", e);
        }
    }

    private static void logError(String message, Exception e) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("error_log.txt", true))) {
            pw.printf("%s: %s - %s%n", LocalDateTime.now(), message, e.getMessage());
            e.printStackTrace(pw);
        } catch (IOException ioException) {
            System.out.println("Failed to write to error log: " + ioException.getMessage());
        }
    }
}