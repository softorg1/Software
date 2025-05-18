package healthy.com;

import java.util.Objects;

public class OrderItem {
    private String mealName;
    private int quantity;
    private double unitPrice;
    private double itemTotalPrice;

    public OrderItem(String mealName, int quantity, double unitPrice, double itemTotalPrice) {
        this.mealName = mealName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.itemTotalPrice = itemTotalPrice;
    }

    public String getMealName() {
        return mealName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getItemTotalPrice() {
        return itemTotalPrice;
    }

    @Override
    public String toString() {
        return quantity + " x " + mealName + " @ " + String.format("%.2f", unitPrice) + " = " + String.format("%.2f", itemTotalPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderItem orderItem = (OrderItem) o;
        return quantity == orderItem.quantity &&
                Double.compare(orderItem.unitPrice, unitPrice) == 0 &&
                Double.compare(orderItem.itemTotalPrice, itemTotalPrice) == 0 &&
                Objects.equals(mealName, orderItem.mealName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mealName, quantity, unitPrice, itemTotalPrice);
    }
}