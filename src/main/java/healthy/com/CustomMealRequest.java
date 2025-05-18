package healthy.com;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomMealRequest {
    private String customerEmail;
    private String mealName;
    private List<Ingredient> selectedIngredients;
    private double totalPrice;
    private Set<String> mealTags;
    private boolean creationSuccessful;
    private String failureReason;

    public CustomMealRequest(String customerEmail, String mealName) {
        this.customerEmail = customerEmail;
        this.mealName = mealName;
        this.selectedIngredients = new ArrayList<>();
        this.mealTags = new HashSet<>();
        this.totalPrice = 0.0;
        this.creationSuccessful = false;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getMealName() {
        return mealName;
    }

    public List<Ingredient> getSelectedIngredients() {
        return selectedIngredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.selectedIngredients.add(ingredient);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Set<String> getMealTags() {
        return mealTags;
    }

    public void addMealTag(String tag) {
        this.mealTags.add(tag);
    }

    public void clearMealTags() {
        this.mealTags.clear();
    }

    public boolean isCreationSuccessful() {
        return creationSuccessful;
    }

    public void setCreationSuccessful(boolean creationSuccessful) {
        this.creationSuccessful = creationSuccessful;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}