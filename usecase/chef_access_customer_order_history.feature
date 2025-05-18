Feature: Chef Accesses Customer Order History for Meal Plan Suggestions
  In order to suggest personalized meal plans,
  As a chef,
  I want to be able to access a customer's order history.

  Scenario: Chef accesses order history for a customer with multiple past orders
    Given a customer with email "maria.g@example.com" has the following past orders:
      | Order ID | Order Date | Meal Name         | Quantity |
      | ORD101   | 2023-11-01 | Chicken Stir-fry  | 1        |
      | ORD102   | 2023-11-05 | Salmon with Quinoa| 2        |
      | ORD103   | 2023-11-10 | Chicken Stir-fry  | 1        |
    And a chef "Chef Ramsey" is logged into the system
    When "Chef Ramsey" requests to view order history for customer "maria.g@example.com"
    Then the system should display the following order history to "Chef Ramsey" for "maria.g@example.com":
      | Order ID | Order Date | Meal Name         | Quantity |
      | ORD101   | 2023-11-01 | Chicken Stir-fry  | 1        |
      | ORD102   | 2023-11-05 | Salmon with Quinoa| 2        |
      | ORD103   | 2023-11-10 | Chicken Stir-fry  | 1        |
    And "Chef Ramsey" should be able to identify frequently ordered meals like "Chicken Stir-fry"

  Scenario: Chef accesses order history for a customer with no past orders
    Given a customer with email "kevin.p@example.com" has no past orders
    And a chef "Chef Julia" is logged into the system
    When "Chef Julia" requests to view order history for customer "kevin.p@example.com"
    Then the system should indicate to "Chef Julia" that "kevin.p@example.com" has no past orders

  Scenario: Chef attempts to access order history for a non-existent customer
    Given a chef "Chef Anton" is logged into the system
    When "Chef Anton" requests to view order history for customer "ghost.user@example.com"
    Then the system should indicate to "Chef Anton" that customer "ghost.user@example.com" was not found