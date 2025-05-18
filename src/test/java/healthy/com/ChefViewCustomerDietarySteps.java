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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ChefViewCustomerDietarySteps {

    private CustomerService customerService;
    private CustomerRepository customerRepository; // For setup
    private Chef currentChef;
    private Customer viewedCustomer;
    private String systemMessageToChef;


    @Before
    public void setUp() {
        try {

            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete customers.txt before scenario in ChefViewSteps: " + e.getMessage());
        }
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.currentChef = null;
        this.viewedCustomer = null;
        this.systemMessageToChef = null;
    }


    @Given("a customer with email {string} has the following dietary information:")
    public void a_customer_with_email_has_the_following_dietary_information(String email, DataTable dataTable) {
        Customer customer = customerService.registerOrGetCustomer(email);
        List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String type = columns.get("type");
            String value = columns.get("value");
            if ("preference".equalsIgnoreCase(type)) {
                customerService.addDietaryPreference(email, value);
            } else if ("allergy".equalsIgnoreCase(type)) {
                customerService.addAllergy(email, value);
            }
        }
    }

    @Given("a customer with email {string} has no specific dietary information stored")
    public void a_customer_with_email_has_no_specific_dietary_information_stored(String email) {
        customerService.registerOrGetCustomer(email);
        // No preferences or allergies added by default
    }

    @Given("a chef is logged into the system")
    public void a_chef_is_logged_into_the_system() {
        this.currentChef = new Chef("DefaultChef");
        this.currentChef.setLoggedIn(true);
    }

    @When("the chef requests to view dietary information for customer {string}")
    public void the_chef_requests_to_view_dietary_information_for_customer(String email) {
        assertThat(this.currentChef).isNotNull();
        assertThat(this.currentChef.isLoggedIn()).isTrue();

        this.viewedCustomer = customerService.getCustomerDietaryInfo(email);
        if (this.viewedCustomer == null) {
            this.systemMessageToChef = "Customer " + email + " was not found";
        }
    }

    @Then("the system should display the following dietary preferences for the chef:")
    public void the_system_should_display_the_following_dietary_preferences_for_the_chef(DataTable expectedPreferencesTable) {
        assertThat(this.viewedCustomer).isNotNull();
        assertThat(this.systemMessageToChef).isNull();
        List<String> expectedPreferences = expectedPreferencesTable.asList(String.class).stream().skip(1).collect(Collectors.toList());
        assertThat(this.viewedCustomer.getDietaryPreferences()).containsExactlyInAnyOrderElementsOf(expectedPreferences);
    }

    @And("the system should display the following allergies for the chef:")
    public void the_system_should_display_the_following_allergies_for_the_chef(DataTable expectedAllergiesTable) {
        assertThat(this.viewedCustomer).isNotNull();
        assertThat(this.systemMessageToChef).isNull();
        List<String> expectedAllergies = expectedAllergiesTable.asList(String.class).stream().skip(1).collect(Collectors.toList());
        assertThat(this.viewedCustomer.getAllergies()).containsExactlyInAnyOrderElementsOf(expectedAllergies);
    }

    @And("the system should display no allergies for the chef for this customer")
    public void the_system_should_display_no_allergies_for_the_chef_for_this_customer() {
        assertThat(this.viewedCustomer).isNotNull();
        assertThat(this.systemMessageToChef).isNull();
        assertThat(this.viewedCustomer.getAllergies()).isEmpty();
    }

    @Then("the system should indicate to the chef that no dietary preferences are stored for this customer")
    public void the_system_should_indicate_to_the_chef_that_no_dietary_preferences_are_stored_for_this_customer() {
        assertThat(this.viewedCustomer).isNotNull();
        assertThat(this.systemMessageToChef).isNull();
        assertThat(this.viewedCustomer.getDietaryPreferences()).isEmpty();
    }

    @And("the system should indicate to the chef that no allergies are stored for this customer")
    public void the_system_should_indicate_to_the_chef_that_no_allergies_are_stored_for_this_customer() {
        assertThat(this.viewedCustomer).isNotNull();
        assertThat(this.systemMessageToChef).isNull();
        assertThat(this.viewedCustomer.getAllergies()).isEmpty();
    }

    @Then("the system should indicate to the chef that the customer {string} was not found")
    public void the_system_should_indicate_to_the_chef_that_the_customer_was_not_found(String email) {
        assertThat(this.viewedCustomer).isNull();
        assertThat(this.systemMessageToChef).isEqualTo("Customer " + email + " was not found");
    }
}