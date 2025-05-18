package healthy.com;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class SupplierIntegrationSteps {

    private static class Supplier {
        String id;
        String name;
        String contactEmail;
        Map<String, Double> realTimePrices = new HashMap<>();

        public Supplier(String id, String name, String contactEmail) {
            this.id = id;
            this.name = name;
            this.contactEmail = contactEmail;
        }
    }

    private static class IngredientSupplierLink {
        String ingredientName;
        String supplierId;
        int defaultReorderQty;
        String unit;
        int criticalStockLevel;
        int currentStock;

        public IngredientSupplierLink(String ingredientName, String supplierId, int defaultReorderQty, String unit, int criticalStockLevel, int currentStock) {
            this.ingredientName = ingredientName;
            this.supplierId = supplierId;
            this.defaultReorderQty = defaultReorderQty;
            this.unit = unit;
            this.criticalStockLevel = criticalStockLevel;
            this.currentStock = currentStock;
        }
    }

    private static class PurchaseOrder {
        String ingredientName;
        String supplierName;
        int quantity;
        String unit;
        double pricePerUnit;
        boolean automaticallyGenerated;

        public PurchaseOrder(String ingredientName, String supplierName, int quantity, String unit, double pricePerUnit, boolean auto) {
            this.ingredientName = ingredientName;
            this.supplierName = supplierName;
            this.quantity = quantity;
            this.unit = unit;
            this.pricePerUnit = pricePerUnit;
            this.automaticallyGenerated = auto;
        }
    }

    private Map<String, Supplier> suppliers = new HashMap<>();
    private Map<String, IngredientSupplierLink> ingredientLinks = new HashMap<>();
    private String currentManagerName;
    private boolean managerLoggedIn;
    private Double fetchedPrice;
    private String fetchedPriceUnit;
    private List<PurchaseOrder> generatedPurchaseOrders = new ArrayList<>();
    private List<String> managerNotifications = new ArrayList<>();
    private String systemMessageToManager;


    public SupplierIntegrationSteps() {
        // Initialization moved to field declaration or Background
    }

    @Given("the following suppliers are known to the system:")
    public void the_following_suppliers_are_known_to_the_system(DataTable suppliersTable) {
        this.suppliers.clear();
        List<Map<String, String>> rows = suppliersTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String id = columns.get("Supplier ID");
            String name = columns.get("Supplier Name");
            this.suppliers.put(id, new Supplier(
                    id,
                    name,
                    columns.get("Contact Email")
            ));
        }
    }

    @Given("the following ingredients are linked to suppliers with their standard reorder quantity:")
    public void the_following_ingredients_are_linked_to_suppliers(DataTable linksTable) {
        this.ingredientLinks.clear();
        List<Map<String, String>> rows = linksTable.asMaps(String.class, String.class);
        for (Map<String, String> columns : rows) {
            String ingredientName = columns.get("Ingredient Name");
            this.ingredientLinks.put(ingredientName, new IngredientSupplierLink(
                    ingredientName,
                    columns.get("Supplier ID"),
                    Integer.parseInt(columns.get("Default Reorder Qty")),
                    columns.get("Unit"),
                    Integer.parseInt(columns.get("Critical Stock Level")),
                    Integer.parseInt(columns.get("Current Stock")) // Read initial stock from Background
            ));
        }
    }

    @Given("{string} real-time price for {string} is {double} per {string}")
    public void real_time_price_for_ingredient_is_per_unit(String supplierName, String ingredientName, Double price, String unit) {
        Supplier supplier = this.suppliers.values().stream()
                .filter(s -> s.name.equals(supplierName))
                .findFirst()
                .orElse(null);
        assertThat(supplier).as("Supplier '%s' not found when setting price for '%s'", supplierName, ingredientName).isNotNull();
        supplier.realTimePrices.put(ingredientName, price);
    }

    @When("{string} requests the real-time price for {string} from {string}")
    public void manager_requests_real_time_price(String managerName, String ingredientName, String supplierName) {
        this.currentManagerName = managerName;
        this.managerLoggedIn = true;
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
        this.fetchedPrice = null;
        this.fetchedPriceUnit = null;
        this.systemMessageToManager = null;

        Supplier supplier = this.suppliers.values().stream().filter(s -> s.name.equals(supplierName)).findFirst().orElse(null);
        if (supplier == null) {
            this.systemMessageToManager = "Supplier " + supplierName + " is not recognized";
            return;
        }

        this.fetchedPrice = supplier.realTimePrices.get(ingredientName);
        IngredientSupplierLink link = this.ingredientLinks.get(ingredientName);
        if (link != null) {
            this.fetchedPriceUnit = link.unit;
        }
    }

    @Then("the system should display the fetched price as {double} per {string} for {string} from {string}")
    public void the_system_should_display_the_fetched_price_as_per_unit_for_from(Double expectedPrice, String expectedUnit, String ingredientName, String supplierName) {
        assertThat(this.systemMessageToManager).as("System message should be null if price fetch is successful, but was: " + this.systemMessageToManager).isNull();
        assertThat(this.fetchedPrice).as("Fetched price for " + ingredientName + " from " + supplierName).isEqualTo(expectedPrice);
        assertThat(this.fetchedPriceUnit).as("Fetched price unit for " + ingredientName + " from " + supplierName).isEqualTo(expectedUnit);
    }

    @Given("for supplier integration, the current stock of {string} is {int} pack")
    public void for_supplier_integration_the_current_stock_of_ingredient_is_pack(String ingredientName, Integer stock) {
        set_current_stock_for_supplier_integration(ingredientName, stock);
    }

    @Given("for supplier integration, the current stock of {string} is {int} kg")
    public void for_supplier_integration_the_current_stock_of_ingredient_is_kg(String ingredientName, Integer stock) {
        set_current_stock_for_supplier_integration(ingredientName, stock);
    }

    private void set_current_stock_for_supplier_integration(String ingredientName, int stock){
        IngredientSupplierLink link = this.ingredientLinks.get(ingredientName);
        assertThat(link).as("Ingredient link for " + ingredientName + " not found for stock update.").isNotNull();
        link.currentStock = stock;
    }

    @When("the system checks for critically low stock items")
    public void system_checks_for_critically_low_stock_items() {
        this.generatedPurchaseOrders.clear();
        this.managerNotifications.clear();
        this.systemMessageToManager = null;

        for (IngredientSupplierLink link : this.ingredientLinks.values()) {
            if (link.currentStock < link.criticalStockLevel) {
                Supplier supplier = this.suppliers.get(link.supplierId);
                assertThat(supplier).as("Supplier with ID '%s' for ingredient '%s' not found.", link.supplierId, link.ingredientName).isNotNull();
                Double price = supplier.realTimePrices.get(link.ingredientName);
                assertThat(price).as("Real-time price for '%s' from '%s' must be known for auto-ordering. Current prices for supplier %s: %s", link.ingredientName, supplier.name, supplier.name, supplier.realTimePrices).isNotNull();

                this.generatedPurchaseOrders.add(new PurchaseOrder(
                        link.ingredientName,
                        supplier.name,
                        link.defaultReorderQty,
                        link.unit,
                        price,
                        true
                ));
                this.managerNotifications.add("Automatically generated purchase order for " + link.ingredientName);
            }
        }
    }

    @Then("a purchase order should be automatically generated for {string} to {string}")
    public void purchase_order_should_be_automatically_generated(String ingredientName, String supplierName) {
        assertThat(this.generatedPurchaseOrders.stream()
                .anyMatch(po -> po.ingredientName.equals(ingredientName) &&
                        po.supplierName.equals(supplierName) &&
                        po.automaticallyGenerated)
        ).as("Expected auto PO for " + ingredientName + " to " + supplierName).isTrue();
    }

    @And("the purchase order should request the default reorder quantity of {int} packs of {string}")
    public void purchase_order_should_request_default_reorder_quantity_packs(Integer quantity, String ingredientName) {
        verify_po_quantity_and_unit(quantity, ingredientName, "pack");
    }

    @And("the purchase order should request the default reorder quantity of {int} kg of {string}")
    public void purchase_order_should_request_default_reorder_quantity_kg(Integer quantity, String ingredientName) {
        verify_po_quantity_and_unit(quantity, ingredientName, "kg");
    }

    private void verify_po_quantity_and_unit(int quantity, String ingredientName, String unit){
        PurchaseOrder order = this.generatedPurchaseOrders.stream()
                .filter(po -> po.ingredientName.equals(ingredientName))
                .findFirst().orElseThrow(() -> new AssertionError("PO not found for " + ingredientName + " to verify quantity/unit."));
        assertThat(order.quantity).isEqualTo(quantity);
        assertThat(order.unit).isEqualTo(unit);
    }

    @And("the purchase order should use the real-time price of {double} per pack")
    public void purchase_order_should_use_real_time_price_per_pack(Double price) {
        verify_po_price(price, "pack");
    }

    @And("the purchase order should use the real-time price of {double} per kg")
    public void purchase_order_should_use_real_time_price_per_kg(Double price) {
        verify_po_price(price, "kg");
    }

    private void verify_po_price(double price, String unitContext){
        PurchaseOrder order = this.generatedPurchaseOrders.stream()
                .filter(po -> po.pricePerUnit == price && po.unit.equals(unitContext))
                .findFirst().orElseThrow(() -> new AssertionError("PO with price " + price + " per " + unitContext + " not found. Found: " + this.generatedPurchaseOrders));
        assertThat(order.pricePerUnit).isEqualTo(price);
    }

    @And("{string} should be notified of the automatically generated purchase order for {string}")
    public void manager_should_be_notified_of_auto_po(String managerName, String ingredientName) {
        this.currentManagerName = managerName;
        this.managerLoggedIn = true;
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.managerNotifications).contains("Automatically generated purchase order for " + ingredientName);
    }

    @When("{string} decides to manually order {int} kg of {string} from {string}")
    public void manager_decides_to_manually_order_kg(String managerName, Integer quantity, String ingredientName, String supplierName) {
        this.currentManagerName = managerName;
        this.managerLoggedIn = true;
        assertThat(this.managerLoggedIn).isTrue();
        assertThat(this.currentManagerName).isEqualTo(managerName);
        this.generatedPurchaseOrders.clear();

        Supplier supplier = this.suppliers.values().stream().filter(s -> s.name.equals(supplierName)).findFirst().orElse(null);
        assertThat(supplier).as("Supplier " + supplierName + " for manual order not found.").isNotNull();
        IngredientSupplierLink link = this.ingredientLinks.get(ingredientName);
        assertThat(link).as("Ingredient link for " + ingredientName + " for manual order not found.").isNotNull();
        Double price = supplier.realTimePrices.get(ingredientName);
        assertThat(price).as("Real-time price for " + ingredientName + " from " + supplier.name + " for manual order not found.").isNotNull();

        this.generatedPurchaseOrders.add(new PurchaseOrder(
                ingredientName,
                supplier.name,
                quantity,
                link.unit,
                price,
                false
        ));
    }

    @Then("a purchase order should be generated for {int} kg of {string} to {string}")
    public void purchase_order_should_be_generated_for_kg(Integer quantity, String ingredientName, String supplierName) {
        assertThat(this.generatedPurchaseOrders.stream()
                .anyMatch(po -> po.ingredientName.equals(ingredientName) &&
                        po.supplierName.equals(supplierName) &&
                        po.quantity == quantity &&
                        !po.automaticallyGenerated &&
                        po.unit.equals("kg"))
        ).as("Expected manual PO for " + quantity + "kg of " + ingredientName + " to " + supplierName).isTrue();
    }

    @Then("this purchase order should use the real-time price of {double} per kg")
    public void this_purchase_order_should_use_real_time_price_per_kg(Double price){
        verify_po_price(price, "kg");
    }

    @Then("the system should inform {string} that supplier {string} is not recognized")
    public void system_should_inform_manager_supplier_not_recognized(String managerName, String supplierName) {
        this.currentManagerName = managerName;
        this.managerLoggedIn = true;
        assertThat(this.currentManagerName).isEqualTo(managerName);
        assertThat(this.systemMessageToManager).isEqualTo("Supplier " + supplierName + " is not recognized");
    }

    @Then("no automatic purchase order should be generated for {string}")
    public void no_automatic_purchase_order_should_be_generated_for(String ingredientName) {
        assertThat(this.generatedPurchaseOrders.stream()
                .noneMatch(po -> po.ingredientName.equals(ingredientName) && po.automaticallyGenerated)
        ).as("Expected no auto PO for " + ingredientName + " but found some: " + this.generatedPurchaseOrders).isTrue();
    }
}