Feature: Customer Dietary Preferences and Allergies Management
  In order to receive suitable meal recommendations and avoid unwanted ingredients,
  As a customer,
  I want to be able to input and save my dietary preferences and allergies.

  Scenario: Customer successfully adds a dietary preference and an allergy
    Given a customer with email "sarah.k@example.com" is registered and logged in
    When the customer navigates to their dietary information page
    And the customer adds "Vegetarian" as a dietary preference
    And the customer adds "Peanuts" as an allergy
    And the customer saves their dietary information
    Then the system should store "Vegetarian" as a dietary preference for "sarah.k@example.com"
    And the system should store "Peanuts" as an allergy for "sarah.k@example.com"

  Scenario: Customer successfully adds multiple dietary preferences and no allergies
    Given a customer with email "ahmad.m@example.com" is registered and logged in
    When the customer navigates to their dietary information page
    And the customer adds "Gluten-Free" as a dietary preference
    And the customer adds "Dairy-Free" as a dietary preference
    And the customer saves their dietary information
    Then the system should store "Gluten-Free" as a dietary preference for "ahmad.m@example.com"
    And the system should store "Dairy-Free" as a dietary preference for "ahmad.m@example.com"
    And the system should record no allergies for "ahmad.m@example.com"