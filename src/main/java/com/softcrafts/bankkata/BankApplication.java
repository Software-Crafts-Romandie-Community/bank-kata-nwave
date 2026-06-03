package com.softcrafts.bankkata;

import com.softcrafts.bankkata.application.AccountService;
import com.softcrafts.bankkata.application.port.in.AccountUseCase;
import com.softcrafts.bankkata.application.port.out.AccountRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot composition root.
 *
 * Declares AccountService as a bean to keep Spring annotations out of the hexagonal core.
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
}
