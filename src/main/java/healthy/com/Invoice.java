package healthy.com;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Invoice {
    private String invoiceId;
    private String orderId;
    private String customerEmail;
    private LocalDate invoiceDate;
    private List<OrderItem> items;
    private double subtotal;
    private double taxAmount;
    private double grandTotal;
    private String paymentStatus;

    public Invoice(String orderId, String customerEmail, List<OrderItem> orderItems, double orderTotalAmount, String orderPaymentStatus) {
        this.invoiceId = "INV-" + orderId;
        this.orderId = orderId;
        this.customerEmail = customerEmail;
        this.invoiceDate = LocalDate.now();
        this.items = new ArrayList<>(orderItems);
        this.subtotal = orderTotalAmount;
        this.taxAmount = 0.0;
        this.grandTotal = this.subtotal + this.taxAmount;
        this.paymentStatus = orderPaymentStatus;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(invoiceId, invoice.invoiceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(invoiceId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- INVOICE ---\n");
        sb.append("Invoice ID: ").append(invoiceId).append("\n");
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer: ").append(customerEmail).append("\n");
        sb.append("Date: ").append(invoiceDate.format(DateTimeFormatter.ISO_LOCAL_DATE)).append("\n");
        sb.append("Payment Status: ").append(paymentStatus).append("\n");
        sb.append("\nItems:\n");
        for (OrderItem item : items) {
            sb.append(String.format("  - %s (Qty: %d, Unit Price: %.2f, Total: %.2f)\n",
                    item.getMealName(), item.getQuantity(), item.getUnitPrice(), item.getItemTotalPrice()));
        }
        sb.append(String.format("\nSubtotal: %.2f\n", subtotal));
        sb.append(String.format("Tax: %.2f\n", taxAmount));
        sb.append(String.format("Grand Total: %.2f\n", grandTotal));
        sb.append("----------------\n");
        return sb.toString();
    }
}