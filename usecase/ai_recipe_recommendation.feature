Feature: AI Recipe Recommendation Assistant
  To help users find suitable recipes quickly,
  As a user,
  I want the system (acting as a recipe recommendation assistant) to suggest the best recipe
  based on my dietary restrictions, available ingredients, and available time, from a given database of recipes.

  Scenario: Assistant recommends the best vegan recipe based on available ingredients and time
    Given the user has the following preferences for recipe recommendation:
      | Preference Type     | Value            |
      | Dietary Restriction | Vegan            |
      | Available Time    | 30 minutes       |
    And the user has the following ingredients available:
      | Ingredient |
      | Tomatoes   |
      | Basil      |
      | Pasta      |
    And the system has the following recipe database:
      | Recipe Name                 | Ingredients                      | Time       | Tags   |
      | Spaghetti with Tomato Sauce | Tomatoes, Pasta, Basil, Olive Oil | 25 minutes | Vegan  |
      | Tomato Basil Soup           | Tomatoes, Basil, Garlic          | 40 minutes | Vegan  |
      | Vegan Pesto Pasta           | Basil, Pasta, Olive Oil, Garlic  | 20 minutes | Vegan  |
    When the user requests a recipe recommendation
    Then the system should recommend "Spaghetti with Tomato Sauce"
    And the system should explain the recommendation clearly, stating it matches all criteria:
      """
      The best recipe for you is "Spaghetti with Tomato Sauce".
      It is Vegan, requires only Basil, Pasta, Tomatoes (plus Olive Oil which is a common pantry item or assumed available for such recipes),
      and can be prepared in 25 minutes, which is within your available 30 minutes.
      "Vegan Pesto Pasta" is also a good option (20 minutes, uses available ingredients), but "Spaghetti with Tomato Sauce" uses more of your specified available ingredients directly.
      "Tomato Basil Soup" takes 40 minutes, which is longer than your available time.
      """