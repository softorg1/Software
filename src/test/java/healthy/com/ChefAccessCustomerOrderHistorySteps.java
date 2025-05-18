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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ChefAccessCustomerOrderHistorySteps {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private CustomerService customerService;
    private CustomerRepository customerRepository;

    private Chef currentChef;
    private List<Order> viewedCustomerOrderHistory;
    private String systemMessageToChef;

    // Re-using Order class from main production code
    // No need for SimpleOrder inner class anymore

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/orders.txt"));
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files before scenario: " + e.getMessage());
        }
        this.orderRepository = new OrderRepository();
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.orderService = new OrderService(this.orderRepository, this.customerRepository);

        this.viewedCustomerOrderHistory = new ArrayList<>();
        this.systemMessageToChef = null;
        this.currentChef = null;
    }


    @Given("a customer with email {string} has the following past orders:")
    public void a_customer_with_email_has_the_following_past_orders(String email, DataTable ordersTable) {
        customerService.registerOrGetCustomer(email); // Ensure customer exists
        List<Map<String, String>> rows = ordersTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            Order order = new Order(
                    columns.get("Order ID"),
                    email,
                    columns.get("Order Date"),
                    "Delivered" // Assuming past orders are delivered for this context
            );
            OrderItem item = new OrderItem(
                    columns.get("Meal Name"),
                    Integer.parseInt(columns.get("Quantity")),
                    0, // Unit price not specified in this Gherkin table, can be calculated if needed
                    0  // Item total price not specified, can be calculated if needed
            );
            order.addItem(item);
            // If a total order price was in the Gherkin table, we'd set it here
            // For now, Order class might calculate it based on items or it's not critical for this test's THEN steps.
            orderRepository.saveOrder(order);
        }
    }

    @Given("a customer with email {string} has no past orders")
    public void a_customer_with_email_has_no_past_orders(String email) {
        customerService.registerOrGetCustomer(email); // Ensure customer exists
        // No orders added for this customer
    }

    @Given("a chef {string} is logged into the system")
    public void a_chef_is_logged_into_the_system(String chefName) {
        this.currentChef = new Chef(chefName);
        this.currentChef.setLoggedIn(true);
    }

    @When("{string} requests to view order history for customer {string}")
    public void requests_to_view_order_history_for_customer(String chefName, String customerEmail) {
        assertThat(this.currentChef).isNotNull();
        assertThat(this.currentChef.getName()).isEqualTo(chefName);
        assertThat(this.currentChef.isLoggedIn()).isTrue();
        this.viewedCustomerOrderHistory.clear();
        this.systemMessageToChef = null;

        Customer customer = customerService.getCustomerDietaryInfo(customerEmail); // Check if customer exists
        if (customer == null) {
            this.systemMessageToChef = "Customer " + customerEmail + " was not found";
            return;
        }

        this.viewedCustomerOrderHistory = orderService.getPastOrdersForCustomer(customerEmail);
        if (this.viewedCustomerOrderHistory.isEmpty() && customer != null) {
            this.systemMessageToChef = customerEmail + " has no past orders";
        }
    }

    @Then("the system should display the following order history to {string} for {string}:")
    public void the_system_should_display_the_following_order_history_to_for(String chefName, String customerEmail, DataTable expectedOrdersTable) {
        assertThat(this.currentChef.getName()).isEqualTo(chefName);
        assertThat(this.systemMessageToChef).isNull();

        List<Map<String, String>> expectedRows = expectedOrdersTable.asMaps(String.class, String.class);
        assertThat(this.viewedCustomerOrderHistory).hasSameSizeAs(expectedRows);

        for (Map<String, String> expectedRow : expectedRows) {
            String expectedOrderId = expectedRow.get("Order ID");
            Order actualOrder = this.viewedCustomerOrderHistory.stream()
                    .filter(o -> o.getOrderId().equals(expectedOrderId))
                    .findFirst().orElse(null);

            assertThat(actualOrder).isNotNull();
            assertThat(actualOrder.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo(expectedRow.get("Order Date"));

            assertThat(actualOrder.getItems()).isNotEmpty();
            assertThat(actualOrder.getItems().get(0).getMealName()).isEqualTo(expectedRow.get("Meal Name"));
            assertThat(actualOrder.getItems().get(0).getQuantity()).isEqualTo(Integer.parseInt(expectedRow.get("Quantity")));
        }
    }

    @And("{string} should be able to identify frequently ordered meals like {string}")
    public void should_be_able_to_identify_frequently_ordered_meals_like(String chefName, String frequentlyOrderedMeal) {
        assertThat(this.currentChef.getName()).isEqualTo(chefName);
        assertThat(this.viewedCustomerOrderHistory).isNotNull();

        if (this.viewedCustomerOrderHistory.isEmpty()) {
            assertThat(this.viewedCustomerOrderHistory.stream().anyMatch(o -> o.getItems().stream().anyMatch(i -> i.getMealName().equals(frequentlyOrderedMeal))))
                    .as("Expected to find meal '" + frequentlyOrderedMeal + "' in history to check frequency, but history is empty or meal not found.").isTrue();
            return;
        }


        Map<String, Long> mealCounts = this.viewedCustomerOrderHistory.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getMealName, Collectors.counting()));

        boolean foundAndFrequent = mealCounts.entrySet().stream()
                .anyMatch(entry -> entry.getKey().equals(frequentlyOrderedMeal) && entry.getValue() > 1);

        assertThat(foundAndFrequent)
                .as("Chef " + chefName + " should be able to identify '" + frequentlyOrderedMeal + "' as ordered more than once. Counts: " + mealCounts)
                .isTrue();
    }

    @Then("the system should indicate to {string} that {string} has no past orders")
    public void the_system_should_indicate_to_that_has_no_past_orders(String chefName, String customerEmail) {
        assertThat(this.currentChef.getName()).isEqualTo(chefName);
        assertThat(this.viewedCustomerOrderHistory).isEmpty();
        assertThat(this.systemMessageToChef).isEqualTo(customerEmail + " has no past orders");
    }

    @Then("the system should indicate to {string} that customer {string} was not found")
    public void the_system_should_indicate_to_that_customer_was_not_found(String chefName, String customerEmail) {
        assertThat(this.currentChef.getName()).isEqualTo(chefName);
        assertThat(this.viewedCustomerOrderHistory).isEmpty();
        assertThat(this.systemMessageToChef).isEqualTo("Customer " + customerEmail + " was not found");
    }
}