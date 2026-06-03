package com.softcrafts.bankkata.application;

import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import com.softcrafts.bankkata.application.port.out.AccountRepository;
import com.softcrafts.bankkata.domain.Account;

import java.math.BigDecimal;

/**
 * Application service implementing the AccountUseCase driving port.
 *
 * No Spring annotations — declared as a bean by the composition root (BankApplication).
 */
public class AccountService implements AccountUseCase {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public BigDecimal getBalance() {
        return accountRepository.load().getBalance();
    }

    @Override
    public BigDecimal deposit(BigDecimal amount) {
        Account account = accountRepository.load();
        account.deposit(amount);
        accountRepository.save(account);
        return account.getBalance();
    }

    @Override
    public BigDecimal withdraw(BigDecimal amount) {
        Account account = accountRepository.load();
        account.withdraw(amount);
        accountRepository.save(account);
        return account.getBalance();
    }
}
