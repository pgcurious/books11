# Java 20 Features Guide

**Release Date:** March 2023
**Type:** Feature Release

Java 20 continued refining Virtual Threads and introduced Scoped Values.

## Table of Contents
- [Scoped Values (Incubator)](#scoped-values-incubator)
- [Record Patterns (2nd Preview)](#record-patterns-2nd-preview)
- [Pattern Matching for switch (4th Preview)](#pattern-matching-for-switch-4th-preview)
- [Virtual Threads (2nd Preview)](#virtual-threads-2nd-preview)
- [Structured Concurrency (2nd Incubator)](#structured-concurrency-2nd-incubator)

---

## Scoped Values (Incubator)

Scoped Values provide a way to share immutable data within and across threads.

> Note: Requires `--add-modules jdk.incubator.concurrent`

### Why Scoped Values?

```java
import jdk.incubator.concurrent.ScopedValue;

public class ScopedValuesIntro {
    // ThreadLocal problems:
    // 1. Mutable - can be changed anywhere
    // 2. Inherited by child threads (memory overhead)
    // 3. Not optimized for virtual threads
    // 4. Must be cleaned up manually

    // ScopedValue advantages:
    // 1. Immutable within scope
    // 2. Automatically cleaned up
    // 3. Efficiently rebound for child threads
    // 4. Optimized for virtual threads

    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        // Bind value for a scope
        ScopedValue.where(USER, "alice").run(() -> {
            System.out.println("User: " + USER.get());
            processRequest();
        });

        // Value is not available outside scope
        System.out.println("Has value: " + USER.isBound()); // false
    }

    static void processRequest() {
        System.out.println("Processing for: " + USER.get());
        handleData();
    }

    static void handleData() {
        // Value available throughout the call stack
        System.out.println("Handling data for: " + USER.get());
    }
}
```

### Scoped Values with Return Values

```java
import jdk.incubator.concurrent.ScopedValue;

public class ScopedValueWithResult {
    private static final ScopedValue<Integer> USER_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    record UserData(String name, int age) {}

    public static void main(String[] args) throws Exception {
        // Call with return value
        UserData result = ScopedValue.where(USER_ID, 123)
            .where(REQUEST_ID, "REQ-001")
            .call(() -> {
                System.out.println("Request: " + REQUEST_ID.get());
                return fetchUserData();
            });

        System.out.println("Result: " + result);
    }

    static UserData fetchUserData() {
        int userId = USER_ID.get();
        System.out.println("Fetching user " + userId);
        return new UserData("Alice", 30);
    }
}
```

### Rebinding Scoped Values

```java
import jdk.incubator.concurrent.ScopedValue;

public class ScopedValueRebinding {
    private static final ScopedValue<String> CONTEXT = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        ScopedValue.where(CONTEXT, "outer").run(() -> {
            System.out.println("Outer: " + CONTEXT.get());

            // Rebind for nested scope
            ScopedValue.where(CONTEXT, "inner").run(() -> {
                System.out.println("Inner: " + CONTEXT.get());
            });

            // Original binding restored
            System.out.println("Back to outer: " + CONTEXT.get());
        });
    }
}
```

### Scoped Values with Structured Concurrency

```java
import jdk.incubator.concurrent.*;

public class ScopedValueConcurrency {
    private static final ScopedValue<String> USER = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        ScopedValue.where(USER, "admin").run(() -> {
            try {
                String result = processWithConcurrency();
                System.out.println("Result: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    static String processWithConcurrency() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Scoped values are inherited by forked tasks
            var task1 = scope.fork(() -> {
                Thread.sleep(50);
                return "Task 1 for " + USER.get();
            });
            var task2 = scope.fork(() -> {
                Thread.sleep(100);
                return "Task 2 for " + USER.get();
            });

            scope.join();
            scope.throwIfFailed();

            return task1.get() + " + " + task2.get();
        }
    }
}
```

### Scoped Values vs ThreadLocal

```java
import jdk.incubator.concurrent.ScopedValue;
import java.util.concurrent.*;

public class ScopedVsThreadLocal {
    // ThreadLocal approach
    private static final ThreadLocal<String> TL_USER = new ThreadLocal<>();

    // ScopedValue approach
    private static final ScopedValue<String> SV_USER = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        // ThreadLocal
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                executor.submit(() -> {
                    TL_USER.set("user-" + userId);
                    try {
                        processWithThreadLocal();
                    } finally {
                        TL_USER.remove(); // Must clean up!
                    }
                });
            }
        }

        Thread.sleep(100);

        // ScopedValue - cleaner!
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 5; i++) {
                final int userId = i;
                executor.submit(() -> {
                    ScopedValue.where(SV_USER, "user-" + userId).run(() -> {
                        processWithScopedValue();
                    });
                    // Automatic cleanup
                });
            }
        }
    }

    static void processWithThreadLocal() {
        System.out.println("[TL] " + TL_USER.get());
    }

    static void processWithScopedValue() {
        System.out.println("[SV] " + SV_USER.get());
    }
}
```

---

## Record Patterns (2nd Preview)

Refined record patterns with better inference and nesting.

```java
public class RecordPatterns2 {
    record Point(int x, int y) {}
    record Rectangle(Point topLeft, Point bottomRight) {}
    record ColoredRectangle(Rectangle rect, String color) {}

    public static void main(String[] args) {
        // Simple deconstruction
        Point p = new Point(10, 20);
        if (p instanceof Point(int x, int y)) {
            System.out.println("x=" + x + ", y=" + y);
        }

        // Nested deconstruction
        Rectangle rect = new Rectangle(new Point(0, 0), new Point(100, 200));
        if (rect instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) {
            int width = x2 - x1;
            int height = y2 - y1;
            System.out.println("Size: " + width + "x" + height);
        }

        // Deep nesting
        ColoredRectangle cr = new ColoredRectangle(rect, "blue");
        if (cr instanceof ColoredRectangle(
                Rectangle(Point(int x1, int y1), Point p2),
                String color)) {
            System.out.println("Colored rect starting at (" + x1 + "," + y1 + ")");
            System.out.println("Color: " + color);
        }

        // In switch
        Object obj = new Rectangle(new Point(5, 5), new Point(15, 20));
        String result = switch (obj) {
            case Rectangle(Point(int x, int y), Point _)
                    when x == 0 && y == 0 -> "Starts at origin";
            case Rectangle(Point p1, Point p2) -> "Rectangle from " + p1 + " to " + p2;
            default -> "Not a rectangle";
        };
        System.out.println(result);
    }
}
```

### Generic Record Patterns

```java
import java.util.*;

public class GenericRecordPatterns {
    record Box<T>(T content) {}
    record Pair<A, B>(A first, B second) {}

    public static void main(String[] args) {
        // Generic record patterns
        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> intBox = new Box<>(42);

        Object[] items = {stringBox, intBox, new Box<>(List.of(1, 2, 3))};

        for (Object item : items) {
            String result = switch (item) {
                case Box(String s) -> "String: " + s;
                case Box(Integer i) -> "Integer: " + i;
                case Box(List<?> list) -> "List with " + list.size() + " elements";
                case Box(var other) -> "Other: " + other;
                default -> "Not a box";
            };
            System.out.println(result);
        }

        // Pair with inference
        Pair<String, Integer> pair = new Pair<>("age", 30);
        if (pair instanceof Pair(String key, Integer value)) {
            System.out.println(key + " = " + value);
        }
    }
}
```

---

## Pattern Matching for switch (4th Preview)

Further refinements to switch pattern matching.

```java
public class PatternSwitch4 {
    sealed interface Expr permits Num, Add, Mul, Var {}
    record Num(int value) implements Expr {}
    record Add(Expr left, Expr right) implements Expr {}
    record Mul(Expr left, Expr right) implements Expr {}
    record Var(String name) implements Expr {}

    public static void main(String[] args) {
        Expr expr = new Add(new Num(1), new Mul(new Num(2), new Var("x")));

        // Evaluate with x = 5
        int result = evaluate(expr, Map.of("x", 5));
        System.out.println("1 + (2 * x) where x=5 = " + result); // 11

        // Simplify
        Expr simplified = simplify(new Mul(new Num(1), new Var("y")));
        System.out.println("Simplified: " + simplified);
    }

    static int evaluate(Expr expr, java.util.Map<String, Integer> env) {
        return switch (expr) {
            case Num(int n) -> n;
            case Add(Expr l, Expr r) -> evaluate(l, env) + evaluate(r, env);
            case Mul(Expr l, Expr r) -> evaluate(l, env) * evaluate(r, env);
            case Var(String name) -> env.getOrDefault(name, 0);
        };
    }

    static Expr simplify(Expr expr) {
        return switch (expr) {
            case Add(Num(int a), Num(int b)) -> new Num(a + b);
            case Mul(Num(int a), Num(int b)) -> new Num(a * b);
            case Mul(Num(int n), Expr e) when n == 0 -> new Num(0);
            case Mul(Num(int n), Expr e) when n == 1 -> simplify(e);
            case Mul(Expr e, Num(int n)) when n == 0 -> new Num(0);
            case Mul(Expr e, Num(int n)) when n == 1 -> simplify(e);
            case Add(Num(int n), Expr e) when n == 0 -> simplify(e);
            case Add(Expr e, Num(int n)) when n == 0 -> simplify(e);
            default -> expr;
        };
    }
}
```

---

## Virtual Threads (2nd Preview)

Continued improvements to virtual threads.

```java
import java.util.concurrent.*;
import java.time.*;

public class VirtualThreads2 {
    public static void main(String[] args) throws Exception {
        // Thread.Builder API
        Thread.Builder builder = Thread.ofVirtual()
            .name("worker-", 0)
            .inheritInheritableThreadLocals(false);

        Thread t1 = builder.start(() -> {
            System.out.println("Thread: " + Thread.currentThread().getName());
        });
        Thread t2 = builder.start(() -> {
            System.out.println("Thread: " + Thread.currentThread().getName());
        });

        t1.join();
        t2.join();

        // Stress test with many virtual threads
        Instant start = Instant.now();
        int count = 1_000_000;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var latch = new CountDownLatch(count);

            for (int i = 0; i < count; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(Duration.ofMillis(1));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            latch.await();
        }

        Duration elapsed = Duration.between(start, Instant.now());
        System.out.printf("Ran %,d virtual threads in %s%n", count, elapsed);
    }
}
```

### Monitoring Virtual Threads

```java
import java.lang.management.*;
import java.util.concurrent.*;

public class VirtualThreadMonitoring {
    public static void main(String[] args) throws Exception {
        // Thread MXBean
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        System.out.println("Before: " + threadBean.getThreadCount() + " threads");

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(1000);

            for (int i = 0; i < 1000; i++) {
                executor.submit(() -> {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    latch.countDown();
                });
            }

            Thread.sleep(50);
            // Note: Virtual threads don't increase platform thread count significantly
            System.out.println("During: " + threadBean.getThreadCount() + " threads");

            latch.await();
        }

        System.out.println("After: " + threadBean.getThreadCount() + " threads");
    }
}
```

---

## Structured Concurrency (2nd Incubator)

Improved structured concurrency API.

```java
import jdk.incubator.concurrent.*;
import java.time.*;
import java.util.*;

public class StructuredConcurrency2 {
    record Weather(String city, double temp) {}
    record Stock(String symbol, double price) {}
    record Dashboard(Weather weather, List<Stock> stocks) {}

    public static void main(String[] args) throws Exception {
        Dashboard dashboard = fetchDashboard();
        System.out.println("Weather: " + dashboard.weather());
        System.out.println("Stocks: " + dashboard.stocks());
    }

    static Dashboard fetchDashboard() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var weatherTask = scope.fork(() -> fetchWeather("NYC"));
            var stocksTask = scope.fork(() -> fetchStocks());

            scope.join();
            scope.throwIfFailed(e -> new RuntimeException("Failed to load dashboard", e));

            return new Dashboard(weatherTask.get(), stocksTask.get());
        }
    }

    static Weather fetchWeather(String city) throws Exception {
        Thread.sleep(100);
        return new Weather(city, 72.5);
    }

    static List<Stock> fetchStocks() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var aapl = scope.fork(() -> fetchStock("AAPL"));
            var googl = scope.fork(() -> fetchStock("GOOGL"));
            var msft = scope.fork(() -> fetchStock("MSFT"));

            scope.join();
            scope.throwIfFailed();

            return List.of(aapl.get(), googl.get(), msft.get());
        }
    }

    static Stock fetchStock(String symbol) throws Exception {
        Thread.sleep(50);
        return new Stock(symbol, Math.random() * 500 + 100);
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Scoped Values | Incubator | Immutable thread-local alternative |
| Record Patterns | 2nd Preview | Refined deconstruction |
| Pattern Matching Switch | 4th Preview | Further refinements |
| Virtual Threads | 2nd Preview | Lightweight threads |
| Structured Concurrency | 2nd Incubator | Task coordination |

[← Java 19 Features](java-19.md) | [Java 21 Features →](java-21.md)
