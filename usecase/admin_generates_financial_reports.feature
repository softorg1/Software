Feature: Administrator Generates Financial Reports
  To analyze revenue and track business performance,
  As a system administrator,
  I want to be able to generate financial reports.

  Background:
    Given the system has the following completed and paid orders with their totals:
      | Order ID      | Customer Email             | Order Date | Order Total |
      | ORD-FIN-100   | finance.user1@example.com  | 2023-10-05 | 25.50       |
      | ORD-FIN-101   | finance.user2@example.com  | 2023-10-15 | 40.00       |
      | ORD-FIN-102   | finance.user1@example.com  | 2023-11-01 | 15.75       |
      | ORD-FIN-103   | finance.user3@example.com  | 2023-11-10 | 60.25       |
      | ORD-FIN-104   | finance.user2@example.com  | 2023-11-20 | 30.00       |
    And an administrator "AdminFinance" is logged in

  Scenario: Administrator generates a total revenue report for a specific month
    When "AdminFinance" requests a total revenue report for the month of "October 2023"
    Then the generated financial report should show a total revenue of 65.50 for "October 2023"

  Scenario: Administrator generates a total revenue report for another specific month
    When "AdminFinance" requests a total revenue report for the month of "November 2023"
    Then the generated financial report should show a total revenue of 106.00 for "November 2023"

  Scenario: Administrator generates a total revenue report for a month with no orders
    When "AdminFinance" requests a total revenue report for the month of "September 2023"
    Then the generated financial report should show a total revenue of 0.00 for "September 2023"
    And the system should indicate that no sales were recorded for "September 2023"

  Scenario: Administrator generates an overall revenue report for all time
    When "AdminFinance" requests an overall total revenue report
    Then the generated financial report should show an overall total revenue of 171.50