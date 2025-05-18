Feature: Ingredient Substitution Suggestions

  Background:
    Given the system has the following available ingredients with their properties:
      | Ingredient Name | Price | Tags                     | Alternatives For |
      | Tomato          | 0.5   | vegetable, sauce_base, vegan |                  |
      | Pasta           | 1.0   | grain, main_course, vegan  |                  |
      | Chicken Breast  | 2.5   | protein, meat, non_vegan   |                  |
      | Parmesan Cheese | 0.8   | dairy, topping, non_vegan  |                  |
      | Tofu            | 1.5   | protein, soy, vegan      | Chicken Breast   |
      | Nutritional Yeast| 0.7  | vegan, cheese_flavor     | Parmesan Cheese  |
      | Zucchini Noodles| 1.2   | vegetable, low_carb, vegan | Pasta            |
      | Bell Pepper     | 0.6   | vegetable, vegan           | Tomato           |
    And a customer "vegan_diner@example.com" with dietary preference "Vegan" is logged in
    And a customer "keto_diner@example.com" with dietary preference "Keto" is logged in
    And a chef "ChefMaster" is available in the system

  Scenario: System suggests a vegan alternative for a non-vegan ingredient due to dietary restriction
    Given "vegan_diner@example.com" is creating a meal
    And "vegan_diner@example.com" attempts to add "Parmesan Cheese" to their meal
    When the system detects "Parmesan Cheese" conflicts with "Vegan" preference for "vegan_diner@example.com"
    Then the system should suggest "Nutritional Yeast" as an alternative for "Parmesan Cheese"
    And if "vegan_diner@example.com" accepts the substitution of "Nutritional Yeast"
    And the meal is finalized with "Nutritional Yeast"
    Then "ChefMaster" should receive an alert that "Parmesan Cheese" was substituted with "Nutritional Yeast" for "vegan_diner@example.com"'s meal

  Scenario: System suggests an alternative for an unavailable ingredient
    Given a customer "user200@example.com" is logged in and creating a meal
    And ingredient "Regular Flour" is marked as unavailable
    And the system knows "Almond Flour" (price 1.8, tags "low_carb, vegan, baking") is an alternative for "Regular Flour"
    When "user200@example.com" attempts to add "Regular Flour" to their meal
    Then the system should indicate to "user200@example.com" that ingredient "Regular Flour" is currently out of stock
    And the system should suggest "Almond Flour" as an alternative for "Regular Flour"

  Scenario: System suggests a low-carb alternative due to dietary restriction
    Given "keto_diner@example.com" is creating a meal
    And "keto_diner@example.com" attempts to add "Pasta" to their meal
    When the system detects "Pasta" conflicts with "Keto" preference for "keto_diner@example.com"
    Then the system should suggest "Zucchini Noodles" as an alternative for "Pasta"
    And if "keto_diner@example.com" accepts the substitution of "Zucchini Noodles"
    And the meal is finalized with "Zucchini Noodles"
    Then "ChefMaster" should receive an alert that "Pasta" was substituted with "Zucchini Noodles" for "keto_diner@example.com"'s meal

  Scenario: Customer declines a suggested substitution
    Given "vegan_diner@example.com" is creating a meal
    And "vegan_diner@example.com" attempts to add "Chicken Breast" to their meal
    When the system detects "Chicken Breast" conflicts with "Vegan" preference for "vegan_diner@example.com"
    Then the system should suggest "Tofu" as an alternative for "Chicken Breast"
    And "vegan_diner@example.com" declines the substitution
    Then the meal should not contain "Chicken Breast" or "Tofu" unless explicitly added later
    And "ChefMaster" should not receive a substitution alert for this specific interaction