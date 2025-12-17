# Java 23 Features Guide

**Release Date:** September 2024
**Type:** Feature Release

Java 23 introduced Markdown documentation comments and primitive types in patterns as preview.

## Table of Contents
- [Markdown Documentation Comments (Standard)](#markdown-documentation-comments-standard)
- [Primitive Types in Patterns (Preview)](#primitive-types-in-patterns-preview)
- [Module Import Declarations (Preview)](#module-import-declarations-preview)
- [Implicitly Declared Classes (2nd Preview)](#implicitly-declared-classes-2nd-preview)
- [Structured Concurrency (3rd Preview)](#structured-concurrency-3rd-preview)
- [Stream Gatherers (2nd Preview)](#stream-gatherers-2nd-preview)

---

## Markdown Documentation Comments (Standard)

Java 23 allows Markdown syntax in JavaDoc comments.

```java
/// A utility class for mathematical operations.
///
/// ## Features
///
/// - Basic arithmetic
/// - Statistical functions
/// - Geometric calculations
///
/// ## Usage Example
///
/// ```java
/// MathUtils utils = new MathUtils();
/// double result = utils.average(1, 2, 3, 4, 5);
/// System.out.println(result); // 3.0
/// ```
///
/// @see java.lang.Math
public class MathUtils {

    /// Calculates the average of the given numbers.
    ///
    /// The formula used is:
    ///
    /// $$\bar{x} = \frac{\sum_{i=1}^{n} x_i}{n}$$
    ///
    /// ### Parameters
    ///
    /// | Parameter | Description |
    /// |-----------|-------------|
    /// | numbers   | Array of numbers to average |
    ///
    /// ### Example
    ///
    /// ```java
    /// double avg = average(1.0, 2.0, 3.0);
    /// assert avg == 2.0;
    /// ```
    ///
    /// @param numbers the numbers to average
    /// @return the arithmetic mean
    /// @throws IllegalArgumentException if no numbers provided
    public double average(double... numbers) {
        if (numbers.length == 0) {
            throw new IllegalArgumentException("At least one number required");
        }
        double sum = 0;
        for (double n : numbers) {
            sum += n;
        }
        return sum / numbers.length;
    }

    /// Checks if a number is prime.
    ///
    /// A prime number is a natural number greater than 1 that has no
    /// positive divisors other than 1 and itself.
    ///
    /// **Time Complexity**: O(√n)
    ///
    /// @param n the number to check
    /// @return `true` if the number is prime, `false` otherwise
    public boolean isPrime(int n) {
        if (n <= 1) return false;
        if (n <= 3) return true;
        if (n % 2 == 0 || n % 3 == 0) return false;
        for (int i = 5; i * i <= n; i += 6) {
            if (n % i == 0 || n % (i + 2) == 0) return false;
        }
        return true;
    }

    /// Calculates the factorial of n.
    ///
    /// > **Note**: This method uses iteration, not recursion,
    /// > for better performance with large numbers.
    ///
    /// The factorial is defined as:
    /// - 0! = 1
    /// - n! = n × (n-1)!
    ///
    /// @param n non-negative integer
    /// @return n factorial
    /// @throws IllegalArgumentException if n is negative
    public long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        long result = 1;
        for (int i = 2; i <= n; i++) {
            result *= i;
        }
        return result;
    }
}
```

### Markdown Features in JavaDoc

```java
/// # Main Heading
///
/// ## Subheading
///
/// ### Sub-subheading
///
/// Normal paragraph with **bold**, *italic*, and `code`.
///
/// - Bullet list item 1
/// - Bullet list item 2
///   - Nested item
///
/// 1. Numbered list item 1
/// 2. Numbered list item 2
///
/// > Blockquote for important notes
///
/// Inline code: `System.out.println("Hello")`
///
/// Code block:
/// ```java
/// public class Example {
///     public static void main(String[] args) {
///         System.out.println("Hello, World!");
///     }
/// }
/// ```
///
/// Links: [Java Docs](https://docs.oracle.com/en/java/)
///
/// | Column 1 | Column 2 | Column 3 |
/// |----------|----------|----------|
/// | Value 1  | Value 2  | Value 3  |
/// | Value 4  | Value 5  | Value 6  |
///
public class MarkdownDocDemo {
}
```

---

## Primitive Types in Patterns (Preview)

Java 23 allows primitive types in patterns, enabling exhaustive matching.

```java
public class PrimitivePatterns {
    public static void main(String[] args) {
        // Primitive type patterns in switch
        testPrimitiveSwitch(42);
        testPrimitiveSwitch(3.14f);
        testPrimitiveSwitch(100L);
        testPrimitiveSwitch((byte) 10);

        // Boolean patterns
        testBooleanPattern(true);
        testBooleanPattern(false);

        // Record patterns with primitives
        testRecordWithPrimitives();
    }

    static void testPrimitiveSwitch(Object obj) {
        String result = switch (obj) {
            case Integer i when i > 100 -> "Large integer: " + i;
            case Integer i -> "Integer: " + i;
            case Long l -> "Long: " + l;
            case Float f -> "Float: " + f;
            case Double d -> "Double: " + d;
            case Byte b -> "Byte: " + b;
            case Short s -> "Short: " + s;
            case Character c -> "Character: " + c;
            default -> "Other: " + obj;
        };
        System.out.println(result);
    }

    static void testBooleanPattern(boolean flag) {
        // Exhaustive switch on boolean
        String result = switch (flag) {
            case true -> "It's true!";
            case false -> "It's false!";
        };
        System.out.println(result);
    }

    record Measurement(String name, double value) {}

    static void testRecordWithPrimitives() {
        Object[] measurements = {
            new Measurement("temperature", 98.6),
            new Measurement("distance", 42.0),
            new Measurement("speed", 0.0)
        };

        for (Object m : measurements) {
            String desc = switch (m) {
                case Measurement(String name, double v) when v == 0.0 ->
                    name + " is zero";
                case Measurement(String name, double v) when v > 50 ->
                    name + " is high: " + v;
                case Measurement(String name, double v) ->
                    name + " is normal: " + v;
                default -> "Unknown";
            };
            System.out.println(desc);
        }
    }
}
```

### Primitive Patterns with Guards

```java
public class PrimitivePatternGuards {
    public static void main(String[] args) {
        // Integer ranges
        for (int i : new int[]{-10, 0, 50, 100, 150}) {
            System.out.println(i + " -> " + categorize(i));
        }

        // Double comparisons
        for (double d : new double[]{-1.5, 0.0, 0.5, 1.0, 100.0}) {
            System.out.println(d + " -> " + describeDouble(d));
        }
    }

    static String categorize(int n) {
        return switch (n) {
            case int i when i < 0 -> "Negative";
            case int i when i == 0 -> "Zero";
            case int i when i <= 100 -> "Normal";
            case int i -> "High";  // Exhaustive
        };
    }

    static String describeDouble(double d) {
        return switch (d) {
            case double x when x < 0 -> "Negative";
            case double x when x == 0 -> "Zero";
            case double x when x <= 1 -> "Fraction";
            case double x when Double.isNaN(x) -> "Not a number";
            case double x when Double.isInfinite(x) -> "Infinite";
            case double x -> "Regular positive";
        };
    }
}
```

---

## Module Import Declarations (Preview)

Import all exported packages from a module with a single declaration.

```java
// Import all from java.base module
import module java.base;

// Now can use classes without individual imports:
// - java.util.List
// - java.io.File
// - java.time.LocalDate
// etc.

public class ModuleImportDemo {
    public static void main(String[] args) {
        // All these work without separate imports
        List<String> list = List.of("a", "b", "c");
        LocalDate today = LocalDate.now();
        Map<String, Integer> map = Map.of("one", 1);

        System.out.println("List: " + list);
        System.out.println("Today: " + today);
        System.out.println("Map: " + map);
    }
}
```

---

## Implicitly Declared Classes (2nd Preview)

Simplified main class declaration for simple programs.

```java
// File: Hello.java
// No class declaration, no public, no static
void main() {
    System.out.println("Hello, World!");
}

// File: Greeting.java
// Instance fields and methods
String greeting = "Hello";

void main(String[] args) {
    var name = args.length > 0 ? args[0] : "World";
    println(STR."\{greeting}, \{name}!");
}

void println(Object obj) {
    System.out.println(obj);
}

// File: Calculator.java
// More complex example
int result = 0;

void main() {
    add(10);
    add(20);
    multiply(2);
    println("Result: " + result);
}

void add(int n) {
    result += n;
}

void multiply(int n) {
    result *= n;
}

void println(Object obj) {
    System.out.println(obj);
}
```

---

## Structured Concurrency (3rd Preview)

Continued refinements to structured concurrency.

```java
import java.util.concurrent.*;

public class StructuredConcurrency3 {
    record Product(int id, String name, double price) {}
    record Review(int productId, int rating, String comment) {}
    record Stock(int productId, int quantity) {}
    record ProductDetails(Product product, List<Review> reviews, Stock stock) {}

    public static void main(String[] args) throws Exception {
        ProductDetails details = fetchProductDetails(1);
        System.out.println("Product: " + details.product());
        System.out.println("Reviews: " + details.reviews().size());
        System.out.println("Stock: " + details.stock().quantity());
    }

    static ProductDetails fetchProductDetails(int productId) throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var productTask = scope.fork(() -> fetchProduct(productId));
            var reviewsTask = scope.fork(() -> fetchReviews(productId));
            var stockTask = scope.fork(() -> fetchStock(productId));

            scope.join().throwIfFailed();

            return new ProductDetails(
                productTask.get(),
                reviewsTask.get(),
                stockTask.get()
            );
        }
    }

    static Product fetchProduct(int id) throws Exception {
        Thread.sleep(100);
        return new Product(id, "Widget", 29.99);
    }

    static List<Review> fetchReviews(int productId) throws Exception {
        Thread.sleep(150);
        return List.of(
            new Review(productId, 5, "Great!"),
            new Review(productId, 4, "Good value")
        );
    }

    static Stock fetchStock(int productId) throws Exception {
        Thread.sleep(80);
        return new Stock(productId, 42);
    }
}
```

---

## Stream Gatherers (2nd Preview)

Refined Stream Gatherers with additional built-in gatherers.

```java
import java.util.*;
import java.util.stream.*;

public class StreamGatherers2 {
    public static void main(String[] args) {
        // Fixed-size windows
        var windows = Stream.of(1, 2, 3, 4, 5, 6, 7, 8)
            .gather(Gatherers.windowFixed(3))
            .toList();
        System.out.println("Fixed windows: " + windows);

        // Sliding windows
        var sliding = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.windowSliding(3))
            .toList();
        System.out.println("Sliding windows: " + sliding);

        // Fold with initial value
        var folded = Stream.of("a", "b", "c")
            .gather(Gatherers.fold(
                () -> "",
                (acc, s) -> acc + s
            ))
            .toList();
        System.out.println("Folded: " + folded);

        // Scan (running accumulation)
        var scanned = Stream.of(1, 2, 3, 4, 5)
            .gather(Gatherers.scan(
                () -> 0,
                Integer::sum
            ))
            .toList();
        System.out.println("Scanned: " + scanned);

        // Concurrent mapping with limit
        var concurrent = Stream.of("a", "b", "c", "d")
            .gather(Gatherers.mapConcurrent(2, String::toUpperCase))
            .toList();
        System.out.println("Concurrent: " + concurrent);
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Markdown Doc Comments | Standard | Use Markdown in `///` comments |
| Primitive Patterns | Preview | Primitives in pattern matching |
| Module Imports | Preview | `import module java.base;` |
| Implicitly Declared Classes | 2nd Preview | Simplified main programs |
| Structured Concurrency | 3rd Preview | Task coordination |
| Stream Gatherers | 2nd Preview | Custom intermediate operations |

[← Java 22 Features](java-22.md) | [Java 24 Features →](java-24.md)
