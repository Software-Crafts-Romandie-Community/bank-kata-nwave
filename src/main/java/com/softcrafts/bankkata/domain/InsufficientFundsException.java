package com.softcrafts.bankkata.domain;

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

    private final BigDecimal availableBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(BigDecimal availableBalance, BigDecimal requestedAmount) {
        super("Insufficient funds: available=" + availableBalance + ", requested=" + requestedAmount);
        this.availableBalance = availableBalance;
        this.requestedAmount = requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
