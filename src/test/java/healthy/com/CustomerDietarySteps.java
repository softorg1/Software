package healthy.com; // أو healthy.com.stepdefinitions إذا كان في مجلد فرعي

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.java.Before;

// import healthy.com.repository.CustomerRepository; // تم حذف .repository
// import healthy.com.service.CustomerService;    // تم حذف .service
// لا حاجة لـ import إذا كانت الكلاسات في نفس الحزمة healthy.com
// ولكن إذا كانت CustomerDietarySteps في حزمة فرعية مثل stepdefinitions، فستحتاج إلى:
// import healthy.com.CustomerRepository;
// import healthy.com.CustomerService;
// import healthy.com.Customer;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerDietarySteps {

    private CustomerService customerService;
    private CustomerRepository customerRepository;
    private Customer currentCustomer;
    private String currentCustomerEmail;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt")); // تم تعديل المسار
        } catch (IOException e) {
            System.err.println("Could not delete customers.txt before scenario: " + e.getMessage());
        }
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.currentCustomer = null;
        this.currentCustomerEmail = null;
    }

    @Given("a customer with email {string} is registered and logged in")
    public void a_customer_with_email_is_registered_and_logged_in(String email) {
        this.currentCustomerEmail = email;
        this.currentCustomer = customerService.registerOrGetCustomer(email);
        assertThat(this.currentCustomer).isNotNull();
    }

    @When("the customer navigates to their dietary information page")
    public void the_customer_navigates_to_their_dietary_information_page() {
    }

    @And("the customer adds {string} as a dietary preference")
    public void the_customer_adds_as_a_dietary_preference(String preference) {
        assertThat(currentCustomerEmail).isNotNull();
        customerService.addDietaryPreference(currentCustomerEmail, preference);
    }

    @And("the customer adds {string} as an allergy")
    public void the_customer_adds_as_an_allergy(String allergy) {
        assertThat(currentCustomerEmail).isNotNull();
        customerService.addAllergy(currentCustomerEmail, allergy);
    }

    @And("the customer saves their dietary information")
    public void the_customer_saves_their_dietary_information() {
    }

    @Then("the system should store {string} as a dietary preference for {string}")
    public void the_system_should_store_as_a_dietary_preference_for(String preference, String email) {
        Customer customer = customerService.getCustomerDietaryInfo(email);
        assertThat(customer).as("Customer with email " + email + " should exist").isNotNull();
        assertThat(customer.getDietaryPreferences()).as("Dietary preferences for " + email).contains(preference);
    }

    @And("the system should store {string} as an allergy for {string}")
    public void the_system_should_store_as_an_allergy_for(String allergy, String email) {
        Customer customer = customerService.getCustomerDietaryInfo(email);
        assertThat(customer).as("Customer with email " + email + " should exist").isNotNull();
        assertThat(customer.getAllergies()).as("Allergies for " + email).contains(allergy);
    }

    @And("the system should record no allergies for {string}")
    public void the_system_should_record_no_allergies_for(String email) {
        Customer customer = customerService.getCustomerDietaryInfo(email);
        assertThat(customer).as("Customer with email " + email + " should exist").isNotNull();
        assertThat(customer.getAllergies()).as("Allergies for " + email).isEmpty();
    }
}