package healthy.com;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class PurchaseOrder {
    private String purchaseOrderId; // New field for a unique PO ID
    private String ingredientName;
    private String supplierName; // Or Supplier object
    private int quantity;
    private String unit;
    private double pricePerUnit;
    private double totalCost;
    private LocalDate orderDate;
    private String status; // e.g., "Generated", "Sent", "Confirmed", "Received"
    private boolean automaticallyGenerated;

    public PurchaseOrder(String purchaseOrderId, String ingredientName, String supplierName, int quantity, String unit, double pricePerUnit, boolean auto) {
        this.purchaseOrderId = purchaseOrderId;
        this.ingredientName = ingredientName;
        this.supplierName = supplierName;
        this.quantity = quantity;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.totalCost = quantity * pricePerUnit;
        this.orderDate = LocalDate.now(); // Default to current date
        this.status = "Generated"; // Default status
        this.automaticallyGenerated = auto;
    }

    public String getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(String purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isAutomaticallyGenerated() {
        return automaticallyGenerated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchaseOrder that = (PurchaseOrder) o;
        return Objects.equals(purchaseOrderId, that.purchaseOrderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(purchaseOrderId);
    }

    @Override
    public String toString() {
        return "PurchaseOrder{" +
                "id='" + purchaseOrderId + '\'' +
                ", item='" + ingredientName + '\'' +
                ", supplier='" + supplierName + '\'' +
                ", qty=" + quantity + " " + unit +
                ", price=" + String.format("%.2f", pricePerUnit) +
                ", total=" + String.format("%.2f", totalCost) +
                ", date=" + orderDate.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                ", status='" + status + '\'' +
                ", auto=" + automaticallyGenerated +
                '}';
    }
}