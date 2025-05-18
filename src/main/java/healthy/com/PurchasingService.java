package healthy.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PurchasingService {
    private final IngredientRepository ingredientRepository;
    private final SupplierRepository supplierRepository;
    private Map<String, IngredientSupplierLink> ingredientLinksData;

    public PurchasingService(IngredientRepository ingredientRepository, SupplierRepository supplierRepository, Map<String, IngredientSupplierLink> ingredientLinksData) {
        this.ingredientRepository = ingredientRepository;
        this.supplierRepository = supplierRepository;
        this.ingredientLinksData = ingredientLinksData != null ? new HashMap<>(ingredientLinksData) : new HashMap<>();
    }

    public PurchasingService(IngredientRepository ingredientRepository, SupplierRepository supplierRepository) {
        this.ingredientRepository = ingredientRepository;
        this.supplierRepository = supplierRepository;
        this.ingredientLinksData = new HashMap<>();
    }

    public Double fetchRealTimePrice(String ingredientName, String supplierName) {
        Supplier supplier = supplierRepository.findSupplierByName(supplierName);
        if (supplier != null) {
            return supplier.getPriceForItem(ingredientName);
        }
        return null;
    }

    public PurchaseOrder generateAutomaticPurchaseOrder(Ingredient ingredientToOrder, Supplier supplier, int quantity, double pricePerUnit) {
        if (ingredientToOrder == null || supplier == null || quantity <= 0 || pricePerUnit <= 0) {
            System.err.println("Invalid data for automatic purchase order generation.");
            return null;
        }
        String poId = "AUTO-PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PurchaseOrder po = new PurchaseOrder(poId, ingredientToOrder.getName(), supplier.getName(), quantity, ingredientToOrder.getUnit(), pricePerUnit, true);

        System.out.println("Generated automatic PO: " + po);
        return po;
    }

    public PurchaseOrder generateManualPurchaseOrder(String managerName, String ingredientName, int quantity, String supplierName) {
        Ingredient ingredient = ingredientRepository.findIngredientByName(ingredientName);
        Supplier supplier = supplierRepository.findSupplierByName(supplierName);

        if (ingredient == null) {
            System.err.println("Cannot create manual PO: Ingredient " + ingredientName + " not found.");
            return null;
        }
        if (supplier == null) {
            System.err.println("Cannot create manual PO: Supplier " + supplierName + " not found.");
            return null;
        }

        Double price = supplier.getPriceForItem(ingredientName);
        if (price == null) {
            System.err.println("Cannot create manual PO: Price for " + ingredientName + " from " + supplierName + " not available.");
            return null;
        }

        String poId = "MANUAL-PO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        PurchaseOrder po = new PurchaseOrder(poId, ingredient.getName(), supplier.getName(), quantity, ingredient.getUnit(), price, false);

        System.out.println("Generated manual PO by " + managerName + ": " + po);
        return po;
    }

    public List<String> checkAndGenerateAutoOrders(InventoryService inventoryService) {
        List<String> notifications = new ArrayList<>();
        if (inventoryService == null) {
            System.err.println("InventoryService is null in checkAndGenerateAutoOrders.");
            return notifications;
        }

        List<Ingredient> lowStockIngredients = inventoryService.getIngredientsNeedingRestocking();

        for (Ingredient ingredient : lowStockIngredients) {
            IngredientSupplierLink linkData = findLinkDataForIngredient(ingredient.getName());
            if (linkData == null) {
                System.err.println("No supplier link data found for critically low ingredient: " + ingredient.getName());
                continue;
            }

            Supplier supplier = supplierRepository.findSupplierById(linkData.getSupplierId());
            if (supplier == null) {
                System.err.println("Supplier with ID " + linkData.getSupplierId() + " not found for ingredient " + ingredient.getName());
                continue;
            }

            Double price = supplier.getPriceForItem(ingredient.getName());
            if (price == null) {
                System.err.println("Real-time price for " + ingredient.getName() + " from " + supplier.getName() + " not set for auto-ordering.");
                continue;
            }

            PurchaseOrder po = generateAutomaticPurchaseOrder(ingredient, supplier, linkData.getDefaultReorderQty(), price);
            if (po != null) {
                notifications.add("Automatically generated purchase order for " + ingredient.getName());
            }
        }
        return notifications;
    }

    public void loadIngredientLinksData(Map<String, IngredientSupplierLink> links) {
        if (links != null) {
            this.ingredientLinksData = new HashMap<>(links);
        } else {
            this.ingredientLinksData = new HashMap<>();
        }
    }

    private IngredientSupplierLink findLinkDataForIngredient(String ingredientName) {
        return this.ingredientLinksData.get(ingredientName);
    }
}