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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IngredientRepository {
    private static final String FILE_PATH = "src/main/resources/ingredients.txt";
    private static final String SEPARATOR = ";";
    private static final String LIST_SEPARATOR = ",";

    private Map<String, Ingredient> ingredientsCache;

    public IngredientRepository() {
        this.ingredientsCache = new HashMap<>();
        loadIngredientsFromFile();
    }

    private void loadIngredientsFromFile() {
        this.ingredientsCache.clear();
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                Path path = Paths.get(FILE_PATH);
                if (!Files.exists(path.getParent())) {
                    Files.createDirectories(path.getParent());
                }
                Files.createFile(path);
            } catch (IOException e) {
                System.err.println("Error creating ingredient data file: " + e.getMessage());
            }
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length >= 6) { // name;price;tags;alternatives;stock;unit;reorderLvl
                    String name = parts[0].trim();
                    double price = Double.parseDouble(parts[1].trim());
                    String tagsString = parts[2].trim();
                    String alternativesString = (parts.length > 3 && parts[3] != null) ? parts[3].trim() : "";
                    int currentStock = (parts.length > 4 && parts[4] != null && !parts[4].isEmpty()) ? Integer.parseInt(parts[4].trim()) : 0;
                    String unit = (parts.length > 5 && parts[5] != null) ? parts[5].trim() : "unit";
                    int reorderLevel = (parts.length > 6 && parts[6] != null && !parts[6].isEmpty()) ? Integer.parseInt(parts[6].trim()) : 0;

                    this.ingredientsCache.put(name, new Ingredient(name, price, tagsString, alternativesString, currentStock, unit, reorderLevel));
                } else {
                    System.err.println("Skipping malformed line in ingredients.txt (expected at least 6 parts): " + line);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading ingredients: " + e.getMessage());
        }
    }

    public void refreshCache() {
        loadIngredientsFromFile();
    }

    public Ingredient findIngredientByName(String name) {
        if (this.ingredientsCache.isEmpty() && new File(FILE_PATH).exists() && new File(FILE_PATH).length() > 0) {
            loadIngredientsFromFile();
        }
        return this.ingredientsCache.get(name);
    }

    public List<Ingredient> getAllIngredients() {
        if (this.ingredientsCache.isEmpty() && new File(FILE_PATH).exists() && new File(FILE_PATH).length() > 0) {
            loadIngredientsFromFile();
        }
        return new ArrayList<>(this.ingredientsCache.values());
    }

    public void saveAllIngredients(List<Ingredient> ingredientsToSave) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Ingredient ingredient : ingredientsToSave) {
                String tags = String.join(LIST_SEPARATOR, ingredient.getTags());
                String alternatives = String.join(LIST_SEPARATOR, ingredient.getSuggestedAlternatives());
                writer.write(String.join(SEPARATOR,
                        ingredient.getName(),
                        String.valueOf(ingredient.getPrice()),
                        tags,
                        alternatives,
                        String.valueOf(ingredient.getCurrentStock()),
                        ingredient.getUnit(),
                        String.valueOf(ingredient.getReorderLevel())
                ));
                writer.newLine();
            }
            refreshCache();
        } catch (IOException e) {
            System.err.println("Error saving ingredients: " + e.getMessage());
        }
    }

    public void updateIngredient(Ingredient ingredientToUpdate) {
        List<Ingredient> allIngredients = getAllIngredients(); // Loads from file if cache is empty
        boolean found = false;
        for (int i = 0; i < allIngredients.size(); i++) {
            if (allIngredients.get(i).getName().equals(ingredientToUpdate.getName())) {
                allIngredients.set(i, ingredientToUpdate);
                found = true;
                break;
            }
        }
        if (!found) {
            allIngredients.add(ingredientToUpdate); // Add if not found
        }
        saveAllIngredients(allIngredients);
    }
}