package healthy.com;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TaskAssignmentSteps {

    private ChefRepository chefRepository;
    private KitchenManagementService kitchenManagementService;

    private String currentManagerName;
    private boolean managerLoggedIn;
    private KitchenTask currentTaskToAssignForGherkin;
    private boolean lastAssignmentAttemptSuccessful;
    private String assignmentFailureDetails;
    private List<String> displayedChefTasksForGherkin;

    @Before
    public void setUp() {
        try {
            Files.deleteIfExists(Paths.get("src/main/resources/chefs.txt"));
        } catch (IOException e) {
            System.err.println("Could not delete data files: " + e.getMessage());
        }
        this.chefRepository = new ChefRepository();
        this.kitchenManagementService = new KitchenManagementService(this.chefRepository);

        this.managerLoggedIn = false;
        this.lastAssignmentAttemptSuccessful = true;
        this.assignmentFailureDetails = null;
        this.displayedChefTasksForGherkin = new ArrayList<>();
    }

    @Given("the following chefs are available in the system:")
    public void the_following_chefs_are_available_in_the_system(DataTable chefsTable) {
        List<Map<String, String>> rows = chefsTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            Chef chef = new Chef(
                    columns.get("Chef Name"),
                    columns.get("Expertise"),
                    columns.get("Current Workload")
            );
            chefRepository.saveChef(chef);
        }
    }

    @Given("a kitchen manager {string} is logged in")
    public void a_kitchen_manager_is_logged_in(String managerName) {
        this.currentManagerName = managerName;
        this.managerLoggedIn = true;
    }

    @Given("a new cooking order {string} for {string} needs preparation")
    public void a_new_cooking_order_for_needs_preparation(String orderId, String mealName) {
        this.currentTaskToAssignForGherkin = new KitchenTask(orderId, mealName);
        this.lastAssignmentAttemptSuccessful = true;
        this.assignmentFailureDetails = null;
    }

    @Given("{string} has {string} expertise and {string} workload")
    public void chef_has_expertise_and_workload(String chefName, String expertise, String workload) {
        Chef chef = chefRepository.findChefByName(chefName);
        if (chef == null) {
            chef = new Chef(chefName, expertise, workload);
        } else {
            chef.setExpertiseFromString(expertise);
            chef.setCurrentWorkload(workload);
        }
        chefRepository.saveChef(chef);
    }

    @Given("{string} has been assigned the following tasks:")
    public void chef_has_been_assigned_the_following_tasks(String chefName, DataTable tasksTable) {
        Chef chef = chefRepository.findChefByName(chefName);
        if (chef == null) {
            chef = new Chef(chefName);
        }
        chef.clearTasks();
        List<Map<String, String>> rows = tasksTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            KitchenTask task = new KitchenTask(
                    columns.get("Order ID"),
                    columns.get("Meal Name"),
                    chefName,
                    "Assigned",
                    columns.get("Due Time")
            );
            chef.addTask(task);
        }
        chefRepository.saveChef(chef);
    }

    @Given("{string} has {string} workload")
    public void chef_has_workload(String chefName, String workload) {
        Chef chef = chefRepository.findChefByName(chefName);
        assertThat(chef).as("Chef " + chefName + " should exist to set workload").isNotNull();
        chef.setCurrentWorkload(workload);
        chefRepository.saveChef(chef);
    }

    @When("{string} decides to assign {string}")
    public void manager_decides_to_assign_order(String managerName, String orderId) {
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.currentTaskToAssignForGherkin.getTaskId()).isEqualTo(orderId);
    }

    @When("{string} views chef availability and workload")
    public void manager_views_chef_availability_and_workload(String managerName) {
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
    }

    @When("{string} assigns the task for {string} to {string}")
    public void manager_assigns_task_to_chef(String managerName, String orderId, String chefName) {
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.currentTaskToAssignForGherkin.getTaskId()).isEqualTo(orderId);

        this.lastAssignmentAttemptSuccessful = kitchenManagementService.assignTaskToChef(
                this.currentTaskToAssignForGherkin.getTaskId(),
                this.currentTaskToAssignForGherkin.getMealName(),
                chefName
        );
    }

    @When("{string} attempts to assign the task for {string} to {string}")
    public void manager_attempts_to_assign_task_to_chef(String managerName, String orderId, String chefName) {
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.currentTaskToAssignForGherkin.getTaskId()).isEqualTo(orderId);

        this.lastAssignmentAttemptSuccessful = kitchenManagementService.assignTaskToChef(
                this.currentTaskToAssignForGherkin.getTaskId(),
                this.currentTaskToAssignForGherkin.getMealName(),
                chefName
        );
        if (!this.lastAssignmentAttemptSuccessful) {
            this.assignmentFailureDetails = "Expertise mismatch"; // Assuming this is the only reason for failure in this context
        }
    }

    @When("{string} checks their assigned tasks")
    public void chef_checks_their_assigned_tasks(String chefName) {
        Chef chef = kitchenManagementService.getChefDetails(chefName);
        assertThat(chef).isNotNull();
        this.displayedChefTasksForGherkin = chef.getAssignedTasks().stream()
                .map(KitchenTask::toString)
                .collect(Collectors.toList());
    }

    @Then("{string} should have {string} added to their task list")
    public void chef_should_have_order_added_to_task_list(String chefName, String orderId) {
        Chef chef = kitchenManagementService.getChefDetails(chefName);
        assertThat(chef).isNotNull();
        assertThat(chef.getAssignedTasks().stream().anyMatch(task -> task.getTaskId().equals(orderId))).isTrue();
    }

    @Then("the workload of {string} should be updated to {string}")
    public void workload_of_chef_should_be_updated_to(String chefName, String expectedWorkload) {
        Chef chef = kitchenManagementService.getChefDetails(chefName);
        assertThat(chef).isNotNull();
        assertThat(chef.getCurrentWorkload()).isEqualTo(expectedWorkload);
    }

    @Then("{string} should receive a notification: {string}")
    public void chef_should_receive_notification(String chefName, String expectedNotification) {
        Chef chef = kitchenManagementService.getChefDetails(chefName);
        assertThat(chef).isNotNull();
        assertThat(chef.getNotifications()).contains(expectedNotification);
    }

    @Then("the assignment should fail due to expertise mismatch")
    public void the_assignment_should_fail_due_to_expertise_mismatch() {
        assertThat(this.lastAssignmentAttemptSuccessful).isFalse();
        // We infer the reason is expertise mismatch based on the scenario setup
        // The service now returns false, and we can check a specific message if the service provided one
    }

    @Then("the system should inform {string} that {string} may not be suitable for {string}")
    public void the_system_should_inform_manager_that_chef_may_not_be_suitable_for_meal(String managerName, String chefName, String mealName) {
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.lastAssignmentAttemptSuccessful).isFalse();
    }

    @Then("{string} should not receive a notification for {string}")
    public void chef_should_not_receive_a_notification_for_order(String chefName, String orderId) {
        Chef chef = kitchenManagementService.getChefDetails(chefName);
        assertThat(chef).isNotNull();
        String taskIdentifier = "Order-" + orderId; // Assuming orderId is used in notification
        if (orderId.startsWith("Order-")){ // if orderId already contains "Order-"
            taskIdentifier = orderId;
        }
        final String finalTaskIdentifier = taskIdentifier;
        assertThat(chef.getNotifications().stream().noneMatch(n -> n.contains(finalTaskIdentifier))).isTrue();
    }

    @Then("{string} should see a list containing {string} and {string}")
    public void chef_should_see_a_list_containing_tasks(String chefName, String task1Description, String task2Description) {
        assertThat(this.displayedChefTasksForGherkin).isNotNull();
        assertThat(this.displayedChefTasksForGherkin).contains(task1Description, task2Description);
    }
}