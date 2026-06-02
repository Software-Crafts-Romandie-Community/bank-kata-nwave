package com.softcrafts.bankkata.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Account aggregate — core domain object.
 *
 * Rules enforced:
 * - Balance is always >= 0.00
 * - Deposits must be strictly positive
 * - Withdrawals must be strictly positive and not exceed the current balance
 *
 * No Spring annotations — testable without the framework.
 */
public class Account {

    private BigDecimal balance;
    private final List<Transaction> transactions;

    public Account() {
        this.balance = BigDecimal.ZERO;
        this.transactions = new ArrayList<>();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly positive");
        }
        balance = balance.add(amount);
        transactions.add(new Transaction(Transaction.Type.DEPOSIT, amount, Instant.now()));
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
        }
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance = balance.subtract(amount);
        transactions.add(new Transaction(Transaction.Type.WITHDRAWAL, amount, Instant.now()));
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
