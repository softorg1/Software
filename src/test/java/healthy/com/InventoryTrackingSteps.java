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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class InventoryTrackingSteps {

    private IngredientRepository ingredientRepository;
    private InventoryService inventoryService;

    private String currentManagerName; // This will now be set by steps that pass manager name
    private List<Map<String, String>> displayedStockLevelsView;
    private List<String> actualRestockingSuggestions;
    private String systemMessageForRestocking;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/ingredients.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete ingredients.txt: " + e.getMessage());
        }
        this.ingredientRepository = new IngredientRepository();
        this.inventoryService = new InventoryService(this.ingredientRepository);

        this.displayedStockLevelsView = new ArrayList<>();
        this.actualRestockingSuggestions = new ArrayList<>();
        this.systemMessageForRestocking = null;
        this.currentManagerName = null; // Reset manager name
    }

    @Given("the following ingredients are managed in the inventory system with reorder levels:")
    public void the_following_ingredients_are_managed_in_the_inventory_system_with_reorder_levels(DataTable ingredientsTable) {
        List<Ingredient> ingredientsToSave = new ArrayList<>();
        List<Map<String, String>> rows = ingredientsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            ingredientsToSave.add(new Ingredient(
                    columns.get("Ingredient Name"),
                    0.0,
                    "",
                    "",
                    Integer.parseInt(columns.get("Current Stock")),
                    columns.get("Unit"),
                    Integer.parseInt(columns.get("Reorder Level"))
            ));
        }
        ingredientRepository.saveAllIngredients(ingredientsToSave);
    }

    // The @Given("a kitchen manager {string} is logged in") step is now solely in TaskAssignmentSteps.java
    // If this feature needs to assume a manager is logged in for certain scenarios,
    // that Gherkin step needs to be present in the .feature file for this specific feature
    // and it will be picked up by TaskAssignmentSteps.java (if glue paths are set correctly).
    // For steps within *this* file that take managerName as a parameter, we'll set currentManagerName.

    @When("{string} requests to view all ingredient stock levels")
    public void requests_to_view_all_ingredient_stock_levels(String managerName) {
        this.currentManagerName = managerName; // Set current manager for this interaction
        // We assume the login step from another file has set a global login state if needed,
        // or this step is self-contained for a logged-in manager for this action.
        this.displayedStockLevelsView.clear();
        List<Ingredient> ingredients = inventoryService.getAllIngredientStockLevels();
        for (Ingredient item : ingredients) {
            Map<String, String> stockEntry = new HashMap<>();
            stockEntry.put("Ingredient Name", item.getName());
            stockEntry.put("Current Stock", String.valueOf(item.getCurrentStock()));
            stockEntry.put("Unit", item.getUnit());
            this.displayedStockLevelsView.add(stockEntry);
        }
    }

    @Then("the system should display the following stock levels:")
    public void the_system_should_display_the_following_stock_levels(DataTable expectedStockTable) {
        List<Map<String, String>> expected = expectedStockTable.asMaps(String.class, String.class);
        assertThat(this.displayedStockLevelsView).containsExactlyInAnyOrderElementsOf(expected);
    }

    private void set_ingredient_stock(String ingredientName, int stock) {
        inventoryService.updateIngredientStock(ingredientName, stock);
    }

    @Given("the current stock of {string} is {int} packs")
    public void the_current_stock_of_is_packs(String ingredientName, Integer stock) {
        set_ingredient_stock(ingredientName, stock);
    }

    @Given("the current stock of {string} is {int} bunch")
    public void the_current_stock_of_is_bunch(String ingredientName, Integer stock) {
        set_ingredient_stock(ingredientName, stock);
    }

    @Given("the current stock of {string} is {int} kg")
    public void the_current_stock_of_is_kg(String ingredientName, Integer stock) {
        set_ingredient_stock(ingredientName, stock);
    }

    @Given("the current stock of {string} is {int} bottle")
    public void the_current_stock_of_is_bottle(String ingredientName, Integer stock) {
        set_ingredient_stock(ingredientName, stock);
    }

    @When("{int} packs of {string} are used for an order")
    public void packs_of_are_used_for_an_order(Integer quantity, String ingredientName) {
        boolean success = inventoryService.useSingleIngredient(ingredientName, quantity);
        assertThat(success).as("Failed to use " + quantity + " packs of " + ingredientName).isTrue();
    }

    private void verify_stock_level(String ingredientName, int expectedStock) {
        Ingredient item = inventoryService.getIngredientStock(ingredientName);
        assertThat(item).isNotNull();
        assertThat(item.getCurrentStock()).isEqualTo(expectedStock);
    }

    @Then("the stock level of {string} should be {int} packs")
    public void the_stock_level_of_should_be_packs(String ingredientName, Integer expectedStock) {
        verify_stock_level(ingredientName, expectedStock);
    }

    @Then("the stock level of {string} should be {int} bunch")
    public void the_stock_level_of_should_be_bunch(String ingredientName, Integer expectedStock) {
        verify_stock_level(ingredientName, expectedStock);
    }

    @Then("the stock level of {string} should be {int} kg")
    public void the_stock_level_of_should_be_kg(String ingredientName, Integer expectedStock) {
        verify_stock_level(ingredientName, expectedStock);
    }

    @Then("the stock level of {string} should be {int} bottle")
    public void the_stock_level_of_should_be_bottle(String ingredientName, Integer expectedStock) {
        verify_stock_level(ingredientName, expectedStock);
    }

    @And("the system should generate a restocking suggestion for {string} because its stock \\({int}) is below reorder level \\({int})")
    public void the_system_should_generate_a_restocking_suggestion_for_because_its_stock_is_below_reorder_level(String ingredientName, Integer currentStock, Integer reorderLevel) {
        Ingredient item = inventoryService.getIngredientStock(ingredientName);
        assertThat(item).isNotNull();
        assertThat(item.getCurrentStock()).isEqualTo(currentStock);
        assertThat(item.getReorderLevel()).isEqualTo(reorderLevel);

        List<Ingredient> needingRestock = inventoryService.getIngredientsNeedingRestocking();
        assertThat(needingRestock.stream().anyMatch(ing -> ing.getName().equals(ingredientName))).isTrue();
    }

    @And("{string} should be notified of the restocking suggestion for {string}")
    public void should_be_notified_of_the_restocking_suggestion_for(String managerName, String ingredientName) {
        this.currentManagerName = managerName;
        List<Ingredient> needingRestock = inventoryService.getIngredientsNeedingRestocking();
        boolean notified = needingRestock.stream().anyMatch(ing -> ing.getName().equals(ingredientName));
        assertThat(notified).as("Manager " + managerName + " should be notified for " + ingredientName).isTrue();
    }

    @When("{string} checks for restocking suggestions")
    public void checks_for_restocking_suggestions(String managerName) {
        this.currentManagerName = managerName;
        this.actualRestockingSuggestions = inventoryService.getIngredientsNeedingRestocking()
                .stream()
                .map(Ingredient::getName)
                .collect(Collectors.toList());
        if (this.actualRestockingSuggestions.isEmpty()) {
            this.systemMessageForRestocking = "No ingredients currently need restocking.";
        }
    }

    @Then("the system should list {string} in restocking suggestions")
    public void the_system_should_list_in_restocking_suggestions(String ingredientName) {
        assertThat(this.actualRestockingSuggestions).contains(ingredientName);
    }

    @And("{string} should see a notification for {string} restocking")
    public void should_see_a_notification_for_restocking(String managerName, String ingredientName) {
        this.currentManagerName = managerName;
        List<Ingredient> needingRestock = inventoryService.getIngredientsNeedingRestocking();
        boolean notified = needingRestock.stream().anyMatch(ing -> ing.getName().equals(ingredientName));
        assertThat(notified).as("Manager " + managerName + " should see notification for " + ingredientName).isTrue();
    }

    @When("{string} requests a list of all ingredients needing restocking")
    public void requests_a_list_of_all_ingredients_needing_restocking(String managerName){
        checks_for_restocking_suggestions(managerName);
    }

    @Then("the restocking list should include {string}")
    public void the_restocking_list_should_include(String ingredientName) {
        assertThat(this.actualRestockingSuggestions).contains(ingredientName);
    }

    @Given("all ingredients are above their reorder levels")
    public void all_ingredients_are_above_their_reorder_levels(DataTable ingredientsTable) {
        List<Map<String, String>> rows = ingredientsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String name = columns.get("Ingredient Name");
            int stock = Integer.parseInt(columns.get("Current Stock"));
            int reorderLvl = Integer.parseInt(columns.get("Reorder Level"));

            inventoryService.updateIngredientStock(name, stock);
            inventoryService.setIngredientReorderLevel(name, reorderLvl);

            Ingredient item = inventoryService.getIngredientStock(name);
            assertThat(item.getCurrentStock()).isGreaterThanOrEqualTo(item.getReorderLevel());
        }
    }

    @Then("the system should indicate that no ingredients currently need restocking")
    public void the_system_should_indicate_that_no_ingredients_currently_need_restocking() {
        assertThat(this.actualRestockingSuggestions).isEmpty();
        assertThat(this.systemMessageForRestocking).isEqualTo("No ingredients currently need restocking.");
    }
}