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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CustomerReceivesInvoiceSteps {

    private OrderService orderService;
    private OrderRepository orderRepository;
    private CustomerService customerService;
    private CustomerRepository customerRepository;

    private String currentLoggedInCustomerEmail;
    private Invoice generatedInvoiceResult;
    private String systemMessageToCustomerResult;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/orders.txt"));
            Files.deleteIfExists(Paths.get("src/main/resources/customers.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files before scenario in CustomerReceivesInvoiceSteps: " + e.getMessage());
        }
        this.orderRepository = new OrderRepository();
        this.customerRepository = new CustomerRepository();
        this.customerService = new CustomerService(this.customerRepository);
        this.orderService = new OrderService(this.orderRepository, this.customerRepository);

        this.generatedInvoiceResult = null;
        this.systemMessageToCustomerResult = null;
        this.currentLoggedInCustomerEmail = null;
    }

    @Given("the system has a completed order {string} for customer {string} with the following details:")
    public void the_system_has_a_completed_order_for_customer_with_details(String orderId, String customerEmail, DataTable itemsTable) {
        customerService.registerOrGetCustomer(customerEmail);

        Order order = new Order(orderId, customerEmail, LocalDate.now().toString(), "Paid");

        List<Map<String, String>> rows = itemsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            OrderItem item = new OrderItem(
                    columns.get("Item Description"),
                    Integer.parseInt(columns.get("Quantity")),
                    Double.parseDouble(columns.get("Unit Price")),
                    Double.parseDouble(columns.get("Total Price"))
            );
            order.addItem(item);
        }
        orderRepository.saveOrder(order);
    }

    @Given("the total amount for order {string} is {double}")
    public void the_total_amount_for_order_is(String orderId, Double totalAmount) {
        Order order = orderRepository.findOrderById(orderId);
        assertThat(order).isNotNull();
        order.setOrderTotalPrice(totalAmount);
        orderRepository.saveOrder(order);
    }

    @Given("customer {string} is logged in")
    public void customer_is_logged_in(String email) {
        this.currentLoggedInCustomerEmail = email;
        customerService.registerOrGetCustomer(email);
    }

    @When("{string} requests the invoice for order {string}")
    public void customer_requests_invoice_for_order(String customerEmail, String orderId) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(customerEmail);
        this.generatedInvoiceResult = null;
        this.systemMessageToCustomerResult = null;

        this.generatedInvoiceResult = orderService.generateInvoiceForOrder(orderId, customerEmail);

        if (this.generatedInvoiceResult == null) {
            Order order = orderRepository.findOrderById(orderId);
            if (order == null) {
                this.systemMessageToCustomerResult = "Order " + orderId + " was not found or no invoice is available";
            } else if (!order.getCustomerEmail().equals(customerEmail)) {
                this.systemMessageToCustomerResult = "You are not authorized to view the invoice for order " + orderId;
            } else {
                this.systemMessageToCustomerResult = "Invoice for order " + orderId + " could not be generated (e.g., not completed/paid).";
            }
        }
    }

    @When("{string} requests the invoice for a non-existent order {string}")
    public void customer_requests_invoice_for_non_existent_order(String customerEmail, String orderId) {
        customer_requests_invoice_for_order(customerEmail, orderId);
    }

    @Then("an invoice {string} should be generated for {string}")
    public void an_invoice_should_be_generated_for(String expectedInvoiceId, String expectedCustomerEmail) {
        assertThat(this.generatedInvoiceResult).as("Generated invoice should not be null").isNotNull();
        assertThat(this.generatedInvoiceResult.getInvoiceId()).isEqualTo(expectedInvoiceId);
        assertThat(this.generatedInvoiceResult.getCustomerEmail()).isEqualTo(expectedCustomerEmail);
        assertThat(this.systemMessageToCustomerResult).as("System message should be null on successful invoice generation").isNull();
    }

    @And("the invoice {string} should contain the order ID {string}")
    public void the_invoice_should_contain_order_id(String invoiceId, String expectedOrderId) {
        assertThat(this.generatedInvoiceResult).isNotNull();
        assertThat(this.generatedInvoiceResult.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(this.generatedInvoiceResult.getOrderId()).isEqualTo(expectedOrderId);
    }

    @And("the invoice {string} should list item {string} with quantity {int} and total price {double}")
    public void the_invoice_should_list_item_with_quantity_and_total_price(String invoiceId, String itemDescription, Integer quantity, Double totalPrice) {
        assertThat(this.generatedInvoiceResult).isNotNull();
        assertThat(this.generatedInvoiceResult.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(this.generatedInvoiceResult.getItems().stream()
                .anyMatch(item -> item.getMealName().equals(itemDescription) &&
                        item.getQuantity() == quantity &&
                        item.getItemTotalPrice() == totalPrice)
        ).isTrue();
    }

    @And("the invoice {string} should show a grand total of {double}")
    public void the_invoice_should_show_grand_total(String invoiceId, Double expectedGrandTotal) {
        assertThat(this.generatedInvoiceResult).isNotNull();
        assertThat(this.generatedInvoiceResult.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(this.generatedInvoiceResult.getGrandTotal()).isEqualTo(expectedGrandTotal);
    }

    @And("the invoice {string} should indicate payment status as {string}")
    public void the_invoice_should_indicate_payment_status_as(String invoiceId, String expectedPaymentStatus) {
        assertThat(this.generatedInvoiceResult).isNotNull();
        assertThat(this.generatedInvoiceResult.getInvoiceId()).isEqualTo(invoiceId);
        assertThat(this.generatedInvoiceResult.getPaymentStatus()).isEqualTo(expectedPaymentStatus);
    }

    @Then("the system should inform {string} that order {string} was not found or no invoice is available")
    public void system_should_inform_customer_order_not_found(String customerEmail, String orderId) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(customerEmail);
        assertThat(this.generatedInvoiceResult).isNull();
        assertThat(this.systemMessageToCustomerResult).isEqualTo("Order " + orderId + " was not found or no invoice is available");
    }

    @Then("the system should inform {string} that they are not authorized to view the invoice for order {string}")
    public void system_should_inform_customer_not_authorized(String customerEmail, String orderId) {
        assertThat(this.currentLoggedInCustomerEmail).isEqualTo(customerEmail);
        assertThat(this.generatedInvoiceResult).isNull();
        assertThat(this.systemMessageToCustomerResult).isEqualTo("You are not authorized to view the invoice for order " + orderId);
    }
}