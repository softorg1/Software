Feature: Customer Receives Invoice
  To have a record of my purchase and payment details,
  As a customer,
  I want to receive an invoice after my order is completed.

  Background:
    Given the system has a completed order "ORD-FINAL-001" for customer "customer.bill@example.com" with the following details:
      | Item Description     | Quantity | Unit Price | Total Price |
      | Spaghetti Tomato     | 1        | 12.99      | 12.99       |
      | Extra Basil Topping  | 1        | 0.50       | 0.50        |
    And the total amount for order "ORD-FINAL-001" is 13.49
    And customer "customer.bill@example.com" is logged in

  Scenario: Customer requests their invoice for a completed order
    When "customer.bill@example.com" requests the invoice for order "ORD-FINAL-001"
    Then an invoice "INV-ORD-FINAL-001" should be generated for "customer.bill@example.com"
    And the invoice "INV-ORD-FINAL-001" should contain the order ID "ORD-FINAL-001"
    And the invoice "INV-ORD-FINAL-001" should list item "Spaghetti Tomato" with quantity 1 and total price 12.99
    And the invoice "INV-ORD-FINAL-001" should list item "Extra Basil Topping" with quantity 1 and total price 0.50
    And the invoice "INV-ORD-FINAL-001" should show a grand total of 13.49
    And the invoice "INV-ORD-FINAL-001" should indicate payment status as "Paid"

  Scenario: Customer attempts to request an invoice for a non-existent order
    When "customer.bill@example.com" requests the invoice for a non-existent order "ORD-FAKE-999"
    Then the system should inform "customer.bill@example.com" that order "ORD-FAKE-999" was not found or no invoice is available

  Scenario: Customer attempts to request an invoice for an order not belonging to them
    Given the system has a completed order "ORD-OTHER-002" for customer "other.user@example.com"
    When "customer.bill@example.com" requests the invoice for order "ORD-OTHER-002"
    Then the system should inform "customer.bill@example.com" that they are not authorized to view the invoice for order "ORD-OTHER-002"