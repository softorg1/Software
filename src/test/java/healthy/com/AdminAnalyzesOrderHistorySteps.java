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
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AdminAnalyzesOrderHistorySteps {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private CustomerService customerService;

    private Administrator currentAdmin;
    private double reportedRevenueForLastMonth;
    private String reportPeriodMessage;
    private List<Order> retrievedOrdersForAdmin;
    private String mostFrequentMealIdentified;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/orders.txt"));
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files before scenario in AdminAnalyzesSteps: " + e.getMessage());
        }
        this.orderRepository = new OrderRepository();
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.orderService = new OrderService(this.orderRepository, this.customerRepository);

        this.reportedRevenueForLastMonth = 0.0;
        this.reportPeriodMessage = null;
        this.retrievedOrdersForAdmin = new ArrayList<>();
        this.currentAdmin = null;
        this.mostFrequentMealIdentified = null;
    }

    @Given("the system has the following aggregated order data:")
    public void the_system_has_the_following_aggregated_order_data(DataTable ordersTable) {
        List<Map<String, String>> rows = ordersTable.asMaps(String.class, String.class);
        Set<String> processedCustomerEmails = new HashSet<>();

        for (Map<String, String> columns : rows) {
            String customerEmail = columns.get("Customer Email");
            String orderId = columns.get("Order ID");
            String mealName = columns.get("Meal Name");
            String orderDate = columns.get("Order Date");
            String quantityStr = columns.get("Quantity");
            String totalPriceStr = columns.get("Total Price");

            if (customerEmail == null || orderId == null || mealName == null || orderDate == null || quantityStr == null || totalPriceStr == null) {
                System.err.println("ERROR AdminSteps: Row has missing columns: " + columns);
                continue;
            }

            if (!processedCustomerEmails.contains(customerEmail)) {
                customerService.registerOrGetCustomer(customerEmail);
                processedCustomerEmails.add(customerEmail);
            }

            int quantity = Integer.parseInt(quantityStr);
            double totalPrice = Double.parseDouble(totalPriceStr);
            double unitPrice = (quantity > 0) ? totalPrice / quantity : 0.0;


            Order order = new Order(
                    orderId,
                    customerEmail,
                    orderDate,
                    "Paid"
            );
            OrderItem item = new OrderItem(
                    mealName,
                    quantity,
                    unitPrice,
                    totalPrice
            );
            order.addItem(item);
            order.setOrderTotalPrice(totalPrice);
            orderRepository.saveOrder(order);
        }
    }

    @Given("an administrator {string} is logged in")
    public void an_administrator_is_logged_in(String adminUsername) {
        this.currentAdmin = new Administrator(adminUsername);
        this.currentAdmin.setLoggedIn(true);
    }

    @When("{string} requests a summary of all customer order history for the last month")
    public void requests_a_summary_of_all_customer_order_history_for_the_last_month(String adminUsername) {
        assertThat(this.currentAdmin).isNotNull();
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        assertThat(this.currentAdmin.isLoggedIn()).isTrue();
        this.retrievedOrdersForAdmin.clear();
        this.mostFrequentMealIdentified = null;
        this.reportPeriodMessage = null;
        this.reportedRevenueForLastMonth = 0.0;

        YearMonth targetMonth = YearMonth.of(2023, 11);

        this.retrievedOrdersForAdmin = orderService.getAllCompletedOrders().stream()
                .filter(order -> YearMonth.from(order.getOrderDate()).equals(targetMonth))
                .collect(Collectors.toList());

        if (this.retrievedOrdersForAdmin.isEmpty()){
            this.reportPeriodMessage = "No orders found for the last month.";
        } else {
            Map<String, Long> mealCounts = this.retrievedOrdersForAdmin.stream()
                    .flatMap(order -> order.getItems().stream())
                    .collect(Collectors.groupingBy(OrderItem::getMealName, Collectors.counting()));

            this.mostFrequentMealIdentified = mealCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);

            this.reportedRevenueForLastMonth = this.retrievedOrdersForAdmin.stream()
                    .mapToDouble(Order::getOrderTotalPrice)
                    .sum();
        }
    }

    @Then("the system should provide {string} with a dataset containing all orders from the last month")
    public void the_system_should_provide_with_a_dataset_containing_all_orders_from_the_last_month(String adminUsername) {
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        if(this.reportPeriodMessage != null && this.reportPeriodMessage.equals("No orders found for the last month.")){
            assertThat(this.retrievedOrdersForAdmin).isEmpty();
        } else {
            assertThat(this.retrievedOrdersForAdmin).isNotEmpty();
        }
    }

    @And("{string} should be able to identify the most frequently ordered meal as {string}")
    public void should_be_able_to_identify_the_most_frequently_ordered_meal_as(String adminUsername, String expectedMostFrequentMeal) {
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        assertThat(this.mostFrequentMealIdentified).isEqualTo(expectedMostFrequentMeal);
    }

    @And("{string} should be able to identify the total revenue from orders in the last month")
    public void should_be_able_to_identify_the_total_revenue_from_orders_in_the_last_month(String adminUsername) {
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        double expectedRevenueForLastMonth = 62.99;
        assertThat(this.reportedRevenueForLastMonth)
                .withFailMessage("Expected revenue for last month (November 2023) to be <%s> but was <%s>", expectedRevenueForLastMonth, this.reportedRevenueForLastMonth)
                .isEqualTo(expectedRevenueForLastMonth);
    }

    @When("{string} requests the complete order history for customer {string}")
    public void requests_the_complete_order_history_for_customer(String adminUsername, String customerEmail) {
        assertThat(this.currentAdmin).isNotNull();
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        assertThat(this.currentAdmin.isLoggedIn()).isTrue();
        customerService.registerOrGetCustomer(customerEmail);
        this.retrievedOrdersForAdmin = orderService.getPastOrdersForCustomer(customerEmail);
    }

    @Then("the system should provide {string} with the following orders for {string}:")
    public void the_system_should_provide_with_the_following_orders_for(String adminUsername, String customerEmail, DataTable expectedOrdersTable) {
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        List<Map<String, String>> expectedRows = expectedOrdersTable.asMaps(String.class, String.class);

        assertThat(this.retrievedOrdersForAdmin)
                .as("Checking orders for customer " + customerEmail)
                .hasSameSizeAs(expectedRows);

        for (Map<String, String> expectedRow : expectedRows) {
            String expectedOrderId = expectedRow.get("Order ID");
            Order actualOrder = this.retrievedOrdersForAdmin.stream()
                    .filter(o -> o.getOrderId().equals(expectedOrderId))
                    .findFirst().orElse(null);

            assertThat(actualOrder).as("Order with ID " + expectedOrderId + " not found for customer " + customerEmail).isNotNull();
            assertThat(actualOrder.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE)).isEqualTo(expectedRow.get("Order Date"));
            assertThat(actualOrder.getItems().get(0).getMealName()).isEqualTo(expectedRow.get("Meal Name"));
            assertThat(actualOrder.getItems().get(0).getQuantity()).isEqualTo(Integer.parseInt(expectedRow.get("Quantity")));
            assertThat(actualOrder.getOrderTotalPrice()).isEqualTo(Double.parseDouble(expectedRow.get("Total Price")));
        }
    }

    @Given("the system has no order data stored")
    public void the_system_has_no_order_data_stored() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/orders.txt"));
            this.orderRepository = new OrderRepository();
            this.orderService = new OrderService(this.orderRepository, this.customerRepository);
        } catch (IOException e) {
            System.err.println("Could not delete orders.txt for 'no order data' step: " + e.getMessage());
        }
        assertThat(orderService.getAllCompletedOrders()).isEmpty();
    }

    @When("{string} requests a summary of all customer order history")
    public void requests_a_summary_of_all_customer_order_history(String adminUsername) {
        assertThat(this.currentAdmin).isNotNull();
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        assertThat(this.currentAdmin.isLoggedIn()).isTrue();
        this.retrievedOrdersForAdmin = orderService.getAllCompletedOrders();
        if (this.retrievedOrdersForAdmin.isEmpty()){
            this.reportPeriodMessage = "No order data is available for analysis.";
        }
    }

    @Then("the system should indicate to {string} that no order data is available for analysis")
    public void the_system_should_indicate_to_that_no_order_data_is_available_for_analysis(String adminUsername) {
        assertThat(this.currentAdmin.getUsername()).isEqualTo(adminUsername);
        assertThat(this.retrievedOrdersForAdmin).isEmpty();
        assertThat(this.reportPeriodMessage).isEqualTo("No order data is available for analysis.");
    }
}