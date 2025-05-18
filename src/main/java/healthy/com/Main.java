package healthy.com;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
// import java.util.Arrays; // Unused import
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    private static CustomerService customerService;
    private static OrderService orderService;
    private static IngredientRepository ingredientRepository;
    private static CustomMealService customMealService;
    private static KitchenManagementService kitchenManagementService;
    private static ChefRepository chefRepository;
    private static InventoryService inventoryService;
    private static SupplierRepository supplierRepository;
    private static PurchasingService purchasingService;
    private static RecipeRepository recipeRepository;
    private static RecipeSuggestionService recipeSuggestionService;

    private static CustomerRepository customerRepository; // Added static field
    private static OrderRepository orderRepository;       // Added static field

    private static final Scanner scanner = new Scanner(System.in);
    private static String loggedInUserEmail = null;
    private static String loggedInUserRole = null;

    public static void main(String[] args) {
        initializeServices();
        System.out.println("Welcome to the Special Cook Project Management System!");

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = getInputInt();

            if (loggedInUserEmail == null && choice != 1 && choice != 0 && choice != 7) {
                System.out.println("Please login first or choose option 7 for recipe recommendation.");
                continue;
            }

            switch (choice) {
                case 1:
                    manageUserLogin();
                    break;
                case 2:
                    handleOptionTwo();
                    break;
                case 3:
                    handleOptionThree();
                    break;
                case 4:
                    handleOptionFour();
                    break;
                case 5:
                    handleOptionFive();
                    break;
                case 6:
                    handleOptionSix();
                    break;
                case 7:
                    getRecipeRecommendationConsole();
                    break;
                case 9:
                    logoutUser();
                    break;
                case 0:
                    running = false;
                    System.out.println("Exiting application. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void initializeServices() {
        customerRepository = new CustomerRepository();
        orderRepository = new OrderRepository();
        ingredientRepository = new IngredientRepository();
        chefRepository = new ChefRepository();
        supplierRepository = new SupplierRepository();
        recipeRepository = new RecipeRepository();

        customerService = new CustomerService(customerRepository);
        orderService = new OrderService(orderRepository, customerRepository);
        customMealService = new CustomMealService(ingredientRepository, customerService);
        kitchenManagementService = new KitchenManagementService(chefRepository);
        inventoryService = new InventoryService(ingredientRepository);

        Map<String, IngredientSupplierLink> initialLinks = new HashMap<>();
        purchasingService = new PurchasingService(ingredientRepository, supplierRepository, initialLinks);
        recipeSuggestionService = new RecipeSuggestionService(recipeRepository);
    }

    private static void handleOptionTwo() {
        if ("customer".equals(loggedInUserRole)) {
            manageDietaryInfo(loggedInUserEmail);
        } else if ("admin".equals(loggedInUserRole)) {
            generateMonthlyRevenueReport();
        } else if ("chef".equals(loggedInUserRole)) {
            viewCustomerDietaryInfoAsChef();
        }
        else {
            System.out.println("Invalid option for your role or not logged in.");
            loginOrPrompt();
        }
    }

    private static void handleOptionThree() {
        if ("customer".equals(loggedInUserRole)) {
            viewPastOrders(loggedInUserEmail);
        } else if ("admin".equals(loggedInUserRole)) {
            generateOverallRevenueReport();
        } else if ("chef".equals(loggedInUserRole)) {
            assignTaskToChefConsole();
        }
        else {
            System.out.println("Invalid option for your role or not logged in.");
            loginOrPrompt();
        }
    }

    private static void handleOptionFour() {
        if ("customer".equals(loggedInUserRole)) {
            createCustomMealConsole();
        } else if ("admin".equals(loggedInUserRole)) {
            viewAllOrdersAsAdmin();
        } else if ("chef".equals(loggedInUserRole)) {
            viewMyTasksAsChef();
        }
        else {
            System.out.println("Invalid option for your role or not logged in.");
            loginOrPrompt();
        }
    }

    private static void handleOptionFive() {
        if ("customer".equals(loggedInUserRole)) {
            requestInvoiceConsole();
        } else if ("admin".equals(loggedInUserRole)) {
            manageInventoryConsole();
        }
        else {
            System.out.println("Invalid option for your role or not logged in.");
            loginOrPrompt();
        }
    }

    private static void handleOptionSix() {
        if ("admin".equals(loggedInUserRole)) {
            manageSuppliersConsole();
        } else {
            System.out.println("This option is for Administrators only.");
            loginOrPrompt();
        }
    }

    private static void loginOrPrompt(){
        if (loggedInUserEmail == null) {
            // System.out.println("No user logged in. Please login first."); // Message might be redundant if called after another message
            manageUserLogin();
        }
    }


    private static int getInputInt() {
        int choice = -1; // Initializer is not redundant if loop doesn't execute
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid input. Please enter a number.");
            scanner.nextLine();
            System.out.print("Enter your choice: ");
        }
        choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    private static void printMainMenu() {
        System.out.println("\n--- Main Menu ---");
        if (loggedInUserEmail == null) {
            System.out.println("1. Login (Customer/Chef/Admin)");
        } else {
            System.out.println("Logged in as: " + loggedInUserEmail + " (" + loggedInUserRole + ")");
            switch (loggedInUserRole) {
                case "customer":
                    System.out.println("2. Manage My Dietary Info");
                    System.out.println("3. View My Past Orders");
                    System.out.println("5. Create Custom Meal");
                    // Add option for Request Invoice if needed, adjust numbering
                    break;
                case "chef":
                    System.out.println("2. View Customer Dietary Info");
                    System.out.println("3. Assign Task to Chef (Simulated)");
                    System.out.println("4. View My Assigned Tasks");
                    break;
                case "admin":
                    System.out.println("2. Generate Monthly Revenue Report");
                    System.out.println("3. Generate Overall Revenue Report");
                    System.out.println("4. View All Orders");
                    System.out.println("5. Manage Inventory");
                    System.out.println("6. Manage Suppliers");
                    break;
            }
            System.out.println("7. Get Recipe Recommendation");
            System.out.println("9. Logout");
        }
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void manageUserLogin() {
        if (loggedInUserEmail != null) {
            System.out.println("You are already logged in as " + loggedInUserEmail + ". Please logout first or choose another option.");
            return;
        }
        System.out.println("\n--- Login As ---");
        System.out.println("1. Customer");
        System.out.println("2. Chef");
        System.out.println("3. Administrator");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your role choice: ");
        int roleChoice = getInputInt();

        switch (roleChoice) {
            case 1:
                loginCustomer();
                break;
            case 2:
                loginChef();
                break;
            case 3:
                loginAdmin();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid role choice.");
        }
    }

    private static void loginCustomer() {
        System.out.print("Enter your email to login/register as Customer: ");
        String email = scanner.nextLine();
        if (email.trim().isEmpty()){
            System.out.println("Email cannot be empty.");
            return;
        }
        Customer customer = customerService.registerOrGetCustomer(email);
        if (customer != null) {
            loggedInUserEmail = customer.getEmail();
            loggedInUserRole = "customer";
            System.out.println("Logged in as Customer: " + loggedInUserEmail);
        } else {
            System.out.println("Customer Login/Registration failed.");
        }
    }

    private static void loginChef() {
        System.out.print("Enter Chef username to login (e.g., Chef Alice): ");
        String chefName = scanner.nextLine();
        if (chefName.trim().isEmpty()){
            System.out.println("Chef name cannot be empty.");
            return;
        }
        Chef chef = chefRepository.findChefByName(chefName);
        if(chef == null) {
            System.out.println("Chef " + chefName + " not found. Creating for session and saving to chefs.txt.");
            System.out.print("Enter expertise for " + chefName + " (comma-separated, e.g., Italian,Pastas): ");
            String expertise = scanner.nextLine();
            System.out.print("Enter initial workload for " + chefName + " (Low, Medium, High): ");
            String workload = scanner.nextLine();
            chef = new Chef(chefName, expertise, workload);
            chefRepository.saveChef(chef);
        }
        chef.setLoggedIn(true);
        loggedInUserEmail = chef.getName();
        loggedInUserRole = "chef";
        System.out.println("Logged in as Chef: " + loggedInUserEmail);
    }

    private static void loginAdmin() {
        System.out.print("Enter Admin username to login (e.g., AdminFinance): ");
        String adminName = scanner.nextLine();
        if (adminName.trim().isEmpty()){
            System.out.println("Admin name cannot be empty.");
            return;
        }
        loggedInUserEmail = adminName;
        loggedInUserRole = "admin";
        System.out.println("Logged in as Administrator: " + loggedInUserEmail);
    }

    private static void logoutUser() {
        if (loggedInUserEmail != null) {
            System.out.println("Logging out " + loggedInUserEmail + "...");
            loggedInUserEmail = null;
            loggedInUserRole = null;
        } else {
            System.out.println("No user is currently logged in.");
        }
    }

    private static void manageDietaryInfo(String email) {
        boolean managing = true;
        while (managing) {
            System.out.println("\n--- Dietary Info Menu for " + email + " ---");
            System.out.println("1. Add Dietary Preference");
            System.out.println("2. Add Allergy");
            System.out.println("3. View Current Dietary Info");
            System.out.println("0. Back");
            System.out.print("Enter your choice: ");

            int choice = getInputInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter dietary preference to add: ");
                    String preference = scanner.nextLine();
                    customerService.addDietaryPreference(email, preference);
                    System.out.println("Dietary preference '" + preference + "' added.");
                    break;
                case 2:
                    System.out.print("Enter allergy to add: ");
                    String allergy = scanner.nextLine();
                    customerService.addAllergy(email, allergy);
                    System.out.println("Allergy '" + allergy + "' added.");
                    break;
                case 3:
                    Customer currentInfo = customerService.getCustomerDietaryInfo(email);
                    if (currentInfo != null) {
                        System.out.println("Preferences: " + currentInfo.getDietaryPreferences());
                        System.out.println("Allergies: " + currentInfo.getAllergies());
                    } else {
                        System.out.println("Could not retrieve info for " + email);
                    }
                    break;
                case 0:
                    managing = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void viewPastOrders(String customerEmail) {
        System.out.println("\n--- Your Past Orders ---");
        List<Order> pastOrders = orderService.getPastOrdersForCustomer(customerEmail);

        if (pastOrders == null || pastOrders.isEmpty()) {
            System.out.println("You have no past orders.");
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Order order : pastOrders) {
            System.out.println("\nOrder ID: " + order.getOrderId());
            System.out.println("Date: " + order.getOrderDate().format(dateFormatter));
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total: " + String.format("%.2f", order.getOrderTotalPrice()));
            System.out.println("Items:");
            if (order.getItems().isEmpty()) {
                System.out.println("  (No items listed for this order)");
            } else {
                for (OrderItem item : order.getItems()) {
                    System.out.println("  - " + item.getQuantity() + "x " + item.getMealName() +
                            " (Unit Price: " + String.format("%.2f", item.getUnitPrice()) +
                            ", Item Total: " + String.format("%.2f", item.getItemTotalPrice()) + ")");
                }
            }
        }
        System.out.println("--- End of Past Orders ---");
    }

    private static void viewCustomerDietaryInfoAsChef() {
        System.out.print("Enter customer email to view their dietary info: ");
        String customerEmailToView = scanner.nextLine();
        Customer customerInfo = customerService.getCustomerDietaryInfo(customerEmailToView);

        if (customerInfo != null) {
            System.out.println("\n--- Dietary Information for Customer: " + customerEmailToView + " ---");
            System.out.println("Preferences: " + customerInfo.getDietaryPreferences());
            System.out.println("Allergies: " + customerInfo.getAllergies());
            if (customerInfo.getDietaryPreferences().isEmpty() && customerInfo.getAllergies().isEmpty()) {
                System.out.println("(No specific dietary information stored for this customer)");
            }
            System.out.println("--- End of Dietary Information ---");
        } else {
            System.out.println("Customer with email '" + customerEmailToView + "' not found.");
        }
    }

    private static void generateMonthlyRevenueReport() {
        System.out.print("Enter month and year for report (e.g., October 2023): ");
        String monthYearStr = scanner.nextLine();
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            YearMonth targetMonthYear = YearMonth.parse(monthYearStr, formatter);
            double revenue = orderService.getTotalRevenueForMonth(targetMonthYear);
            System.out.println("Total revenue for " + monthYearStr + ": " + String.format("%.2f", revenue));
            if (revenue == 0.0) {
                List<Order> ordersInMonth = orderService.getAllCompletedOrders().stream()
                        .filter(order -> order.getOrderDate() != null && YearMonth.from(order.getOrderDate()).equals(targetMonthYear))
                        .collect(Collectors.toList());
                if(ordersInMonth.isEmpty()) {
                    System.out.println("No sales were recorded for " + monthYearStr + ".");
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please use 'MMMM yyyy' (e.g., October 2023). Error: " + e.getMessage());
        }
    }

    private static void generateOverallRevenueReport() {
        double overallRevenue = orderService.getOverallTotalRevenue();
        System.out.println("\n--- Overall Total Revenue ---");
        System.out.println("Overall Total Revenue: " + String.format("%.2f", overallRevenue));
        System.out.println("--- End of Report ---");
    }

    private static void createCustomMealConsole() {
        System.out.println("\n--- Create Custom Meal ---");
        System.out.print("Enter a name for your custom meal: ");
        String mealName = scanner.nextLine();

        CustomMealRequest mealRequest = customMealService.startCustomMeal(loggedInUserEmail, mealName);
        if (mealRequest == null) return;

        List<String> selectedIngredientNames = new ArrayList<>();
        boolean addingIngredients = true;
        System.out.println("Available ingredients (type 'done' when finished):");
        ingredientRepository.getAllIngredients().forEach(ing -> System.out.println("- " + ing.getName() + " (Price: " + String.format("%.2f", ing.getPrice()) + ", Tags: " + ing.getTags() + ")"));

        while(addingIngredients) {
            System.out.print("Add ingredient (or 'done'): ");
            String ingredientInput = scanner.nextLine();
            if ("done".equalsIgnoreCase(ingredientInput)) {
                addingIngredients = false;
            } else {
                Ingredient foundIngredient = ingredientRepository.findIngredientByName(ingredientInput);
                if (foundIngredient == null) {
                    System.out.println("Ingredient '" + ingredientInput + "' not found. Please choose from the list.");
                    List<Ingredient> suggestions = customMealService.suggestAlternatives(ingredientInput, loggedInUserEmail);
                    if (suggestions != null && !suggestions.isEmpty()) {
                        System.out.println("Did you mean one of these (or other alternatives)?");
                        suggestions.forEach(s -> System.out.println("- " + s.getName()));
                    }
                    continue;
                }

                if (customMealService.addIngredientToCustomMeal(mealRequest, ingredientInput)) {
                    selectedIngredientNames.add(ingredientInput);
                    System.out.println(ingredientInput + " added.");
                } else {
                    System.out.println("Failed to add " + ingredientInput + ". Reason: " + mealRequest.getFailureReason());
                    List<Ingredient> suggestions = customMealService.suggestAlternatives(ingredientInput, loggedInUserEmail);
                    if(suggestions != null && !suggestions.isEmpty()){
                        System.out.println("Available alternatives for " + ingredientInput + ":");
                        suggestions.forEach(s -> System.out.println("- " + s.getName()));
                    }
                }
            }
        }

        if (selectedIngredientNames.isEmpty()) {
            System.out.println("No ingredients selected. Custom meal creation cancelled.");
            return;
        }

        System.out.println("Finalizing custom meal...");
        mealRequest = customMealService.finalizeCustomMeal(mealRequest);

        if (mealRequest.isCreationSuccessful()) {
            System.out.println("Custom meal '" + mealRequest.getMealName() + "' created successfully!");
            System.out.println("Selected Ingredients: " + mealRequest.getSelectedIngredients().stream().map(Ingredient::getName).collect(Collectors.joining(", ")));
            System.out.println("Total Price: " + String.format("%.2f", mealRequest.getTotalPrice()));
            System.out.println("Meal Tags: " + mealRequest.getMealTags());
        } else {
            System.out.println("Failed to create custom meal. Reason: " + mealRequest.getFailureReason());
        }
    }

    private static void requestInvoiceConsole() {
        System.out.print("Enter Order ID to get invoice: ");
        String orderId = scanner.nextLine();
        Invoice invoice = orderService.generateInvoiceForOrder(orderId, loggedInUserEmail);
        if (invoice != null) {
            System.out.println(invoice.toString());
        } else {
            System.out.println("Could not generate invoice for order " + orderId + ".");
        }
    }

    private static void viewAllOrdersAsAdmin() {
        System.out.println("\n--- All Completed Orders ---");
        List<Order> allOrders = orderService.getAllCompletedOrders();
        if (allOrders.isEmpty()) {
            System.out.println("No completed orders found in the system.");
            return;
        }
        for (Order order : allOrders) {
            System.out.println(order.toString());
        }
        System.out.println("--- End of All Orders ---");
    }

    private static void manageInventoryConsole() {
        System.out.println("\n--- Inventory Management (Admin) ---");
        System.out.println("1. View All Ingredient Stock Levels");
        System.out.println("2. View Ingredients Needing Restocking");
        System.out.println("3. Use Ingredient (Simulate Order Fulfillment)");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
        int choice = getInputInt();

        switch (choice) {
            case 1:
                viewAllIngredientStockConsole();
                break;
            case 2:
                checkRestockingNeedsConsole();
                break;
            case 3:
                useIngredientConsole();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void viewAllIngredientStockConsole() {
        List<Ingredient> allIngredients = inventoryService.getAllIngredientStockLevels();
        if (allIngredients.isEmpty()) {
            System.out.println("No ingredients found in inventory.");
            return;
        }
        System.out.println("\nCurrent Stock Levels:");
        System.out.printf("%-25s | %-10s | %-6s | %-15s%n", "Ingredient", "Stock", "Unit", "Reorder Level");
        System.out.println("--------------------------------------------------------------------");
        for (Ingredient ingredient : allIngredients) {
            System.out.printf("%-25s | %-10d | %-6s | %-15d%n",
                    ingredient.getName(), ingredient.getCurrentStock(), ingredient.getUnit(), ingredient.getReorderLevel());
        }
    }

    private static void checkRestockingNeedsConsole() {
        List<Ingredient> lowStock = inventoryService.getIngredientsNeedingRestocking();
        if (lowStock.isEmpty()) {
            System.out.println("No ingredients currently need restocking.");
        } else {
            System.out.println("\nIngredients Needing Restocking:");
            lowStock.forEach(ing ->
                    System.out.println("- " + ing.getName() + " (Current: " + ing.getCurrentStock() + " " + ing.getUnit() + ", Reorder at: " + ing.getReorderLevel() + ")")
            );
        }
    }

    private static void useIngredientConsole() {
        System.out.print("Enter ingredient name to use: ");
        String name = scanner.nextLine();
        System.out.print("Enter quantity to use: ");
        int qty = getInputInt();
        if (inventoryService.useSingleIngredient(name, qty)) {
            System.out.println(qty + " of " + name + " used successfully.");
        } else {
            System.out.println("Failed to use " + name + ". Check stock or if ingredient exists.");
        }
    }

    private static void manageSuppliersConsole() {
        System.out.println("\n--- Supplier Management (Admin) ---");
        System.out.println("1. View All Suppliers");
        System.out.println("2. Fetch Real-time Price for an Ingredient");
        System.out.println("3. Auto-generate Purchase Orders for Critical Stock (Simulation)");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
        int choice = getInputInt();

        switch (choice) {
            case 1:
                List<Supplier> suppliers = supplierRepository.getAllSuppliers();
                if(suppliers.isEmpty()){
                    System.out.println("No suppliers found in the system.");
                    break;
                }
                System.out.println("\nAvailable Suppliers:");
                for(Supplier s : suppliers){
                    System.out.println("- " + s.getName() + " (ID: " + s.getId() + ", Email: " + s.getContactEmail() + ")");
                    if(!s.getItemPrices().isEmpty()){
                        System.out.println("  Known item prices: " + s.getItemPrices());
                    }
                }
                break;
            case 2:
                fetchIngredientPriceConsole();
                break;
            case 3:
                System.out.println("Checking for critically low stock items to generate purchase orders...");
                List<String> poNotifications = purchasingService.checkAndGenerateAutoOrders(inventoryService);
                if (poNotifications.isEmpty()) {
                    System.out.println("No purchase orders were automatically generated (either stock is sufficient or prices are missing).");
                } else {
                    System.out.println("The following purchase orders were generated/suggested:");
                    poNotifications.forEach(System.out::println);
                }
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private static void fetchIngredientPriceConsole() {
        System.out.print("Enter ingredient name: ");
        String ingredientName = scanner.nextLine();
        System.out.print("Enter supplier name: ");
        String supplierName = scanner.nextLine();

        Double price = purchasingService.fetchRealTimePrice(ingredientName, supplierName);
        if (price != null) {
            Ingredient ing = ingredientRepository.findIngredientByName(ingredientName);
            String unit = (ing != null) ? ing.getUnit() : "units";
            System.out.println("Real-time price for " + ingredientName + " from " + supplierName + " is: " + String.format("%.2f", price) + " per " + unit);
        } else {
            System.out.println("Could not fetch price. Supplier or ingredient might not be found, or price not set for it by this supplier.");
        }
    }

    private static void assignTaskToChefConsole() {
        System.out.println("\n--- Assign Task to Chef (Admin/Manager Action) ---");
        System.out.print("Enter Order ID for the task: ");
        String orderId = scanner.nextLine();
        System.out.print("Enter Meal Name for the task: ");
        String mealName = scanner.nextLine();
        System.out.println("Available Chefs:");
        chefRepository.getAllChefs().forEach(c -> System.out.println("- " + c.getName() + " (Expertise: " + c.getExpertise() + ", Workload: " + c.getCurrentWorkload() + ")"));
        System.out.print("Enter Chef Name to assign the task to: ");
        String chefName = scanner.nextLine();

        boolean success = kitchenManagementService.assignTaskToChef(orderId, mealName, chefName);
        if (success) {
            System.out.println("Task for order " + orderId + " successfully assigned to " + chefName + ".");
        } else {
            System.out.println("Failed to assign task for order " + orderId + " to " + chefName + ".");
        }
    }

    private static void viewMyTasksAsChef(){
        if(loggedInUserEmail == null || !"chef".equals(loggedInUserRole)){
            System.out.println("You must be logged in as a Chef to view tasks.");
            return;
        }
        System.out.println("\n--- Tasks for Chef " + loggedInUserEmail + " ---");
        List<KitchenTask> tasks = kitchenManagementService.getChefTasks(loggedInUserEmail);
        if(tasks.isEmpty()){
            System.out.println("No tasks assigned to you currently.");
        } else {
            for(KitchenTask task : tasks){
                System.out.println("- " + task.getMealName() + " for Order: " + task.getTaskId() +
                        " (Status: " + task.getStatus() +
                        (task.getDueTime() != null ? ", Due: " + task.getDueTime() : "") + ")");
            }
        }
        System.out.println("--------------------------");
    }

    private static void getRecipeRecommendationConsole() {
        System.out.println("\n--- AI Recipe Recommendation ---");
        System.out.print("Enter your dietary restriction (e.g., Vegan, Keto, or leave blank): ");
        String diet = scanner.nextLine();
        System.out.print("Enter available time in minutes (e.g., 30): ");
        int time = getInputInt();
        if(time <=0) {
            System.out.println("Invalid time. Using default of 30 minutes.");
            time = 30;
        }

        Set<String> availableIngredients = new HashSet<>();
        System.out.println("Enter available ingredients (one per line, type 'done' when finished):");
        String ingInput;
        while (!(ingInput = scanner.nextLine()).equalsIgnoreCase("done")) {
            if (!ingInput.trim().isEmpty()) {
                availableIngredients.add(ingInput.trim());
            }
        }

        RecipeSuggestionService.UserRecipePreferences prefs =
                new RecipeSuggestionService.UserRecipePreferences(diet.isEmpty() ? null : diet, time, availableIngredients);

        RecipeSuggestionService.RecommendationResult result = recipeSuggestionService.recommendRecipe(prefs);

        if (result != null && result.recommendedRecipe != null) {
            System.out.println("\n--- Recommendation ---");
            System.out.println(result.explanation);
        } else if (result != null) {
            System.out.println("\n" + result.explanation);
        } else {
            System.out.println("Sorry, an error occurred while generating recommendations.");
        }
    }
}