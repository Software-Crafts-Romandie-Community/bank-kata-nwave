package com.softcrafts.bankkata.application.port.out;

import com.softcrafts.bankkata.domain.Account;

/**
 * Secondary port (driven port) — behavioural contract for account persistence.
 *
 * No Spring annotations — the interface is independent of the framework.
 */
public interface AccountRepository {

    Account load();

    void save(Account account);
}
