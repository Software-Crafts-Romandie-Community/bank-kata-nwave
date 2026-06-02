package com.softcrafts.bankkata.acceptance.steps;

import com.softcrafts.bankkata.adapter.out.InMemoryAccountRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

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
 * All others are @skip — enabled one at a time in DELIVER.
 */
public class AccountManagementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryAccountRepository accountRepository;

    // State carried between Given / When / Then steps within a scenario
    private ResultActions lastResult;

    // -------------------------------------------------------------------------
    // Lifecycle — reset InMemoryAccountRepository state before each scenario
    // -------------------------------------------------------------------------

    @Before
    public void resetAccountState() {
        accountRepository.reset();
    }

    // -------------------------------------------------------------------------
    // Given steps
    // -------------------------------------------------------------------------

    @Given("a new bank account with no transactions")
    public void aNewBankAccountWithNoTransactions() {
        // Repository is reset in @Before — this step confirms the precondition
        // No action required: balance is already 0.00 after reset
    }

    @Given("the customer has already deposited {double} euros")
    public void theCustomerHasAlreadyDeposited(double amount) throws Exception {
        mockMvc.perform(post("/api/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"))
                .andExpect(status().isOk());
    }

    // -------------------------------------------------------------------------
    // When steps
    // -------------------------------------------------------------------------

    @When("the customer checks their balance")
    public void theCustomerChecksTheirBalance() throws Exception {
        lastResult = mockMvc.perform(get("/api/balance")
                .accept(MediaType.APPLICATION_JSON));
    }

    @When("the customer deposits {double} euros")
    public void theCustomerDeposits(double amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"));
    }

    @When("the customer attempts to deposit {double} euros")
    public void theCustomerAttemptsToDeposit(double amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"));
    }

    @When("the customer withdraws {double} euros")
    public void theCustomerWithdraws(double amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"));
    }

    @When("the customer attempts to withdraw {double} euros")
    public void theCustomerAttemptsToWithdraw(double amount) throws Exception {
        lastResult = mockMvc.perform(post("/api/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":" + BigDecimal.valueOf(amount).toPlainString() + "}"));
    }

    @When("the customer attempts to withdraw any amount greater than their balance")
    public void theCustomerAttemptsToWithdrawAnyAmountGreaterThanTheirBalance() throws Exception {
        lastResult = mockMvc.perform(post("/api/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":999999.99}"));
    }

    // -------------------------------------------------------------------------
    // Then steps
    // -------------------------------------------------------------------------

    @Then("the balance shown is {double} euros")
    public void theBalanceShownIs(double expected) throws Exception {
        mockMvc.perform(get("/api/balance").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(expected));
    }

    @Then("the deposit is refused with an invalid amount message")
    public void theDepositIsRefusedWithAnInvalidAmountMessage() throws Exception {
        lastResult.andExpect(status().isBadRequest());
    }

    @Then("the withdrawal is refused with an invalid amount message")
    public void theWithdrawalIsRefusedWithAnInvalidAmountMessage() throws Exception {
        lastResult.andExpect(status().isBadRequest());
    }

    @Then("the withdrawal is refused with an insufficient funds message showing {double} euros available")
    public void theWithdrawalIsRefusedWithInsufficientFundsMessageShowing(double availableBalance) throws Exception {
        lastResult.andExpect(status().isConflict());
    }

    @Then("the balance shown is never below {double} euros")
    public void theBalanceShownIsNeverBelow(double floor) throws Exception {
        lastResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(org.hamcrest.Matchers.greaterThanOrEqualTo(floor)));
    }

    @And("the withdrawal is refused with an insufficient funds message")
    public void andTheWithdrawalIsRefusedWithInsufficientFundsMessage() throws Exception {
        lastResult.andExpect(status().isConflict());
    }
}
