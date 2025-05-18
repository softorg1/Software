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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderRepository {
    private static final String FILE_PATH = "src/main/resources/orders.txt";
    private static final String ORDER_SEPARATOR = ";";
    private static final String ITEMS_SEPARATOR = "\\|";
    private static final String ITEM_DETAIL_SEPARATOR = ",";

    public OrderRepository() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Error initializing orders data file: " + e.getMessage());
        }
    }

    private List<Order> loadOrders() {
        List<Order> orders = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists() || file.length() == 0) {
            System.out.println("DEBUG OrderRepository: orders.txt does not exist or is empty.");
            return orders;
        }
        System.out.println("DEBUG OrderRepository: Loading orders from " + FILE_PATH);
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("DEBUG OrderRepository: Reading line: " + line);
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(ORDER_SEPARATOR, -1);
                if (parts.length >= 6) {
                    String orderId = parts[0];
                    String customerEmail = parts[1];
                    String orderDateStr = parts[2];
                    String status = parts[3];
                    double orderTotalPriceFromFile = Double.parseDouble(parts[4]);
                    String itemsString = parts[5];
                    System.out.println("DEBUG OrderRepository: Parsed Order ID: " + orderId + ", Total: " + orderTotalPriceFromFile);


                    Order order = new Order(orderId, customerEmail, orderDateStr, status);

                    if (!itemsString.isEmpty()) {
                        String[] itemEntries = itemsString.split(ITEMS_SEPARATOR);
                        for (String itemEntry : itemEntries) {
                            String[] itemDetails = itemEntry.split(ITEM_DETAIL_SEPARATOR);
                            if (itemDetails.length == 4) {
                                order.addItem(new OrderItem(
                                        itemDetails[0],
                                        Integer.parseInt(itemDetails[1]),
                                        Double.parseDouble(itemDetails[2]),
                                        Double.parseDouble(itemDetails[3])
                                ));
                            }
                        }
                    }
                    order.setOrderTotalPrice(orderTotalPriceFromFile);
                    orders.add(order);
                } else {
                    System.out.println("DEBUG OrderRepository: Skipped line due to insufficient parts: " + line);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("DEBUG OrderRepository: Loaded " + orders.size() + " orders.");
        return orders;
    }

    private void saveAllOrders(List<Order> orders) {
        System.out.println("DEBUG OrderRepository: Saving " + orders.size() + " orders to " + FILE_PATH);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Order order : orders) {
                StringBuilder itemsString = new StringBuilder();
                for (int i = 0; i < order.getItems().size(); i++) {
                    OrderItem item = order.getItems().get(i);
                    itemsString.append(item.getMealName()).append(ITEM_DETAIL_SEPARATOR)
                            .append(item.getQuantity()).append(ITEM_DETAIL_SEPARATOR)
                            .append(String.format("%.2f", item.getUnitPrice())).append(ITEM_DETAIL_SEPARATOR)
                            .append(String.format("%.2f", item.getItemTotalPrice()));
                    if (i < order.getItems().size() - 1) {
                        itemsString.append("|");
                    }
                }
                String lineToWrite = String.join(ORDER_SEPARATOR,
                        order.getOrderId(),
                        order.getCustomerEmail(),
                        order.getOrderDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        order.getStatus(),
                        String.format("%.2f", order.getOrderTotalPrice()),
                        itemsString.toString()
                );
                System.out.println("DEBUG OrderRepository: Writing line: " + lineToWrite);
                writer.write(lineToWrite);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving orders: " + e.getMessage());
        }
    }

    public void saveOrder(Order orderToSave) {
        List<Order> orders = loadOrders();
        Optional<Order> existingOrder = orders.stream()
                .filter(o -> o.getOrderId().equals(orderToSave.getOrderId()))
                .findFirst();

        if (existingOrder.isPresent()) {
            orders.remove(existingOrder.get());
        }
        orders.add(orderToSave);
        saveAllOrders(orders);
    }

    public Order findOrderById(String orderId) {
        return loadOrders().stream()
                .filter(order -> order.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
    }

    public List<Order> findOrdersByCustomerEmail(String email) {
        return loadOrders().stream()
                .filter(order -> order.getCustomerEmail().equals(email))
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() {
        return loadOrders();
    }
}