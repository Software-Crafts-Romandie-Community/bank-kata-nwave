@real-io @driving_port @US-S1
Feature: Account statement API — GET /api/statement content and format
  As a banking customer
  I want to retrieve all my transactions via the statement API
  So that I can review the type, amount, and date of every operation

  Scenario: The statement shows all transactions in reverse chronological order
    Given a new bank account with no transactions
    And the customer has already deposited 200.00 euros
    And the customer has already deposited 100.00 euros
    And the customer has already withdrawn 50.00 euros
    When the customer requests the account statement
    Then the statement contains 3 transactions
    And the statement entry at position 0 has type "WITHDRAWAL" and amount 50.00
    And the statement entry at position 1 has type "DEPOSIT" and amount 100.00
    And the statement entry at position 2 has type "DEPOSIT" and amount 200.00

  Scenario: Each transaction in the statement has the correct JSON fields
    Given a new bank account with no transactions
    And the customer has already deposited 150.00 euros
    When the customer requests the account statement
    Then the response status is 200 OK
    And each transaction in the statement has type, amount, and date fields
    And the date field of the first transaction is a non-empty string

  Scenario: The statement returns an empty array when no transactions have occurred
    Given a new bank account with no transactions
    When the customer requests the account statement
    Then the response status is 200 OK
    And the statement is empty

  @skip
  Scenario: Decimal transaction amounts are preserved without loss of precision
    Given a new bank account with no transactions
    And the customer has already deposited 149.99 euros
    And the customer has already withdrawn 0.01 euros
    When the customer requests the account statement
    Then the statement contains 2 transactions
    And the statement entry at position 0 has type "WITHDRAWAL" and amount 0.01
    And the statement entry at position 1 has type "DEPOSIT" and amount 149.99

  @skip
  Scenario: Statement amounts and current balance are consistent after mixed operations
    Given a new bank account with no transactions
    And the customer has already deposited 300.00 euros
    And the customer has already withdrawn 50.00 euros
    When the customer requests the account statement
    Then the statement contains 2 transactions
    And the statement entry at position 0 has type "WITHDRAWAL" and amount 50.00
    And the statement entry at position 1 has type "DEPOSIT" and amount 300.00
    And the balance shown is 250.00 euros
