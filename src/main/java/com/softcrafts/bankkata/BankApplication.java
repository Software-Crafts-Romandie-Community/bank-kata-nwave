package com.softcrafts.bankkata;

// SCAFFOLD: true
// Composition root — @SpringBootApplication replaces the manual Main.java from the kata.
// Wires AccountController -> AccountUseCase -> AccountService -> AccountRepository -> InMemoryAccountRepository.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot composition root — BankApplication.
 *
 * Replaces the manual Main.java from the CLI kata.
 * The Spring IoC container handles all wiring:
 *   AccountController <- AccountUseCase (injected as AccountService)
 *   AccountService <- AccountRepository (injected as InMemoryAccountRepository @Component)
 *
 * Static resources (index.html, app.js) are served from src/main/resources/static/.
 * Server port: 8080 (Spring Boot default).
 */
@SpringBootApplication
public class BankApplication {

    // SCAFFOLD: true

    public static void main(String[] args) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
