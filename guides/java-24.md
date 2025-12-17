# Java 24 Features Guide

**Release Date:** March 2025
**Type:** Feature Release

Java 24 brings Stream Gatherers to standard and introduces flexible constructor bodies.

## Table of Contents
- [Stream Gatherers (Standard)](#stream-gatherers-standard)
- [Flexible Constructor Bodies (2nd Preview)](#flexible-constructor-bodies-2nd-preview)
- [Primitive Types in Patterns (2nd Preview)](#primitive-types-in-patterns-2nd-preview)
- [Module Import Declarations (2nd Preview)](#module-import-declarations-2nd-preview)
- [Scoped Values (Standard)](#scoped-values-standard)
- [Structured Concurrency (Standard)](#structured-concurrency-standard)
- [Class-File API (2nd Preview)](#class-file-api-2nd-preview)

---

## Stream Gatherers (Standard)

Stream Gatherers are now a standard feature, enabling custom intermediate operations.

```java
import java.util.*;
import java.util.stream.*;

public class StreamGatherersStandard {
    public static void main(String[] args) {
        // Built-in Gatherers
        demonstrateBuiltInGatherers();

        // Custom Gatherers
        demonstrateCustomGatherers();

        // Practical examples
        demonstratePracticalUsage();
    }

    static void demonstrateBuiltInGatherers() {
        System.out.println("=== Built-in Gatherers ===\n");

        // windowFixed - non-overlapping windows
        List<List<Integer>> fixedWindows = Stream.iterate(1, n -> n + 1)
            .limit(10)
            .gather(Gatherers.windowFixed(3))
            .toList();
        System.out.println("Fixed windows (size 3): " + fixedWindows);
        // [[1, 2, 3], [4, 5, 6], [7, 8, 9], [10]]

        // windowSliding - overlapping windows
        List<List<Integer>> slidingWindows = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.windowSliding(3))
            .toList();
        System.out.println("Sliding windows (size 3): " + slidingWindows);
        // [[1, 2, 3], [2, 3, 4], [3, 4, 5]]

        // fold - accumulate with intermediate results
        List<String> folded = Stream.of("a", "b", "c", "d")
            .gather(Gatherers.fold(
                () -> "",
                (acc, s) -> acc.isEmpty() ? s : acc + "-" + s
            ))
            .toList();
        System.out.println("Folded: " + folded);
        // [a, a-b, a-b-c, a-b-c-d]

        // scan - similar to fold, different semantics
        List<Integer> running = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.scan(() -> 0, Integer::sum))
            .toList();
        System.out.println("Running sum: " + running);
        // [0, 1, 3, 6, 10, 15]

        // mapConcurrent - bounded parallel mapping
        List<String> concurrent = Stream.of("a", "b", "c", "d")
            .gather(Gatherers.mapConcurrent(2, s -> {
                // At most 2 concurrent operations
                return s.toUpperCase();
            }))
            .toList();
        System.out.println("Concurrent mapped: " + concurrent);
    }

    static void demonstrateCustomGatherers() {
        System.out.println("\n=== Custom Gatherers ===\n");

        // Distinct by key
        record Person(String name, String city) {}
        List<Person> people = List.of(
            new Person("Alice", "NYC"),
            new Person("Bob", "LA"),
            new Person("Carol", "NYC"),
            new Person("David", "Chicago")
        );

        List<Person> distinctByCity = people.stream()
            .gather(distinctByKey(Person::city))
            .toList();
        System.out.println("Distinct by city: " + distinctByCity);

        // Take while changing
        List<Integer> numbers = List.of(1, 1, 2, 2, 2, 3, 3, 1, 1);
        List<Integer> changes = numbers.stream()
            .gather(takeOnChange())
            .toList();
        System.out.println("On change: " + changes);
        // [1, 2, 3, 1]

        // Batch by predicate
        List<String> items = List.of("a", "b", "SPLIT", "c", "d", "e", "SPLIT", "f");
        List<List<String>> batches = items.stream()
            .gather(batchBySeparator(s -> s.equals("SPLIT")))
            .toList();
        System.out.println("Batched: " + batches);
    }

    // Custom: distinct by key
    static <T, K> Gatherer<T, ?, T> distinctByKey(
            java.util.function.Function<T, K> keyExtractor) {
        return Gatherer.ofSequential(
            HashSet<K>::new,
            (seen, element, downstream) -> {
                K key = keyExtractor.apply(element);
                if (seen.add(key)) {
                    return downstream.push(element);
                }
                return true;
            }
        );
    }

    // Custom: emit only on value change
    static <T> Gatherer<T, ?, T> takeOnChange() {
        class State {
            T previous = null;
            boolean first = true;
        }
        return Gatherer.ofSequential(
            State::new,
            (state, element, downstream) -> {
                if (state.first || !Objects.equals(state.previous, element)) {
                    state.first = false;
                    state.previous = element;
                    return downstream.push(element);
                }
                return true;
            }
        );
    }

    // Custom: batch by separator
    static <T> Gatherer<T, ?, List<T>> batchBySeparator(
            java.util.function.Predicate<T> isSeparator) {
        return Gatherer.ofSequential(
            ArrayList<T>::new,
            (batch, element, downstream) -> {
                if (isSeparator.test(element)) {
                    if (!batch.isEmpty()) {
                        downstream.push(new ArrayList<>(batch));
                        batch.clear();
                    }
                } else {
                    batch.add(element);
                }
                return true;
            },
            (batch, downstream) -> {
                if (!batch.isEmpty()) {
                    downstream.push(batch);
                }
            }
        );
    }

    static void demonstratePracticalUsage() {
        System.out.println("\n=== Practical Usage ===\n");

        // Moving average
        List<Double> prices = List.of(100.0, 102.0, 101.0, 103.0, 105.0, 104.0, 106.0);
        List<Double> movingAvg = prices.stream()
            .gather(Gatherers.windowSliding(3))
            .map(window -> window.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0))
            .toList();
        System.out.println("3-day moving average: " + movingAvg);

        // Pairwise operations
        List<Integer> values = List.of(1, 4, 2, 8, 5);
        List<Integer> differences = values.stream()
            .gather(Gatherers.windowSliding(2))
            .map(pair -> pair.get(1) - pair.get(0))
            .toList();
        System.out.println("Differences: " + differences);
        // [3, -2, 6, -3]

        // Chunked processing
        List<Integer> data = Stream.iterate(1, n -> n + 1).limit(100).toList();
        data.stream()
            .gather(Gatherers.windowFixed(10))
            .forEach(chunk -> System.out.println(
                "Processing chunk: " + chunk.get(0) + "-" + chunk.get(chunk.size() - 1)));
    }
}
```

---

## Flexible Constructor Bodies (2nd Preview)

Enhanced control over constructor execution flow.

```java
public class FlexibleConstructorBodies {
    public static void main(String[] args) {
        // Validation before super
        var pos = new PositiveInteger(42);
        System.out.println("Value: " + pos.getValue());

        // Parsing before super
        var config = new DatabaseConfig("localhost:5432/mydb");
        System.out.println("Host: " + config.getHost());
        System.out.println("Port: " + config.getPort());
        System.out.println("Database: " + config.getDatabase());

        // Complex initialization
        var cache = new BoundedCache<String, Integer>(100);
        cache.put("key", 42);
    }
}

// Base class
class IntWrapper {
    private final int value;
    IntWrapper(int value) { this.value = value; }
    int getValue() { return value; }
}

// Subclass with validation before super()
class PositiveInteger extends IntWrapper {
    PositiveInteger(int value) {
        // Validation before super()
        if (value <= 0) {
            throw new IllegalArgumentException(
                "Value must be positive, got: " + value);
        }
        // Now call super
        super(value);
    }
}

// Base class for database connection
class DatabaseConnection {
    private final String host;
    private final int port;
    private final String database;

    DatabaseConnection(String host, int port, String database) {
        this.host = host;
        this.port = port;
        this.database = database;
    }

    String getHost() { return host; }
    int getPort() { return port; }
    String getDatabase() { return database; }
}

// Subclass that parses connection string
class DatabaseConfig extends DatabaseConnection {
    DatabaseConfig(String connectionString) {
        // Parse before super()
        String[] parts = connectionString.split("[:/]");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);
        String database = parts[2];

        // Validate
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Invalid port: " + port);
        }

        // Call super with parsed values
        super(host, port, database);
    }
}

// Base cache class
class Cache<K, V> {
    private final java.util.Map<K, V> data;

    Cache(java.util.Map<K, V> initialData) {
        this.data = initialData;
    }

    void put(K key, V value) { data.put(key, value); }
    V get(K key) { return data.get(key); }
}

// Subclass with computed initial state
class BoundedCache<K, V> extends Cache<K, V> {
    private final int maxSize;

    BoundedCache(int maxSize) {
        // Compute initial data structure before super()
        var boundedMap = new java.util.LinkedHashMap<K, V>(maxSize, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
                return size() > maxSize;
            }
        };

        // Pass to super
        super(boundedMap);
        this.maxSize = maxSize;
    }
}
```

---

## Scoped Values (Standard)

Scoped Values are now a standard feature for efficient thread-local-like sharing.

```java
import java.lang.ScopedValue;
import java.util.concurrent.*;

public class ScopedValuesStandard {
    // Declare scoped values
    private static final ScopedValue<String> USER = ScopedValue.newInstance();
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        // Basic usage
        ScopedValue.where(USER, "alice").run(() -> {
            System.out.println("User: " + USER.get());
            processRequest();
        });

        // With return value
        String result = ScopedValue.where(USER, "bob")
            .where(REQUEST_ID, "REQ-123")
            .call(() -> {
                return handleRequest();
            });
        System.out.println("Result: " + result);

        // With virtual threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                executor.submit(() -> {
                    ScopedValue.where(USER, "user-" + userId)
                        .where(REQUEST_ID, "REQ-" + System.currentTimeMillis())
                        .run(() -> {
                            System.out.println(Thread.currentThread() +
                                " processing for " + USER.get());
                            processRequest();
                        });
                });
            }
        }
    }

    static void processRequest() {
        System.out.println("  Processing request for: " + USER.get());
        validateUser();
        fetchData();
    }

    static void validateUser() {
        String user = USER.get();
        System.out.println("  Validating: " + user);
    }

    static void fetchData() {
        String user = USER.get();
        System.out.println("  Fetching data for: " + user);
    }

    static String handleRequest() {
        String user = USER.get();
        String requestId = REQUEST_ID.get();
        return "Handled " + requestId + " for " + user;
    }
}
```

---

## Structured Concurrency (Standard)

Structured Concurrency is now a standard feature.

```java
import java.util.concurrent.*;
import java.time.*;
import java.util.*;

public class StructuredConcurrencyStandard {
    record Weather(String city, double temperature) {}
    record Flight(String airline, String time, double price) {}
    record Hotel(String name, int rating, double pricePerNight) {}
    record TravelPlan(Weather weather, List<Flight> flights, List<Hotel> hotels) {}

    public static void main(String[] args) throws Exception {
        TravelPlan plan = planTrip("Paris", LocalDate.now());
        System.out.println("Weather: " + plan.weather());
        System.out.println("Flights: " + plan.flights().size());
        System.out.println("Hotels: " + plan.hotels().size());
    }

    static TravelPlan planTrip(String destination, LocalDate date) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork concurrent tasks
            Subtask<Weather> weatherTask = scope.fork(
                () -> fetchWeather(destination));
            Subtask<List<Flight>> flightsTask = scope.fork(
                () -> searchFlights(destination, date));
            Subtask<List<Hotel>> hotelsTask = scope.fork(
                () -> searchHotels(destination, date));

            // Wait and handle failures
            scope.join()
                 .throwIfFailed(e -> new RuntimeException(
                     "Failed to plan trip", e));

            // All tasks succeeded
            return new TravelPlan(
                weatherTask.get(),
                flightsTask.get(),
                hotelsTask.get()
            );
        }
    }

    // Simulated API calls
    static Weather fetchWeather(String city) throws Exception {
        Thread.sleep(100);
        return new Weather(city, 22.5);
    }

    static List<Flight> searchFlights(String dest, LocalDate date) throws Exception {
        Thread.sleep(200);
        return List.of(
            new Flight("AirFrance", "08:00", 350.0),
            new Flight("Lufthansa", "10:30", 420.0)
        );
    }

    static List<Hotel> searchHotels(String dest, LocalDate date) throws Exception {
        Thread.sleep(150);
        return List.of(
            new Hotel("Grand Hotel", 5, 250.0),
            new Hotel("City Inn", 3, 120.0)
        );
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Stream Gatherers | Standard | Custom intermediate operations |
| Flexible Constructor Bodies | 2nd Preview | Code before super() |
| Primitive Patterns | 2nd Preview | Primitives in patterns |
| Module Imports | 2nd Preview | Import entire modules |
| Scoped Values | Standard | Efficient context sharing |
| Structured Concurrency | Standard | Coordinated task execution |

[← Java 23 Features](java-23.md) | [Java 25 Features →](java-25.md)
