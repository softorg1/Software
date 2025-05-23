package healthy.com;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Chef {
    private String name;
    private List<String> expertise;
    private String currentWorkload;
    private List<KitchenTask> assignedTasks;
    private List<String> notifications;
    private boolean loggedIn;

    public Chef(String name, String expertiseString, String initialWorkload) {
        this.name = name;
        if (expertiseString != null && !expertiseString.trim().isEmpty()) {
            this.expertise = Arrays.stream(expertiseString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.expertise = new ArrayList<>();
        }
        this.currentWorkload = initialWorkload;
        this.assignedTasks = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.loggedIn = false;
    }

    public Chef(String name) {
        this(name, "", "Low");
    }

    public String getName() {
        return name;
    }

    public List<String> getExpertise() {
        return new ArrayList<>(expertise);
    }

    public void setExpertise(List<String> expertise) {
        this.expertise = new ArrayList<>(expertise);
    }

    public void setExpertiseFromString(String expertiseString) {
        if (expertiseString != null && !expertiseString.trim().isEmpty()) {
            this.expertise = Arrays.stream(expertiseString.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } else {
            this.expertise = new ArrayList<>();
        }
    }

    public void addExpertise(String skill) {
        if (skill != null && !skill.trim().isEmpty() && !this.expertise.contains(skill.trim())) {
            this.expertise.add(skill.trim());
        }
    }

    public String getCurrentWorkload() {
        return currentWorkload;
    }

    public void setCurrentWorkload(String currentWorkload) {
        this.currentWorkload = currentWorkload;
    }

    public List<KitchenTask> getAssignedTasks() {
        return new ArrayList<>(assignedTasks);
    }

public void addTask(KitchenTask task) {
    if (task != null && this.assignedTasks.stream().noneMatch(t -> t.getTaskId().equals(task.getTaskId()))) {
        this.assignedTasks.add(task);
    }
}


    public void clearTasks() {
        this.assignedTasks.clear();
    }

    public List<String> getNotifications() {
        return new ArrayList<>(notifications);
    }

    public void addNotification(String notification) {
        if (notification != null && !notification.trim().isEmpty()) {
            this.notifications.add(notification);
        }
    }

    public void clearNotifications() {
        this.notifications.clear();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chef chef = (Chef) o;
        return Objects.equals(name, chef.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Chef{name='" + name + "', expertise=" + expertise + ", workload='" + currentWorkload + "'}";
    }
}
