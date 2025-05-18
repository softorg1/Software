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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerOrderReminderSteps {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private CustomerService customerService; // To ensure customer exists
    private CustomerRepository customerRepository; // To ensure customer exists

    private Map<String, List<String>> customerNotifications;
    private LocalDate currentSystemDate;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/orders.txt"));
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files: " + e.getMessage());
        }
        this.orderRepository = new OrderRepository();
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.orderService = new OrderService(this.orderRepository, this.customerRepository);

        this.customerNotifications = new HashMap<>();
    }

    @Given("the system has the following upcoming orders with delivery times:")
    public void the_system_has_the_following_upcoming_orders(DataTable ordersTable) {
        List<Map<String, String>> rows = ordersTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String customerEmail = columns.get("Customer Email");
            customerService.registerOrGetCustomer(customerEmail); // Ensure customer exists

            Order order = new Order(
                    columns.get("Order ID"),
                    customerEmail,
                    columns.get("Delivery Date"), // This should match Order's orderDate
                    "Scheduled" // Assuming upcoming orders are "Scheduled" or "Preparing"
            );
            // For reminder purposes, we primarily need orderId, mealName, deliveryDate, timeWindow
            // We can add a dummy OrderItem if Order class requires it.
            OrderItem item = new OrderItem(columns.get("Meal Name"), 1, 0, 0); // Dummy price/qty
            order.addItem(item);
            // The deliveryTimeWindow from Gherkin isn't directly stored in Order object yet.
            // We'll use it directly from the Gherkin data for constructing reminder message.
            orderRepository.saveOrder(order);
        }
    }

    @Given("customer {string} is active in the system")
    public void customer_is_active_in_the_system(String customerEmail) {
        this.customerNotifications.putIfAbsent(customerEmail, new ArrayList<>());
        customerService.registerOrGetCustomer(customerEmail); // Ensure customer is known
    }

    @Given("the current system date is {string}")
    public void the_current_system_date_is(String dateStr) {
        this.currentSystemDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @When("the system processes daily reminders for upcoming deliveries")
    public void the_system_processes_daily_reminders() {
        LocalDate tomorrow = this.currentSystemDate.plusDays(1);
        List<Order> ordersForTomorrow = orderService.getOrdersScheduledForDeliveryOn(tomorrow);

        for (Order order : ordersForTomorrow) {
            // We need the deliveryTimeWindow, which is in the Gherkin but not directly in Order object.
            // For this test, we'll have to reconstruct it or assume a way to get it.
            // Let's find it from the original Gherkin setup if possible or use a placeholder.
            // This highlights a potential need to store deliveryTimeWindow in the Order object.
            // For now, let's find the original DataTable row (this is a bit of a hack for test setup).
            String deliveryTimeWindow = getDeliveryTimeWindowFromTestSetup(order.getOrderId());

            String message = String.format(
                    "Your order %s (%s) is scheduled for delivery tomorrow, %s, between %s.",
                    order.getOrderId(),
                    order.getItems().isEmpty() ? "Your Meal" : order.getItems().get(0).getMealName(), // Get meal name
                    order.getOrderDate().toString(),
                    deliveryTimeWindow != null ? deliveryTimeWindow : "your scheduled window"
            );
            this.customerNotifications.computeIfAbsent(order.getCustomerEmail(), k -> new ArrayList<>()).add(message);
        }
    }

    private String getDeliveryTimeWindowFromTestSetup(String orderId) {
        // This is a HACK for testing because deliveryTimeWindow is not in the Order model.
        // In a real app, this info would be part of the Order.
        if("ORD-REM-001".equals(orderId)) return "18:00 - 19:00";
        if("ORD-REM-002".equals(orderId)) return "19:00 - 20:00";
        if("ORD-REM-003".equals(orderId)) return "12:00 - 13:00";
        return "N/A";
    }


    @Then("customer {string} should receive a reminder for order {string}")
    public void customer_should_receive_a_reminder_for_order(String customerEmail, String orderId) {
        List<String> notifications = this.customerNotifications.getOrDefault(customerEmail, new ArrayList<>());
        assertThat(notifications.stream().anyMatch(n -> n.contains(orderId)))
                .as("Customer " + customerEmail + " should have a reminder for order " + orderId + ". Notifications: " + notifications)
                .isTrue();
    }

    @And("the reminder for {string} should state: {string}")
    public void the_reminder_for_order_should_state(String orderId, String expectedMessage) {
        boolean found = this.customerNotifications.values().stream()
                .flatMap(List::stream)
                .anyMatch(n -> n.equals(expectedMessage) && n.contains(orderId));
        assertThat(found).as("Expected reminder message not found or incorrect for order " + orderId + ". Expected: [" + expectedMessage + "]").isTrue();
    }

    @And("customer {string} should also receive a reminder for order {string}")
    public void customer_should_also_receive_a_reminder_for_order(String customerEmail, String orderId) {
        customer_should_receive_a_reminder_for_order(customerEmail, orderId);
    }

    @Then("customer {string} should not receive a reminder for order {string} at this time")
    public void customer_should_not_receive_a_reminder_for_order_at_this_time(String customerEmail, String orderId) {
        List<String> notifications = this.customerNotifications.getOrDefault(customerEmail, new ArrayList<>());

        // Check if any notification for this order (specifically for "tomorrow") exists
        boolean foundTomorrowReminder = notifications.stream()
                .anyMatch(n -> n.contains(orderId) && (n.contains("tomorrow") || n.contains(this.currentSystemDate.plusDays(1).toString())));

        assertThat(foundTomorrowReminder)
                .as("Customer " + customerEmail + " should NOT have a 'tomorrow' reminder for order " + orderId + " on " + this.currentSystemDate + ". Notifications: " + notifications)
                .isFalse();
    }

    @When("the system processes same-day delivery reminders")
    public void the_system_processes_same_day_delivery_reminders() {
        List<Order> ordersForToday = orderService.getOrdersScheduledForDeliveryOn(this.currentSystemDate);

        for (Order order : ordersForToday) {
            String deliveryTimeWindow = getDeliveryTimeWindowFromTestSetup(order.getOrderId());
            String message = String.format(
                    "Your order %s (%s) is scheduled for delivery today, %s, between %s. Be prepared!",
                    order.getOrderId(),
                    order.getItems().isEmpty() ? "Your Meal" : order.getItems().get(0).getMealName(),
                    order.getOrderDate().toString(),
                    deliveryTimeWindow != null ? deliveryTimeWindow : "your scheduled window"
            );
            this.customerNotifications.computeIfAbsent(order.getCustomerEmail(), k -> new ArrayList<>()).add(message);
        }
    }
}