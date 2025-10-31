package com.banking.system;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Enhanced Account class with more fields
public class Account {
    private String accountNumber;
    private String holderName;
    private String email;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private String phone;
    private String ifsc;
    private String accountType;
    private String status;
    private LocalDateTime lastActivity;

    public Account(String accountNumber, String holderName, String email, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.email = email;
        this.balance = balance;
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
        this.accountType = "SAVINGS";
        this.lastActivity = LocalDateTime.now();
    }

    public void deposit(BigDecimal amount) throws BankException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Deposit amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new BankException("Amount cannot have more than 2 decimal places");
        }
        this.balance = this.balance.add(amount);
        this.lastActivity = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) throws BankException {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankException("Withdrawal amount must be positive");
        }
        if (amount.scale() > 2) {
            throw new BankException("Amount cannot have more than 2 decimal places");
        }
        if (amount.compareTo(balance) > 0) {
            throw new BankException("Insufficient balance. Available: " + balance);
        }
        this.balance = this.balance.subtract(amount);
        this.lastActivity = LocalDateTime.now();
    }

    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public String getEmail() { return email; }
    public BigDecimal getBalance() { return balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getIfsc() { return ifsc; }
    public void setIfsc(String ifsc) { this.ifsc = ifsc; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
}