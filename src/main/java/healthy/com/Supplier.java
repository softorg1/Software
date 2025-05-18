package healthy.com;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Supplier {
    private String id;
    private String name;
    private String contactEmail;
    private Map<String, Double> itemPrices; // Ingredient name -> Price

    public Supplier(String id, String name, String contactEmail) {
        this.id = id;
        this.name = name;
        this.contactEmail = contactEmail;
        this.itemPrices = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public Map<String, Double> getItemPrices() {
        return new HashMap<>(itemPrices); // Return a copy
    }

    public void setItemPrice(String itemName, double price) {
        this.itemPrices.put(itemName, price);
    }

    public Double getPriceForItem(String itemName) {
        return this.itemPrices.get(itemName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(id, supplier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Supplier{id='" + id + "', name='" + name + "', contactEmail='" + contactEmail + "'}";
    }
}