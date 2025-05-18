package healthy.com;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KitchenManagementService {
    private ChefRepository chefRepository;

    public KitchenManagementService(ChefRepository chefRepository) {
        this.chefRepository = chefRepository;
    }

    public boolean assignTaskToChef(String orderId, String mealName, String chefName) {
        Chef chef = chefRepository.findChefByName(chefName);
        if (chef == null) {
            System.err.println("Cannot assign task: Chef " + chefName + " not found.");
            return false;
        }

        KitchenTask task = new KitchenTask(orderId, mealName);

        if (!isChefSuitableForTask(task, chef)) {
            System.err.println("Assignment failed: Chef " + chefName + " is not suitable for " + mealName + " based on expertise. Chef expertise: " + chef.getExpertise());
            return false;
        }

        task.setAssignedChefName(chefName);
        task.setStatus("Assigned");
        chef.addTask(task);
        updateChefWorkload(chef, true);
        notifyChef(chef, "New task assigned: Prepare " + mealName + " for " + orderId + ".");

        chefRepository.updateChef(chef);
        return true;
    }

    public boolean isChefSuitableForTask(KitchenTask task, Chef chef) {
        if (chef == null || task == null) return false;
        List<String> chefExpertise = chef.getExpertise();
        if (chefExpertise == null) chefExpertise = new ArrayList<>();

        String mealNameLower = task.getMealName().toLowerCase();
        List<String> chefExpertiseLower = chefExpertise.stream().map(String::toLowerCase).collect(Collectors.toList());

        if (mealNameLower.contains("steak")) {
            return chefExpertiseLower.contains("grilling") || chefExpertiseLower.contains("meats");
        }
        if (mealNameLower.contains("spaghetti") || mealNameLower.contains("pasta") || mealNameLower.contains("carbonara")) {
            return chefExpertiseLower.contains("italian") || chefExpertiseLower.contains("pastas");
        }
        if (mealNameLower.contains("pizza")) {
            return chefExpertiseLower.contains("italian") || chefExpertiseLower.contains("baking") || !chefExpertiseLower.isEmpty();
        }

        return true;
    }

    private void updateChefWorkload(Chef chef, boolean taskAdded) {
        String current = chef.getCurrentWorkload();
        if (taskAdded) {
            if ("Low".equalsIgnoreCase(current)) {
                chef.setCurrentWorkload("Medium");
            } else if ("Medium".equalsIgnoreCase(current)) {
                chef.setCurrentWorkload("High");
            }
        } else {
            if ("High".equalsIgnoreCase(current)) {
                chef.setCurrentWorkload("Medium");
            } else if ("Medium".equalsIgnoreCase(current)) {
                chef.setCurrentWorkload("Low");
            }
        }
    }

    public void notifyChef(Chef chef, String message) {
        chef.addNotification(message);
        System.out.println("Notification for " + chef.getName() + ": " + message);
    }

    public List<KitchenTask> getChefTasks(String chefName) {
        Chef chef = chefRepository.findChefByName(chefName);
        if (chef != null) {
            return chef.getAssignedTasks();
        }
        return new ArrayList<>();
    }

    public Chef getChefDetails(String chefName){
        return chefRepository.findChefByName(chefName);
    }
}