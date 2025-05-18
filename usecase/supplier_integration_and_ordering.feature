Feature: Supplier Integration for Real-time Pricing and Automated Ordering

  Background:
    Given the following suppliers are known to the system:
      | Supplier ID | Supplier Name | Contact Email        |
      | SUP001      | VeggiesInc    | orders@veggies.inc   |
      | SUP002      | GrainsCo      | sales@grainsco.com   |
      | SUP003      | MeatMasters   | supply@meatmasters.co|
    And the following ingredients are linked to suppliers with their standard reorder quantity:
      | Ingredient Name | Supplier ID | Default Reorder Qty | Unit   | Critical Stock Level | Current Stock |
      | Tomato          | SUP001      | 20                  | kg     | 5                    | 0             |
      | Pasta           | SUP002      | 10                  | pack   | 2                    | 0             |
      | Chicken Breast  | SUP003      | 10                  | kg     | 3                    | 0             |
    And a kitchen manager "Manager Leo" is logged in

  Scenario: Kitchen manager fetches real-time price for an ingredient from a supplier
    Given "VeggiesInc" real-time price for "Tomato" is 1.5 per "kg"
    When "Manager Leo" requests the real-time price for "Tomato" from "VeggiesInc"
    Then the system should display the fetched price as 1.5 per "kg" for "Tomato" from "VeggiesInc"

  Scenario: System automatically generates a purchase order when an ingredient stock is critically low
    Given for supplier integration, the current stock of "Pasta" is 1 pack
    And "GrainsCo" real-time price for "Pasta" is 0.9 per "pack"
    And "VeggiesInc" real-time price for "Tomato" is 1.5 per "kg"
    And "MeatMasters" real-time price for "Chicken Breast" is 18.0 per "kg"
    When the system checks for critically low stock items
    Then a purchase order should be automatically generated for "Pasta" to "GrainsCo"
    And the purchase order should request the default reorder quantity of 10 packs of "Pasta"
    And the purchase order should use the real-time price of 0.9 per pack
    And "Manager Leo" should be notified of the automatically generated purchase order for "Pasta"

  Scenario: Kitchen manager manually initiates a purchase order for an ingredient
    Given "MeatMasters" real-time price for "Chicken Breast" is 18.0 per "kg"
    And for supplier integration, the current stock of "Chicken Breast" is 6 kg
    When "Manager Leo" decides to manually order 5 kg of "Chicken Breast" from "MeatMasters"
    Then a purchase order should be generated for 5 kg of "Chicken Breast" to "MeatMasters"
    And this purchase order should use the real-time price of 18.0 per kg

  Scenario: Attempting to fetch price from an unknown supplier for a known ingredient
    When "Manager Leo" requests the real-time price for "Tomato" from "UnknownSupplier"
    Then the system should inform "Manager Leo" that supplier "UnknownSupplier" is not recognized

  Scenario: System does not generate a purchase order if stock is low but not critical
    Given for supplier integration, the current stock of "Tomato" is 8 kg
    And "VeggiesInc" real-time price for "Tomato" is 1.5 per "kg"
    And "GrainsCo" real-time price for "Pasta" is 0.9 per "pack"
    And "MeatMasters" real-time price for "Chicken Breast" is 18.0 per "kg"
    When the system checks for critically low stock items
    Then no automatic purchase order should be generated for "Tomato"