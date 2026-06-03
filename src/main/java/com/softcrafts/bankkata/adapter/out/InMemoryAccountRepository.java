package com.softcrafts.bankkata.adapter.out;

import com.softcrafts.bankkata.application.port.out.AccountRepository;
import com.softcrafts.bankkata.domain.Account;
import org.springframework.stereotype.Component;

/**
 * In-memory implementation of AccountRepository.
 *
 * Singleton Spring @Component — the account state is preserved between HTTP
 * requests for the lifetime of the process. Restarting the server resets to zero
 * (documented Phase 1 behaviour).
 *
 * Exposes reset() for test teardown (called in @BeforeEach within step definitions).
 */
@Component
public class InMemoryAccountRepository implements AccountRepository {

    private Account account;

    public InMemoryAccountRepository() {
        this.account = new Account();
    }

    @Override
    public Account load() {
        return account;
    }

    @Override
    public void save(Account account) {
        this.account = account;
    }

    /**
     * Reset the in-memory state to a fresh account.
     * Called in @Before during acceptance tests to guarantee isolation between scenarios.
     */
    public void reset() {
        this.account = new Account();
    }
}
