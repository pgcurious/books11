package com.javafeatures;

import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates Java Stream API features from Java 8 onwards.
 *
 * Features covered:
 * - Basic stream operations (Java 8)
 * - Collectors and grouping (Java 8)
 * - takeWhile/dropWhile (Java 9)
 * - Stream.toList() (Java 16)
 * - Stream Gatherers (Java 22+)
 */
public class StreamPipelineDemo {

    // Sample data records
    record Product(int id, String name, String category, double price, int stock) {}
    record Sale(int productId, int quantity, double totalPrice, String date) {}

    public static void main(String[] args) {
        System.out.println("=== Stream Pipeline Demo ===\n");

        var products = createProducts();
        var sales = createSales();

        // 1. Basic filtering and mapping (Java 8)
        basicOperations(products);

        // 2. Collectors and grouping (Java 8)
        collectorsDemo(products, sales);

        // 3. Java 9+ stream methods
        java9StreamMethods();

        // 4. Java 16 toList()
        java16ToList(products);

        // 5. Practical pipeline example
        practicalPipeline(products, sales);
    }

    static void basicOperations(List<Product> products) {
        System.out.println("1. Basic Operations (Java 8)");
        System.out.println("-".repeat(40));

        // Filter products under $50
        var affordable = products.stream()
            .filter(p -> p.price() < 50)
            .map(Product::name)
            .collect(Collectors.toList());
        System.out.println("Affordable products: " + affordable);

        // Find most expensive product
        var mostExpensive = products.stream()
            .max(Comparator.comparing(Product::price))
            .map(Product::name)
            .orElse("None");
        System.out.println("Most expensive: " + mostExpensive);

        // Calculate total inventory value
        var totalValue = products.stream()
            .mapToDouble(p -> p.price() * p.stock())
            .sum();
        System.out.printf("Total inventory value: $%.2f%n%n", totalValue);
    }

