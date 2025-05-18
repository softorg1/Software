package healthy.com;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AIRecipeRecommendationSteps {

    private RecipeRepository recipeRepository;
    private RecipeSuggestionService recipeSuggestionService;

    private RecipeSuggestionService.UserRecipePreferences currentUserPreferences;
    private RecipeSuggestionService.RecommendationResult recommendationResult;


    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/recipes.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete recipes.txt: " + e.getMessage());
        }
        this.recipeRepository = new RecipeRepository();
        this.recipeSuggestionService = new RecipeSuggestionService(this.recipeRepository);

        this.currentUserPreferences = new RecipeSuggestionService.UserRecipePreferences(null, 0, new HashSet<>());
        this.recommendationResult = null;
    }

    @Given("the user has the following preferences for recipe recommendation:")
    public void the_user_has_the_following_preferences(DataTable preferencesTable) {
        List<Map<String, String>> rows = preferencesTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String type = columns.get("Preference Type");
            String value = columns.get("Value");
            if ("Dietary Restriction".equals(type)) {
                this.currentUserPreferences.dietaryRestriction = value;
            } else if ("Available Time".equals(type)) {
                this.currentUserPreferences.availableTimeMinutes = Integer.parseInt(value.replace(" minutes", "").trim());
            }
        }
    }

    @Given("the user has the following ingredients available:")
    public void the_user_has_the_following_ingredients_available(DataTable ingredientsTable) {
        this.currentUserPreferences.availableIngredients.clear();
        List<String> ingredients = ingredientsTable.asList(String.class).stream().skip(1).collect(Collectors.toList());
        this.currentUserPreferences.availableIngredients.addAll(ingredients);
    }

    @Given("the system has the following recipe database:")
    public void the_system_has_the_following_recipe_database(DataTable recipesTable) {
        List<Recipe> recipesToSave = new ArrayList<>();
        List<Map<String, String>> rows = recipesTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            recipesToSave.add(new Recipe(
                    columns.get("Recipe Name"),
                    columns.get("Ingredients"),
                    columns.get("Time"),
                    columns.get("Tags")
            ));
        }
        recipeRepository.saveAllRecipes(recipesToSave);
    }

    @When("the user requests a recipe recommendation")
    public void the_user_requests_a_recipe_recommendation() {
        this.recommendationResult = recipeSuggestionService.recommendRecipe(this.currentUserPreferences);
    }

    @Then("the system should recommend {string}")
    public void the_system_should_recommend(String expectedRecipeName) {
        assertThat(this.recommendationResult).isNotNull();
        if (expectedRecipeName.equals("No suitable recipe found")) {
            assertThat(this.recommendationResult.recommendedRecipe).isNull();
            assertThat(this.recommendationResult.explanation).isEqualTo("Sorry, no recipe matches all your criteria from the current database.");
        } else {
            assertThat(this.recommendationResult.recommendedRecipe).isNotNull();
            assertThat(this.recommendationResult.recommendedRecipe.getName()).isEqualTo(expectedRecipeName);
        }
    }

    @And("the system should explain the recommendation clearly, stating it matches all criteria:")
    public void the_system_should_explain_the_recommendation_clearly(String expectedExplanationDocString) {
        assertThat(this.recommendationResult).isNotNull();
        String normalizedExpected = expectedExplanationDocString.replaceAll("\\r\\n", "\n").trim();
        String normalizedActual = this.recommendationResult.explanation.replaceAll("\\r\\n", "\n").trim();
        assertThat(normalizedActual).isEqualTo(normalizedExpected);
    }
}