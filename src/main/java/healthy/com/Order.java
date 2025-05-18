package healthy.com;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Order {
    private String orderId;
    private String customerEmail;
    private LocalDate orderDate;
    private List<OrderItem> items;
    private double orderTotalPrice;
    private String status; // e.g., "Delivered", "Preparing"

    public Order(String orderId, String customerEmail, String orderDateStr, String status) {
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.orderDate = LocalDate.parse(orderDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        this.items = new ArrayList<>();
        this.status = status;
        this.orderTotalPrice = 0.0; // Will be calculated or set
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void addItem(OrderItem item) {
        this.items.add(item);
        this.orderTotalPrice += item.getItemTotalPrice(); // Recalculate total when item is added
    }

    public void setItems(List<OrderItem> items) {
        this.items = new ArrayList<>(items); // Create a new list to avoid external modification issues
        recalculateOrderTotalPrice();
    }


    public double getOrderTotalPrice() {
        return orderTotalPrice;
    }

    public void setOrderTotalPrice(double orderTotalPrice) {
        this.orderTotalPrice = orderTotalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void recalculateOrderTotalPrice() {
        this.orderTotalPrice = 0.0;
        for (OrderItem item : this.items) {
            this.orderTotalPrice += item.getItemTotalPrice();
        }
    }


    @Override
    public String toString() {
        return "Order ID: " + orderId + ", Date: " + orderDate + ", Customer: " + customerEmail +
                ", Status: " + status + ", Total: " + String.format("%.2f", orderTotalPrice) +
                ", Items: " + items.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(orderId, order.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}