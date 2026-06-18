package com.softcrafts.bankkata.application.port.in;

import com.softcrafts.bankkata.domain.Transaction;

import java.math.BigDecimal;
import java.util.List;

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

    List<Transaction> getStatement();
}
