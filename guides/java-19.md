# Java 19 Features Guide

**Release Date:** September 2022
**Type:** Feature Release

Java 19 introduced Virtual Threads and Record Patterns as preview features.

## Table of Contents
- [Virtual Threads (Preview)](#virtual-threads-preview)
- [Record Patterns (Preview)](#record-patterns-preview)
- [Pattern Matching for switch (3rd Preview)](#pattern-matching-for-switch-3rd-preview)
- [Structured Concurrency (Incubator)](#structured-concurrency-incubator)
- [Foreign Function & Memory API (Preview)](#foreign-function--memory-api-preview)

---

## Virtual Threads (Preview)

Virtual threads are lightweight threads that dramatically reduce the cost of thread-per-request.

> Note: Enable with `--enable-preview`. Standard in Java 21.

### Creating Virtual Threads

```java
public class VirtualThreadsBasics {
    public static void main(String[] args) throws Exception {
        // Create virtual thread directly
        Thread virtualThread = Thread.ofVirtual().start(() -> {
            System.out.println("Hello from virtual thread!");
            System.out.println("Thread: " + Thread.currentThread());
        });
        virtualThread.join();

        // Named virtual thread
        Thread namedVirtual = Thread.ofVirtual()
            .name("my-virtual-thread")
            .start(() -> {
                System.out.println("Named: " + Thread.currentThread().getName());
            });
        namedVirtual.join();

        // Virtual thread factory
        var factory = Thread.ofVirtual().name("worker-", 0).factory();
        for (int i = 0; i < 3; i++) {
            factory.newThread(() -> {
                System.out.println("Factory thread: " + Thread.currentThread().getName());
            }).start();
        }

        Thread.sleep(100); // Wait for threads

        // Check if virtual
        System.out.println("Is virtual: " + Thread.currentThread().isVirtual());
    }
}
```

### Executor with Virtual Threads

```java
import java.util.concurrent.*;
import java.util.*;

public class VirtualThreadExecutor {
    public static void main(String[] args) throws Exception {
        // Executor that creates virtual threads
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // Submit many tasks
            List<Future<String>> futures = new ArrayList<>();
            for (int i = 0; i < 10_000; i++) {
                final int taskId = i;
                futures.add(executor.submit(() -> {
                    Thread.sleep(100); // Simulate I/O
                    return "Task " + taskId + " on " + Thread.currentThread();
                }));
            }

            System.out.println("Submitted 10,000 tasks");

            // Collect results
            int completed = 0;
            for (Future<String> future : futures) {
                future.get();
                completed++;
            }
            System.out.println("Completed: " + completed);
        }

        System.out.println("All tasks finished");
    }
}
```

### Virtual vs Platform Threads

```java
import java.util.concurrent.*;
import java.time.*;

public class VirtualVsPlatform {
    public static void main(String[] args) throws Exception {
        int taskCount = 100_000;

        // Virtual threads
        System.out.println("Starting virtual threads test...");
        long virtualStart = System.currentTimeMillis();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(10));
                    return null;
                });
            }
        }

        long virtualTime = System.currentTimeMillis() - virtualStart;
        System.out.println("Virtual threads: " + virtualTime + "ms");

        // Platform threads (limited pool)
        System.out.println("\nStarting platform threads test...");
        long platformStart = System.currentTimeMillis();

        try (var executor = Executors.newFixedThreadPool(200)) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(10));
                    return null;
                });
            }
        }

        long platformTime = System.currentTimeMillis() - platformStart;
        System.out.println("Platform threads: " + platformTime + "ms");

        System.out.println("\nVirtual threads " +
            (platformTime / virtualTime) + "x faster");
    }
}
```

### Best Practices for Virtual Threads

```java
import java.util.concurrent.*;

public class VirtualThreadBestPractices {
    public static void main(String[] args) throws Exception {
        // DO: Use virtual threads for I/O-bound tasks
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // HTTP calls, database queries, file I/O
            executor.submit(() -> {
                // Simulate HTTP call
                Thread.sleep(100);
                return "response";
            });
        }

        // DO: Use virtual threads for concurrent tasks
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<String>> tasks = List.of(
                () -> fetchFromApi("api1"),
                () -> fetchFromApi("api2"),
                () -> fetchFromApi("api3")
            );
            executor.invokeAll(tasks);
        }

        // DON'T: Use synchronized for long operations
        // Virtual threads can't be unmounted when blocked on synchronized
        // Use ReentrantLock instead:
        var lock = new java.util.concurrent.locks.ReentrantLock();
        lock.lock();
        try {
            // Critical section
        } finally {
            lock.unlock();
        }

        // DON'T: Use ThreadLocal for large objects
        // Virtual threads can have millions of instances
        // Use ScopedValue instead (when available)

        // DON'T: Pool virtual threads
        // They are cheap to create - just create new ones
    }

    static String fetchFromApi(String name) throws Exception {
        Thread.sleep(50);
        return name + " result";
    }
}
```

---

## Record Patterns (Preview)

Record patterns enable deconstructing record values directly in pattern matching.

> Note: Enable with `--enable-preview`. Standard in Java 21.

### Basic Record Patterns

```java
public class RecordPatternsBasics {
    record Point(int x, int y) {}
    record Rectangle(Point topLeft, Point bottomRight) {}

    public static void main(String[] args) {
        // Before: instanceof + accessor methods
        Object obj = new Point(10, 20);

        if (obj instanceof Point p) {
            int x = p.x();
            int y = p.y();
            System.out.println("Point: " + x + ", " + y);
        }

        // With record patterns: direct deconstruction
        if (obj instanceof Point(int x, int y)) {
            System.out.println("Point: " + x + ", " + y);
        }

        // Nested record patterns
        Rectangle rect = new Rectangle(new Point(0, 0), new Point(100, 100));

        if (rect instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) {
            System.out.println("Rectangle from (" + x1 + "," + y1 +
                             ") to (" + x2 + "," + y2 + ")");
        }
    }
}
```

### Record Patterns in Switch

```java
public class RecordPatternsSwitch {
    sealed interface Shape permits Circle, Rectangle, Triangle {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double width, double height) implements Shape {}
    record Triangle(double base, double height) implements Shape {}

    record Point(double x, double y) {}
    record Line(Point start, Point end) {}

    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(10, 20),
            new Triangle(8, 6)
        };

        for (Shape shape : shapes) {
            double area = calculateArea(shape);
            System.out.printf("%s: %.2f%n", shape, area);
        }

        // Nested patterns
        Line line = new Line(new Point(0, 0), new Point(3, 4));
        double length = calculateLength(line);
        System.out.println("Line length: " + length);
    }

    static double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Rectangle(double w, double h) -> w * h;
            case Triangle(double b, double h) -> 0.5 * b * h;
        };
    }

    static double calculateLength(Object obj) {
        return switch (obj) {
            case Line(Point(double x1, double y1), Point(double x2, double y2)) -> {
                double dx = x2 - x1;
                double dy = y2 - y1;
                yield Math.sqrt(dx * dx + dy * dy);
            }
            default -> 0;
        };
    }
}
```

### Practical Example: JSON Processing

```java
import java.util.*;

public class RecordPatternsJson {
    sealed interface JsonValue permits JsonString, JsonNumber, JsonBoolean,
                                     JsonNull, JsonArray, JsonObject {}
    record JsonString(String value) implements JsonValue {}
    record JsonNumber(double value) implements JsonValue {}
    record JsonBoolean(boolean value) implements JsonValue {}
    record JsonNull() implements JsonValue {}
    record JsonArray(List<JsonValue> elements) implements JsonValue {}
    record JsonObject(Map<String, JsonValue> properties) implements JsonValue {}

    public static void main(String[] args) {
        JsonValue json = new JsonObject(Map.of(
            "name", new JsonString("Alice"),
            "age", new JsonNumber(30),
            "active", new JsonBoolean(true),
            "scores", new JsonArray(List.of(
                new JsonNumber(95),
                new JsonNumber(87),
                new JsonNumber(92)
            ))
        ));

        System.out.println(format(json));
        System.out.println("\nSum of scores: " + sumNumbers(json));
    }

    static String format(JsonValue json) {
        return switch (json) {
            case JsonString(String s) -> "\"" + s + "\"";
            case JsonNumber(double n) -> String.valueOf(n);
            case JsonBoolean(boolean b) -> String.valueOf(b);
            case JsonNull() -> "null";
            case JsonArray(List<JsonValue> elements) -> {
                StringJoiner sj = new StringJoiner(", ", "[", "]");
                elements.forEach(e -> sj.add(format(e)));
                yield sj.toString();
            }
            case JsonObject(Map<String, JsonValue> props) -> {
                StringJoiner sj = new StringJoiner(", ", "{", "}");
                props.forEach((k, v) -> sj.add("\"" + k + "\": " + format(v)));
                yield sj.toString();
            }
        };
    }

    static double sumNumbers(JsonValue json) {
        return switch (json) {
            case JsonNumber(double n) -> n;
            case JsonArray(List<JsonValue> elements) ->
                elements.stream().mapToDouble(e -> sumNumbers(e)).sum();
            case JsonObject(Map<String, JsonValue> props) ->
                props.values().stream().mapToDouble(v -> sumNumbers(v)).sum();
            default -> 0;
        };
    }
}
```

---

## Structured Concurrency (Incubator)

Structured concurrency treats related tasks as a single unit of work.

```java
// Note: Requires --add-modules jdk.incubator.concurrent
import jdk.incubator.concurrent.*;

public class StructuredConcurrencyDemo {
    record User(String name, int age) {}
    record Order(String id, double total) {}
    record UserDetails(User user, List<Order> orders) {}

    public static void main(String[] args) throws Exception {
        UserDetails details = fetchUserDetails(123);
        System.out.println("User: " + details.user());
        System.out.println("Orders: " + details.orders());
    }

    static UserDetails fetchUserDetails(int userId) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork tasks
            var userTask = scope.fork(() -> fetchUser(userId));
            var ordersTask = scope.fork(() -> fetchOrders(userId));

            // Wait for all tasks to complete or fail
            scope.join();
            scope.throwIfFailed();

            // Both completed successfully
            return new UserDetails(userTask.get(), ordersTask.get());
        }
    }

    static User fetchUser(int id) throws Exception {
        Thread.sleep(100); // Simulate API call
        return new User("Alice", 30);
    }

    static List<Order> fetchOrders(int userId) throws Exception {
        Thread.sleep(150); // Simulate API call
        return List.of(
            new Order("ORD-1", 99.99),
            new Order("ORD-2", 149.99)
        );
    }
}
```

### ShutdownOnSuccess

```java
import jdk.incubator.concurrent.*;

public class StructuredFirstSuccess {
    public static void main(String[] args) throws Exception {
        // Returns first successful result, cancels others
        String result = fetchFirstAvailable();
        System.out.println("First result: " + result);
    }

    static String fetchFirstAvailable() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> fetchFromServer1());
            scope.fork(() -> fetchFromServer2());
            scope.fork(() -> fetchFromServer3());

            scope.join();
            return scope.result();
        }
    }

    static String fetchFromServer1() throws Exception {
        Thread.sleep(200);
        return "Server 1 response";
    }

    static String fetchFromServer2() throws Exception {
        Thread.sleep(100);
        return "Server 2 response"; // This wins
    }

    static String fetchFromServer3() throws Exception {
        Thread.sleep(300);
        return "Server 3 response";
    }
}
```

---

## Foreign Function & Memory API (Preview)

Access native code and memory outside the Java heap.

```java
import java.lang.foreign.*;
import java.lang.invoke.*;

public class ForeignFunctionDemo {
    public static void main(String[] args) throws Throwable {
        // Allocate native memory
        try (Arena arena = Arena.ofConfined()) {
            // Allocate a native string
            MemorySegment cString = arena.allocateUtf8String("Hello, Native!");
            System.out.println("Native string: " + cString.getUtf8String(0));

            // Allocate array
            MemorySegment intArray = arena.allocateArray(ValueLayout.JAVA_INT, 10);
            for (int i = 0; i < 10; i++) {
                intArray.setAtIndex(ValueLayout.JAVA_INT, i, i * i);
            }

            System.out.println("Native array:");
            for (int i = 0; i < 10; i++) {
                int value = intArray.getAtIndex(ValueLayout.JAVA_INT, i);
                System.out.print(value + " ");
            }
            System.out.println();
        }

        // Call native function (e.g., strlen from libc)
        callNativeFunction();
    }

    static void callNativeFunction() throws Throwable {
        // Get native linker
        Linker linker = Linker.nativeLinker();
        SymbolLookup stdlib = linker.defaultLookup();

        // Find strlen function
        MemorySegment strlenAddr = stdlib.find("strlen").orElseThrow();

        // Create method handle
        MethodHandle strlen = linker.downcallHandle(
            strlenAddr,
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );

        // Call strlen
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment str = arena.allocateUtf8String("Hello!");
            long length = (long) strlen.invoke(str);
            System.out.println("strlen(\"Hello!\") = " + length);
        }
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Virtual Threads | Preview | Lightweight threads |
| Record Patterns | Preview | Deconstruct records in patterns |
| Pattern Matching Switch | 3rd Preview | Refinements |
| Structured Concurrency | Incubator | Task coordination |
| Foreign Function API | Preview | Native code interop |

[← Java 18 Features](java-18.md) | [Java 20 Features →](java-20.md)
