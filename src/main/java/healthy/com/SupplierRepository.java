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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SupplierRepository {
    private static final String FILE_PATH = "src/main/resources/suppliers.txt";
    private static final String SEPARATOR = ";";
    private static final String PRICE_SEPARATOR = ":";
    private static final String PRICE_LIST_SEPARATOR = ",";

    public SupplierRepository() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error initializing suppliers data file: " + e.getMessage());
        }
    }

    private List<Supplier> loadSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return suppliers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length >= 3) { // id;name;contactEmail;item1:price1,item2:price2
                    Supplier supplier = new Supplier(parts[0].trim(), parts[1].trim(), parts[2].trim());
                    if (parts.length >= 4 && !parts[3].isEmpty()) {
                        String[] priceEntries = parts[3].split(PRICE_LIST_SEPARATOR);
                        for (String priceEntry : priceEntries) {
                            String[] itemPricePair = priceEntry.split(PRICE_SEPARATOR);
                            if (itemPricePair.length == 2) {
                                supplier.setItemPrice(itemPricePair[0].trim(), Double.parseDouble(itemPricePair[1].trim()));
                            }
                        }
                    }
                    suppliers.add(supplier);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading suppliers: " + e.getMessage());
        }
        return suppliers;
    }

    private void saveAllSuppliers(List<Supplier> suppliers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Supplier supplier : suppliers) {
                StringBuilder pricesString = new StringBuilder();
                List<String> priceEntries = new ArrayList<>();
                for (Map.Entry<String, Double> entry : supplier.getItemPrices().entrySet()) {
                    priceEntries.add(entry.getKey() + PRICE_SEPARATOR + String.format("%.2f", entry.getValue()));
                }
                pricesString.append(String.join(PRICE_LIST_SEPARATOR, priceEntries));

                writer.write(String.join(SEPARATOR,
                        supplier.getId(),
                        supplier.getName(),
                        supplier.getContactEmail(),
                        pricesString.toString()
                ));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving suppliers: " + e.getMessage());
        }
    }

    public void saveSupplier(Supplier supplierToSave) {
        List<Supplier> suppliers = loadSuppliers();
        Optional<Supplier> existingSupplier = suppliers.stream()
                .filter(s -> s.getId().equals(supplierToSave.getId()))
                .findFirst();

        if (existingSupplier.isPresent()) {
            suppliers.remove(existingSupplier.get());
        }
        suppliers.add(supplierToSave);
        saveAllSuppliers(suppliers);
    }

    public Supplier findSupplierById(String id) {
        return loadSuppliers().stream()
                .filter(supplier -> supplier.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Supplier findSupplierByName(String name) {
        return loadSuppliers().stream()
                .filter(supplier -> supplier.getName().equals(name))
                .findFirst()
                .orElse(null);
    }


    public List<Supplier> getAllSuppliers() {
        return loadSuppliers();
    }

    public void updateSupplier(Supplier supplier) {
        saveSupplier(supplier);
    }
}