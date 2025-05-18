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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ChefRepository {
    private static final String FILE_PATH = "src/main/resources/chefs.txt";
    private static final String SEPARATOR = ";";
    private static final String LIST_SEPARATOR = ",";
    private static final String TASK_SEPARATOR = "~";
    private static final String TASK_LIST_SEPARATOR = "|";

    public ChefRepository() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error initializing chefs data file: " + e.getMessage());
        }
    }

    private List<Chef> loadChefs() {
        List<Chef> chefs = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return chefs;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length >= 3) {
                    Chef chef = new Chef(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    if (parts.length >= 4 && !parts[3].isEmpty()) {
                        String[] taskEntries = parts[3].split(TASK_LIST_SEPARATOR_REGEX());
                        for (String taskEntry : taskEntries) {
                            if (taskEntry.trim().isEmpty()) continue;
                            String[] taskDetails = taskEntry.split(TASK_SEPARATOR);
                            if (taskDetails.length >= 2) {
                                String taskId = taskDetails[0];
                                String mealName = taskDetails[1];
                                String dueTime = taskDetails.length > 2 && !taskDetails[2].equals("null") ? taskDetails[2] : null;
                                String status = taskDetails.length > 3 ? taskDetails[3] : "Assigned";
                                chef.addTask(new KitchenTask(taskId, mealName, chef.getName(), status, dueTime));
                            }
                        }
                    }
                    if (parts.length >= 5 && !parts[4].isEmpty()) {
                        Arrays.stream(parts[4].split(LIST_SEPARATOR))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(chef::addNotification);
                    }
                    chefs.add(chef);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading chefs: " + e.getMessage());
        }
        return chefs;
    }

    private void saveAllChefs(List<Chef> chefs) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Chef chef : chefs) {
                String expertiseString = String.join(LIST_SEPARATOR, chef.getExpertise());
                String tasksString = chef.getAssignedTasks().stream()
                        .map(task -> String.join(TASK_SEPARATOR, task.getTaskId(), task.getMealName(),
                                task.getDueTime() == null ? "null" : task.getDueTime(),
                                task.getStatus() == null ? "Assigned" : task.getStatus()))
                        .collect(Collectors.joining(TASK_LIST_SEPARATOR));
                String notificationsString = String.join(LIST_SEPARATOR, chef.getNotifications());

                writer.write(String.join(SEPARATOR,
                        chef.getName(),
                        expertiseString,
                        chef.getCurrentWorkload(),
                        tasksString,
                        notificationsString
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving chefs: " + e.getMessage());
        }
    }

    private static String TASK_LIST_SEPARATOR_REGEX() {
        return "\\" + TASK_LIST_SEPARATOR;
    }

    public void saveChef(Chef chefToSave) {
        List<Chef> chefs = loadChefs();
        Optional<Chef> existingChef = chefs.stream()
                .filter(c -> c.getName().equals(chefToSave.getName()))
                .findFirst();

        if (existingChef.isPresent()) {
            chefs.remove(existingChef.get());
        }
        chefs.add(chefToSave);
        saveAllChefs(chefs);
    }

    public Chef findChefByName(String name) {
        return loadChefs().stream()
                .filter(chef -> chef.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public List<Chef> getAllChefs() {
        return loadChefs();
    }

    public void updateChef(Chef updatedChef) {
        saveChef(updatedChef);
    }
}