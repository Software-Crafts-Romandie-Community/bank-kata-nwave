package com.softcrafts.bankkata.application.port.in;

import java.math.BigDecimal;

/**
 * Primary port (driving port) — behavioural contract for account operations.
 *
 * No Spring annotations — the interface is independent of the framework.
 * Spring injects the implementation at composition time via BankApplication.
 */
public interface AccountUseCase {

    BigDecimal getBalance();

    BigDecimal deposit(BigDecimal amount);

    BigDecimal withdraw(BigDecimal amount);
}
