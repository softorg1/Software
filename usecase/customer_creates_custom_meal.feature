Feature: Customer Creates Custom Meal Request
  In order to get meals tailored to my specific taste and dietary needs,
  As a customer,
  I want to select ingredients and customize my meal,
  And the system should validate my selections.

  Background:
    Given the system has the following available ingredients with their compatibility tags:
      | Ingredient Name | Price | Tags                               |
      | Tomato          | 0.5   | vegetable, sauce_base, vegan       |
      | Pasta           | 1.0   | grain, main_course, vegan          |
      | Basil           | 0.3   | herb, topping, vegan               |
      | Chicken Breast  | 2.5   | protein, meat, non_vegan           |
      | Lettuce         | 0.4   | vegetable, salad_base, vegan       |
      | Olive Oil       | 0.2   | oil, dressing, vegan               |
      | Parmesan Cheese | 0.8   | dairy, topping, non_vegan          |
      | Tofu            | 1.5   | protein, soy, vegan                |
    And a customer "user123@example.com" is logged in

  Scenario: Customer creates a valid custom vegan meal
    When "user123@example.com" starts creating a custom meal named "My Vegan Delight"
    And "user123@example.com" selects the following ingredients for the custom meal:
      | Ingredient Name |
      | Tomato          |
      | Pasta           |
      | Basil           |
      | Tofu            |
      | Olive Oil       |
    And "user123@example.com" requests to finalize the custom meal
    Then the custom meal "My Vegan Delight" should be created successfully for "user123@example.com"
    And the total price of "My Vegan Delight" should be calculated correctly based on selected ingredients
    And the meal "My Vegan Delight" should be tagged as "vegan" based on its ingredients

  Scenario: Customer attempts to create a custom meal with an unavailable ingredient
    When "user123@example.com" starts creating a custom meal named "Risky Meal"
    And "user123@example.com" selects the following ingredients for the custom meal:
      | Ingredient Name |
      | Tomato          |
      | Pasta           |
      | Unicorn Meat    |
    And "user123@example.com" requests to finalize the custom meal
    Then the custom meal "Risky Meal" creation should fail for "user123@example.com"
    And the system should inform "user123@example.com" that "Unicorn Meat" is unavailable

  Scenario: Customer attempts to create a custom meal with incompatible ingredients (e.g., conflicting dietary tags)
    Given "user123@example.com" has a dietary preference for "Vegan"
    When "user123@example.com" starts creating a custom meal named "Not So Vegan"
    And "user123@example.com" selects the following ingredients for the custom meal:
      | Ingredient Name |
      | Tofu            |
      | Pasta           |
      | Parmesan Cheese |
    And "user123@example.com" requests to finalize the custom meal
    Then the custom meal "Not So Vegan" creation should fail for "user123@example.com"
    And the system should inform "user123@example.com" that "Parmesan Cheese" is not compatible with their "Vegan" preference or meal composition

  Scenario: Customer creates a valid custom non-vegan meal
    When "user123@example.com" starts creating a custom meal named "Chicken Pasta"
    And "user123@example.com" selects the following ingredients for the custom meal:
      | Ingredient Name |
      | Chicken Breast  |
      | Pasta           |
      | Tomato          |
      | Olive Oil       |
    And "user123@example.com" requests to finalize the custom meal
    Then the custom meal "Chicken Pasta" should be created successfully for "user123@example.com"
    And the total price of "Chicken Pasta" should be calculated correctly
    And the meal "Chicken Pasta" should be tagged as "non_vegan"