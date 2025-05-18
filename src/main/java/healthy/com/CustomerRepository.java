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

public class CustomerRepository {
    private static final String FILE_PATH = "src/main/resources/customers.txt"; // تم تعديل المسار
    private static final String SEPARATOR = ";";
    private static final String LIST_SEPARATOR = ",";

    public CustomerRepository() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error initializing customer data file: " + e.getMessage());
        }
    }

    private List<Customer> loadCustomers() {
        List<Customer> customers = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            return customers;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(SEPARATOR, -1);
                if (parts.length >= 1) {
                    Customer customer = new Customer(parts[0]);
                    if (parts.length >= 2 && !parts[1].isEmpty()) {
                        Arrays.stream(parts[1].split(LIST_SEPARATOR))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(customer::addDietaryPreference);
                    }
                    if (parts.length >= 3 && !parts[2].isEmpty()) {
                        Arrays.stream(parts[2].split(LIST_SEPARATOR))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .forEach(customer::addAllergy);
                    }
                    customers.add(customer);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    private void saveAllCustomers(List<Customer> customers) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Customer customer : customers) {
                String preferences = String.join(LIST_SEPARATOR, customer.getDietaryPreferences());
                String allergies = String.join(LIST_SEPARATOR, customer.getAllergies());
                writer.write(String.join(SEPARATOR, customer.getEmail(), preferences, allergies));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving customers: " + e.getMessage());
        }
    }

    public void saveCustomer(Customer customerToSave) {
        List<Customer> customers = loadCustomers();
        Optional<Customer> existingCustomer = customers.stream()
                .filter(c -> c.getEmail().equals(customerToSave.getEmail()))
                .findFirst();

        if (existingCustomer.isPresent()) {
            customers.remove(existingCustomer.get());
        }
        customers.add(customerToSave);
        saveAllCustomers(customers);
    }

    public Customer findCustomerByEmail(String email) {
        return loadCustomers().stream()
                .filter(customer -> customer.getEmail().equals(email))
                .findFirst()
                .orElse(null);
    }
}