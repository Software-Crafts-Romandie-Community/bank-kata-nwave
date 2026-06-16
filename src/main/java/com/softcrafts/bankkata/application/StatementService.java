package com.softcrafts.bankkata.application;

// SCAFFOLD: true -- created by DISTILL (phase2-transaction-history). RED until DELIVER.

import com.softcrafts.bankkata.application.port.in.StatementUseCase;
import com.softcrafts.bankkata.application.port.out.AccountRepository;
import com.softcrafts.bankkata.domain.Transaction;

import java.time.Instant;
import java.util.List;

/**
 * Application service implementing the StatementUseCase driving port.
 *
 * Strictly read-only -- must never call AccountRepository.save() (Mandate 12, ADR-004).
 * Pipeline order (D11, ADR-005): filter by date -> sort -> paginate.
 *
 * No Spring annotations -- declared as a bean by the composition root (BankApplication).
 */
public class StatementService implements StatementUseCase {

    private final AccountRepository accountRepository;

    public StatementService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Transaction> getStatement(Instant from, Instant to, int page, int size, String sortBy, String sortDir) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
