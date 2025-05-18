Feature: Chef Views Customer Dietary Information
  In order to customize meals according to customer needs,
  As a chef,
  I want to be able to view a customer's stored dietary preferences and allergies.

  Scenario: Chef views dietary information for a customer with preferences and allergies
    Given a customer with email "john.doe@example.com" has the following dietary information:
      | type       | value       |
      | preference | Vegan       |
      | preference | Soy-Free    |
      | allergy    | Nuts        |
    And a chef is logged into the system
    When the chef requests to view dietary information for customer "john.doe@example.com"
    Then the system should display the following dietary preferences for the chef:
      | preference |
      | Vegan      |
      | Soy-Free   |
    And the system should display the following allergies for the chef:
      | allergy |
      | Nuts    |

  Scenario: Chef views dietary information for a customer with only preferences
    Given a customer with email "jane.smith@example.com" has the following dietary information:
      | type       | value          |
      | preference | Gluten-Free    |
      | preference | Dairy-Free     |
    And a chef is logged into the system
    When the chef requests to view dietary information for customer "jane.smith@example.com"
    Then the system should display the following dietary preferences for the chef:
      | preference   |
      | Gluten-Free  |
      | Dairy-Free   |
    And the system should display no allergies for the chef for this customer

  Scenario: Chef views dietary information for a customer with no specific dietary information
    Given a customer with email "peter.jones@example.com" has no specific dietary information stored
    And a chef is logged into the system
    When the chef requests to view dietary information for customer "peter.jones@example.com"
    Then the system should indicate to the chef that no dietary preferences are stored for this customer
    And the system should indicate to the chef that no allergies are stored for this customer

  Scenario: Chef attempts to view dietary information for a non-existent customer
    Given a chef is logged into the system
    When the chef requests to view dietary information for customer "unknown.user@example.com"
    Then the system should indicate to the chef that the customer "unknown.user@example.com" was not found