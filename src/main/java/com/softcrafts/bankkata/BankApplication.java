package com.softcrafts.bankkata;

import com.softcrafts.bankkata.application.AccountService;
import com.softcrafts.bankkata.application.StatementService;
import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import com.softcrafts.bankkata.application.port.in.StatementUseCase;
import com.softcrafts.bankkata.application.port.out.AccountRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot composition root.
 *
 * Declares AccountService/StatementService as beans to keep Spring annotations out of the
 * hexagonal core. StatementUseCase (Phase 2) is read-only -- it reuses AccountRepository in
 * lecture seule, no new driven port (D3, brief.md).
 */
@SpringBootApplication
public class BankApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankApplication.class, args);
    }

    @Bean
    public AccountUseCase accountUseCase(AccountRepository accountRepository) {
        return new AccountService(accountRepository);
    }

    @Bean
    public StatementUseCase statementUseCase(AccountRepository accountRepository) {
        return new StatementService(accountRepository);
    }
}
