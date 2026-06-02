package com.softcrafts.bankkata.acceptance.config;

// SCAFFOLD: true

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Cucumber + Spring Boot integration configuration.
 *
 * Wires the full Spring application context (production composition root) with
 * MockMvc as the driving adapter for HTTP acceptance tests.
 *
 * webEnvironment = MOCK: no real Tomcat started — MockMvc exercises the full
 * controller → service → domain → repository chain in-process.
 *
 * InMemoryAccountRepository is a real @Component bean in this context.
 * Its state is reset in @BeforeEach inside AccountManagementSteps.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CucumberSpringConfiguration {
    // No body needed — annotation-driven configuration.
}
