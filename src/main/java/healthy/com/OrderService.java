package healthy.com;

import java.time.LocalDate; // Added
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderService {
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    public void addOrderForTesting(Order order) {
        if (orderRepository.findOrderById(order.getOrderId()) == null) {
            orderRepository.saveOrder(order);
        }
    }

    public List<Order> getPastOrdersForCustomer(String customerEmail) {
        Customer customer = customerRepository.findCustomerByEmail(customerEmail);
        if (customer == null) {
            System.err.println("Customer not found: " + customerEmail);
            return Collections.emptyList();
        }
        return orderRepository.findOrdersByCustomerEmail(customerEmail);
    }

    public Order getOrderDetails(String orderId, String customerEmail) {
        Order order = orderRepository.findOrderById(orderId);
        if (order == null) {
            return null;
        }
        if (!order.getCustomerEmail().equals(customerEmail)) {
            System.err.println("Customer " + customerEmail + " is not authorized to view order " + orderId);
            return null;
        }
        return order;
    }

    public double getTotalRevenueForMonth(YearMonth monthYear) {
        return orderRepository.getAllOrders().stream()
                .filter(order -> order.getOrderDate() != null && YearMonth.from(order.getOrderDate()).equals(monthYear))
                .mapToDouble(Order::getOrderTotalPrice)
                .sum();
    }

    public double getOverallTotalRevenue() {
        return orderRepository.getAllOrders().stream()
                .mapToDouble(Order::getOrderTotalPrice)
                .sum();
    }

    public List<Order> getAllCompletedOrders() {
        return orderRepository.getAllOrders().stream()
                .filter(order -> order.getStatus() != null && ("Paid".equalsIgnoreCase(order.getStatus()) || "Delivered".equalsIgnoreCase(order.getStatus())))
                .collect(Collectors.toList());
    }

    public Invoice generateInvoiceForOrder(String orderId, String requestingCustomerEmail) {
        Order order = orderRepository.findOrderById(orderId);

        if (order == null) {
            System.err.println("Invoice generation failed: Order " + orderId + " not found.");
            return null;
        }

        if (!order.getCustomerEmail().equals(requestingCustomerEmail)) {
            System.err.println("Invoice generation failed: Customer " + requestingCustomerEmail +
                    " is not authorized for order " + orderId);
            return null;
        }

        if (order.getStatus() == null || (!"Paid".equalsIgnoreCase(order.getStatus()) && !"Delivered".equalsIgnoreCase(order.getStatus()))) {
            System.err.println("Invoice generation failed: Order " + orderId + " is not yet completed or paid.");
            return null;
        }

        return new Invoice(order.getOrderId(), order.getCustomerEmail(), order.getItems(), order.getOrderTotalPrice(), order.getStatus());
    }

    public List<Order> getOrdersScheduledForDeliveryOn(LocalDate date) {
        return orderRepository.getAllOrders().stream()
                .filter(order -> order.getOrderDate() != null && order.getOrderDate().equals(date))
                .collect(Collectors.toList());
    }
}