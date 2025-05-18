package healthy.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomMealService {
    private final IngredientRepository ingredientRepository;
    private final CustomerService customerService;

    public CustomMealService(IngredientRepository ingredientRepository, CustomerService customerService) {
        this.ingredientRepository = ingredientRepository;
        this.customerService = customerService;
    }

    public CustomMealRequest startCustomMeal(String customerEmail, String mealName) {
        if (customerEmail == null || mealName == null || customerEmail.trim().isEmpty() || mealName.trim().isEmpty()) {
            System.err.println("Customer email and meal name cannot be null or empty to start a custom meal.");
            return null;
        }
        return new CustomMealRequest(customerEmail, mealName);
    }

    public boolean addIngredientToCustomMeal(CustomMealRequest mealRequest, String ingredientName) {
        if (mealRequest == null || ingredientName == null || ingredientName.trim().isEmpty()) {
            System.err.println("Meal request or ingredient name cannot be null or empty.");
            if(mealRequest != null) mealRequest.setFailureReason("Invalid ingredient data provided.");
            return false;
        }
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        if (ingredient == null) {
            mealRequest.setFailureReason(ingredientName + " is unavailable");
            mealRequest.setCreationSuccessful(false);
            return false;
        }
        mealRequest.addIngredient(ingredient);
        return true;
    }

    public CustomMealRequest finalizeCustomMeal(CustomMealRequest mealRequest) {
        if (mealRequest == null) {
            System.err.println("Meal request cannot be null for finalization.");
            return null;
        }
        if (mealRequest.getSelectedIngredients().isEmpty()) {
            mealRequest.setFailureReason("Cannot finalize a meal with no ingredients.");
            mealRequest.setCreationSuccessful(false);
            return mealRequest;
        }

        Customer customer = customerService.getCustomerDietaryInfo(mealRequest.getCustomerEmail());
        List<String> customerPreferences = new ArrayList<>();
        if (customer != null && customer.getDietaryPreferences() != null) {
            customerPreferences.addAll(customer.getDietaryPreferences());
        }
        List<String> customerAllergies = new ArrayList<>();
        if (customer != null && customer.getAllergies() != null) {
            customerAllergies.addAll(customer.getAllergies());
        }

        for (Ingredient selectedIngredient : mealRequest.getSelectedIngredients()) {
            Ingredient systemIngredient = ingredientRepository.findIngredientByName(selectedIngredient.getName());
            if (systemIngredient == null) {
                mealRequest.setFailureReason(selectedIngredient.getName() + " became unavailable.");
                mealRequest.setCreationSuccessful(false);
                return mealRequest;
            }
            if (customerPreferences.contains("Vegan") && (systemIngredient.getTags() == null || !systemIngredient.getTags().contains("vegan"))) {
                mealRequest.setFailureReason(systemIngredient.getName() + " is not compatible with their \"Vegan\" preference or meal composition");
                mealRequest.setCreationSuccessful(false);
                return mealRequest;
            }
            if (!isIngredientGenerallyCompatible(systemIngredient, customerPreferences, customerAllergies)) {
                mealRequest.setFailureReason(systemIngredient.getName() + " is not compatible with customer preferences or allergies.");
                mealRequest.setCreationSuccessful(false);
                return mealRequest;
            }
        }

        double totalPrice = 0;
        mealRequest.clearMealTags();
        boolean allIngredientsAreVegan = true;

        if (mealRequest.getSelectedIngredients().isEmpty()){
            mealRequest.setCreationSuccessful(false);
            mealRequest.setFailureReason("No ingredients selected.");
            return mealRequest;
        }

        for (Ingredient ingredient : mealRequest.getSelectedIngredients()) {
            totalPrice += ingredient.getPrice();
            if (ingredient.getTags() != null) {
                if (!ingredient.getTags().contains("vegan")) {
                    allIngredientsAreVegan = false;
                }
            } else {
                allIngredientsAreVegan = false;
            }
        }
        mealRequest.setTotalPrice(totalPrice);

        if (allIngredientsAreVegan) {
            mealRequest.addMealTag("vegan");
        } else {
            mealRequest.addMealTag("non_vegan");
        }

        mealRequest.setCreationSuccessful(true);
        return mealRequest;
    }

    private boolean isIngredientGenerallyCompatible(Ingredient ingredient, List<String> preferences, List<String> allergies) {
        if (ingredient == null) return false;
        if (preferences != null) {
            if (preferences.contains("Keto") ) {
                boolean isHighCarb = ingredient.getTags() != null && ingredient.getTags().contains("high_carb");
                boolean isPastaNotLowCarb = ingredient.getName().equalsIgnoreCase("Pasta") && (ingredient.getTags() == null || !ingredient.getTags().contains("low_carb"));
                if(isHighCarb || (isPastaNotLowCarb && !ingredient.getName().equals("Zucchini Noodles"))){
                    return false;
                }
            }
        }
        if (allergies != null && ingredient.getTags() != null) {
            for (String allergy : allergies) {
                if (allergy == null || allergy.trim().isEmpty()) continue;
                if (ingredient.getName().toLowerCase().contains(allergy.toLowerCase()) ||
                        ingredient.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(allergy))) {
                    return false;
                }
            }
        }
        return true;
    }


    public List<Ingredient> suggestAlternatives(String originalIngredientName, String customerEmail) {
        List<Ingredient> suggestions = new ArrayList<>();
        if (originalIngredientName == null || originalIngredientName.trim().isEmpty() || customerEmail == null || customerEmail.trim().isEmpty()){
            return suggestions;
        }

        Ingredient originalIngredient = ingredientRepository.findIngredientByName(originalIngredientName);
        Customer customer = customerService.getCustomerDietaryInfo(customerEmail);

        if (originalIngredient == null || customer == null) {
            return suggestions;
        }

        List<String> customerPreferences = customer.getDietaryPreferences();
        List<String> customerAllergies = customer.getAllergies();

        List<Ingredient> allSystemIngredients = ingredientRepository.getAllIngredients();

        for (Ingredient potentialAlternative : allSystemIngredients) {
            if (potentialAlternative.getName().equals(originalIngredientName)) {
                continue;
            }

            boolean isCompatible = isIngredientCompatibleWithPreferences(potentialAlternative, customerPreferences, customerAllergies);
            if (!isCompatible) {
                continue;
            }

            if (originalIngredient.getSuggestedAlternatives() != null &&
                    originalIngredient.getSuggestedAlternatives().contains(potentialAlternative.getName())) {
                suggestions.add(potentialAlternative);
                continue;
            }

            // More generic suggestion logic based on conflicts
            boolean originalIsNonVegan = (originalIngredient.getTags() != null && !originalIngredient.getTags().contains("vegan"));
            boolean preferenceIsVegan = preferencesContain(customerPreferences, "Vegan");
            boolean alternativeIsVegan = (potentialAlternative.getTags() != null && potentialAlternative.getTags().contains("vegan"));

            if (preferenceIsVegan && originalIsNonVegan && alternativeIsVegan && areIngredientTypesSimilar(originalIngredient, potentialAlternative)) {
                suggestions.add(potentialAlternative);
                continue;
            }

            boolean preferenceIsKeto = preferencesContain(customerPreferences,"Keto");
            boolean originalIsHighCarbPasta = originalIngredient.getName().equalsIgnoreCase("Pasta") && (originalIngredient.getTags() == null || !originalIngredient.getTags().contains("low_carb"));
            boolean alternativeIsLowCarb = (potentialAlternative.getTags() != null && potentialAlternative.getTags().contains("low_carb"));

            if (preferenceIsKeto && originalIsHighCarbPasta && alternativeIsLowCarb && areIngredientTypesSimilar(originalIngredient, potentialAlternative)) {
                if (potentialAlternative.getName().equals("Zucchini Noodles")) { // Specific good alternative
                    suggestions.add(potentialAlternative);
                }
            }
        }
        return suggestions.stream().distinct().collect(Collectors.toList());
    }

    private boolean preferencesContain(List<String> preferences, String preferenceToCheck) {
        if (preferences == null) return false;
        return preferences.contains(preferenceToCheck);
    }

    private boolean isIngredientCompatibleWithPreferences(Ingredient ingredient, List<String> preferences, List<String> allergies) {
        if (ingredient == null) return false;

        if (preferences != null) {
            if (preferences.contains("Vegan") && (ingredient.getTags() == null || !ingredient.getTags().contains("vegan"))) {
                return false;
            }
            if (preferences.contains("Keto") ) {
                boolean isHighCarb = ingredient.getTags() != null && ingredient.getTags().contains("high_carb");
                // Allow Pasta if it's specifically tagged as low_carb (e.g. konjac pasta)
                boolean isPastaAndNotLowCarb = ingredient.getName().equalsIgnoreCase("Pasta") && (ingredient.getTags() == null || !ingredient.getTags().contains("low_carb"));

                if(isHighCarb || isPastaAndNotLowCarb){
                    // Exception: Zucchini Noodles is a low_carb pasta alternative
                    if (!ingredient.getName().equals("Zucchini Noodles")) {
                        return false;
                    }
                }
            }
        }

        if (allergies != null) {
            for (String allergy : allergies) {
                if (allergy == null || allergy.trim().isEmpty()) continue;
                if (ingredient.getName().toLowerCase().contains(allergy.toLowerCase()) ||
                        (ingredient.getTags() != null && ingredient.getTags().stream().anyMatch(tag -> tag.equalsIgnoreCase(allergy)))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean areIngredientTypesSimilar(Ingredient ing1, Ingredient ing2) {
        if (ing1 == null || ing2 == null || ing1.getTags() == null || ing2.getTags() == null) return false;
        Set<String> typeTags = Set.of("protein", "vegetable", "grain", "sauce_base", "main_course", "topping", "cheese_flavor", "flour");
        for (String typeTag : typeTags) {
            if (ing1.getTags().contains(typeTag) && ing2.getTags().contains(typeTag)) {
                return true;
            }
        }
        if (ing1.getName().contains("Cheese") && ing2.getName().contains("Yeast")) return true;
        if (ing1.getName().contains("Pasta") && ing2.getName().contains("Noodles")) return true;
        if (ing1.getName().contains("Flour") && ing2.getName().contains("Flour")) return true;
        return false;
    }
}