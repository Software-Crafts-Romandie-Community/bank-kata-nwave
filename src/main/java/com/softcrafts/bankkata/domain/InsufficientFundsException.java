package com.softcrafts.bankkata.domain;

// SCAFFOLD: true
// Domain exception — raised by Account.withdraw() when requested amount exceeds balance.
// Non-checked (extends RuntimeException) — pure business signal, no infrastructure coupling.

import java.math.BigDecimal;

/**
 * Domain exception signalling that a withdrawal was rejected because the requested
 * amount exceeds the current account balance.
 *
 * Carries the available balance so the HTTP adapter can include it in the RFC 7807
 * Problem Details response without calling the repository a second time.
 *
 * No Spring annotations — pure domain object.
 */
public class InsufficientFundsException extends RuntimeException {

    // SCAFFOLD: true
    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super("Insufficient funds: available=" + availableBalance + ", requested=" + requestedAmount);
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public BigDecimal getAvailableBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    public BigDecimal getRequestedAmount() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
