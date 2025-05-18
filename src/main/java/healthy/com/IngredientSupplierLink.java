package healthy.com;

public class IngredientSupplierLink {
    private String ingredientName;
    private String supplierId;
    private int defaultReorderQty;
    private String unit;
    private int criticalStockLevel;
    private int currentStock;

    public IngredientSupplierLink(String ingredientName, String supplierId, int defaultReorderQty, String unit, int criticalStockLevel, int currentStock) {
        this.ingredientName = ingredientName;
        this.supplierId = supplierId;
        this.defaultReorderQty = defaultReorderQty;
        this.unit = unit;
        this.criticalStockLevel = criticalStockLevel;
        this.currentStock = currentStock;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public int getDefaultReorderQty() {
        return defaultReorderQty;
    }

    public String getUnit() {
        return unit;
    }

    public int getCriticalStockLevel() {
        return criticalStockLevel;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int currentStock) {
        this.currentStock = currentStock;
    }
}