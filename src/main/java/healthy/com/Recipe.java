package healthy.com;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Recipe {
    private String name;
    private Set<String> ingredients;
    private int timeMinutes;
    private Set<String> tags;

    public Recipe(String name, String ingredientsString, String timeString, String tagsString) {
        this.name = name;
        this.ingredients = new HashSet<>();
        if (ingredientsString != null && !ingredientsString.trim().isEmpty()) {
            this.ingredients.addAll(Arrays.stream(ingredientsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }

        if (timeString != null && !timeString.trim().isEmpty()) {
            try {
                this.timeMinutes = Integer.parseInt(timeString.replaceAll("[^0-9]", ""));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Could not parse time for recipe '" + name + "': " + timeString);
                this.timeMinutes = 0; // Default or error indicator
            }
        } else {
            this.timeMinutes = 0;
        }

        this.tags = new HashSet<>();
        if (tagsString != null && !tagsString.trim().isEmpty()) {
            this.tags.addAll(Arrays.stream(tagsString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet()));
        }
    }

    public String getName() {
        return name;
    }

    public Set<String> getIngredients() {
        return new HashSet<>(ingredients);
    }

    public int getTimeMinutes() {
        return timeMinutes;
    }

    public Set<String> getTags() {
        return new HashSet<>(tags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(name, recipe.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Recipe{name='" + name + "', ingredients=" + ingredients +
                ", timeMinutes=" + timeMinutes + ", tags=" + tags + "}";
    }
}