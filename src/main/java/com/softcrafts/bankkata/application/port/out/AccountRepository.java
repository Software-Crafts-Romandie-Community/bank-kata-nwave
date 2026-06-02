package com.softcrafts.bankkata.application.port.out;

// SCAFFOLD: true
// Secondary port (driven port) — contract for loading and saving the account.
// No Spring annotations — pure Java interface.

import com.softcrafts.bankkata.domain.Account;

/**
 * Secondary port (driven port) — behavioural contract for account persistence.
 *
 * Implementations: InMemoryAccountRepository (@Component — Phase 1)
 * Consumers: AccountService (application layer)
 *
 * No Spring annotations — the interface is independent of the framework.
 */
public interface AccountRepository {

    // SCAFFOLD: true

    /**
     * Load the current (unique) account.
     * Phase 1 manages a single account — no account ID required.
     *
     * @return the current Account aggregate
     */
    Account load();

    /**
     * Persist the current state of the account after an operation.
     *
     * @param account the account to save
     */
    void save(Account account);
}
