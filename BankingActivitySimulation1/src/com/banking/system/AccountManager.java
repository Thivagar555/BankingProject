package com.banking.system;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AccountManager {
    private Map<String, Account> accounts = new HashMap<>();
    private AccountDao accountDao = new AccountDao();

    public String generateAccountNumber(Connection conn) throws SQLException {
        return accountDao.generateNextAccountNumber(conn);
    }

    public boolean createAccount(Connection conn, Account account, String passwordHash) {
        try {
            boolean dbSuccess = accountDao.createAccount(conn, account, passwordHash);
            if (dbSuccess) {
                accounts.put(account.getAccountNumber(), account);
                return true;
            }
        } catch (Exception e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
        return false;
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }

    public Account getAccountFromDB(Connection conn, String accountNumber) {
        try {
            return accountDao.findByAccountNumber(conn, accountNumber);
        } catch (SQLException e) {
            System.out.println("Error fetching account from DB: " + e.getMessage());
            return null;
        }
    }

    public Account findAccountByName(Connection conn, String holderName) {
        try {
            List<Account> accounts = accountDao.searchAccountsByName(conn, holderName);
            if (accounts.size() == 1) {
                return accounts.get(0);
            } else if (accounts.size() > 1) {
                System.out.println("\nMultiple accounts found with similar names:");
                for (int i = 0; i < accounts.size(); i++) {
                    Account acc = accounts.get(i);
                    System.out.printf("%d. Account: %s, Holder: %s, Phone: %s%n",
                            i + 1, acc.getAccountNumber(), acc.getHolderName(), acc.getPhone());
                }
                System.out.print("Select account number (1-" + accounts.size() + "): ");
                Scanner s = new Scanner(System.in);
                int choice = s.nextInt();
                s.nextLine();
                if (choice > 0 && choice <= accounts.size()) {
                    return accounts.get(choice - 1);
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("Error finding account by name: " + e.getMessage());
            return null;
        }
    }

    public List<Account> listAllAccounts(Connection conn) {
        try {
            return accountDao.listAllAccounts(conn);
        } catch (SQLException e) {
            System.out.println("Error listing accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Account> searchAccountsByName(Connection conn, String holderName) {
        try {
            return accountDao.searchAccountsByName(conn, holderName);
        } catch (SQLException e) {
            System.out.println("Error searching accounts: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void loadAccountsFromDatabase(Connection conn) {
        try {
            List<Account> dbAccounts = accountDao.listAllAccounts(conn);
            accounts.clear();
            for (Account account : dbAccounts) {
                accounts.put(account.getAccountNumber(), account);
            }
            System.out.println("Loaded " + dbAccounts.size() + " accounts from database.");
        } catch (SQLException e) {
            System.out.println("Error loading accounts from database: " + e.getMessage());
        }
    }

    public boolean deposit(Connection conn, String accountNumber, BigDecimal amount) {
        try {
            Account account = accounts.get(accountNumber);
            if (account == null) {
                throw new BankException("Account not found!");
            }

            account.deposit(amount);
            return accountDao.updateBalance(conn, accountNumber, account.getBalance());
        } catch (BankException e) {
            System.out.println("Error during deposit: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error during deposit: " + e.getMessage());
            return false;
        }
    }

    public boolean withdraw(Connection conn, String accountNumber, BigDecimal amount) {
        try {
            Account account = accounts.get(accountNumber);
            if (account == null) {
                throw new BankException("Account not found!");
            }

            account.withdraw(amount);
            return accountDao.updateBalance(conn, accountNumber, account.getBalance());
        } catch (BankException e) {
            System.out.println("Error during withdrawal: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error during withdrawal: " + e.getMessage());
            return false;
        }
    }

    public boolean transfer(Connection conn, String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        try {
            Account fromAccount = accounts.get(fromAccountNumber);
            Account toAccount = accounts.get(toAccountNumber);

            if (fromAccount == null || toAccount == null) {
                throw new BankException("One or both accounts not found!");
            }

            if (fromAccountNumber.equals(toAccountNumber)) {
                throw new BankException("Cannot transfer to the same account!");
            }

            fromAccount.withdraw(amount);
            toAccount.deposit(amount);

            boolean success1 = accountDao.updateBalance(conn, fromAccountNumber, fromAccount.getBalance());
            boolean success2 = accountDao.updateBalance(conn, toAccountNumber, toAccount.getBalance());

            return success1 && success2;
        } catch (BankException e) {
            System.out.println("Error during transfer: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Unexpected error during transfer: " + e.getMessage());
            return false;
        }
    }
}