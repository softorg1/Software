package healthy.com;

import java.util.Objects;

public class KitchenTask {
    private String taskId;
    private String mealName;
    private String assignedChefName;
    private String status;
    private String dueTime;

    public KitchenTask(String taskId, String mealName, String assignedChefName, String status, String dueTime) {
        this.taskId = taskId;
        this.mealName = mealName;
        this.assignedChefName = assignedChefName;
        this.status = status;
        this.dueTime = dueTime;
    }

    public KitchenTask(String taskId, String mealName) {
        this(taskId, mealName, null, "Pending", null);
    }

    public String getTaskId() {
        return taskId;
    }

    public String getMealName() {
        return mealName;
    }

    public String getAssignedChefName() {
        return assignedChefName;
    }

    public void setAssignedChefName(String assignedChefName) {
        this.assignedChefName = assignedChefName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDueTime() {
        return dueTime;
    }

    public void setDueTime(String dueTime) {
        this.dueTime = dueTime;
    }

    @Override
    public String toString() {
        return mealName + " for " + taskId + (dueTime != null ? " (Due: " + dueTime + ")" : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KitchenTask that = (KitchenTask) o;
        return Objects.equals(taskId, that.taskId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
}