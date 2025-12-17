# Java 17 Features Guide

**Release Date:** September 2021
**Type:** LTS (Long-Term Support)

Java 17 is a major LTS release that finalized sealed classes and introduced pattern matching in switch as preview.

## Table of Contents
- [Sealed Classes (Standard)](#sealed-classes-standard)
- [Pattern Matching for switch (Preview)](#pattern-matching-for-switch-preview)
- [Enhanced Pseudo-Random Number Generators](#enhanced-pseudo-random-number-generators)
- [Context-Specific Deserialization Filters](#context-specific-deserialization-filters)
- [Deprecations and Removals](#deprecations-and-removals)
- [Restore Always-Strict Floating-Point](#restore-always-strict-floating-point)

---

## Sealed Classes (Standard)

Sealed classes are now a standard feature in Java 17.

### Complete Sealed Classes Guide

```java
// Sealed abstract class
public sealed abstract class Vehicle
    permits Car, Truck, Motorcycle {

    private final String brand;

    protected Vehicle(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    public abstract int wheels();
}

// Final subclass - cannot be extended
public final class Car extends Vehicle {
    private final int doors;

    public Car(String brand, int doors) {
        super(brand);
        this.doors = doors;
    }

    @Override
    public int wheels() {
        return 4;
    }

    public int getDoors() {
        return doors;
    }
}

// Sealed subclass - further restricts inheritance
public sealed class Truck extends Vehicle
    permits PickupTruck, SemiTruck {

    private final double cargoCapacity;

    public Truck(String brand, double cargoCapacity) {
        super(brand);
        this.cargoCapacity = cargoCapacity;
    }

    @Override
    public int wheels() {
        return 6;
    }

    public double getCargoCapacity() {
        return cargoCapacity;
    }
}

public final class PickupTruck extends Truck {
    public PickupTruck(String brand) {
        super(brand, 1000);
    }
}

public final class SemiTruck extends Truck {
    public SemiTruck(String brand) {
        super(brand, 20000);
    }
}

// Non-sealed - open for extension
public non-sealed class Motorcycle extends Vehicle {
    public Motorcycle(String brand) {
        super(brand);
    }

    @Override
    public int wheels() {
        return 2;
    }
}

// Anyone can extend non-sealed class
public class SportBike extends Motorcycle {
    public SportBike(String brand) {
        super(brand);
    }
}
```

### Sealed Interfaces with Records

```java
// Perfect combination: sealed interfaces + records
public sealed interface PaymentMethod
    permits CreditCard, DebitCard, BankTransfer, DigitalWallet {

    String getDescription();
    boolean validate();
}

public record CreditCard(String number, String expiry, String cvv)
    implements PaymentMethod {

    @Override
    public String getDescription() {
        return "Credit Card ending in " + number.substring(number.length() - 4);
    }

    @Override
    public boolean validate() {
        return number.length() == 16 && cvv.length() == 3;
    }
}

public record DebitCard(String number, String pin)
    implements PaymentMethod {

    @Override
    public String getDescription() {
        return "Debit Card ending in " + number.substring(number.length() - 4);
    }

    @Override
    public boolean validate() {
        return number.length() == 16 && pin.length() == 4;
    }
}

public record BankTransfer(String accountNumber, String routingNumber)
    implements PaymentMethod {

    @Override
    public String getDescription() {
        return "Bank Transfer to account " + accountNumber;
    }

    @Override
    public boolean validate() {
        return accountNumber.length() >= 8 && routingNumber.length() == 9;
    }
}

public record DigitalWallet(String provider, String email)
    implements PaymentMethod {

    @Override
    public String getDescription() {
        return provider + " (" + email + ")";
    }

    @Override
    public boolean validate() {
        return email.contains("@");
    }
}
```

### Using Sealed Hierarchies

```java
public class SealedClassesDemo {
    public static void main(String[] args) {
        // Process different payment methods
        PaymentMethod[] methods = {
            new CreditCard("1234567890123456", "12/25", "123"),
            new DebitCard("9876543210987654", "1234"),
            new BankTransfer("12345678", "021000021"),
            new DigitalWallet("PayPal", "user@example.com")
        };

        for (PaymentMethod method : methods) {
            System.out.printf("%s - Valid: %b%n",
                method.getDescription(),
                method.validate());
        }

        // Type-safe processing with pattern matching
        System.out.println("\nProcessing fees:");
        for (PaymentMethod method : methods) {
            double fee = calculateFee(method);
            System.out.printf("%s: $%.2f fee%n",
                method.getDescription(), fee);
        }
    }

    static double calculateFee(PaymentMethod method) {
        // Pattern matching with sealed types (Preview in Java 17)
        return switch (method) {
            case CreditCard cc -> 0.029 * 100; // 2.9%
            case DebitCard dc -> 0.015 * 100;  // 1.5%
            case BankTransfer bt -> 0.50;      // Flat $0.50
            case DigitalWallet dw -> 0.025 * 100; // 2.5%
        };
    }
}
```

---

## Pattern Matching for switch (Preview)

Java 17 introduced pattern matching in switch statements and expressions.

> Note: Enable with `--enable-preview`. Standard in Java 21.

### Type Patterns in Switch

```java
public class PatternMatchingSwitch {
    public static void main(String[] args) {
        Object[] values = {42, "Hello", 3.14, true, null, new int[]{1,2,3}};

        for (Object value : values) {
            String result = formatValue(value);
            System.out.println(result);
        }
    }

    static String formatValue(Object obj) {
        return switch (obj) {
            case Integer i -> "Integer: " + i;
            case String s -> "String: \"" + s + "\"";
            case Double d -> "Double: " + String.format("%.2f", d);
            case Boolean b -> "Boolean: " + (b ? "yes" : "no");
            case int[] arr -> "Int array of length " + arr.length;
            case null -> "null value";
            default -> "Unknown: " + obj.getClass().getSimpleName();
        };
    }
}
```

### Guarded Patterns

```java
public class GuardedPatterns {
    record Person(String name, int age) {}

    public static void main(String[] args) {
        Object[] items = {
            new Person("Alice", 25),
            new Person("Bob", 17),
            new Person("Charlie", 65),
            "Hello",
            42,
            -5
        };

        for (Object item : items) {
            System.out.println(categorize(item));
        }
    }

    static String categorize(Object obj) {
        return switch (obj) {
            // Guarded pattern with 'when' (was '&&' in preview)
            case Person p when p.age() >= 65 -> p.name() + " is a senior";
            case Person p when p.age() >= 18 -> p.name() + " is an adult";
            case Person p -> p.name() + " is a minor";

            case String s when s.length() > 10 -> "Long string";
            case String s -> "Short string: " + s;

            case Integer i when i > 0 -> "Positive: " + i;
            case Integer i when i < 0 -> "Negative: " + i;
            case Integer i -> "Zero";

            case null -> "null";
            default -> "Other: " + obj;
        };
    }
}
```

### Pattern Matching with Sealed Classes

```java
sealed interface Shape permits Circle, Rectangle, Triangle {}

record Circle(double radius) implements Shape {}
record Rectangle(double width, double height) implements Shape {}
record Triangle(double base, double height) implements Shape {}

public class SealedPatternMatching {
    public static void main(String[] args) {
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(10, 20),
            new Triangle(8, 6)
        };

        for (Shape shape : shapes) {
            double area = calculateArea(shape);
            System.out.printf("%s: area = %.2f%n",
                shape.getClass().getSimpleName(), area);
        }
    }

    // Exhaustive switch - no default needed for sealed types
    static double calculateArea(Shape shape) {
        return switch (shape) {
            case Circle c -> Math.PI * c.radius() * c.radius();
            case Rectangle r -> r.width() * r.height();
            case Triangle t -> 0.5 * t.base() * t.height();
            // No default needed - compiler knows all cases
        };
    }

    // With guards
    static String describe(Shape shape) {
        return switch (shape) {
            case Circle c when c.radius() > 10 -> "Large circle";
            case Circle c -> "Small circle";
            case Rectangle r when r.width() == r.height() -> "Square";
            case Rectangle r -> "Rectangle";
            case Triangle t -> "Triangle";
        };
    }
}
```

### Null Handling in Switch

```java
public class NullInSwitch {
    public static void main(String[] args) {
        String[] values = {"hello", "", null, "world"};

        for (String value : values) {
            System.out.println(describe(value));
        }
    }

    static String describe(String s) {
        return switch (s) {
            case null -> "null value";
            case String str when str.isEmpty() -> "empty string";
            case String str -> "string: " + str;
        };
    }

    // Combined null and default
    static String process(Object obj) {
        return switch (obj) {
            case String s -> "String: " + s;
            case Integer i -> "Integer: " + i;
            case null, default -> "null or unknown";
        };
    }
}
```

---

## Enhanced Pseudo-Random Number Generators

Java 17 introduced new random number generator interfaces and implementations.

```java
import java.util.random.*;

public class RandomGenerators {
    public static void main(String[] args) {
        // New RandomGenerator interface
        RandomGenerator random = RandomGenerator.getDefault();
        System.out.println("Default generator: " + random.getClass().getName());

        // List available algorithms
        System.out.println("\nAvailable algorithms:");
        RandomGeneratorFactory.all()
            .map(f -> f.name())
            .sorted()
            .forEach(name -> System.out.println("  " + name));

        // Create specific algorithm
        RandomGenerator xoroshiro = RandomGeneratorFactory
            .of("Xoroshiro128PlusPlus")
            .create();
        System.out.println("\nXoroshiro128PlusPlus:");
        for (int i = 0; i < 5; i++) {
            System.out.println("  " + xoroshiro.nextInt(100));
        }

        // Splittable generator (for parallel streams)
        SplittableGenerator splittable = RandomGeneratorFactory
            .<SplittableGenerator>of("L64X128MixRandom")
            .create();

        System.out.println("\nParallel random numbers:");
        splittable.splits(4)
            .forEach(rng -> System.out.println(
                "  Thread " + Thread.currentThread().getId() +
                ": " + rng.nextInt(100)));

        // Jumpable generator
        JumpableGenerator jumpable = RandomGeneratorFactory
            .<JumpableGenerator>of("Xoroshiro128PlusPlus")
            .create();

        System.out.println("\nJumpable sequences:");
        for (int i = 0; i < 3; i++) {
            System.out.println("  Sequence " + i + ": " + jumpable.nextInt(100));
            jumpable = jumpable.jump();
        }

        // Stream of random numbers
        System.out.println("\nStream of doubles:");
        random.doubles(5, 0, 1)
            .forEach(d -> System.out.printf("  %.4f%n", d));

        // Gaussian distribution
        System.out.println("\nGaussian distribution (mean=50, stddev=10):");
        for (int i = 0; i < 5; i++) {
            double value = random.nextGaussian(50, 10);
            System.out.printf("  %.2f%n", value);
        }
    }
}
```

### RandomGenerator Hierarchy

```java
import java.util.random.*;

public class RandomHierarchy {
    public static void main(String[] args) {
        // RandomGenerator - base interface
        // - StreamableGenerator - can produce streams of values
        // - SplittableGenerator - can split into multiple generators
        // - JumpableGenerator - can jump ahead in sequence
        // - LeapableGenerator - can make large jumps
        // - ArbitrarilyJumpableGenerator - can jump by any distance

        // Algorithms comparison
        System.out.println("Algorithm Properties:");
        RandomGeneratorFactory.all()
            .filter(f -> !f.name().equals("SecureRandom"))
            .limit(5)
            .forEach(factory -> {
                System.out.printf("  %s:%n", factory.name());
                System.out.printf("    Period: 2^%d%n", factory.stateBits());
                System.out.printf("    Splittable: %b%n", factory.isSplittable());
                System.out.printf("    Jumpable: %b%n", factory.isJumpable());
            });

        // Legacy random upgraded
        java.util.Random legacyRandom = new java.util.Random();
        // Now implements RandomGenerator interface
        double d = ((RandomGenerator) legacyRandom).nextDouble();
    }
}
```

---

## Context-Specific Deserialization Filters

Java 17 allows setting deserialization filters per-stream and context.

```java
import java.io.*;

public class DeserializationFilters {
    public static void main(String[] args) {
        // Global filter (set via property or API)
        ObjectInputFilter globalFilter = ObjectInputFilter.Config.createFilter(
            "maxdepth=5;maxrefs=100;maxbytes=1000000;!*"
        );

        // Filter factory for context-specific filters
        ObjectInputFilter.Config.setSerialFilterFactory((current, next) -> {
            // Merge current and next filters
            if (current == null) return next;
            if (next == null) return current;
            return ObjectInputFilter.merge(next, current);
        });

        // Example: Allow only specific classes
        ObjectInputFilter safeFilter = ObjectInputFilter.Config.createFilter(
            "java.lang.*;java.util.*;!*"
        );

        System.out.println("Filter created: " + safeFilter);

        // Test serialization/deserialization
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject("Safe String");
                oos.writeObject(java.util.List.of(1, 2, 3));
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                ois.setObjectInputFilter(safeFilter);

                Object obj1 = ois.readObject();
                Object obj2 = ois.readObject();

                System.out.println("Deserialized: " + obj1);
                System.out.println("Deserialized: " + obj2);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
```

---

## Deprecations and Removals

### Removed Features

```java
public class RemovedFeatures {
    public static void main(String[] args) {
        // Removed in Java 17:

        // 1. RMI Activation System removed
        // java.rmi.activation.* no longer exists

        // 2. Applet API deprecated for removal
        // @Deprecated(forRemoval = true)

        // 3. Security Manager deprecated for removal
        // System.setSecurityManager() deprecated

        // 4. AOT and JIT compiler removed (experimental)

        // Still available but deprecated:
        System.out.println("Security Manager deprecated: " +
            System.getSecurityManager()); // null and deprecated
    }
}
```

### Strongly Encapsulated JDK Internals

```java
public class EncapsulatedInternals {
    public static void main(String[] args) {
        // Internal APIs are strongly encapsulated by default
        // --illegal-access option removed in Java 17

        // Cannot access sun.misc.Unsafe directly (without --add-opens)
        // Cannot access internal packages

        // Use standard APIs instead:
        // - VarHandle instead of Unsafe for memory operations
        // - MethodHandles.Lookup for reflective access
        // - Foreign Memory API for native memory

        // Check module encapsulation
        Module javaBase = String.class.getModule();
        System.out.println("Module: " + javaBase.getName());
        System.out.println("Is open: " + javaBase.isOpen("java.lang"));
        System.out.println("Exports java.lang: " +
            javaBase.isExported("java.lang"));
    }
}
```

---

## Restore Always-Strict Floating-Point

Java 17 restored always-strict floating-point semantics.

```java
public class StrictFloatingPoint {
    public static void main(String[] args) {
        // Before Java 17: strictfp required for consistent results
        // Java 17+: All floating-point operations are strict by default

        // strictfp keyword still valid but has no effect
        // (for backwards compatibility)

        double a = 1e308;
        double b = 1.0001;
        double result = a * b / b;

        System.out.println("Result: " + result);
        System.out.println("Equals original: " + (result == a));

        // Benefits:
        // - Consistent behavior across platforms
        // - No unexpected differences between x87 and SSE
        // - Simpler code (no need for strictfp annotation)

        // This method would have been strictfp before Java 17:
        double compute(double x, double y) {
            return x * y + x / y - Math.sqrt(x * x + y * y);
        }
    }

    // strictfp has no effect in Java 17+ but compiles
    strictfp double oldStyle(double x) {
        return x * x;
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Sealed Classes | Standard | Restricted class hierarchies |
| Pattern Matching Switch | Preview | Type patterns in switch |
| Enhanced PRNGs | Standard | New random number APIs |
| Deserialization Filters | Standard | Context-specific filtering |
| Strong Encapsulation | Standard | JDK internals fully encapsulated |
| Strict FP | Standard | Always-strict floating-point |

---

## Hands-On Challenge

Build a complete domain model using sealed classes and pattern matching:

```java
// Challenge: Build an expression evaluator

public sealed interface Expr permits
    Literal, Variable, BinaryExpr, UnaryExpr, FunctionCall {}

record Literal(double value) implements Expr {}
record Variable(String name) implements Expr {}

sealed interface BinaryExpr extends Expr permits Add, Sub, Mul, Div, Pow {}
record Add(Expr left, Expr right) implements BinaryExpr {}
record Sub(Expr left, Expr right) implements BinaryExpr {}
record Mul(Expr left, Expr right) implements BinaryExpr {}
record Div(Expr left, Expr right) implements BinaryExpr {}
record Pow(Expr base, Expr exponent) implements BinaryExpr {}

sealed interface UnaryExpr extends Expr permits Neg, Sqrt {}
record Neg(Expr operand) implements UnaryExpr {}
record Sqrt(Expr operand) implements UnaryExpr {}

record FunctionCall(String name, java.util.List<Expr> args) implements Expr {}

// Implement:
// 1. evaluate(Expr expr, Map<String, Double> variables)
// 2. simplify(Expr expr) - simplify expressions
// 3. derivative(Expr expr, String variable) - compute derivative
// 4. toString(Expr expr) - pretty print
```

[← Java 16 Features](java-16.md) | [Java 18 Features →](java-18.md)
