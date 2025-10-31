package com.banking.system;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDao {

    public String generateNextAccountNumber(Connection conn) throws SQLException {
        String sql = "SELECT last_account_number FROM account_sequence FOR UPDATE";
        String updateSql = "UPDATE account_sequence SET last_account_number = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                long lastNumber = rs.getLong("last_account_number");
                long newNumber = lastNumber + 1;

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setLong(1, newNumber);
                    updateStmt.executeUpdate();
                }

                return String.format("%010d", newNumber); // 10-digit account number with leading zeros
            }
            throw new SQLException("Could not generate account number");
        }
    }

    public boolean createAccount(Connection conn, Account account, String passwordHash) throws SQLException {
        String sql = "INSERT INTO accounts(account_number, holder_name, email, phone, ifsc, balance, password_hash, account_type, status, created_at) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getAccountNumber());
            pstmt.setString(2, account.getHolderName());
            pstmt.setString(3, account.getEmail());
            pstmt.setString(4, account.getPhone());
            pstmt.setString(5, account.getIfsc());
            pstmt.setBigDecimal(6, account.getBalance());
            pstmt.setString(7, passwordHash);
            pstmt.setString(8, account.getAccountType());
            pstmt.setString(9, account.getStatus());
            pstmt.setTimestamp(10, Timestamp.valueOf(account.getCreatedAt()));

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Failed to create account in database: " + e.getMessage(), e);
        }
    }

    public Account findByAccountNumber(Connection conn, String accountNumber) throws SQLException {
        String sql = "SELECT account_number, holder_name, email, phone, ifsc, balance, account_type, status, created_at, last_activity " +
                "FROM accounts WHERE account_number = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Account account = new Account(
                            rs.getString("account_number"),
                            rs.getString("holder_name"),
                            rs.getString("email"),
                            rs.getBigDecimal("balance")
                    );
                    account.setPhone(rs.getString("phone"));
                    account.setIfsc(rs.getString("ifsc"));
                    account.setAccountType(rs.getString("account_type"));
                    account.setStatus(rs.getString("status"));

                    Timestamp lastActivity = rs.getTimestamp("last_activity");
                    if (lastActivity != null) {
                        account.setLastActivity(lastActivity.toLocalDateTime());
                    }

                    return account;
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to find account: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Account> listAllAccounts(Connection conn) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, holder_name, email, phone, ifsc, balance, account_type, status, created_at " +
                "FROM accounts ORDER BY created_at DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Account account = new Account(
                        rs.getString("account_number"),
                        rs.getString("holder_name"),
                        rs.getString("email"),
                        rs.getBigDecimal("balance")
                );
                account.setPhone(rs.getString("phone"));
                account.setIfsc(rs.getString("ifsc"));
                account.setAccountType(rs.getString("account_type"));
                account.setStatus(rs.getString("status"));
                accounts.add(account);
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to list accounts: " + e.getMessage(), e);
        }
        return accounts;
    }

    public List<Account> searchAccountsByName(Connection conn, String holderName) throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT account_number, holder_name, email, phone, ifsc, balance, account_type, status, created_at " +
                "FROM accounts WHERE holder_name LIKE ? ORDER BY holder_name";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + holderName + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account account = new Account(
                            rs.getString("account_number"),
                            rs.getString("holder_name"),
                            rs.getString("email"),
                            rs.getBigDecimal("balance")
                    );
                    account.setPhone(rs.getString("phone"));
                    account.setIfsc(rs.getString("ifsc"));
                    account.setAccountType(rs.getString("account_type"));
                    account.setStatus(rs.getString("status"));
                    accounts.add(account);
                }
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to search accounts: " + e.getMessage(), e);
        }
        return accounts;
    }

    public boolean updateBalance(Connection conn, String accountNumber, BigDecimal newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ?, last_activity = CURRENT_TIMESTAMP WHERE account_number = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setString(2, accountNumber);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new SQLException("Failed to update balance: " + e.getMessage(), e);
        }
    }
}