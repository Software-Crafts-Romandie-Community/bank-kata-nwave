package com.softcrafts.bankkata.domain;

// SCAFFOLD: true
// Aggregate — maintains account balance (BigDecimal), enforces balance >= 0 rule,
// raises InsufficientFundsException when withdrawal would breach the invariant.

import java.math.BigDecimal;
import java.util.ArrayList;
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

    // SCAFFOLD: true
    private BigDecimal balance;
    private final List<Transaction> transactions;

    public Account() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public BigDecimal getBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public void deposit(BigDecimal amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public void withdraw(BigDecimal amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public List<Transaction> getTransactions() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
