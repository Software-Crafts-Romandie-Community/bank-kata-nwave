package com.softcrafts.bankkata.application.port.in;

// SCAFFOLD: true
// Primary port (driving port) — contract for account use cases exposed to AccountController.
// No Spring annotations — pure Java interface.

import java.math.BigDecimal;

/**
 * Primary port (driving port) — behavioural contract for account operations.
 *
 * Implementations: AccountService (production)
 * Consumers: AccountController (@RestController)
 *
 * No Spring annotations — the interface is independent of the framework.
 * Spring injects the implementation at composition time via BankApplication.
 */
public interface AccountUseCase {

    // SCAFFOLD: true

    /**
     * Retrieve the current account balance without modifying it.
     *
     * @return the current balance (always >= 0)
     */
    BigDecimal getBalance();

    /**
     * Deposit a strictly positive amount onto the account.
     *
     * @param amount the amount to deposit — must be > 0
     * @return the updated balance after the deposit
     * @throws IllegalArgumentException if amount is null, zero, or negative
     */
    BigDecimal deposit(BigDecimal amount);

    /**
     * Withdraw a strictly positive amount from the account if funds are sufficient.
     *
     * @param amount the amount to withdraw — must be > 0 and <= current balance
     * @return the updated balance after the withdrawal
     * @throws IllegalArgumentException if amount is null, zero, or negative
     * @throws com.softcrafts.bankkata.domain.InsufficientFundsException if amount > balance
     */
    BigDecimal withdraw(BigDecimal amount);
}
