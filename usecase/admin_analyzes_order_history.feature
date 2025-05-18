Feature: Administrator Analyzes Customer Order History
  In order to analyze trends and improve service offerings,
  As a system administrator,
  I want to be able to retrieve and analyze customer order history.

  Scenario: Administrator retrieves order history for trend analysis
    Given the system has the following aggregated order data:
      | Customer Email      | Order ID | Meal Name          | Order Date | Quantity | Total Price |
      | sara.ali@example.com| ORD001   | Spaghetti Tomato   | 2023-10-15 | 1        | 12.99       |
      | sara.ali@example.com| ORD002   | Vegan Pesto Pasta  | 2023-10-22 | 1        | 15.50       |
      | john.doe@example.com| ORD004   | Chicken Stir-fry   | 2023-11-01 | 2        | 25.00       |
      | maria.g@example.com | ORD101   | Chicken Stir-fry   | 2023-11-01 | 1        | 12.50       |
      | maria.g@example.com | ORD103   | Chicken Stir-fry   | 2023-11-10 | 1        | 12.50       |
      | john.doe@example.com| ORD005   | Spaghetti Tomato   | 2023-11-12 | 1        | 12.99       |
    And an administrator "AdminUser01" is logged in
    When "AdminUser01" requests a summary of all customer order history for the last month
    Then the system should provide "AdminUser01" with a dataset containing all orders from the last month
    And "AdminUser01" should be able to identify the most frequently ordered meal as "Chicken Stir-fry"
    And "AdminUser01" should be able to identify the total revenue from orders in the last month

  Scenario: Administrator retrieves order history for a specific customer
    Given the system has the following aggregated order data:
      | Customer Email      | Order ID | Meal Name          | Order Date | Quantity | Total Price |
      | sara.ali@example.com| ORD001   | Spaghetti Tomato   | 2023-10-15 | 1        | 12.99       |
      | sara.ali@example.com| ORD002   | Vegan Pesto Pasta  | 2023-10-22 | 1        | 15.50       |
      | john.doe@example.com| ORD004   | Chicken Stir-fry   | 2023-11-01 | 2        | 25.00       |
    And an administrator "AdminUser02" is logged in
    When "AdminUser02" requests the complete order history for customer "sara.ali@example.com"
    Then the system should provide "AdminUser02" with the following orders for "sara.ali@example.com":
      | Order ID | Meal Name          | Order Date | Quantity | Total Price |
      | ORD001   | Spaghetti Tomato   | 2023-10-15 | 1        | 12.99       |
      | ORD002   | Vegan Pesto Pasta  | 2023-10-22 | 1        | 15.50       |

  Scenario: Administrator attempts to retrieve data when no orders exist in the system
    Given the system has no order data stored
    And an administrator "AdminUser03" is logged in
    When "AdminUser03" requests a summary of all customer order history
    Then the system should indicate to "AdminUser03" that no order data is available for analysis