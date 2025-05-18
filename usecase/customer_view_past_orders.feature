Feature: Customer Views Past Meal Orders
  In order to easily reorder preferred meals or review history,
  As a customer,
  I want to be able to view my past meal orders.

  Scenario: Customer with past orders views their order history
    Given a customer with email "sara.ali@example.com" is logged in
    And the customer "sara.ali@example.com" has the following past orders:
      | Order ID | Order Date | Meal Name            | Total Price | Status    |
      | ORD001   | 2023-10-15 | Spaghetti Tomato     | 12.99       | Delivered |
      | ORD002   | 2023-10-22 | Vegan Pesto Pasta    | 15.50       | Delivered |
      | ORD003   | 2023-10-28 | Tomato Basil Soup    | 8.75        | Preparing |
    When the customer requests to view their past orders
    Then the system should display the following orders for "sara.ali@example.com":
      | Order ID | Order Date | Meal Name            | Total Price | Status    |
      | ORD001   | 2023-10-15 | Spaghetti Tomato     | 12.99       | Delivered |
      | ORD002   | 2023-10-22 | Vegan Pesto Pasta    | 15.50       | Delivered |
      | ORD003   | 2023-10-28 | Tomato Basil Soup    | 8.75        | Preparing |

  Scenario: Customer with no past orders views their order history
    Given a customer with email "new.user@example.com" is logged in
    And the customer "new.user@example.com" has no past orders
    When the customer requests to view their past orders
    Then the system should indicate to "new.user@example.com" that they have no past orders

  Scenario: Logged out user attempts to view past orders
    Given no customer is logged in
    When a user attempts to view past orders
    Then the system should prompt the user to log in to view their order history