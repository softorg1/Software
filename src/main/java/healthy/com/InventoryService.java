package healthy.com;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InventoryService {
    private final IngredientRepository ingredientRepository;

    public InventoryService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    public Ingredient getIngredientStock(String ingredientName) {
        return ingredientRepository.findIngredientByName(ingredientName);
    }

    public List<Ingredient> getAllIngredientStockLevels() {
        return ingredientRepository.getAllIngredients();
    }

    public boolean useIngredients(Map<String, Integer> ingredientsUsed) {
        if (ingredientsUsed == null || ingredientsUsed.isEmpty()) {
            return true;
        }
        // First check if all ingredients are available in sufficient quantity
        for (Map.Entry<String, Integer> entry : ingredientsUsed.entrySet()) {
            Ingredient ingredient = ingredientRepository.findIngredientByName(entry.getKey());
            if (ingredient == null || ingredient.getCurrentStock() < entry.getValue()) {
                System.err.println("Not enough stock or ingredient not found for: " + entry.getKey());
                return false; // Or throw InsufficientStockException
            }
        }

        // If all checks pass, then decrease stock
        for (Map.Entry<String, Integer> entry : ingredientsUsed.entrySet()) {
            Ingredient ingredient = ingredientRepository.findIngredientByName(entry.getKey());
            // ingredient should not be null here due to the check above, but good practice
            if (ingredient != null) {
                ingredient.decreaseStock(entry.getValue());
                ingredientRepository.updateIngredient(ingredient); // Save changes
            }
        }
        return true;
    }

    public boolean useSingleIngredient(String ingredientName, int quantity) {
        if (ingredientName == null || quantity <= 0) {
            return false;
        }
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        if (ingredient == null) {
            System.err.println("Ingredient not found: " + ingredientName);
            return false;
        }
        if (ingredient.getCurrentStock() < quantity) {
            System.err.println("Not enough stock for: " + ingredientName + ". Required: " + quantity + ", Available: " + ingredient.getCurrentStock());
            return false;
        }
        ingredient.decreaseStock(quantity);
        ingredientRepository.updateIngredient(ingredient);
        return true;
    }


    public List<Ingredient> getIngredientsNeedingRestocking() {
        return ingredientRepository.getAllIngredients().stream()
                .filter(ingredient -> ingredient.getCurrentStock() < ingredient.getReorderLevel())
                .collect(Collectors.toList());
    }

    public void updateIngredientStock(String ingredientName, int newStockLevel) {
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        if (ingredient != null) {
            ingredient.setCurrentStock(newStockLevel);
            ingredientRepository.updateIngredient(ingredient);
        } else {
            System.err.println("Cannot update stock, ingredient not found: " + ingredientName);
        }
    }

    public void setIngredientReorderLevel(String ingredientName, int newReorderLevel) {
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        if (ingredient != null) {
            ingredient.setReorderLevel(newReorderLevel);
            ingredientRepository.updateIngredient(ingredient);
        } else {
            System.err.println("Cannot set reorder level, ingredient not found: " + ingredientName);
        }
    }
}