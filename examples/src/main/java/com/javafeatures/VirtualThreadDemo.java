package com.javafeatures;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates Virtual Threads and related concurrency features.
 *
 * Features covered:
 * - Virtual Threads (Java 21)
 * - Structured Concurrency patterns
 * - Executor with virtual threads
 * - Performance comparison
 */
public class VirtualThreadDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Virtual Thread Demo ===\n");

        // 1. Creating virtual threads
        createVirtualThreads();

        // 2. Virtual thread executor
        virtualThreadExecutor();

        // 3. High concurrency example
        highConcurrencyDemo();

        // 4. Practical API aggregation example
        apiAggregationDemo();
    }

    static void createVirtualThreads() throws Exception {
        System.out.println("1. Creating Virtual Threads (Java 21)");
        System.out.println("-".repeat(40));

        // Method 1: Thread.startVirtualThread()
        Thread vt1 = Thread.startVirtualThread(() -> {
            System.out.println("Virtual thread 1: " + Thread.currentThread());
        });

        // Method 2: Thread.ofVirtual()
        Thread vt2 = Thread.ofVirtual()
            .name("my-virtual-thread")
            .start(() -> {
                System.out.println("Virtual thread 2: " + Thread.currentThread().getName());
            });

        // Method 3: Builder with factory
        var factory = Thread.ofVirtual().name("worker-", 0).factory();
        Thread vt3 = factory.newThread(() -> {
            System.out.println("Virtual thread 3 (factory): " + Thread.currentThread().getName());
        });
        vt3.start();

        // Wait for all to complete
        vt1.join();
        vt2.join();
        vt3.join();

        // Check thread properties
        System.out.println("\nCurrent thread is virtual: " + Thread.currentThread().isVirtual());
        System.out.println();
    }

    static void virtualThreadExecutor() throws Exception {
        System.out.println("2. Virtual Thread Executor");
        System.out.println("-".repeat(40));

        // Create executor that uses virtual threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> futures = new ArrayList<>();

            // Submit 10 tasks
            for (int i = 0; i < 10; i++) {
                final int taskId = i;
                futures.add(executor.submit(() -> {
                    Thread.sleep(100); // Simulate I/O
                    return "Task " + taskId + " completed on " +
                        Thread.currentThread().getName();
                }));
            }

            // Collect results
            System.out.println("Results:");
            for (var future : futures) {
                System.out.println("  " + future.get());
            }
        }
        System.out.println();
    }

    static void highConcurrencyDemo() throws Exception {
        System.out.println("3. High Concurrency Demo");
        System.out.println("-".repeat(40));

        int numTasks = 10_000;
        var latch = new CountDownLatch(numTasks);

        System.out.println("Starting " + numTasks + " virtual threads...");
        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < numTasks; i++) {
                executor.submit(() -> {
                    try {
                        // Simulate I/O operation
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            // Wait for all tasks
            latch.await();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("Completed %,d tasks in %d ms%n", numTasks, elapsed);
        System.out.printf("Average: %.2f ms per task (with parallelism)%n%n",
            (double) elapsed / numTasks * 100);
    }

    static void apiAggregationDemo() throws Exception {
        System.out.println("4. API Aggregation Example");
        System.out.println("-".repeat(40));

        // Simulate fetching data from multiple APIs concurrently
        record User(int id, String name, String email) {}
        record Order(int id, String status, double total) {}
        record Activity(String type, String timestamp) {}
        record UserDashboard(User user, List<Order> orders, List<Activity> activities) {}

        System.out.println("Fetching user dashboard data...");
        long startTime = System.currentTimeMillis();

        // Using virtual threads for concurrent API calls
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // Fork concurrent tasks
            var userFuture = executor.submit(() -> fetchUser(1));
            var ordersFuture = executor.submit(() -> fetchOrders(1));
            var activitiesFuture = executor.submit(() -> fetchActivities(1));

            // Wait for all and combine
            User user = userFuture.get();
            List<Order> orders = ordersFuture.get();
            List<Activity> activities = activitiesFuture.get();

            var dashboard = new UserDashboard(user, orders, activities);

            long elapsed = System.currentTimeMillis() - startTime;

            System.out.println("\nDashboard loaded in " + elapsed + "ms:");
            System.out.println("  User: " + dashboard.user().name());
            System.out.println("  Orders: " + dashboard.orders().size());
            System.out.println("  Activities: " + dashboard.activities().size());

            System.out.println("\nOrder details:");
            for (var order : dashboard.orders()) {
                System.out.printf("  Order #%d: %s ($%.2f)%n",
                    order.id(), order.status(), order.total());
            }
        }
    }

    // Simulated API calls
    static User fetchUser(int id) throws InterruptedException {
        Thread.sleep(200); // Simulate network latency
        return new User(id, "Alice Johnson", "alice@example.com");
    }

    static List<Order> fetchOrders(int userId) throws InterruptedException {
        Thread.sleep(300); // Simulate network latency
        return List.of(
            new Order(1001, "Delivered", 99.99),
            new Order(1002, "Processing", 149.50),
            new Order(1003, "Shipped", 75.00)
        );
    }

    static List<Activity> fetchActivities(int userId) throws InterruptedException {
        Thread.sleep(250); // Simulate network latency
        return List.of(
            new Activity("login", "2024-01-20 09:30:00"),
            new Activity("purchase", "2024-01-20 09:35:00"),
            new Activity("review", "2024-01-20 10:00:00")
        );
    }

    // Inner record types
    record User(int id, String name, String email) {}
    record Order(int id, String status, double total) {}
    record Activity(String type, String timestamp) {}
}
