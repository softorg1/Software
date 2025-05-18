package healthy.com; // أو healthy.com.stepdefinitions

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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerCreatesCustomMealSteps {

    private IngredientRepository ingredientRepository;
    private CustomerRepository customerRepository;
    private CustomerService customerService;
    private CustomMealService customMealService;

    private String currentLoggedInCustomerEmail;
    private CustomMealRequest currentCustomMealRequest;
    private List<Ingredient> backgroundIngredients; // To temporarily store ingredients for setup

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/ingredients.txt"));
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files: " + e.getMessage());
        }
        this.ingredientRepository = new IngredientRepository();
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.customMealService = new CustomMealService(this.ingredientRepository, this.customerService);

        this.currentLoggedInCustomerEmail = null;
        this.currentCustomMealRequest = null;
        this.backgroundIngredients = new ArrayList<>();
    }

    @Given("the system has the following available ingredients with their compatibility tags:")
    public void the_system_has_the_following_available_ingredients_with_their_compatibility_tags(DataTable ingredientsTable) {
        this.backgroundIngredients.clear();
        List<Map<String, String>> rows = ingredientsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String name = columns.get("Ingredient Name");
            double price = Double.parseDouble(columns.get("Price"));
            String tags = columns.get("Tags");
            this.backgroundIngredients.add(new Ingredient(name, price, tags));
        }
        ingredientRepository.saveAllIngredients(this.backgroundIngredients); // Save to file
    }

    @Given("a customer {string} is logged in")
    public void a_customer_is_logged_in(String email) {
        this.currentLoggedInCustomerEmail = email;
        customerService.registerOrGetCustomer(email);
    }

    @Given("{string} has a dietary preference for {string}")
    public void has_a_dietary_preference_for(String email, String preference) {
        customerService.addDietaryPreference(email, preference);
    }

    @When("{string} starts creating a custom meal named {string}")
    public void starts_creating_a_custom_meal_named(String email, String mealName) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(email);
        this.currentCustomMealRequest = customMealService.startCustomMeal(email, mealName);
    }

    @And("{string} selects the following ingredients for the custom meal:")
    public void selects_the_following_ingredients_for_the_custom_meal(String email, DataTable ingredientsTable) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(email);
        assertThat(this.currentCustomMealRequest).isNotNull();
        List<String> selectedIngredientNames = ingredientsTable.asList(String.class).stream().skip(1).collect(Collectors.toList());

        for (String ingredientName : selectedIngredientNames) {
            boolean addedSuccessfully = customMealService.addIngredientToCustomMeal(this.currentCustomMealRequest, ingredientName);
            if (!addedSuccessfully) {
                // The failure reason is already set in mealRequest by the service
                break;
            }
        }
    }

    @And("{string} requests to finalize the custom meal")
    public void requests_to_finalize_the_custom_meal(String email) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(email);
        assertThat(this.currentCustomMealRequest).isNotNull();

        // If adding ingredient already failed, don't try to finalize
        if (this.currentCustomMealRequest.getFailureReason() != null &&
                this.currentCustomMealRequest.getFailureReason().endsWith("is unavailable")) {
            return;
        }
        this.currentCustomMealRequest = customMealService.finalizeCustomMeal(this.currentCustomMealRequest);
    }

    @Then("the custom meal {string} should be created successfully for {string}")
    public void the_custom_meal_should_be_created_successfully_for(String mealName, String email) {
        assertThat(this.currentCustomMealRequest).isNotNull();
        assertThat(this.currentCustomMealRequest.getMealName()).isEqualTo(mealName);
        assertThat(this.currentCustomMealRequest.getCustomerEmail()).isEqualTo(email);
        assertThat(this.currentCustomMealRequest.isCreationSuccessful())
                .as("Meal creation failed with reason: " + this.currentCustomMealRequest.getFailureReason())
                .isTrue();
        assertThat(this.currentCustomMealRequest.getFailureReason()).isNull();
    }

    @And("the total price of {string} should be calculated correctly based on selected ingredients")
    public void the_total_price_of_should_be_calculated_correctly_based_on_selected_ingredients(String mealName) {
        assertThat(this.currentCustomMealRequest.getMealName()).isEqualTo(mealName);
        double expectedPrice = 0;
        for(Ingredient ing : this.currentCustomMealRequest.getSelectedIngredients()){
            Ingredient systemIng = ingredientRepository.findIngredientByName(ing.getName()); // get price from repo
            if(systemIng != null) expectedPrice += systemIng.getPrice();
        }
        assertThat(this.currentCustomMealRequest.getTotalPrice()).isEqualTo(expectedPrice);
    }

    @And("the total price of {string} should be calculated correctly")
    public void the_total_price_of_should_be_calculated_correctly(String mealName) {
        the_total_price_of_should_be_calculated_correctly_based_on_selected_ingredients(mealName);
    }


    @Then("the meal {string} should be tagged as {string} based on its ingredients")
    @And("the meal {string} should be tagged as {string}")
    public void the_meal_should_be_tagged_as_based_on_its_ingredients(String mealName, String expectedTag) {
        assertThat(this.currentCustomMealRequest.getMealName()).isEqualTo(mealName);
        assertThat(this.currentCustomMealRequest.isCreationSuccessful()).isTrue();
        assertThat(this.currentCustomMealRequest.getMealTags()).contains(expectedTag);
        if (expectedTag.equals("vegan")) {
            assertThat(this.currentCustomMealRequest.getMealTags()).doesNotContain("non_vegan");
        } else if (expectedTag.equals("non_vegan")) {
            assertThat(this.currentCustomMealRequest.getMealTags()).doesNotContain("vegan");
        }
        assertThat(this.currentCustomMealRequest.getMealTags()).hasSize(1);
    }

    @Then("the custom meal {string} creation should fail for {string}")
    public void the_custom_meal_creation_should_fail_for(String mealName, String email) {
        assertThat(this.currentCustomMealRequest).isNotNull();
        assertThat(this.currentCustomMealRequest.getMealName()).isEqualTo(mealName);
        assertThat(this.currentCustomMealRequest.getCustomerEmail()).isEqualTo(email);
        assertThat(this.currentCustomMealRequest.isCreationSuccessful()).isFalse();
        assertThat(this.currentCustomMealRequest.getFailureReason()).isNotNull();
    }

    @And("the system should inform {string} that {string} is unavailable")
    public void the_system_should_inform_that_is_unavailable(String email, String ingredientName) {
        assertThat(this.currentCustomMealRequest.getCustomerEmail()).isEqualTo(email);
        assertThat(this.currentCustomMealRequest.getFailureReason()).isEqualTo(ingredientName + " is unavailable");
    }

    @And("the system should inform {string} that {string} is not compatible with their {string} preference or meal composition")
    public void the_system_should_inform_that_is_not_compatible_with_their_preference_or_meal_composition(String email, String ingredientName, String preference) {
        assertThat(this.currentCustomMealRequest.getCustomerEmail()).isEqualTo(email);
        assertThat(this.currentCustomMealRequest.getFailureReason()).isEqualTo(ingredientName + " is not compatible with their \"" + preference + "\" preference or meal composition");
    }
}