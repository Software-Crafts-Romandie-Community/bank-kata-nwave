package com.softcrafts.bankkata.application;

// SCAFFOLD: true
// Implements AccountUseCase — orchestrates domain + AccountRepository.
// No Spring annotations in this class — pure application service.
// Spring injects it as an AccountUseCase bean via BankApplication composition root.

import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import com.softcrafts.bankkata.application.port.out.AccountRepository;

import java.math.BigDecimal;

/**
 * Application service implementing the AccountUseCase driving port.
 *
 * Responsibilities:
 * - Delegates use-case operations to the Account domain aggregate
 * - Persists state changes via AccountRepository
 * - Contains zero HTTP or Spring logic
 *
 * No Spring annotations — declared as a bean by the composition root (BankApplication).
 */
public class AccountService implements AccountUseCase {

    // SCAFFOLD: true
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Override
    public BigDecimal getBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Override
    public BigDecimal deposit(BigDecimal amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
