# Java 15 Features Guide

**Release Date:** September 2020
**Type:** Feature Release

Java 15 made text blocks standard and introduced sealed classes as a preview feature.

## Table of Contents
- [Text Blocks (Standard)](#text-blocks-standard)
- [Sealed Classes (Preview)](#sealed-classes-preview)
- [Records (2nd Preview)](#records-2nd-preview)
- [Pattern Matching instanceof (2nd Preview)](#pattern-matching-instanceof-2nd-preview)
- [Hidden Classes](#hidden-classes)
- [Nashorn Removal](#nashorn-removal)
- [ZGC and Shenandoah Production Ready](#zgc-and-shenandoah-production-ready)

---

## Text Blocks (Standard)

Text blocks are now a standard feature in Java 15.

```java
public class TextBlocksStandard {
    public static void main(String[] args) {
        // Basic text block
        String html = """
                <!DOCTYPE html>
                <html>
                    <head>
                        <title>Java 15</title>
                    </head>
                    <body>
                        <h1>Text Blocks are Standard!</h1>
                    </body>
                </html>
                """;
        System.out.println(html);

        // JSON configuration
        String config = """
                {
                    "database": {
                        "host": "localhost",
                        "port": 5432,
                        "name": "myapp",
                        "pool": {
                            "min": 5,
                            "max": 20
                        }
                    },
                    "cache": {
                        "enabled": true,
                        "ttl": 3600
                    }
                }
                """;
        System.out.println(config);

        // Escape sequences
        String withEscapes = """
                Line continuation: \
                This continues on the same line.
                Trailing space preserved:\s
                Tab character:\there
                """;
        System.out.println(withEscapes);

        // String methods with text blocks
        String template = """
                Hello, %s!
                Today is %s.
                You have %d messages.
                """;
        String formatted = template.formatted("Alice", "Monday", 5);
        System.out.println(formatted);
    }
}
```

---

## Sealed Classes (Preview)

Sealed classes restrict which classes can extend or implement them.

> Note: Enable with `--enable-preview`. Standard in Java 17.

### Basic Sealed Classes

```java
// Sealed class with permitted subclasses
public sealed class Shape
    permits Circle, Rectangle, Triangle {

    public abstract double area();
}

// Final - cannot be extended further
public final class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

    public double radius() { return radius; }
}

// Final subclass
public final class Rectangle extends Shape {
    private final double width, height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

// Non-sealed - open for extension
public non-sealed class Triangle extends Shape {
    private final double base, height;

    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}

// Can extend non-sealed class
public class EquilateralTriangle extends Triangle {
    public EquilateralTriangle(double side) {
        super(side, side * Math.sqrt(3) / 2);
    }
}
```

### Sealed Interfaces

```java
// Sealed interface
public sealed interface Expression
    permits Constant, Variable, BinaryOp, UnaryOp {

    double evaluate();
}

public record Constant(double value) implements Expression {
    @Override
    public double evaluate() {
        return value;
    }
}

public record Variable(String name, double value) implements Expression {
    @Override
    public double evaluate() {
        return value;
    }
}

public sealed interface BinaryOp extends Expression
    permits Add, Subtract, Multiply, Divide {

    Expression left();
    Expression right();
}

public record Add(Expression left, Expression right) implements BinaryOp {
    @Override
    public double evaluate() {
        return left.evaluate() + right.evaluate();
    }
}

public record Subtract(Expression left, Expression right) implements BinaryOp {
    @Override
    public double evaluate() {
        return left.evaluate() - right.evaluate();
    }
}

public record Multiply(Expression left, Expression right) implements BinaryOp {
    @Override
    public double evaluate() {
        return left.evaluate() * right.evaluate();
    }
}

public record Divide(Expression left, Expression right) implements BinaryOp {
    @Override
    public double evaluate() {
        if (right.evaluate() == 0) throw new ArithmeticException("Division by zero");
        return left.evaluate() / right.evaluate();
    }
}

public sealed interface UnaryOp extends Expression
    permits Negate, Abs {

    Expression operand();
}

public record Negate(Expression operand) implements UnaryOp {
    @Override
    public double evaluate() {
        return -operand.evaluate();
    }
}

public record Abs(Expression operand) implements UnaryOp {
    @Override
    public double evaluate() {
        return Math.abs(operand.evaluate());
    }
}
```

### Using Sealed Classes

```java
public class SealedClassesDemo {
    public static void main(String[] args) {
        // Create shapes
        Shape circle = new Circle(5);
        Shape rectangle = new Rectangle(10, 20);
        Shape triangle = new Triangle(8, 6);

        Shape[] shapes = {circle, rectangle, triangle};

        for (Shape shape : shapes) {
            System.out.printf("%s: area = %.2f%n",
                shape.getClass().getSimpleName(),
                shape.area());
        }

        // Expression tree: (3 + 4) * 2
        Expression expr = new Multiply(
            new Add(new Constant(3), new Constant(4)),
            new Constant(2)
        );
        System.out.println("(3 + 4) * 2 = " + expr.evaluate());

        // More complex: abs(10 - 15) + 3
        Expression complex = new Add(
            new Abs(new Subtract(new Constant(10), new Constant(15))),
            new Constant(3)
        );
        System.out.println("abs(10 - 15) + 3 = " + complex.evaluate());
    }
}
```

### Benefits of Sealed Classes

```java
// 1. Exhaustiveness checking (with switch in Java 17+)
public class SealedBenefits {

    // Compiler knows all possible subtypes
    static String describe(Shape shape) {
        // With pattern matching (Java 17+)
        return switch (shape) {
            case Circle c -> "Circle with radius " + c.radius();
            case Rectangle r -> "Rectangle";
            case Triangle t -> "Triangle";
            // No default needed - compiler knows these are all options
        };
    }

    // 2. Controlled inheritance hierarchy
    // Only permitted classes can extend Shape
    // This is enforced at compile time

    // 3. Better domain modeling
    // Algebraic Data Types (ADTs) pattern
    sealed interface Result<T> permits Success, Failure {}
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {}

    public static void main(String[] args) {
        Result<Integer> result = new Success<>(42);

        String message = switch (result) {
            case Success<Integer> s -> "Got value: " + s.value();
            case Failure<Integer> f -> "Error: " + f.error();
        };
        System.out.println(message);
    }
}
```

---

## Records (2nd Preview)

Records continue as preview with refinements.

```java
// Records can implement sealed interfaces
public sealed interface JsonValue
    permits JsonString, JsonNumber, JsonBoolean, JsonNull, JsonArray, JsonObject {}

public record JsonString(String value) implements JsonValue {}
public record JsonNumber(double value) implements JsonValue {}
public record JsonBoolean(boolean value) implements JsonValue {}
public record JsonNull() implements JsonValue {}
public record JsonArray(java.util.List<JsonValue> elements) implements JsonValue {}
public record JsonObject(java.util.Map<String, JsonValue> properties) implements JsonValue {}

public class RecordsPreview2 {
    public static void main(String[] args) {
        // Build JSON structure
        JsonValue json = new JsonObject(java.util.Map.of(
            "name", new JsonString("Alice"),
            "age", new JsonNumber(30),
            "active", new JsonBoolean(true),
            "tags", new JsonArray(java.util.List.of(
                new JsonString("developer"),
                new JsonString("java")
            ))
        ));

        // Pretty print
        System.out.println(prettyPrint(json, 0));
    }

    static String prettyPrint(JsonValue value, int indent) {
        String spaces = "  ".repeat(indent);
        if (value instanceof JsonString s) {
            return "\"" + s.value() + "\"";
        } else if (value instanceof JsonNumber n) {
            return String.valueOf(n.value());
        } else if (value instanceof JsonBoolean b) {
            return String.valueOf(b.value());
        } else if (value instanceof JsonNull) {
            return "null";
        } else if (value instanceof JsonArray arr) {
            var sb = new StringBuilder("[\n");
            var elements = arr.elements();
            for (int i = 0; i < elements.size(); i++) {
                sb.append(spaces).append("  ");
                sb.append(prettyPrint(elements.get(i), indent + 1));
                if (i < elements.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append(spaces).append("]");
            return sb.toString();
        } else if (value instanceof JsonObject obj) {
            var sb = new StringBuilder("{\n");
            var entries = new java.util.ArrayList<>(obj.properties().entrySet());
            for (int i = 0; i < entries.size(); i++) {
                var entry = entries.get(i);
                sb.append(spaces).append("  \"").append(entry.getKey()).append("\": ");
                sb.append(prettyPrint(entry.getValue(), indent + 1));
                if (i < entries.size() - 1) sb.append(",");
                sb.append("\n");
            }
            sb.append(spaces).append("}");
            return sb.toString();
        }
        return "unknown";
    }
}
```

---

## Hidden Classes

Hidden classes are classes that cannot be used directly by bytecode of other classes.

```java
import java.lang.invoke.*;
import java.util.Base64;

public class HiddenClassDemo {
    public static void main(String[] args) throws Throwable {
        // Hidden classes are mainly for framework developers
        // They are used to define classes dynamically that:
        // - Cannot be discovered or used by other classes
        // - Can be unloaded independently of class loader
        // - Are not visible to JVM tools

        // Example: Creating a hidden class
        // This is typically done by frameworks like Mockito, Spring

        byte[] classBytes = generateClassBytes();

        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // Define hidden class
        MethodHandles.Lookup hiddenClassLookup = lookup.defineHiddenClass(
            classBytes,
            true,  // initialize
            MethodHandles.Lookup.ClassOption.NESTMATE
        );

        Class<?> hiddenClass = hiddenClassLookup.lookupClass();

        System.out.println("Hidden class: " + hiddenClass);
        System.out.println("Is hidden: " + hiddenClass.isHidden());
        System.out.println("Name contains /: " + hiddenClass.getName().contains("/"));

        // Hidden class names have a special format with "/"
        // They cannot be loaded by name using Class.forName()
    }

    static byte[] generateClassBytes() {
        // In real use, you'd generate bytecode dynamically
        // This is a placeholder - actual implementation would use ASM or similar
        return new byte[0]; // Simplified for demonstration
    }
}
```

---

## ZGC and Shenandoah Production Ready

Both garbage collectors are now production-ready.

```java
public class GCDemo {
    public static void main(String[] args) {
        // ZGC - Low latency GC
        // java -XX:+UseZGC MyApp

        // Shenandoah - Low pause time GC
        // java -XX:+UseShenandoahGC MyApp

        // Both target:
        // - Sub-millisecond pause times
        // - Pause times independent of heap size
        // - Suitable for heaps from 8MB to 16TB

        System.out.println("GC: " + java.lang.management.ManagementFactory
            .getGarbageCollectorMXBeans());

        // Memory info
        Runtime rt = Runtime.getRuntime();
        System.out.println("Max Memory: " + formatBytes(rt.maxMemory()));
        System.out.println("Total Memory: " + formatBytes(rt.totalMemory()));
        System.out.println("Free Memory: " + formatBytes(rt.freeMemory()));
    }

    static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}
```

---

## Nashorn Removal

The Nashorn JavaScript engine has been removed. Use GraalJS or other alternatives.

```java
// Nashorn is gone. For JavaScript execution:

// Option 1: GraalJS
// Add dependency: org.graalvm.js:js:21.0.0

// Option 2: Other engines
// - Rhino
// - J2V8

// Example with ScriptEngine (if GraalJS is on classpath)
import javax.script.*;

public class JavaScriptAlternative {
    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();

        // List available engines
        System.out.println("Available script engines:");
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            System.out.printf("  %s (%s) - %s%n",
                factory.getEngineName(),
                factory.getEngineVersion(),
                factory.getLanguageName());
        }

        // GraalJS example (if available)
        ScriptEngine engine = manager.getEngineByName("graal.js");
        if (engine != null) {
            Object result = engine.eval("1 + 2");
            System.out.println("1 + 2 = " + result);
        } else {
            System.out.println("GraalJS not available");
        }
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Text Blocks | Standard | Multi-line string literals |
| Sealed Classes | Preview | Restricted class hierarchies |
| Records | 2nd Preview | Immutable data carriers |
| Pattern Matching | 2nd Preview | Type patterns in instanceof |
| Hidden Classes | Standard | Dynamically generated non-discoverable classes |
| ZGC | Production | Low-latency garbage collector |
| Shenandoah | Production | Low-pause-time garbage collector |

[← Java 14 Features](java-14.md) | [Java 16 Features →](java-16.md)
