package com.softcrafts.bankkata.acceptance.steps;

import com.softcrafts.bankkata.adapter.out.InMemoryAccountRepository;
import com.softcrafts.bankkata.domain.Account;
import com.softcrafts.bankkata.domain.Transaction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Cucumber step definitions for the transaction-history feature (phase2).
 *
 * Tier A acceptance tests -- production Spring context via MockMvc (same context and
 * @Before reset hook as AccountManagementSteps -- both classes share the
 * "com.softcrafts.bankkata.acceptance" glue package, see CucumberSpringConfiguration).
 *
 * All step methods delegate to the production composition root (StatementUseCase via
 * StatementController). Zero business logic in this class.
 *
 * "the customer sees the message {string}" / "no error is displayed" translate the
 * business-language UI observation into the underlying API contract that produces it
 * (the literal UI text is asserted by the frontend Vitest test written in DELIVER --
 * see docs/feature/phase2-transaction-history/distill/upstream-issues.md).
 *
 * Transaction backdating: the domain (Account.deposit()/withdraw(), unmodified -- additive
 * extension constraint) always stamps Instant.now(). To exercise date-range filtering
 * deterministically without touching Account.java, this class seeds transactions through
 * the real domain methods and then rewrites the timestamp of the last entry via reflection
 * on the private `transactions` field -- see upstream-issues.md, Finding 1, for the full
 * rationale and the rejected alternatives (no production code is modified by this choice).
 */
