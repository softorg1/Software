package healthy.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Ingredient {
    private String name;
    private double price;
    private Set<String> tags;
    private List<String> suggestedAlternatives;
    private int currentStock;
    private String unit;
    private int reorderLevel;

    public Ingredient(String name, double price, String tagsString, String alternativesString, int currentStock, String unit, int reorderLevel) {
        this.name = name;
        this.price = price;
        this.tags = new HashSet<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            this.tags.addAll(Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(tag -> !tag.isEmpty())
                    .collect(Collectors.toSet()));
        }
        this.suggestedAlternatives = new ArrayList<>();
        if (alternativesString != null && !alternativesString.trim().isEmpty()) {
            this.suggestedAlternatives.addAll(
                    Arrays.stream(alternativesString.split(","))
                            .map(String::trim)
                            .filter(alt -> !alt.isEmpty())
                            .collect(Collectors.toList())
            );
        }
        this.currentStock = currentStock;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
    }

    public Ingredient(String name, double price, String tagsString, String alternativesString) {
        this(name, price, tagsString, alternativesString, 0, "unit", 0); // Default stock values
    }

    public Ingredient(String name, double price, String tagsString) {
        this(name, price, tagsString, "", 0, "unit", 0);
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Set<String> getTags() {
        return tags;
    }

    public List<String> getSuggestedAlternatives() {
        return new ArrayList<>(suggestedAlternatives);
    }

    public void addSuggestedAlternative(String alternative) {
        if (alternative != null && !alternative.trim().isEmpty() && !this.suggestedAlternatives.contains(alternative.trim())) {
            this.suggestedAlternatives.add(alternative.trim());
        }
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public void decreaseStock(int quantity) {
        if (quantity > 0) {
            this.currentStock -= quantity;
            if (this.currentStock < 0) {
                this.currentStock = 0; // Prevent negative stock
            }
        }
    }

    public void increaseStock(int quantity) {
        if (quantity > 0) {
            this.currentStock += quantity;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Ingredient{name='" + name + "', price=" + price +
                ", stock=" + currentStock + " " + unit +
                ", reorderLvl=" + reorderLevel + ", tags=" + tags +
                ", alternatives=" + suggestedAlternatives + "}";
    }
}