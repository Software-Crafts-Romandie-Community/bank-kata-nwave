package com.softcrafts.bankkata.adapter.out;

// SCAFFOLD: true
// Driven adapter — implements AccountRepository using in-memory storage.
// @Component: singleton Spring bean — account state preserved across HTTP requests.
// Exposes reset() for test @BeforeEach cleanup.

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
 *
 * Probe method — called at startup via ApplicationRunner to validate the adapter
 * before accepting traffic. Failure → structured health event + startup refusal.
 */
@Component
public class InMemoryAccountRepository implements AccountRepository {

    // SCAFFOLD: true

    @Override
    public Account load() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Override
    public void save(Account account) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    /**
     * Reset the in-memory state to a fresh account.
     * Called in @BeforeEach during acceptance tests to guarantee isolation between scenarios.
     */
    public void reset() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    /**
     * Startup probe — validates create / load / mutate / reload round-trip.
     * Called via ApplicationRunner at context startup.
     * Throws RuntimeException on failure to prevent the application from accepting traffic.
     */
    public void probe() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
