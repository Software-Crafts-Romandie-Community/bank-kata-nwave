@driving_port @US-S2
Feature: Customer withdraws money from their account
  As a banking customer
  I want to withdraw money from my account via the banking interface
  So that my balance is decreased and I receive immediate confirmation

  Scenario: A valid withdrawal decreases the account balance
    Given the customer has already deposited 300.00 euros
    When the customer withdraws 80.00 euros
    Then the balance shown is 220.00 euros

  Scenario: Withdrawing the exact account balance brings the balance to zero
    Given the customer has already deposited 150.00 euros
    When the customer withdraws 150.00 euros
    Then the balance shown is 0.00 euros

  Scenario: A withdrawal with a decimal amount updates the balance correctly
    Given the customer has already deposited 100.00 euros
    When the customer withdraws 33.33 euros
    Then the balance shown is 66.67 euros

  @skip @error
  Scenario: A withdrawal of zero is rejected and the balance stays unchanged
    Given the customer has already deposited 100.00 euros
    When the customer attempts to withdraw 0.00 euros
    Then the withdrawal is refused with an invalid amount message
    And the balance shown is 100.00 euros

  @skip @error
  Scenario: A negative withdrawal amount is rejected and the balance stays unchanged
    Given the customer has already deposited 100.00 euros
    When the customer attempts to withdraw -50.00 euros
    Then the withdrawal is refused with an invalid amount message
    And the balance shown is 100.00 euros
