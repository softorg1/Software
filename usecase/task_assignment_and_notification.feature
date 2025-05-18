Feature: Task Assignment and Chef Notification

  Background:
    Given the following chefs are available in the system:
      | Chef Name   | Expertise        | Current Workload |
      | Chef Alice  | Italian, Pastas  | Low              |
      | Chef Bob    | Grilling, Meats  | Medium           |
      | Chef Carol  | Baking, Desserts | Low              |
    And a kitchen manager "Manager Mike" is logged in

  Scenario: Kitchen manager assigns a new cooking task to a chef with low workload
    Given a new cooking order "Order-123" for "Spaghetti Carbonara" needs preparation
    When "Manager Mike" decides to assign "Order-123"
    And "Manager Mike" views chef availability and workload
    And "Manager Mike" assigns the task for "Order-123" to "Chef Alice"
    Then "Chef Alice" should have "Order-123" added to their task list
    And the workload of "Chef Alice" should be updated to "Medium"
    And "Chef Alice" should receive a notification: "New task assigned: Prepare Spaghetti Carbonara for Order-123."

  Scenario: Kitchen manager attempts to assign a task requiring specific expertise to an unsuitable chef
    Given a new cooking order "Order-456" for "Grilled Steak" needs preparation
    And "Chef Carol" has "Baking, Desserts" expertise and "Low" workload
    When "Manager Mike" attempts to assign the task for "Order-456" to "Chef Carol"
    Then the assignment should fail due to expertise mismatch
    And the system should inform "Manager Mike" that "Chef Carol" may not be suitable for "Grilled Steak"
    And "Chef Carol" should not receive a notification for "Order-456"

  Scenario: Chef checks their assigned tasks
    Given "Chef Bob" has been assigned the following tasks:
      | Order ID | Meal Name        | Due Time |
      | ORD-789  | Beef Burger      | 18:00    |
      | ORD-790  | Grilled Salmon   | 18:30    |
    When "Chef Bob" checks their assigned tasks
    Then "Chef Bob" should see a list containing "Beef Burger for ORD-789 (Due: 18:00)" and "Grilled Salmon for ORD-790 (Due: 18:30)"

  Scenario: Task assignment to a chef increases their workload beyond a threshold (e.g., to High)
    Given a new cooking order "Order-101" for "Large Pizza" needs preparation
    And "Chef Bob" has "Medium" workload
    When "Manager Mike" assigns the task for "Order-101" to "Chef Bob"
    Then "Chef Bob" should have "Order-101" added to their task list
    And the workload of "Chef Bob" should be updated to "High"
    And "Chef Bob" should receive a notification: "New task assigned: Prepare Large Pizza for Order-101."