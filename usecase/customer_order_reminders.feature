Feature: Customer Order Reminders
  To ensure I am prepared for meal deliveries and aware of my schedule,
  As a customer,
  I want to receive reminders for my upcoming orders and deliveries.

  Background:
    Given the system has the following upcoming orders with delivery times:
      | Order ID      | Customer Email          | Meal Name          | Delivery Date | Delivery Time Window |
      | ORD-REM-001   | reminder.user@example.com | Vegan Delight      | 2023-12-25    | 18:00 - 19:00      |
      | ORD-REM-002   | another.user@example.com  | Chicken Pasta      | 2023-12-25    | 19:00 - 20:00      |
      | ORD-REM-003   | reminder.user@example.com | Keto Salmon Salad  | 2023-12-26    | 12:00 - 13:00      |
    And customer "reminder.user@example.com" is active in the system
    And the current system date is "2023-12-24"

  Scenario: Customer receives a reminder one day before delivery
    When the system processes daily reminders for upcoming deliveries
    Then customer "reminder.user@example.com" should receive a reminder for order "ORD-REM-001"
    And the reminder for "ORD-REM-001" should state: "Your order ORD-REM-001 (Vegan Delight) is scheduled for delivery tomorrow, 2023-12-25, between 18:00 - 19:00."
    And customer "another.user@example.com" should also receive a reminder for order "ORD-REM-002"

  Scenario: Customer does not receive a reminder if delivery is more than one day away
    Given the current system date is "2023-12-24"
    When the system processes daily reminders for upcoming deliveries
    Then customer "reminder.user@example.com" should not receive a reminder for order "ORD-REM-003" at this time

  Scenario: Customer receives a reminder on the day of delivery (morning reminder)
    Given the current system date is "2023-12-25"
    When the system processes same-day delivery reminders
    Then customer "reminder.user@example.com" should receive a reminder for order "ORD-REM-001"
    And the reminder for "ORD-REM-001" should state: "Your order ORD-REM-001 (Vegan Delight) is scheduled for delivery today, 2023-12-25, between 18:00 - 19:00. Be prepared!"