package com.javafeatures;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

/**
 * Demonstrates combined usage of modern Java features.
 *
 * This example shows how different features work together
 * to create clean, expressive, and efficient code.
 */
public class ModernJavaDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Modern Java Combined Demo ===\n");

        // 1. Text blocks + records + streams
        jsonProcessingDemo();

        // 2. Switch expressions + sealed types
        shoppingCartDemo();

        // 3. HTTP Client + virtual threads
        httpClientDemo();

        // 4. Date/Time API + var + method references
        dateTimeDemo();
    }

    // ========== 1. JSON Processing Demo ==========

    static void jsonProcessingDemo() {
        System.out.println("1. JSON Processing with Modern Features");
        System.out.println("-".repeat(40));

        // Text block for JSON (Java 15)
        var jsonData = """
            [
                {"name": "Alice", "age": 30, "city": "NYC", "active": true},
                {"name": "Bob", "age": 25, "city": "LA", "active": false},
                {"name": "Carol", "age": 35, "city": "NYC", "active": true},
                {"name": "David", "age": 28, "city": "Chicago", "active": true}
            ]
            """;

        // Record for data model (Java 16)
        record User(String name, int age, String city, boolean active) {}

        // Simple parsing simulation (in real code, use Jackson/Gson)
        var users = List.of(
            new User("Alice", 30, "NYC", true),
            new User("Bob", 25, "LA", false),
            new User("Carol", 35, "NYC", true),
            new User("David", 28, "Chicago", true)
        );

        // Stream processing with toList() (Java 16)
        var activeNYCUsers = users.stream()
            .filter(User::active)
            .filter(u -> "NYC".equals(u.city()))
            .toList();

        System.out.println("Active NYC users: " + activeNYCUsers);

        // Grouping with collectors
        var usersByCity = users.stream()
            .collect(Collectors.groupingBy(User::city,
                Collectors.averagingInt(User::age)));

        System.out.println("Average age by city: " + usersByCity);

        // Using sequenced collections (Java 21)
        var sortedByAge = new ArrayList<>(users);
        sortedByAge.sort(Comparator.comparing(User::age));

        SequencedCollection<User> sequenced = sortedByAge;
        System.out.println("Youngest: " + sequenced.getFirst().name());
        System.out.println("Oldest: " + sequenced.getLast().name());
        System.out.println();
    }

    // ========== 2. Shopping Cart Demo ==========

    sealed interface CartItem permits PhysicalProduct, DigitalProduct, Subscription {}

    record PhysicalProduct(String name, double price, double weight)
        implements CartItem {}

    record DigitalProduct(String name, double price, String downloadUrl)
        implements CartItem {}

    record Subscription(String name, double monthlyPrice, int months)
        implements CartItem {}

    static void shoppingCartDemo() {
        System.out.println("2. Shopping Cart with Sealed Types");
        System.out.println("-".repeat(40));

        List<CartItem> cart = List.of(
            new PhysicalProduct("Laptop", 999.99, 2.5),
            new DigitalProduct("Software License", 49.99, "https://example.com/download"),
            new Subscription("Cloud Service", 9.99, 12),
            new PhysicalProduct("Mouse", 29.99, 0.1)
        );

        // Calculate totals using pattern matching (Java 21)
        double total = 0;
        double shipping = 0;

        for (var item : cart) {
            switch (item) {
                case PhysicalProduct(String name, double price, double weight) -> {
                    total += price;
                    shipping += weight * 5; // $5 per kg
                    System.out.printf("Physical: %s - $%.2f (%.1fkg)%n",
                        name, price, weight);
                }
                case DigitalProduct(String name, double price, String url) -> {
                    total += price;
                    System.out.printf("Digital: %s - $%.2f%n", name, price);
                }
                case Subscription(String name, double monthly, int months) -> {
                    double subtotal = monthly * months;
                    total += subtotal;
                    System.out.printf("Subscription: %s - $%.2f/mo x %d = $%.2f%n",
                        name, monthly, months, subtotal);
                }
            }
        }

        System.out.println("-".repeat(30));
        System.out.printf("Subtotal: $%.2f%n", total);
        System.out.printf("Shipping: $%.2f%n", shipping);
        System.out.printf("Total: $%.2f%n%n", total + shipping);
    }

    // ========== 3. HTTP Client Demo ==========

    static void httpClientDemo() throws Exception {
        System.out.println("3. HTTP Client (Java 11)");
        System.out.println("-".repeat(40));

        // Create HTTP client
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get"))
            .header("Accept", "application/json")
            .GET()
            .build();

        System.out.println("Sending request to httpbin.org...");

        try {
            HttpResponse<String> response = client.send(
                request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Status: " + response.statusCode());
            System.out.println("Response (first 200 chars):");
            System.out.println(response.body().substring(0,
                Math.min(200, response.body().length())) + "...");
        } catch (Exception e) {
            System.out.println("Request failed (network unavailable): " + e.getMessage());
        }
        System.out.println();
    }

    // ========== 4. Date/Time Demo ==========

    static void dateTimeDemo() {
        System.out.println("4. Date/Time API (Java 8)");
        System.out.println("-".repeat(40));

        // Current date/time
        var now = LocalDateTime.now();
        var today = LocalDate.now();
        var currentTime = LocalTime.now();

        System.out.println("Now: " + now);
        System.out.println("Today: " + today);
        System.out.println("Time: " + currentTime);

        // Formatting
        var formatter = java.time.format.DateTimeFormatter
            .ofPattern("EEEE, MMMM d, yyyy");
        System.out.println("Formatted: " + today.format(formatter));

        // Calculations
        var nextWeek = today.plusWeeks(1);
        var lastMonth = today.minusMonths(1);
        System.out.println("Next week: " + nextWeek);
        System.out.println("Last month: " + lastMonth);

        // Duration between times
        var startOfDay = LocalTime.of(9, 0);
        var endOfDay = LocalTime.of(17, 30);
        var workHours = Duration.between(startOfDay, endOfDay);
        System.out.println("Work hours: " + workHours.toHours() + "h " +
            workHours.toMinutesPart() + "m");

        // Time zones
        var zones = List.of("America/New_York", "Europe/London",
            "Asia/Tokyo", "Australia/Sydney");

        System.out.println("\nCurrent time in different zones:");
        for (var zone : zones) {
            var zoned = ZonedDateTime.now(ZoneId.of(zone));
            System.out.printf("  %-20s %s%n", zone,
                zoned.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
        }

        // Business days calculation
        var startDate = LocalDate.of(2024, 1, 1);
        var endDate = LocalDate.of(2024, 1, 31);

        long businessDays = startDate.datesUntil(endDate.plusDays(1))
            .filter(date -> {
                var dow = date.getDayOfWeek();
                return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
            })
            .count();

        System.out.println("\nBusiness days in Jan 2024: " + businessDays);
    }
}
