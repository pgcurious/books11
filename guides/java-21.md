# Java 21 Features Guide

**Release Date:** September 2023
**Type:** LTS (Long-Term Support)

Java 21 is a major LTS release that finalized Virtual Threads, Pattern Matching, and introduced Sequenced Collections.

## Table of Contents
- [Virtual Threads (Standard)](#virtual-threads-standard)
- [Pattern Matching for switch (Standard)](#pattern-matching-for-switch-standard)
- [Record Patterns (Standard)](#record-patterns-standard)
- [Sequenced Collections](#sequenced-collections)
- [String Templates (Preview)](#string-templates-preview)
- [Scoped Values (Preview)](#scoped-values-preview)
- [Structured Concurrency (Preview)](#structured-concurrency-preview)
- [Unnamed Patterns and Variables (Preview)](#unnamed-patterns-and-variables-preview)
- [Unnamed Classes and Instance Main Methods (Preview)](#unnamed-classes-and-instance-main-methods-preview)

---

## Virtual Threads (Standard)

Virtual threads are now a standard feature in Java 21.

### Complete Virtual Threads Guide

```java
import java.util.concurrent.*;
import java.time.*;
import java.util.*;

public class VirtualThreadsStandard {
    public static void main(String[] args) throws Exception {
        // Creating virtual threads
        // Method 1: Thread.ofVirtual()
        Thread vThread = Thread.ofVirtual().start(() -> {
            System.out.println("Virtual thread: " + Thread.currentThread());
        });
        vThread.join();

        // Method 2: Thread.startVirtualThread()
        Thread vThread2 = Thread.startVirtualThread(() -> {
            System.out.println("Started virtual thread");
        });
        vThread2.join();

        // Method 3: ExecutorService
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> System.out.println("Executor virtual thread"));
        }

        // Check if thread is virtual
        System.out.println("Is virtual: " + Thread.currentThread().isVirtual());

        // Million thread test
        millionThreadsTest();
    }

    static void millionThreadsTest() throws Exception {
        int numThreads = 1_000_000;
        CountDownLatch latch = new CountDownLatch(numThreads);

        long startTime = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofSeconds(1));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            System.out.println("Submitted " + numThreads + " tasks");
            latch.await();
        }

        long elapsed = System.currentTimeMillis() - startTime;
        System.out.printf("Completed %,d virtual threads in %d ms%n",
            numThreads, elapsed);
    }
}
```

### HTTP Server with Virtual Threads

```java
import java.net.*;
import java.io.*;
import java.util.concurrent.*;

public class VirtualThreadHttpServer {
    public static void main(String[] args) throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server listening on port 8080");

            // Use virtual threads for each connection
            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    executor.submit(() -> handleRequest(clientSocket));
                }
            }
        }
    }

    static void handleRequest(Socket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Read request
            String requestLine = in.readLine();
            System.out.println("Request: " + requestLine);

            // Simulate some I/O work
            Thread.sleep(100);

            // Send response
            String response = "Hello from Virtual Thread!";
            out.println("HTTP/1.1 200 OK");
            out.println("Content-Type: text/plain");
            out.println("Content-Length: " + response.length());
            out.println();
            out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

---

## Pattern Matching for switch (Standard)

Pattern matching in switch is now a standard feature.

```java
public class PatternMatchingSwitchStandard {
    public static void main(String[] args) {
        // Type patterns
        Object[] values = {42, "hello", 3.14, null, new int[]{1, 2, 3}};
        for (Object value : values) {
            System.out.println(describe(value));
        }

        // With sealed types
        testShapes();

        // Record patterns
        testRecordPatterns();
    }

    static String describe(Object obj) {
        return switch (obj) {
            case Integer i -> "Integer: " + i;
            case String s -> "String of length " + s.length();
            case Double d -> "Double: " + String.format("%.2f", d);
            case int[] arr -> "Int array of length " + arr.length;
            case null -> "Null value";
            default -> "Unknown: " + obj.getClass().getName();
        };
    }

    // Sealed types with exhaustive switch
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    static void testShapes() {
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(10, 20),
            new Triangle(8, 6)
        };

        for (Shape shape : shapes) {
            double area = switch (shape) {
                case Circle(double r) -> Math.PI * r * r;
                case Rectangle(double w, double h) -> w * h;
                case Triangle(double b, double h) -> 0.5 * b * h;
            };
            System.out.printf("%s: area = %.2f%n",
                shape.getClass().getSimpleName(), area);
        }
    }

    // Record patterns
    record Point(int x, int y) {}
    record Line(Point start, Point end) {}

    static void testRecordPatterns() {
        Line line = new Line(new Point(0, 0), new Point(3, 4));

        String result = switch (line) {
            case Line(Point(int x1, int y1), Point(int x2, int y2))
                    when x1 == 0 && y1 == 0 -> {
                double length = Math.sqrt(x2 * x2 + y2 * y2);
                yield "Line from origin, length: " + length;
            }
            case Line(Point start, Point end) ->
                "Line from " + start + " to " + end;
        };
        System.out.println(result);
    }
}
```

### Guards with when Clause

```java
public class GuardedPatterns {
    record Person(String name, int age) {}
    record Employee(String name, int age, String department) {}

    public static void main(String[] args) {
        Object[] people = {
            new Person("Alice", 17),
            new Person("Bob", 30),
            new Employee("Charlie", 25, "Engineering"),
            new Employee("Diana", 65, "Management")
        };

        for (Object person : people) {
            System.out.println(categorize(person));
        }
    }

    static String categorize(Object obj) {
        return switch (obj) {
            case Employee(String name, int age, String dept) when age >= 65 ->
                name + " is eligible for retirement from " + dept;
            case Employee(String name, int age, String dept) when "Engineering".equals(dept) ->
                name + " (" + age + ") is an engineer";
            case Employee(String name, int age, String dept) ->
                name + " works in " + dept;
            case Person(String name, int age) when age < 18 ->
                name + " is a minor";
            case Person(String name, int age) when age >= 65 ->
                name + " is a senior";
            case Person(String name, int age) ->
                name + " is an adult";
            default -> "Unknown";
        };
    }
}
```

---

## Sequenced Collections

Java 21 introduced new interfaces for collections with defined encounter order.

### New Interfaces

```java
import java.util.*;

public class SequencedCollectionsDemo {
    public static void main(String[] args) {
        // SequencedCollection - ordered collection
        SequencedCollection<String> list = new ArrayList<>(List.of("a", "b", "c"));

        // Get first and last elements
        System.out.println("First: " + list.getFirst());
        System.out.println("Last: " + list.getLast());

        // Add at beginning and end
        list.addFirst("start");
        list.addLast("end");
        System.out.println("List: " + list);

        // Remove first and last
        list.removeFirst();
        list.removeLast();
        System.out.println("After removes: " + list);

        // Reversed view
        SequencedCollection<String> reversed = list.reversed();
        System.out.println("Reversed: " + reversed);

        // Original unchanged
        System.out.println("Original: " + list);
    }
}
```

### SequencedSet

```java
import java.util.*;

public class SequencedSetDemo {
    public static void main(String[] args) {
        // LinkedHashSet maintains insertion order
        SequencedSet<String> set = new LinkedHashSet<>();
        set.add("banana");
        set.add("apple");
        set.add("cherry");

        System.out.println("First: " + set.getFirst());  // banana
        System.out.println("Last: " + set.getLast());    // cherry

        // TreeSet maintains sorted order
        SequencedSet<String> sortedSet = new TreeSet<>();
        sortedSet.add("banana");
        sortedSet.add("apple");
        sortedSet.add("cherry");

        System.out.println("Sorted first: " + sortedSet.getFirst());  // apple
        System.out.println("Sorted last: " + sortedSet.getLast());    // cherry

        // Reversed view
        System.out.println("Reversed sorted: " + sortedSet.reversed());
    }
}
```

### SequencedMap

```java
import java.util.*;

public class SequencedMapDemo {
    public static void main(String[] args) {
        SequencedMap<String, Integer> map = new LinkedHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        // First and last entries
        System.out.println("First: " + map.firstEntry());  // one=1
        System.out.println("Last: " + map.lastEntry());    // three=3

        // Put at beginning and end
        map.putFirst("zero", 0);
        map.putLast("four", 4);
        System.out.println("Map: " + map);

        // Poll (remove) first and last
        Map.Entry<String, Integer> first = map.pollFirstEntry();
        Map.Entry<String, Integer> last = map.pollLastEntry();
        System.out.println("Polled: " + first + ", " + last);
        System.out.println("Remaining: " + map);

        // Reversed view
        SequencedMap<String, Integer> reversed = map.reversed();
        System.out.println("Reversed: " + reversed);

        // Sequenced key set, values, entries
        System.out.println("Keys: " + map.sequencedKeySet());
        System.out.println("Values: " + map.sequencedValues());
        System.out.println("Entries: " + map.sequencedEntrySet());
    }
}
```

### Collections Utility Methods

```java
import java.util.*;

public class SequencedCollectionsUtils {
    public static void main(String[] args) {
        // Unmodifiable sequenced collections
        List<String> list = List.of("a", "b", "c");
        SequencedCollection<String> unmodifiable =
            Collections.unmodifiableSequencedCollection(
                new ArrayList<>(list));

        System.out.println("First: " + unmodifiable.getFirst());

        // Unmodifiable sequenced set
        SequencedSet<String> unmodSet =
            Collections.unmodifiableSequencedSet(
                new LinkedHashSet<>(list));

        // Unmodifiable sequenced map
        SequencedMap<String, Integer> unmodMap =
            Collections.unmodifiableSequencedMap(
                new LinkedHashMap<>(Map.of("a", 1, "b", 2)));
    }
}
```

---

## String Templates (Preview)

String templates provide a safer and more readable way to compose strings.

> Note: Enable with `--enable-preview`

```java
public class StringTemplatesDemo {
    public static void main(String[] args) {
        String name = "Alice";
        int age = 30;

        // STR template processor (standard)
        String message = STR."Hello, \{name}! You are \{age} years old.";
        System.out.println(message);

        // Expressions in templates
        int x = 10, y = 20;
        String calc = STR."\{x} + \{y} = \{x + y}";
        System.out.println(calc);

        // Method calls
        String upper = STR."Name in uppercase: \{name.toUpperCase()}";
        System.out.println(upper);

        // Multi-line
        String json = STR."""
            {
                "name": "\{name}",
                "age": \{age},
                "adult": \{age >= 18}
            }
            """;
        System.out.println(json);

        // FMT for formatting (like printf)
        double price = 99.99;
        String formatted = FMT."Price: $%.2f\{price}";
        System.out.println(formatted);

        // Table formatting
        record Product(String name, int qty, double price) {}
        var products = List.of(
            new Product("Widget", 10, 9.99),
            new Product("Gadget", 5, 24.99),
            new Product("Gizmo", 3, 49.99)
        );

        String table = STR."""
            | Product  | Qty | Price   |
            |----------|-----|---------|
            \{formatProducts(products)}
            """;
        System.out.println(table);
    }

    static String formatProducts(List<Product> products) {
        StringBuilder sb = new StringBuilder();
        for (Product p : products) {
            sb.append(STR."| \{pad(p.name(), 8)} | \{pad(String.valueOf(p.qty()), 3)} | $\{String.format("%.2f", p.price())}  |\n");
        }
        return sb.toString();
    }

    static String pad(String s, int width) {
        return String.format("%-" + width + "s", s);
    }

    record Product(String name, int qty, double price) {}
}
```

---

## Structured Concurrency (Preview)

Structured concurrency is now a preview feature.

```java
import java.util.concurrent.*;

public class StructuredConcurrencyPreview {
    record User(String name) {}
    record Order(int id, double total) {}
    record Account(String type, double balance) {}
    record UserProfile(User user, Order latestOrder, Account account) {}

    public static void main(String[] args) throws Exception {
        UserProfile profile = fetchUserProfile(123);
        System.out.println("User: " + profile.user());
        System.out.println("Latest Order: " + profile.latestOrder());
        System.out.println("Account: " + profile.account());
    }

    static UserProfile fetchUserProfile(int userId) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork subtasks
            Subtask<User> userTask = scope.fork(() -> fetchUser(userId));
            Subtask<Order> orderTask = scope.fork(() -> fetchLatestOrder(userId));
            Subtask<Account> accountTask = scope.fork(() -> fetchAccount(userId));

            // Wait for completion or failure
            scope.join()
                 .throwIfFailed();

            // All succeeded
            return new UserProfile(
                userTask.get(),
                orderTask.get(),
                accountTask.get()
            );
        }
    }

    static User fetchUser(int id) throws Exception {
        Thread.sleep(100);
        return new User("Alice");
    }

    static Order fetchLatestOrder(int userId) throws Exception {
        Thread.sleep(150);
        return new Order(1001, 99.99);
    }

    static Account fetchAccount(int userId) throws Exception {
        Thread.sleep(80);
        return new Account("Premium", 1500.00);
    }
}
```

---

## Unnamed Patterns and Variables (Preview)

Use underscore `_` for unused variables and patterns.

```java
public class UnnamedPatternsDemo {
    record Point(int x, int y, int z) {}
    sealed interface Shape permits Circle, Rectangle {}
    record Circle(double radius, String color) implements Shape {}
    record Rectangle(double w, double h, String color) implements Shape {}

    public static void main(String[] args) {
        // Unnamed variable in enhanced for
        var list = java.util.List.of(1, 2, 3, 4, 5);
        int count = 0;
        for (int _ : list) {  // Don't care about value
            count++;
        }
        System.out.println("Count: " + count);

        // Unnamed variable in try-with-resources
        try (var _ = new AutoCloseable() {
            public void close() { System.out.println("Closed!"); }
        }) {
            System.out.println("Using resource");
        }

        // Unnamed pattern in switch
        Shape shape = new Circle(5.0, "red");
        String result = switch (shape) {
            case Circle(double r, _) -> "Circle with radius " + r;
            case Rectangle(double w, double h, _) -> "Rectangle " + w + "x" + h;
        };
        System.out.println(result);

        // Unnamed in record pattern
        Point point = new Point(1, 2, 3);
        if (point instanceof Point(int x, _, int z)) {
            System.out.println("x=" + x + ", z=" + z);  // Ignore y
        }

        // Multiple unnamed
        var map = java.util.Map.of("a", 1, "b", 2);
        for (var _ : map.entrySet()) {
            System.out.println("Entry exists");
        }

        // In catch block
        try {
            Integer.parseInt("not a number");
        } catch (NumberFormatException _) {
            System.out.println("Parse failed");
        }

        // Lambda with unused parameters
        java.util.function.BiFunction<String, Integer, String> fn =
            (_, _) -> "ignored both";
        System.out.println(fn.apply("hello", 42));
    }
}
```

---

## Unnamed Classes and Instance Main Methods (Preview)

Simplified entry points for simple programs.

```java
// File: HelloWorld.java
// No class declaration needed!
void main() {
    System.out.println("Hello, World!");
}

// File: Greeting.java
// Instance method as entry point
String greeting = "Hello";

void main(String[] args) {
    String name = args.length > 0 ? args[0] : "World";
    println(greeting + ", " + name + "!");
}

void println(Object obj) {
    System.out.println(obj);
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Virtual Threads | Standard | Lightweight threads for high throughput |
| Pattern Matching Switch | Standard | Type patterns in switch |
| Record Patterns | Standard | Deconstruct records in patterns |
| Sequenced Collections | Standard | Ordered collection interfaces |
| String Templates | Preview | Type-safe string composition |
| Structured Concurrency | Preview | Coordinated concurrent tasks |
| Scoped Values | Preview | Thread-local alternative |
| Unnamed Patterns | Preview | `_` for unused variables |
| Unnamed Classes | Preview | Simplified main methods |

---

## Hands-On Challenge: Build a Concurrent Web Scraper

```java
// Use Java 21 features to build a concurrent web scraper:
// - Virtual threads for concurrent HTTP requests
// - Structured concurrency for task management
// - Record patterns for response handling
// - Sequenced collections for ordered results

import java.net.http.*;
import java.net.URI;
import java.util.concurrent.*;

public class WebScraperChallenge {
    sealed interface ScrapingResult permits Success, Failure {}
    record Success(URI url, String content, long responseTime) implements ScrapingResult {}
    record Failure(URI url, String error) implements ScrapingResult {}

    public static void main(String[] args) throws Exception {
        var urls = java.util.List.of(
            "https://example.com",
            "https://httpbin.org/get",
            "https://invalid.url.test"
        );

        // Implement: scrapeAll(urls) using virtual threads
        // Return: SequencedCollection<ScrapingResult>
        // Use pattern matching to process results
    }
}
```

[← Java 20 Features](java-20.md) | [Java 22 Features →](java-22.md)
