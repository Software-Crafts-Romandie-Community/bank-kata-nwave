@driving_port @US-S3
Feature: Insufficient funds protection
  As a banking customer
  I want to be clearly informed when I cannot afford a withdrawal
  So that I understand the refusal and know my actual available balance

  @error
  Scenario: A withdrawal exceeding the balance is refused and the balance stays unchanged
    Given the customer has already deposited 100.00 euros
    When the customer attempts to withdraw 500.00 euros
    Then the withdrawal is refused with an insufficient funds message showing 100.00 euros available
    And the balance shown is 100.00 euros

  @error
  Scenario: A withdrawal of one cent more than the balance is refused
    Given the customer has already deposited 50.00 euros
    When the customer attempts to withdraw 50.01 euros
    Then the withdrawal is refused with an insufficient funds message showing 50.00 euros available
    And the balance shown is 50.00 euros

  @error
  Scenario: A withdrawal from an empty account is refused
    Given a new bank account with no transactions
    When the customer attempts to withdraw 10.00 euros
    Then the withdrawal is refused with an insufficient funds message showing 0.00 euros available
    And the balance shown is 0.00 euros

  @error
  Scenario: The balance stays unchanged after a refused withdrawal following multiple deposits
    Given the customer has already deposited 200.00 euros
    And the customer has already deposited 100.00 euros
    When the customer attempts to withdraw 500.00 euros
    Then the withdrawal is refused with an insufficient funds message showing 300.00 euros available
    And the balance shown is 300.00 euros

  @property
  Scenario: The account balance is never negative regardless of withdrawal attempts
    Given a new bank account with no transactions
    When the customer attempts to withdraw any amount greater than their balance
    Then the balance shown is never below 0.00 euros
    And the withdrawal is refused with an insufficient funds message
