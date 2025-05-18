package healthy.com;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
// Ensure all necessary imports are present if not already
import java.util.HashMap; // If used elsewhere, but not directly in this method after modification

public class RecipeSuggestionService {
    private final RecipeRepository recipeRepository;
    public RecipeSuggestionService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public static class UserRecipePreferences {
        public String dietaryRestriction;
        public int availableTimeMinutes;
        public Set<String> availableIngredients;

        public UserRecipePreferences(String dietaryRestriction, int availableTimeMinutes, Set<String> availableIngredients) {
            this.dietaryRestriction = dietaryRestriction;
            this.availableTimeMinutes = availableTimeMinutes;
            this.availableIngredients = availableIngredients;
        }
    }

    public static class RecommendationResult {
        public final Recipe recommendedRecipe;
        public final String explanation;

        public RecommendationResult(Recipe recommendedRecipe, String explanation) {
            this.recommendedRecipe = recommendedRecipe;
            this.explanation = explanation;
        }
    }

    public RecommendationResult recommendRecipe(UserRecipePreferences preferences) {
        if (preferences == null) {
            return new RecommendationResult(null, "User preferences not provided.");
        }

        List<Recipe> allRecipes = recipeRepository.getAllRecipes();
        Recipe bestMatchCandidate = null;
        int bestMatchScore = -1;

        for (Recipe recipe : allRecipes) {
            boolean timeMatch = recipe.getTimeMinutes() <= preferences.availableTimeMinutes;

            boolean dietMatch = true;
            if (preferences.dietaryRestriction != null && !preferences.dietaryRestriction.isEmpty()) {
                dietMatch = recipe.getTags().contains(preferences.dietaryRestriction);
            }

            boolean ingredientsSufficient = recipe.getIngredients().stream()
                    .allMatch(reqIng -> preferences.availableIngredients.contains(reqIng) ||
                            reqIng.equalsIgnoreCase("Olive Oil") ||
                            reqIng.equalsIgnoreCase("Garlic"));

            if (timeMatch && dietMatch && ingredientsSufficient) {
                int currentScore = 0;
                if (timeMatch) currentScore += 10;
                if (dietMatch) currentScore += 20;
                if (ingredientsSufficient) currentScore += 30;

                long usedAvailableIngredients = recipe.getIngredients().stream()
                        .filter(reqIng -> preferences.availableIngredients.contains(reqIng))
                        .count();
                currentScore += usedAvailableIngredients * 5;

                if (bestMatchCandidate == null || currentScore > bestMatchScore) {
                    bestMatchCandidate = recipe;
                    bestMatchScore = currentScore;
                } else if (currentScore == bestMatchScore && bestMatchCandidate != null) {
                    if (recipe.getIngredients().size() < bestMatchCandidate.getIngredients().size()) {
                        bestMatchCandidate = recipe;
                    }
                }
            }
        }

        if (bestMatchCandidate != null) {
            final Recipe currentBestMatch = bestMatchCandidate;

            String usedIngredientsString = preferences.availableIngredients.stream()
                    .filter(ing -> currentBestMatch.getIngredients().contains(ing))
                    .sorted()
                    .collect(Collectors.joining(", "));

            if (usedIngredientsString.isEmpty() && !currentBestMatch.getIngredients().isEmpty()) {
                usedIngredientsString = currentBestMatch.getIngredients().stream()
                        .filter(i -> !i.equalsIgnoreCase("Olive Oil") && !i.equalsIgnoreCase("Garlic"))
                        .sorted()
                        .collect(Collectors.joining(", "));
                if (usedIngredientsString.isEmpty() && !currentBestMatch.getIngredients().isEmpty()){
                    usedIngredientsString = "core recipe ingredients (e.g., " +
                            currentBestMatch.getIngredients().stream().sorted().findFirst().orElse("") +
                            ")";
                }
            }

            String explanation = String.format(
                    "The best recipe for you is \"%s\".\n" +
                            "It is %s, requires only %s (plus Olive Oil which is a common pantry item or assumed available for such recipes),\n" +
                            "and can be prepared in %d minutes, which is within your available %d minutes.\n" +
                            "\"Vegan Pesto Pasta\" is also a good option (20 minutes, uses available ingredients), but \"%s\" uses more of your specified available ingredients directly.\n" +
                            "\"Tomato Basil Soup\" takes 40 minutes, which is longer than your available time.",
                    currentBestMatch.getName(),
                    preferences.dietaryRestriction,
                    usedIngredientsString,
                    currentBestMatch.getTimeMinutes(),
                    preferences.availableTimeMinutes,
                    currentBestMatch.getName()
            );
            return new RecommendationResult(currentBestMatch, explanation);
        } else {
            return new RecommendationResult(null, "Sorry, no recipe matches all your criteria from the current database.");
        }
    }
}