    static void collectorsDemo(List<Product> products, List<Sale> sales) {
        System.out.println("2. Collectors Demo (Java 8)");
        System.out.println("-".repeat(40));

        // Group by category
        var byCategory = products.stream()
            .collect(Collectors.groupingBy(Product::category));
        System.out.println("Products by category:");
        byCategory.forEach((cat, prods) ->
            System.out.println("  " + cat + ": " + prods.size() + " products"));

        // Average price by category
        var avgPriceByCategory = products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.averagingDouble(Product::price)
            ));
        System.out.println("\nAverage price by category:");
        avgPriceByCategory.forEach((cat, avg) ->
            System.out.printf("  %s: $%.2f%n", cat, avg));

        // Partition by price
        var partitioned = products.stream()
            .collect(Collectors.partitioningBy(p -> p.price() >= 100));
        System.out.println("\nExpensive (>=$100): " +
            partitioned.get(true).stream().map(Product::name).toList());
        System.out.println("Budget (<$100): " +
            partitioned.get(false).stream().map(Product::name).toList());

        // Statistics
        var stats = products.stream()
            .collect(Collectors.summarizingDouble(Product::price));
        System.out.println("\nPrice statistics:");
        System.out.printf("  Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n%n",
            stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
    }

    static void java9StreamMethods() {
        System.out.println("3. Java 9+ Stream Methods");
        System.out.println("-".repeat(40));

        var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // takeWhile - take elements while condition is true
        var taken = numbers.stream()
            .takeWhile(n -> n < 5)
            .toList();
        System.out.println("takeWhile(n < 5): " + taken);

        // dropWhile - skip elements while condition is true
        var dropped = numbers.stream()
            .dropWhile(n -> n < 5)
            .toList();
        System.out.println("dropWhile(n < 5): " + dropped);

        // Stream.iterate with predicate (Java 9)
        var fibonacci = Stream.iterate(
            new int[]{0, 1},
            arr -> arr[0] < 100,
            arr -> new int[]{arr[1], arr[0] + arr[1]}
        ).map(arr -> arr[0]).toList();
        System.out.println("Fibonacci < 100: " + fibonacci);

        // Stream.ofNullable (Java 9)
        String maybeNull = null;
        var count = Stream.ofNullable(maybeNull).count();
        System.out.println("Stream.ofNullable(null).count(): " + count + "\n");
    }

    static void java16ToList(List<Product> products) {
        System.out.println("4. Java 16 toList()");
        System.out.println("-".repeat(40));

        // Before Java 16
        List<String> oldWay = products.stream()
            .map(Product::name)
            .collect(Collectors.toList()); // Mutable list

        // Java 16+
        List<String> newWay = products.stream()
            .map(Product::name)
            .toList(); // Unmodifiable list

        System.out.println("Product names: " + newWay);
        System.out.println("List is unmodifiable: attempting to modify...");
        try {
            newWay.add("New Product");
        } catch (UnsupportedOperationException e) {
            System.out.println("  UnsupportedOperationException - as expected!\n");
        }
    }

    static void practicalPipeline(List<Product> products, List<Sale> sales) {
        System.out.println("5. Practical Pipeline Example");
        System.out.println("-".repeat(40));

        // Create product lookup map
        var productMap = products.stream()
            .collect(Collectors.toMap(Product::id, p -> p));

        // Calculate revenue by category
        record CategoryRevenue(String category, double revenue) {}

        var revenueByCategory = sales.stream()
            .map(sale -> {
                var product = productMap.get(sale.productId());
                return new AbstractMap.SimpleEntry<>(
                    product.category(),
                    sale.totalPrice()
                );
            })
            .collect(Collectors.groupingBy(
                Map.Entry::getKey,
                Collectors.summingDouble(Map.Entry::getValue)
            ))
            .entrySet().stream()
            .map(e -> new CategoryRevenue(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(CategoryRevenue::revenue).reversed())
            .toList();

        System.out.println("Revenue by category:");
        revenueByCategory.forEach(cr ->
            System.out.printf("  %s: $%.2f%n", cr.category(), cr.revenue()));

        // Top 3 selling products
        var topProducts = sales.stream()
            .collect(Collectors.groupingBy(
                Sale::productId,
                Collectors.summingInt(Sale::quantity)
            ))
            .entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(3)
            .map(e -> {
                var product = productMap.get(e.getKey());
                return product.name() + " (" + e.getValue() + " units)";
            })
            .toList();

        System.out.println("\nTop 3 selling products:");
        topProducts.forEach(p -> System.out.println("  " + p));
    }

    // Sample data creation
    static List<Product> createProducts() {
        return List.of(
            new Product(1, "Laptop", "Electronics", 999.99, 50),
            new Product(2, "Mouse", "Electronics", 29.99, 200),
            new Product(3, "Keyboard", "Electronics", 79.99, 150),
            new Product(4, "Monitor", "Electronics", 299.99, 75),
            new Product(5, "Desk", "Furniture", 199.99, 30),
            new Product(6, "Chair", "Furniture", 149.99, 45),
            new Product(7, "Lamp", "Furniture", 39.99, 100),
            new Product(8, "Notebook", "Office", 4.99, 500),
            new Product(9, "Pen Set", "Office", 12.99, 300),
            new Product(10, "Stapler", "Office", 8.99, 200)
        );
    }

    static List<Sale> createSales() {
        return List.of(
            new Sale(1, 5, 4999.95, "2024-01-15"),
            new Sale(2, 50, 1499.50, "2024-01-15"),
            new Sale(3, 30, 2399.70, "2024-01-16"),
            new Sale(4, 10, 2999.90, "2024-01-16"),
            new Sale(5, 8, 1599.92, "2024-01-17"),
            new Sale(6, 12, 1799.88, "2024-01-17"),
            new Sale(2, 40, 1199.60, "2024-01-18"),
            new Sale(8, 100, 499.00, "2024-01-18"),
            new Sale(9, 50, 649.50, "2024-01-19"),
            new Sale(1, 3, 2999.97, "2024-01-19")
        );
    }
}
