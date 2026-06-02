@walking_skeleton @real-io @driving_port @US-WS
Feature: Account balance display via the banking interface
  As a banking customer
  I want to view my account balance when I open the application
  So that I can stay informed about my finances without visiting a branch

  Scenario: Customer views initial balance on a new account
    Given a new bank account with no transactions
    When the customer checks their balance
    Then the balance shown is 0.00 euros
