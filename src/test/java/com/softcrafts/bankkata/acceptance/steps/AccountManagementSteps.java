package com.softcrafts.bankkata.acceptance.steps;

// SCAFFOLD: true
// Step definitions for phase1-account-management acceptance tests.
// Driving port: MockMvc (HTTP REST API) via @SpringBootTest(webEnvironment = MOCK).
// Driven internal: InMemoryAccountRepository (real @Component bean, reset in @BeforeEach).
// Per DISTILL mandate: business language in step names, technical detail here only.

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Cucumber step definitions for the account-management feature.
 *
 * Tier A acceptance tests — production Spring context via MockMvc.
 * All step methods delegate to the production composition root (AccountUseCase).
 * Zero business logic in this class.
 *
 * Walking skeleton scenario is the ONLY enabled scenario.
 * All others are @Disabled("pending") — enabled one at a time in DELIVER.
 */
public class AccountManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // State carried between Given / When / Then steps within a scenario
    private ResultActions lastResult;

    // -------------------------------------------------------------------------
    // Lifecycle — reset InMemoryAccountRepository state before each scenario
    // -------------------------------------------------------------------------

    @Before
    public void resetAccountState() throws Exception {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    // -------------------------------------------------------------------------
    // Given steps
    // -------------------------------------------------------------------------

    @Given("a new bank account with no transactions")
    public void aNewBankAccountWithNoTransactions() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Given("the customer has already deposited {double} euros")
    public void theCustomerHasAlreadyDeposited(double amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    // -------------------------------------------------------------------------
    // When steps
    // -------------------------------------------------------------------------

    @When("the customer checks their balance")
    public void theCustomerChecksTheirBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @When("the customer deposits {double} euros")
    public void theCustomerDeposits(double amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @When("the customer attempts to deposit {double} euros")
    public void theCustomerAttemptsToDeposit(double amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @When("the customer withdraws {double} euros")
    public void theCustomerWithdraws(double amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @When("the customer attempts to withdraw {double} euros")
    public void theCustomerAttemptsToWithdraw(double amount) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @When("the customer attempts to withdraw any amount greater than their balance")
    public void theCustomerAttemptsToWithdrawAnyAmountGreaterThanTheirBalance() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    // -------------------------------------------------------------------------
    // Then steps
    // -------------------------------------------------------------------------

    @Then("the balance shown is {double} euros")
    public void theBalanceShownIs(double expected) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Then("the deposit is refused with an invalid amount message")
    public void theDepositIsRefusedWithAnInvalidAmountMessage() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Then("the withdrawal is refused with an invalid amount message")
    public void theWithdrawalIsRefusedWithAnInvalidAmountMessage() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Then("the withdrawal is refused with an insufficient funds message showing {double} euros available")
    public void theWithdrawalIsRefusedWithInsufficientFundsMessageShowing(double availableBalance) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @Then("the balance shown is never below {double} euros")
    public void theBalanceShownIsNeverBelow(double floor) {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }

    @And("the balance shown is {double} euros")
    public void andTheBalanceShownIs(double expected) {
        // Delegates to same assertion as Then — reuse step definition
        theBalanceShownIs(expected);
    }

    @And("the withdrawal is refused with an insufficient funds message")
    public void andTheWithdrawalIsRefusedWithInsufficientFundsMessage() {
        throw new AssertionError("Not yet implemented -- RED scaffold");
    }
}
