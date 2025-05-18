Feature: Inventory Tracking and Restocking Suggestions
  To maintain continuous kitchen operations and prevent shortages,
  As a kitchen manager, I want to track ingredient stock levels,
  And the system should suggest restocking when levels are low.

  Background:
    Given the following ingredients are managed in the inventory system with reorder levels:
      | Ingredient Name | Current Stock | Unit    | Reorder Level | Supplier      |
      | Tomato          | 50            | kg      | 10            | VeggiesInc    |
      | Pasta           | 20            | pack    | 5             | GrainsCo      |
      | Chicken Breast  | 15            | kg      | 5             | MeatMasters   |
      | Olive Oil       | 10            | bottle  | 2             | OilsUnlimited |
      | Basil           | 5             | bunch   | 2             | HerbsGarden   |
    And a kitchen manager "Manager Sarah" is logged in

  Scenario: Kitchen manager views current stock levels of all ingredients
    When "Manager Sarah" requests to view all ingredient stock levels
    Then the system should display the following stock levels:
      | Ingredient Name | Current Stock | Unit    |
      | Tomato          | 50            | kg      |
      | Pasta           | 20            | pack    |
      | Chicken Breast  | 15            | kg      |
      | Olive Oil       | 10            | bottle  |
      | Basil           | 5             | bunch   |

  Scenario: Ingredient stock level drops below reorder level after usage
    Given the current stock of "Pasta" is 6 packs
    When 2 packs of "Pasta" are used for an order
    Then the stock level of "Pasta" should be 4 packs
    And the system should generate a restocking suggestion for "Pasta" because its stock (4) is below reorder level (5)
    And "Manager Sarah" should be notified of the restocking suggestion for "Pasta"

  Scenario: Ingredient stock is already below reorder level
    Given the current stock of "Basil" is 1 bunch
    When "Manager Sarah" checks for restocking suggestions
    Then the system should list "Basil" in restocking suggestions
    And "Manager Sarah" should see a notification for "Basil" restocking

  Scenario: Multiple ingredients need restocking
    Given the current stock of "Chicken Breast" is 4 kg
    And the current stock of "Olive Oil" is 1 bottle
    When "Manager Sarah" requests a list of all ingredients needing restocking
    Then the restocking list should include "Chicken Breast"
    And the restocking list should include "Olive Oil"

  Scenario: No ingredients need restocking
    Given all ingredients are above their reorder levels
      | Ingredient Name | Current Stock | Reorder Level |
      | Tomato          | 50            | 10            |
      | Pasta           | 20            | 5             |
    When "Manager Sarah" checks for restocking suggestions
    Then the system should indicate that no ingredients currently need restocking