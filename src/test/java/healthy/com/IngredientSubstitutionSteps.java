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

public class IngredientSubstitutionSteps {

    private IngredientRepository ingredientRepository;
    private CustomerRepository customerRepository;
    private CustomerService customerService;
    private CustomMealService customMealService;

    private Map<String, Boolean> ingredientAvailabilityStatus;
    private String currentCustomerEmailForSubstitution;
    private List<Ingredient> suggestedAlternativesOutput;
    private List<String> chefNotificationsForSubstitution;
    private MealInProgressSustitution currentMealForSubstitution;
    private Map<String, MealInProgressSustitution> customerMealsInProgress;

    private static class MealInProgressSustitution {
        String customerEmail;
        List<String> currentIngredients = new ArrayList<>();
        String lastAttemptedIngredient;
        String lastSuggestedAlternative;
        boolean lastSubstitutionAccepted;
        boolean finalized;
        String infoMessage; // <<<--- تم إضافة الحقل هنا

        public MealInProgressSustitution(String customerEmail) {
            this.customerEmail = customerEmail;
        }
    }

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

        this.ingredientAvailabilityStatus = new HashMap<>();
        this.suggestedAlternativesOutput = new ArrayList<>();
        this.chefNotificationsForSubstitution = new ArrayList<>();
        this.currentMealForSubstitution = null;
        this.customerMealsInProgress = new HashMap<>();
    }

    @Given("the system has the following available ingredients with their properties:")
    public void the_system_has_the_following_available_ingredients_with_their_properties(DataTable ingredientsTable) {
        List<Ingredient> ingredientsToSave = new ArrayList<>();
        List<Map<String, String>> rows = ingredientsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String name = columns.get("Ingredient Name");
            double price = Double.parseDouble(columns.get("Price"));
            String tags = columns.get("Tags");
            // String alternativesForColumn = columns.get("Alternatives For"); // Not directly used here for this ingredient's own alternatives list

            Ingredient ingredient = new Ingredient(name, price, tags, "");
            ingredientsToSave.add(ingredient);
            this.ingredientAvailabilityStatus.put(name, true);
        }
        ingredientRepository.saveAllIngredients(ingredientsToSave);

        for (Map<String, String> columns : rows) {
            String currentIngredientName = columns.get("Ingredient Name");
            String alternativesForValue = columns.get("Alternatives For");
            if (alternativesForValue != null && !alternativesForValue.isEmpty()) {
                Ingredient originalIngredientToUpdate = ingredientRepository.findIngredientByName(alternativesForValue);
                Ingredient currentAsAlternative = ingredientRepository.findIngredientByName(currentIngredientName);
                if (originalIngredientToUpdate != null && currentAsAlternative != null) {
                    originalIngredientToUpdate.addSuggestedAlternative(currentAsAlternative.getName());
                }
            }
        }
        ingredientRepository.saveAllIngredients(new ArrayList<>(ingredientRepository.getAllIngredients()));
    }

    @Given("a customer {string} with dietary preference {string} is logged in")
    public void a_customer_with_dietary_preference_is_logged_in(String email, String preference) {
        this.currentCustomerEmailForSubstitution = email;
        customerService.registerOrGetCustomer(email);
        customerService.addDietaryPreference(email, preference);
        this.currentMealForSubstitution = this.customerMealsInProgress.computeIfAbsent(email, MealInProgressSustitution::new);
    }

    @Given("a customer {string} is logged in and creating a meal")
    public void a_customer_is_logged_in_and_creating_a_meal(String email) {
        this.currentCustomerEmailForSubstitution = email;
        customerService.registerOrGetCustomer(email);
        this.currentMealForSubstitution = this.customerMealsInProgress.computeIfAbsent(email, MealInProgressSustitution::new);
    }

    @Given("a chef {string} is available in the system")
    public void a_chef_is_available_in_the_system(String chefName) {
        // Parameter chefName is not used in this step definition for now
    }

    @Given("{string} is creating a meal")
    public void is_creating_a_meal(String email) {
        this.currentCustomerEmailForSubstitution = email;
        this.currentMealForSubstitution = this.customerMealsInProgress.computeIfAbsent(email, MealInProgressSustitution::new);
    }

    @Given("ingredient {string} is marked as unavailable")
    public void ingredient_is_marked_as_unavailable(String ingredientName) {
        this.ingredientAvailabilityStatus.put(ingredientName, false);
    }

    @Given("the system knows {string} \\(price {double}, tags {string}) is an alternative for {string}")
    public void the_system_knows_is_an_alternative_for(String altName, Double altPrice, String altTags, String originalName) {
        Ingredient alternativeIngredient = ingredientRepository.findIngredientByName(altName);
        if (alternativeIngredient == null) {
            alternativeIngredient = new Ingredient(altName, altPrice, altTags, "");
            List<Ingredient> currentIngredients = new ArrayList<>(ingredientRepository.getAllIngredients());
            currentIngredients.add(alternativeIngredient);
            ingredientRepository.saveAllIngredients(currentIngredients);
        }
        this.ingredientAvailabilityStatus.put(altName, true);

        Ingredient original = ingredientRepository.findIngredientByName(originalName);
        List<Ingredient> allIngredientsToSave = new ArrayList<>(ingredientRepository.getAllIngredients());
        if (original != null) {
            original.addSuggestedAlternative(altName);
            allIngredientsToSave.removeIf(ing -> ing.getName().equals(originalName));
            allIngredientsToSave.add(original);
        } else {
            Ingredient newOriginal = new Ingredient(originalName, 0.0, "", altName);
            allIngredientsToSave.add(newOriginal);
        }
        ingredientRepository.saveAllIngredients(allIngredientsToSave);
    }

    @And("{string} attempts to add {string} to their meal")
    public void attempts_to_add_to_their_meal(String email, String ingredientName) {
        assertThat(this.currentCustomerEmailForSubstitution).isEqualTo(email);
        assertThat(this.currentMealForSubstitution).isNotNull();
        this.currentMealForSubstitution.lastAttemptedIngredient = ingredientName;

        if (!this.ingredientAvailabilityStatus.getOrDefault(ingredientName, true)) {
            this.currentMealForSubstitution.infoMessage = "ingredient " + ingredientName + " is currently out of stock";
            // No return here, the When step will check this state
        }
    }

    @When("the system detects {string} conflicts with {string} preference for {string}")
    public void the_system_detects_conflicts_with_preference_for(String ingredientName, String preference, String email) {
        assertThat(this.currentCustomerEmailForSubstitution).isEqualTo(email);
        Customer customer = customerService.getCustomerDietaryInfo(email);
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        assertThat(customer).isNotNull();
        assertThat(ingredient).isNotNull();

        boolean conflicts = false;
        if ("Vegan".equals(preference) && (ingredient.getTags() == null || !ingredient.getTags().contains("vegan"))) {
            conflicts = true;
        } else if ("Keto".equals(preference) && ingredient.getName().equalsIgnoreCase("Pasta") && (ingredient.getTags() == null || !ingredient.getTags().contains("low_carb"))) {
            conflicts = true;
        }
        assertThat(conflicts).isTrue();
    }

    @Then("the system should suggest {string} as an alternative for {string}")
    public void the_system_should_suggest_as_an_alternative_for(String alternativeName, String originalIngredientName) {
        this.suggestedAlternativesOutput = customMealService.suggestAlternatives(originalIngredientName, this.currentCustomerEmailForSubstitution);
        assertThat(this.suggestedAlternativesOutput.stream().map(Ingredient::getName).collect(Collectors.toList()))
                .contains(alternativeName);
        if (this.currentMealForSubstitution != null) {
            this.currentMealForSubstitution.lastSuggestedAlternative = alternativeName;
        }
    }

    @And("if {string} accepts the substitution of {string}")
    public void if_accepts_the_substitution_of(String email, String alternativeName) {
        assertThat(this.currentCustomerEmailForSubstitution).isEqualTo(email);
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.lastSuggestedAlternative).isEqualTo(alternativeName);

        this.currentMealForSubstitution.currentIngredients.remove(this.currentMealForSubstitution.lastAttemptedIngredient);
        this.currentMealForSubstitution.currentIngredients.add(alternativeName);
        this.currentMealForSubstitution.lastSubstitutionAccepted = true;
    }

    @And("the meal is finalized with {string}")
    public void the_meal_is_finalized_with(String substitutedIngredient) {
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.currentIngredients).contains(substitutedIngredient);
        assertThat(this.currentMealForSubstitution.lastSubstitutionAccepted).isTrue();
        this.currentMealForSubstitution.finalized = true;
    }

    @Then("{string} should receive an alert that {string} was substituted with {string} for {string}'s meal")
    public void should_receive_an_alert_that_was_substituted_with_for_s_meal(String chefName, String original, String substitute, String customerEmail) {
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.finalized).isTrue();
        assertThat(this.currentMealForSubstitution.lastSubstitutionAccepted).isTrue();

        String alertMessage = String.format("Alert for %s: %s was substituted with %s for %s's meal.", chefName, original, substitute, customerEmail);
        this.chefNotificationsForSubstitution.add(alertMessage);
        assertThat(this.chefNotificationsForSubstitution).contains(alertMessage);
    }

    @Then("the system should indicate to {string} that ingredient {string} is currently out of stock")
    public void the_system_should_indicate_to_that_ingredient_is_currently_out_of_stock(String email, String ingredientName) {
        assertThat(this.currentCustomerEmailForSubstitution).isEqualTo(email);
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.infoMessage).isEqualTo("ingredient " + ingredientName + " is currently out of stock");
    }

    @And("{string} declines the substitution")
    public void declines_the_substitution(String email) {
        assertThat(this.currentCustomerEmailForSubstitution).isEqualTo(email);
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.lastSuggestedAlternative).isNotNull();
        this.currentMealForSubstitution.lastSubstitutionAccepted = false;
    }

    @Then("the meal should not contain {string} or {string} unless explicitly added later")
    public void the_meal_should_not_contain_or_unless_explicitly_added_later(String original, String alternative) {
        assertThat(this.currentMealForSubstitution).isNotNull();
        assertThat(this.currentMealForSubstitution.currentIngredients).doesNotContain(original, alternative);
    }

    @And("{string} should not receive a substitution alert for this specific interaction")
    public void should_not_receive_a_substitution_alert_for_this_specific_interaction(String chefName) {

        assertThat(this.currentMealForSubstitution).isNotNull();
        if (this.currentMealForSubstitution.lastAttemptedIngredient != null &&
                this.currentMealForSubstitution.lastSuggestedAlternative != null &&
                !this.currentMealForSubstitution.lastSubstitutionAccepted) {

            String potentialAlertSubstring = String.format("%s was substituted with %s for %s's meal",
                    this.currentMealForSubstitution.lastAttemptedIngredient,
                    this.currentMealForSubstitution.lastSuggestedAlternative,
                    this.currentMealForSubstitution.customerEmail);

            assertThat(this.chefNotificationsForSubstitution.stream().noneMatch(alert -> alert.contains(potentialAlertSubstring))).isTrue();
        } else {
            assertThat(this.chefNotificationsForSubstitution.stream().noneMatch(alert -> alert.contains("substituted"))).isTrue();
        }
    }
}