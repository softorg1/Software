package healthy.com; // أو healthy.com.stepdefinitions

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.Before;
import io.cucumber.datatable.DataTable;

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

public class CustomerViewPastOrdersSteps {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private CustomerService customerService; // For customer login state
    private CustomerRepository customerRepository; // For customer login state

    private String currentLoggedInUserEmail;
    private List<Order> displayedOrdersForCustomer;
    private String systemMessageToUser;

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

        this.displayedOrdersForCustomer = new ArrayList<>();
        this.systemMessageToUser = null;
        this.currentLoggedInUserEmail = null;
    }

    @Given("a customer with email {string} is logged in")
    public void a_customer_with_email_is_logged_in(String email) {
        this.currentLoggedInUserEmail = email;
        customerService.registerOrGetCustomer(email);
    }

    @Given("the customer {string} has the following past orders:")
    public void the_customer_has_the_following_past_orders(String email, DataTable ordersTable) {
        List<Map<String, String>> rows = ordersTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            Order order = new Order(
                    columns.get("Order ID"),
                    email, // Use the email from the step parameter
                    columns.get("Order Date"),
                    columns.get("Status")
            );
            // For simplicity, assuming items are described by Meal Name and total for that item line
            // In a real scenario, OrderItem objects would be more detailed
            OrderItem item = new OrderItem(
                    columns.get("Meal Name"),
                    1, // Assuming quantity 1 for simplicity in this Gherkin table
                    Double.parseDouble(columns.get("Total Price")), // Assuming this is item price for qty 1
                    Double.parseDouble(columns.get("Total Price"))
            );
            order.addItem(item);
            // The Gherkin table for this step doesn't explicitly have an "Order Total Price"
            // The Order class recalculates it when items are added, or it can be set.
            // Let's ensure the Order's total matches the sum of its items here.
            // If the Gherkin table had a specific "Order Total Price", we'd use that.
            order.setOrderTotalPrice(Double.parseDouble(columns.get("Total Price"))); // Simulating the total for a single item order

            orderService.addOrderForTesting(order);
        }
    }

    @Given("the customer {string} has no past orders")
    public void the_customer_has_no_past_orders(String email) {
        // No action needed here if orders are loaded on demand and file is empty
        // Or ensure the list for this customer is empty
        List<Order> orders = orderService.getPastOrdersForCustomer(email);
        assertThat(orders).isEmpty();
    }

    @Given("no customer is logged in")
    public void no_customer_is_logged_in() {
        this.currentLoggedInUserEmail = null;
    }

    @When("the customer requests to view their past orders")
    public void the_customer_requests_to_view_their_past_orders() {
        this.displayedOrdersForCustomer.clear();
        this.systemMessageToUser = null;
        if (this.currentLoggedInUserEmail != null) {
            this.displayedOrdersForCustomer = orderService.getPastOrdersForCustomer(this.currentLoggedInUserEmail);
            if (this.displayedOrdersForCustomer.isEmpty()) {
                this.systemMessageToUser = "You have no past orders.";
            }
        } else {
            this.systemMessageToUser = "Please log in to view your order history.";
        }
    }

    @When("a user attempts to view past orders")
    public void a_user_attempts_to_view_past_orders() {
        the_customer_requests_to_view_their_past_orders();
    }


    @Then("the system should display the following orders for {string}:")
    public void the_system_should_display_the_following_orders_for(String email, DataTable expectedOrdersTable) {
        assertThat(this.currentLoggedInUserEmail).isEqualTo(email);
        assertThat(this.systemMessageToUser).isNull();

        List<Map<String, String>> expectedRows = expectedOrdersTable.asMaps(String.class, String.class);
        assertThat(this.displayedOrdersForCustomer).hasSameSizeAs(expectedRows);

        for (Map<String, String> expectedRow : expectedRows) {
            String expectedOrderId = expectedRow.get("Order ID");
            Order actualOrder = this.displayedOrdersForCustomer.stream()
                    .filter(o -> o.getOrderId().equals(expectedOrderId))
                    .findFirst().orElse(null);

            assertThat(actualOrder).isNotNull();
            assertThat(actualOrder.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo(expectedRow.get("Order Date"));
            assertThat(actualOrder.getStatus()).isEqualTo(expectedRow.get("Status"));
            assertThat(actualOrder.getOrderTotalPrice()).isEqualTo(Double.parseDouble(expectedRow.get("Total Price")));

            // Assuming one item per order for simplicity based on the Gherkin table
            assertThat(actualOrder.getItems()).hasSize(1);
            assertThat(actualOrder.getItems().get(0).getMealName()).isEqualTo(expectedRow.get("Meal Name"));
        }
    }

    @Then("the system should indicate to {string} that they have no past orders")
    public void the_system_should_indicate_to_that_they_have_no_past_orders(String email) {
        assertThat(this.currentLoggedInUserEmail).isEqualTo(email);
        assertThat(this.displayedOrdersForCustomer).isEmpty();
        assertThat(this.systemMessageToUser).isEqualTo("You have no past orders.");
    }

    @Then("the system should prompt the user to log in to view their order history")
    public void the_system_should_prompt_the_user_to_log_in_to_view_their_order_history() {
        assertThat(this.currentLoggedInUserEmail).isNull();
        assertThat(this.systemMessageToUser).isEqualTo("Please log in to view your order history.");
    }

    @Given("the system has a completed order {string} for customer {string}")
    public void the_system_has_a_completed_order_for_customer(String orderId, String customerEmail) {
        Order order = new Order(orderId, customerEmail, LocalDate.now().toString(), "Completed");
        // Add a dummy item for simplicity or leave items empty if not specified
        orderService.addOrderForTesting(order);
    }
}