public class StatementSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryAccountRepository accountRepository;

    // State carried between Given / When / Then steps within a scenario
    private ResultActions lastResult;
    private String pendingFromDate;
    private String pendingToDate;

    // -------------------------------------------------------------------------
    // Given steps
    // -------------------------------------------------------------------------

    @Given("the customer has deposited {double} euros on {string}")
    public void theCustomerHasDepositedEurosOn(double amount, String isoDate) throws Exception {
        seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(amount), isoDate);
    }

    @Given("the customer has withdrawn {double} euros on {string}")
    public void theCustomerHasWithdrawnEurosOn(double amount, String isoDate) throws Exception {
        seedTransaction(Transaction.Type.WITHDRAWAL, BigDecimal.valueOf(amount), isoDate);
    }

    @Given("the customer has a transaction on {string}")
    public void theCustomerHasATransactionOn(String isoDate) throws Exception {
        seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(10.00), isoDate);
    }

    @Given("the customer has made {int} transactions in total")
    public void theCustomerHasMadeTransactionsInTotal(int total) throws Exception {
        for (int i = 0; i < total; i++) {
            mockMvc.perform(post("/api/deposit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"amount\":10.00}"));
        }
    }

    @Given("the customer has {int} transactions outside the period {string} to {string}")
    public void theCustomerHasTransactionsOutsideThePeriod(int count, String from, String to) throws Exception {
        String outsideDate = LocalDate.parse(to).plusDays(1).toString();
        for (int i = 0; i < count; i++) {
            seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(10.00), outsideDate);
        }
    }

    @Given("the customer has {int} transactions inside the period {string} to {string}")
    public void theCustomerHasTransactionsInsideThePeriod(int count, String from, String to) throws Exception {
        for (int i = 0; i < count; i++) {
            seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(10.00), from);
        }
    }

    @Given("the customer has transactions of {double}, {double} and {double} euros between {string} and {string}")
    public void theCustomerHasTransactionsOfEurosBetween(double first, double second, double third,
                                                           String from, String to) throws Exception {
        seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(first), from);
        seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(second), to);
        seedTransaction(Transaction.Type.DEPOSIT, BigDecimal.valueOf(third), from);
        pendingFromDate = from;
        pendingToDate = to;
    }

    // -------------------------------------------------------------------------
    // When steps
    // -------------------------------------------------------------------------

    @When("the customer opens the statement page")
    public void theCustomerOpensTheStatementPage() throws Exception {
        lastResult = mockMvc.perform(get("/api/statement").accept(MediaType.APPLICATION_JSON));
    }

    @When("the customer clicks {string}")
    public void theCustomerClicks(String label) throws Exception {
        if (!"Next page".equals(label)) {
            throw new IllegalArgumentException("Unsupported UI action in this step: " + label);
        }
        lastResult = mockMvc.perform(get("/api/statement").param("page", "1"));
    }

    @When("the customer selects a page size of {int}")
    public void theCustomerSelectsAPageSizeOf(int size) throws Exception {
        lastResult = mockMvc.perform(get("/api/statement").param("size", String.valueOf(size)));
    }

    @When("the customer requests page {int}")
    public void theCustomerRequestsPage(int page) throws Exception {
        lastResult = mockMvc.perform(get("/api/statement").param("page", String.valueOf(page)));
    }

    @When("the customer filters from {string} to {string}")
    public void theCustomerFiltersFromTo(String from, String to) throws Exception {
        lastResult = mockMvc.perform(get("/api/statement").param("from", from).param("to", to));
    }

    @When("the customer filters from {string} to {string} with pagination and sorting parameters set")
    public void theCustomerFiltersFromToWithPaginationAndSortingParametersSet(String from, String to) throws Exception {
        lastResult = mockMvc.perform(get("/api/statement")
                .param("from", from)
                .param("to", to)
                .param("page", "0")
                .param("size", "10")
                .param("sortBy", "amount"));
    }

    @When("the customer filters that period and sorts by amount ascending")
    public void theCustomerFiltersThatPeriodAndSortsByAmountAscending() throws Exception {
        lastResult = filterPendingPeriodSortedByAmount("asc");
    }

    @When("the customer filters that period and sorts by amount descending")
    public void theCustomerFiltersThatPeriodAndSortsByAmountDescending() throws Exception {
        lastResult = filterPendingPeriodSortedByAmount("desc");
    }

    // -------------------------------------------------------------------------
    // Then steps
    // -------------------------------------------------------------------------

    @Then("the statement shows {int} transactions ordered from {string} to {string}")
    public void theStatementShowsTransactionsOrderedFromTo(int count, String mostRecent, String oldest) throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(count))
                .andExpect(jsonPath("$.content[0].timestamp", Matchers.startsWith(mostRecent)))
                .andExpect(jsonPath("$.content[" + (count - 1) + "].timestamp", Matchers.startsWith(oldest)));
    }

    @Then("each line shows the date, type and signed amount")
    public void eachLineShowsTheDateTypeAndSignedAmount() throws Exception {
        lastResult.andExpect(jsonPath("$.content[0].type").exists())
                .andExpect(jsonPath("$.content[0].amount").exists())
                .andExpect(jsonPath("$.content[0].timestamp").exists());
    }

    @Then("the customer sees the message {string}")
    public void theCustomerSeesTheMessage(String message) throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Then("no error is displayed")
    public void noErrorIsDisplayed() throws Exception {
        lastResult.andExpect(status().isOk());
    }

    @Then("the customer sees a line {string} with amount {string}")
    public void theCustomerSeesALineWithAmount(String type, String displayedAmount) throws Exception {
        double numeric = Double.parseDouble(displayedAmount.replaceAll("[^0-9.]", ""));
        lastResult.andExpect(jsonPath("$.content[0].type").value(type))
                .andExpect(jsonPath("$.content[0].amount").value(numeric));
    }

    @Then("the date matches the in-memory transaction timestamp")
    public void theDateMatchesTheInMemoryTransactionTimestamp() throws Exception {
        Transaction last = lastTransaction();
        lastResult.andExpect(jsonPath("$.content[0].timestamp").value(last.timestamp().toString()));
    }

    @Then("the response \"content\" holds {int} transactions")
    public void theResponseContentHoldsTransactions(int count) throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(count));
    }

    @Then("the response \"content\" is an empty array")
    public void theResponseContentIsAnEmptyArray() throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    @Then("\"totalElements\" is {int}")
    public void totalElementsIs(int expected) throws Exception {
        lastResult.andExpect(jsonPath("$.totalElements").value(expected));
    }

    @Then("\"totalPages\" is {int}")
    public void totalPagesIs(int expected) throws Exception {
        lastResult.andExpect(jsonPath("$.totalPages").value(expected));
    }

    @And("\"page\" is {int}")
    public void pageIs(int expected) throws Exception {
        lastResult.andExpect(jsonPath("$.page").value(expected));
    }

    @Then("the page size choice is refused with an invalid parameter message")
    public void thePageSizeChoiceIsRefusedWithInvalidParameterMessage() throws Exception {
        lastResult.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Then("the filter is refused with an invalid date range message")
    public void theFilterIsRefusedWithInvalidDateRangeMessage() throws Exception {
        lastResult.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Then("the customer sees {int} transactions dated {string} and {string}")
    public void theCustomerSeesTransactionsDated(int count, String firstDate, String secondDate) throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(count))
                .andExpect(jsonPath("$.content[*].timestamp", Matchers.hasItem(Matchers.startsWith(firstDate))))
                .andExpect(jsonPath("$.content[*].timestamp", Matchers.hasItem(Matchers.startsWith(secondDate))));
    }

    @Then("the transaction dated {string} does not appear")
    public void theTransactionDatedDoesNotAppear(String date) throws Exception {
        lastResult.andExpect(jsonPath("$.content[*].timestamp",
                Matchers.not(Matchers.hasItem(Matchers.startsWith(date)))));
    }

    @Then("the transaction dated {string} appears in the result")
    public void theTransactionDatedAppearsInTheResult(String date) throws Exception {
        lastResult.andExpect(jsonPath("$.content[*].timestamp", Matchers.hasItem(Matchers.startsWith(date))));
    }

    @Then("\"content\" lists the transactions in the order {double}, {double}, {double} euros")
    public void contentListsTheTransactionsInTheOrder(double first, double second, double third) throws Exception {
        lastResult.andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].amount").value(first))
                .andExpect(jsonPath("$.content[1].amount").value(second))
                .andExpect(jsonPath("$.content[2].amount").value(third));
    }

    // -------------------------------------------------------------------------
    // Test-only fixtures (see class javadoc + upstream-issues.md Finding 1)
    // -------------------------------------------------------------------------

    private ResultActions filterPendingPeriodSortedByAmount(String sortDir) throws Exception {
        return mockMvc.perform(get("/api/statement")
                .param("from", pendingFromDate)
                .param("to", pendingToDate)
                .param("sortBy", "amount")
                .param("sortDir", sortDir));
    }

    private Transaction lastTransaction() {
        List<Transaction> transactions = accountRepository.load().getTransactions();
        return transactions.get(transactions.size() - 1);
    }

    private void seedTransaction(Transaction.Type type, BigDecimal amount, String isoDate) throws Exception {
        Account account = accountRepository.load();
        if (type == Transaction.Type.DEPOSIT) {
            account.deposit(amount);
        } else {
            account.withdraw(amount);
        }
        backdateLastTransaction(account, isoDate);
        accountRepository.save(account);
    }

    private void backdateLastTransaction(Account account, String isoDate) throws Exception {
        Instant timestamp = LocalDate.parse(isoDate).atTime(12, 0).toInstant(ZoneOffset.UTC);
        Field transactionsField = Account.class.getDeclaredField("transactions");
        transactionsField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Transaction> transactions = (List<Transaction>) transactionsField.get(account);
        Transaction last = transactions.get(transactions.size() - 1);
        transactions.set(transactions.size() - 1, new Transaction(last.type(), last.amount(), timestamp));
    }
}
