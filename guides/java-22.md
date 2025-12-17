# Java 22 Features Guide

**Release Date:** March 2024
**Type:** Feature Release

Java 22 introduced Unnamed Variables as standard and new preview features like Statements Before super().

## Table of Contents
- [Unnamed Variables and Patterns (Standard)](#unnamed-variables-and-patterns-standard)
- [Statements Before super() (Preview)](#statements-before-super-preview)
- [Foreign Function & Memory API (Standard)](#foreign-function--memory-api-standard)
- [Stream Gatherers (Preview)](#stream-gatherers-preview)
- [String Templates (2nd Preview)](#string-templates-2nd-preview)
- [Class-File API (Preview)](#class-file-api-preview)

---

## Unnamed Variables and Patterns (Standard)

Unnamed variables (`_`) are now standard in Java 22.

```java
import java.util.*;

public class UnnamedVariablesStandard {
    public static void main(String[] args) {
        // In enhanced for loop - count without using elements
        var list = List.of("a", "b", "c", "d", "e");
        int count = 0;
        for (var _ : list) {
            count++;
        }
        System.out.println("Count: " + count);

        // Multiple unnamed in same scope
        for (var _ : list) {
            for (var _ : list) {
                // Both _ are valid
            }
        }

        // In try-with-resources
        try (var _ = ScopedContext.open()) {
            System.out.println("Context is open");
        }

        // In catch blocks
        try {
            int x = Integer.parseInt("invalid");
        } catch (NumberFormatException _) {
            System.out.println("Failed to parse");
        }

        // In lambda parameters
        BiFunction<String, Integer, String> ignoreSecond =
            (s, _) -> s.toUpperCase();
        System.out.println(ignoreSecond.apply("hello", 42));

        // Multiple unnamed in lambda
        TriFunction<String, Integer, Double, String> ignoreAll =
            (_, _, _) -> "ignored all parameters";

        // In pattern matching
        testPatterns();
    }

    interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    record Point(int x, int y, int z) {}

    static void testPatterns() {
        Object obj = new Point(1, 2, 3);

        // Unnamed in type pattern (when you only care about type)
        if (obj instanceof Point _) {
            System.out.println("It's a Point");
        }

        // Unnamed in record pattern components
        if (obj instanceof Point(int x, _, int z)) {
            System.out.println("x=" + x + ", z=" + z);  // y ignored
        }

        // In switch
        sealed interface Shape permits Circle, Square, Rectangle {}
        record Circle(double r) implements Shape {}
        record Square(double side) implements Shape {}
        record Rectangle(double w, double h) implements Shape {}

        Shape shape = new Rectangle(10, 20);

        double area = switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Square(double s) -> s * s;
            case Rectangle(double w, _) when w > 100 -> w * 10; // Special case
            case Rectangle(double w, double h) -> w * h;
        };
        System.out.println("Area: " + area);
    }

    // Helper class
    static class ScopedContext implements AutoCloseable {
        static ScopedContext open() {
            return new ScopedContext();
        }
        public void close() {
            System.out.println("Context closed");
        }
    }
}
```

---

## Statements Before super() (Preview)

Java 22 allows statements before the `super()` or `this()` call in constructors.

> Note: Enable with `--enable-preview`

```java
public class StatementsBeforeSuper {
    public static void main(String[] args) {
        // Validation before super
        var pos = new PositiveNumber(42);
        System.out.println("Value: " + pos.getValue());

        try {
            new PositiveNumber(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Complex initialization
        var config = new ServerConfig("localhost:8080");
        System.out.println("Host: " + config.getHost());
        System.out.println("Port: " + config.getPort());
    }
}

class Number {
    private final int value;

    Number(int value) {
        this.value = value;
    }

    int getValue() { return value; }
}

class PositiveNumber extends Number {
    // Before Java 22: Could not validate before super()

    // Java 22: Can have statements before super()
    PositiveNumber(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Value must be positive: " + value);
        }
        super(value);  // Now after validation
    }
}

class Server {
    private final String host;
    private final int port;

    Server(String host, int port) {
        this.host = host;
        this.port = port;
    }

    String getHost() { return host; }
    int getPort() { return port; }
}

class ServerConfig extends Server {
    // Parse before calling super
    ServerConfig(String hostPort) {
        // Statements before super()
        String[] parts = hostPort.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        // Now call super with parsed values
        super(host, port);
    }
}
```

### Record Validation

```java
public class RecordValidation {
    // Record with complex validation
    record Range(int start, int end) {
        Range {
            // Compact constructor can already validate
            // But sometimes you need to compute values first
            if (start > end) {
                throw new IllegalArgumentException("start > end");
            }
        }
    }

    // Subclass with pre-super validation
    static class ValidatedRange extends Number {
        ValidatedRange(int start, int end) {
            // Validate before super
            if (start > end) {
                int temp = start;
                start = end;
                end = temp;
            }
            super(end - start);  // Pass range size to super
        }
    }
}
```

---

## Foreign Function & Memory API (Standard)

The Foreign Function & Memory API is now a standard feature.

```java
import java.lang.foreign.*;
import java.lang.invoke.*;

public class ForeignFunctionStandard {
    public static void main(String[] args) throws Throwable {
        // Memory allocation
        memoryDemo();

        // Calling native functions
        nativeFunctionDemo();
    }

    static void memoryDemo() {
        // Confined arena - memory freed when closed
        try (Arena arena = Arena.ofConfined()) {
            // Allocate native memory
            MemorySegment segment = arena.allocate(100);
            System.out.println("Allocated " + segment.byteSize() + " bytes");

            // Write data
            segment.set(ValueLayout.JAVA_INT, 0, 42);
            segment.set(ValueLayout.JAVA_INT, 4, 100);

            // Read data
            int val1 = segment.get(ValueLayout.JAVA_INT, 0);
            int val2 = segment.get(ValueLayout.JAVA_INT, 4);
            System.out.println("Values: " + val1 + ", " + val2);

            // Allocate string
            MemorySegment str = arena.allocateFrom("Hello, Native!");
            System.out.println("String: " + str.getString(0));

            // Allocate array
            MemorySegment array = arena.allocateFrom(ValueLayout.JAVA_INT,
                1, 2, 3, 4, 5);
            for (int i = 0; i < 5; i++) {
                System.out.print(array.getAtIndex(ValueLayout.JAVA_INT, i) + " ");
            }
            System.out.println();
        }
        // Memory automatically freed here
    }

    static void nativeFunctionDemo() throws Throwable {
        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = linker.defaultLookup();

        // Find strlen
        MemorySegment strlenAddr = lookup.find("strlen").orElseThrow();
        MethodHandle strlen = linker.downcallHandle(
            strlenAddr,
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment str = arena.allocateFrom("Hello!");
            long len = (long) strlen.invoke(str);
            System.out.println("strlen(\"Hello!\") = " + len);
        }

        // Find and call abs
        MemorySegment absAddr = lookup.find("abs").orElseThrow();
        MethodHandle abs = linker.downcallHandle(
            absAddr,
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );

        int result = (int) abs.invoke(-42);
        System.out.println("abs(-42) = " + result);
    }
}
```

### Struct Handling

```java
import java.lang.foreign.*;

public class StructDemo {
    public static void main(String[] args) {
        // Define a struct layout
        StructLayout pointLayout = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("x"),
            ValueLayout.JAVA_INT.withName("y")
        );

        VarHandle xHandle = pointLayout.varHandle(
            MemoryLayout.PathElement.groupElement("x"));
        VarHandle yHandle = pointLayout.varHandle(
            MemoryLayout.PathElement.groupElement("y"));

        try (Arena arena = Arena.ofConfined()) {
            // Allocate struct
            MemorySegment point = arena.allocate(pointLayout);

            // Set fields
            xHandle.set(point, 0L, 10);
            yHandle.set(point, 0L, 20);

            // Read fields
            int x = (int) xHandle.get(point, 0L);
            int y = (int) yHandle.get(point, 0L);
            System.out.println("Point: (" + x + ", " + y + ")");

            // Array of structs
            MemorySegment points = arena.allocate(pointLayout, 3);
            for (int i = 0; i < 3; i++) {
                long offset = i * pointLayout.byteSize();
                xHandle.set(points, offset, i * 10);
                yHandle.set(points, offset, i * 20);
            }

            System.out.println("Points array:");
            for (int i = 0; i < 3; i++) {
                long offset = i * pointLayout.byteSize();
                System.out.printf("  [%d] = (%d, %d)%n", i,
                    (int) xHandle.get(points, offset),
                    (int) yHandle.get(points, offset));
            }
        }
    }
}
```

---

## Stream Gatherers (Preview)

Stream Gatherers allow custom intermediate operations.

```java
import java.util.*;
import java.util.stream.*;

public class StreamGatherersDemo {
    public static void main(String[] args) {
        // Built-in gatherers (preview)

        // windowFixed - create fixed-size windows
        List<List<Integer>> windows = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9)
            .gather(Gatherers.windowFixed(3))
            .toList();
        System.out.println("Windows of 3: " + windows);
        // [[1, 2, 3], [4, 5, 6], [7, 8, 9]]

        // windowSliding - create sliding windows
        List<List<Integer>> sliding = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.windowSliding(3))
            .toList();
        System.out.println("Sliding windows: " + sliding);
        // [[1, 2, 3], [2, 3, 4], [3, 4, 5]]

        // fold - like reduce but with intermediate results
        List<Integer> runningSum = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.fold(() -> 0, Integer::sum))
            .toList();
        System.out.println("Running sum: " + runningSum);
        // [1, 3, 6, 10, 15]

        // scan - similar to fold but different behavior
        List<Integer> scanned = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.scan(() -> 0, Integer::sum))
            .toList();
        System.out.println("Scanned: " + scanned);

        // mapConcurrent - map with concurrency control
        List<String> results = Stream.of("a", "b", "c", "d")
            .gather(Gatherers.mapConcurrent(2, s -> {
                // Process up to 2 elements concurrently
                return s.toUpperCase();
            }))
            .toList();
        System.out.println("Concurrent map: " + results);
    }
}
```

### Custom Gatherers

```java
import java.util.*;
import java.util.stream.*;

public class CustomGatherers {
    public static void main(String[] args) {
        // Custom gatherer: distinct by key
        List<Person> people = List.of(
            new Person("Alice", 30),
            new Person("Bob", 25),
            new Person("Alice", 35),  // Duplicate name
            new Person("Charlie", 28)
        );

        List<Person> distinctByName = people.stream()
            .gather(distinctBy(Person::name))
            .toList();
        System.out.println("Distinct by name: " + distinctByName);

        // Custom gatherer: batching with condition
        List<List<Integer>> batches = Stream.of(1, 2, 3, 100, 4, 5, 101, 6)
            .gather(batchUntil(n -> n > 50))
            .toList();
        System.out.println("Batched: " + batches);
    }

    record Person(String name, int age) {}

    // Custom gatherer: distinct by a key function
    static <T, K> Gatherer<T, ?, T> distinctBy(
            java.util.function.Function<T, K> keyExtractor) {
        return Gatherer.ofSequential(
            HashSet<K>::new,
            (state, element, downstream) -> {
                K key = keyExtractor.apply(element);
                if (state.add(key)) {
                    return downstream.push(element);
                }
                return true;
            }
        );
    }

    // Custom gatherer: batch until condition
    static <T> Gatherer<T, ?, List<T>> batchUntil(
            java.util.function.Predicate<T> endCondition) {
        return Gatherer.ofSequential(
            ArrayList<T>::new,
            (state, element, downstream) -> {
                if (endCondition.test(element)) {
                    if (!state.isEmpty()) {
                        downstream.push(new ArrayList<>(state));
                        state.clear();
                    }
                } else {
                    state.add(element);
                }
                return true;
            },
            (state, downstream) -> {
                if (!state.isEmpty()) {
                    downstream.push(state);
                }
            }
        );
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Unnamed Variables | Standard | `_` for unused variables |
| Statements Before super() | Preview | Code before constructor delegation |
| Foreign Function API | Standard | Native code interop |
| Stream Gatherers | Preview | Custom intermediate operations |
| String Templates | 2nd Preview | String interpolation |
| Class-File API | Preview | Read/write class files |

[← Java 21 Features](java-21.md) | [Java 23 Features →](java-23.md)
