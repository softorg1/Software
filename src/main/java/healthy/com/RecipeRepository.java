package healthy.com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RecipeRepository {
    private static final String FILE_PATH = "src/main/resources/recipes.txt";
    private static final String SEPARATOR = ";";

    private List<Recipe> recipesCache;

    public RecipeRepository() {
        this.recipesCache = new ArrayList<>();
        loadRecipesFromFile();
    }

    private void loadRecipesFromFile() {
        this.recipesCache.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                Path path = Paths.get(FILE_PATH);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            } catch (IOException e) {
                System.err.println("Error creating recipes data file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length == 4) {
                    String name = parts[0].trim();
                    String ingredientsString = parts[1].trim();
                    String timeString = parts[2].trim();
                    String tagsString = parts[3].trim();

                    this.recipesCache.add(new Recipe(name, ingredientsString, timeString, tagsString));
                } else {
                    System.err.println("Skipping malformed line in recipes.txt: " + line);
                }
            }
        } catch (IOException | NumberFormatException e) { // Catching NumberFormatException from Recipe constructor
            System.err.println("Error loading recipes: " + e.getMessage());
        }
    }

    public void refreshCache() {
        loadRecipesFromFile();
    }

    public List<Recipe> getAllRecipes() {
        if (this.recipesCache.isEmpty() && new File(FILE_PATH).exists() && new File(FILE_PATH).length() > 0) {
            loadRecipesFromFile();
        }
        return new ArrayList<>(this.recipesCache); // Return a copy
    }

    public void saveAllRecipes(List<Recipe> recipesToSave) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Recipe recipe : recipesToSave) {
                String ingredients = String.join(",", recipe.getIngredients());
                String tags = String.join(",", recipe.getTags());
                String time = recipe.getTimeMinutes() + " minutes";

                writer.write(String.join(SEPARATOR,
                        recipe.getName(),
                        ingredients,
                        time,
                        tags
                ));
                writer.newLine();
            }
            refreshCache();
        } catch (IOException e) {
            System.err.println("Error saving recipes: " + e.getMessage());
        }
    }
}