# Java 14 Features Guide

**Release Date:** March 2020
**Type:** Feature Release

Java 14 finalized switch expressions and introduced records and pattern matching for instanceof as preview features.

## Table of Contents
- [Switch Expressions (Standard)](#switch-expressions-standard)
- [Records (Preview)](#records-preview)
- [Pattern Matching for instanceof (Preview)](#pattern-matching-for-instanceof-preview)
- [Helpful NullPointerExceptions](#helpful-nullpointerexceptions)
- [Text Blocks (2nd Preview)](#text-blocks-2nd-preview)

---

## Switch Expressions (Standard)

Switch expressions are now a standard feature in Java 14.

```java
public class SwitchExpressionsStandard {
    public static void main(String[] args) {
        // Arrow syntax (no fall-through)
        String day = "WEDNESDAY";
        int numLetters = switch (day) {
            case "MONDAY", "FRIDAY", "SUNDAY" -> 6;
            case "TUESDAY" -> 7;
            case "THURSDAY", "SATURDAY" -> 8;
            case "WEDNESDAY" -> 9;
            default -> throw new IllegalStateException("Invalid day: " + day);
        };
        System.out.println("Letters: " + numLetters);

        // With yield for complex blocks
        String message = switch (day) {
            case "MONDAY" -> "Start of work week";
            case "FRIDAY" -> {
                var greeting = "TGIF!";
                yield greeting + " Almost weekend!";
            }
            case "SATURDAY", "SUNDAY" -> "Weekend!";
            default -> "Midweek";
        };
        System.out.println("Message: " + message);

        // Traditional syntax with yield
        int value = 2;
        String result = switch (value) {
            case 1:
                yield "One";
            case 2:
                yield "Two";
            default:
                yield "Other";
        };
        System.out.println("Result: " + result);
    }
}
```

---

## Records (Preview)

Records are immutable data carriers with automatic implementations.

> Note: Enable with `--enable-preview`. Standard in Java 16.

### Basic Records

```java
// Simple record definition
record Point(int x, int y) {}

// Record with multiple fields
record Person(String name, int age, String email) {}

// Record is equivalent to:
// - final class
// - private final fields
// - public constructor
// - public accessors (name(), age(), email())
// - equals(), hashCode(), toString()

public class RecordsBasics {
    public static void main(String[] args) {
        // Create record instance
        Point p1 = new Point(10, 20);
        Point p2 = new Point(10, 20);

        // Accessor methods (not getX(), but x())
        System.out.println("X: " + p1.x());
        System.out.println("Y: " + p1.y());

        // Automatic toString()
        System.out.println("Point: " + p1);  // Point[x=10, y=20]

        // Automatic equals() and hashCode()
        System.out.println("Equal: " + p1.equals(p2));  // true
        System.out.println("HashCode equal: " + (p1.hashCode() == p2.hashCode()));

        // Person record
        Person person = new Person("Alice", 30, "alice@example.com");
        System.out.println("Person: " + person);
        System.out.println("Name: " + person.name());
        System.out.println("Age: " + person.age());

        // Immutability - no setters
        // person.age = 31;  // Compilation error!

        // Create modified copy manually
        Person olderPerson = new Person(person.name(), person.age() + 1, person.email());
        System.out.println("Older: " + olderPerson);
    }
}
```

### Compact Constructors

```java
// Validation with compact constructor
record Email(String address) {
    // Compact constructor - parameters implicit
    public Email {
        if (address == null || !address.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + address);
        }
        // No need for: this.address = address; (automatic)
        address = address.toLowerCase();  // Can modify before assignment
    }
}

record Range(int start, int end) {
    public Range {
        if (start > end) {
            throw new IllegalArgumentException(
                "Start must be <= end: " + start + " > " + end);
        }
    }

    // Additional methods
    public int length() {
        return end - start;
    }

    public boolean contains(int value) {
        return value >= start && value <= end;
    }
}

public class RecordConstructors {
    public static void main(String[] args) {
        // Validation happens automatically
        Email email = new Email("ALICE@Example.COM");
        System.out.println("Email: " + email.address());  // alice@example.com

        try {
            new Email("invalid");
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        Range range = new Range(0, 100);
        System.out.println("Range length: " + range.length());
        System.out.println("Contains 50: " + range.contains(50));
    }
}
```

### Records with Methods and Static Members

```java
record Rectangle(double width, double height) {
    // Static fields
    public static final Rectangle UNIT = new Rectangle(1, 1);

    // Instance methods
    public double area() {
        return width * height;
    }

    public double perimeter() {
        return 2 * (width + height);
    }

    public Rectangle scale(double factor) {
        return new Rectangle(width * factor, height * factor);
    }

    // Static factory methods
    public static Rectangle square(double side) {
        return new Rectangle(side, side);
    }

    // Override toString if needed
    @Override
    public String toString() {
        return String.format("Rectangle[%.2fx%.2f]", width, height);
    }
}

public class RecordMethods {
    public static void main(String[] args) {
        Rectangle rect = new Rectangle(10, 5);
        System.out.println("Rectangle: " + rect);
        System.out.println("Area: " + rect.area());
        System.out.println("Perimeter: " + rect.perimeter());

        Rectangle scaled = rect.scale(2);
        System.out.println("Scaled: " + scaled);

        Rectangle square = Rectangle.square(5);
        System.out.println("Square: " + square);

        System.out.println("Unit: " + Rectangle.UNIT);
    }
}
```

### Records with Interfaces

```java
interface Measurable {
    double measure();
}

record Circle(double radius) implements Measurable {
    @Override
    public double measure() {
        return Math.PI * radius * radius;
    }
}

record Square(double side) implements Measurable {
    @Override
    public double measure() {
        return side * side;
    }
}

sealed interface Shape permits CircleShape, RectangleShape, TriangleShape {}
record CircleShape(double radius) implements Shape {}
record RectangleShape(double width, double height) implements Shape {}
record TriangleShape(double base, double height) implements Shape {}

public class RecordInterfaces {
    public static void main(String[] args) {
        Measurable[] shapes = {
            new Circle(5),
            new Square(4)
        };

        for (Measurable shape : shapes) {
            System.out.println(shape + " area: " + shape.measure());
        }
    }
}
```

### Local Records

```java
import java.util.*;
import java.util.stream.*;

public class LocalRecords {
    public static void main(String[] args) {
        List<String> data = List.of(
            "Alice,30,NYC",
            "Bob,25,LA",
            "Charlie,35,Chicago"
        );

        // Local record inside method
        record Person(String name, int age, String city) {}

        List<Person> people = data.stream()
            .map(line -> {
                String[] parts = line.split(",");
                return new Person(parts[0], Integer.parseInt(parts[1]), parts[2]);
            })
            .collect(Collectors.toList());

        // Group by city using record
        Map<String, List<Person>> byCity = people.stream()
            .collect(Collectors.groupingBy(Person::city));

        byCity.forEach((city, persons) ->
            System.out.println(city + ": " + persons));

        // Statistics with local record
        record Stats(double avg, int min, int max) {}

        Stats ageStats = people.stream()
            .map(Person::age)
            .collect(Collectors.teeing(
                Collectors.averagingInt(i -> i),
                Collectors.teeing(
                    Collectors.minBy(Integer::compare),
                    Collectors.maxBy(Integer::compare),
                    (min, max) -> new int[]{min.orElse(0), max.orElse(0)}
                ),
                (avg, minMax) -> new Stats(avg, minMax[0], minMax[1])
            ));
        System.out.println("Age stats: " + ageStats);
    }
}
```

---

## Pattern Matching for instanceof (Preview)

Pattern matching eliminates the need for explicit casting after instanceof.

> Note: Enable with `--enable-preview`. Standard in Java 16.

### Basic Pattern Matching

```java
public class PatternMatchingInstanceof {
    public static void main(String[] args) {
        Object obj = "Hello, World!";

        // Before Java 14
        if (obj instanceof String) {
            String s = (String) obj;
            System.out.println("Length: " + s.length());
        }

        // Java 14+ with pattern matching
        if (obj instanceof String s) {
            // s is already cast to String
            System.out.println("Length: " + s.length());
            System.out.println("Upper: " + s.toUpperCase());
        }

        // Pattern variable scope
        Object value = 42;
        if (value instanceof Integer i) {
            System.out.println("Integer: " + i);
        } else if (value instanceof String s) {
            System.out.println("String: " + s);
        } else if (value instanceof Double d) {
            System.out.println("Double: " + d);
        }
    }
}
```

### Pattern Matching in Conditions

```java
public class PatternMatchingConditions {
    public static void main(String[] args) {
        Object obj = "Hello";

        // Combined with other conditions
        if (obj instanceof String s && s.length() > 3) {
            System.out.println("Long string: " + s);
        }

        // In negative check (variable not in scope after)
        if (!(obj instanceof String s)) {
            System.out.println("Not a string");
            return;
        }
        // s is in scope here because of early return
        System.out.println("String: " + s);

        // Multiple patterns
        Object[] items = {"text", 123, 45.67, true, null};
        for (Object item : items) {
            String description;
            if (item instanceof String s) {
                description = "String of length " + s.length();
            } else if (item instanceof Integer i) {
                description = "Integer: " + (i > 100 ? "large" : "small");
            } else if (item instanceof Double d) {
                description = "Double: " + String.format("%.2f", d);
            } else if (item instanceof Boolean b) {
                description = "Boolean: " + (b ? "yes" : "no");
            } else {
                description = "Unknown or null";
            }
            System.out.println(item + " -> " + description);
        }
    }
}
```

### Pattern Matching with equals()

```java
record Point(int x, int y) {}

public class PatternMatchingEquals {
    public static void main(String[] args) {
        Point p1 = new Point(10, 20);
        Point p2 = new Point(10, 20);

        // Traditional equals check with instanceof
        System.out.println("Equal: " + equals(p1, p2));
    }

    // Using pattern matching in equals
    static boolean equals(Object a, Object b) {
        if (a == b) return true;
        if (a == null || b == null) return false;

        // Pattern matching makes this cleaner
        if (a instanceof Point p1 && b instanceof Point p2) {
            return p1.x() == p2.x() && p1.y() == p2.y();
        }

        return a.equals(b);
    }
}
```

---

## Helpful NullPointerExceptions

Java 14 provides detailed NullPointerException messages.

```java
public class HelpfulNPE {
    record Address(String city) {}
    record Person(String name, Address address) {}
    record Company(Person ceo) {}

    public static void main(String[] args) {
        // Enable with: -XX:+ShowCodeDetailsInExceptionMessages
        // (default on in Java 15+)

        Company company = new Company(new Person("John", null));

        try {
            // This will throw NPE with helpful message
            String city = company.ceo().address().city();
        } catch (NullPointerException e) {
            System.out.println("Exception: " + e.getMessage());
            // Output: Cannot invoke "Address.city()" because the return value
            // of "Person.address()" is null
        }

        // Another example
        String[] names = null;
        try {
            int len = names.length;
        } catch (NullPointerException e) {
            System.out.println("Exception: " + e.getMessage());
            // Output: Cannot read the array length because "names" is null
        }

        // Chained calls
        int[][] matrix = new int[3][];
        try {
            int value = matrix[0][0];
        } catch (NullPointerException e) {
            System.out.println("Exception: " + e.getMessage());
            // Output: Cannot load from int array because "matrix[0]" is null
        }
    }
}
```

---

## Text Blocks (2nd Preview)

Java 14 added new escape sequences to text blocks.

```java
public class TextBlocks2ndPreview {
    public static void main(String[] args) {
        // New: \s (space) - preserves trailing whitespace
        String withTrailing = """
                Line 1\s
                Line 2\s
                Line 3
                """;
        System.out.println("With \\s:");
        withTrailing.lines().forEach(l -> System.out.println("|" + l + "|"));

        // New: \ at end of line - suppresses line terminator
        String singleLine = """
                This is a very long line that \
                would normally span multiple \
                lines in source code but \
                appears as one line in output.""";
        System.out.println("\nSingle line:");
        System.out.println(singleLine);

        // Combining both
        String formatted = """
                Name:  Alice\s\s\s
                Email: alice@example.com\s\s\s
                Phone: +1-555-0123\s\s\s
                """;
        System.out.println("\nFormatted (with trailing spaces):");
        formatted.lines().forEach(l -> System.out.println("|" + l + "|"));

        // Long SQL query without line breaks
        String sql = """
                SELECT u.id, u.name, u.email, \
                       o.order_id, o.total, o.status \
                FROM users u \
                JOIN orders o ON u.id = o.user_id \
                WHERE o.status = 'PENDING' \
                ORDER BY o.created_at DESC""";
        System.out.println("\nSQL (single line):");
        System.out.println(sql);
    }
}
```

---

## Hands-On Exercise: Records and Patterns

```java
import java.util.*;

public class RecordsPatternExercise {
    // Define records for a simple e-commerce domain
    sealed interface Product permits Book, Electronics, Clothing {}

    record Book(String isbn, String title, String author, double price)
        implements Product {}

    record Electronics(String sku, String name, String brand, double price)
        implements Product {}

    record Clothing(String sku, String name, String size, double price)
        implements Product {}

    record CartItem(Product product, int quantity) {}

    record Cart(List<CartItem> items) {
        public double total() {
            return items.stream()
                .mapToDouble(item -> getPrice(item.product()) * item.quantity())
                .sum();
        }

        private static double getPrice(Product product) {
            if (product instanceof Book b) {
                return b.price();
            } else if (product instanceof Electronics e) {
                return e.price();
            } else if (product instanceof Clothing c) {
                return c.price();
            }
            return 0;
        }
    }

    public static void main(String[] args) {
        // Create products
        var book = new Book("978-0-13-468599-1", "Effective Java", "Joshua Bloch", 45.00);
        var laptop = new Electronics("LAP-001", "MacBook Pro", "Apple", 1999.00);
        var shirt = new Clothing("CLO-001", "T-Shirt", "M", 25.00);

        // Create cart
        var items = List.of(
            new CartItem(book, 2),
            new CartItem(laptop, 1),
            new CartItem(shirt, 3)
        );
        var cart = new Cart(items);

        // Display cart
        System.out.println("Shopping Cart:");
        System.out.println("-".repeat(50));
        for (var item : cart.items()) {
            String description = getDescription(item.product());
            double subtotal = getSubtotal(item);
            System.out.printf("%-30s x%d = $%.2f%n",
                description, item.quantity(), subtotal);
        }
        System.out.println("-".repeat(50));
        System.out.printf("Total: $%.2f%n", cart.total());
    }

    static String getDescription(Product product) {
        if (product instanceof Book b) {
            return b.title() + " by " + b.author();
        } else if (product instanceof Electronics e) {
            return e.brand() + " " + e.name();
        } else if (product instanceof Clothing c) {
            return c.name() + " (" + c.size() + ")";
        }
        return "Unknown";
    }

    static double getSubtotal(CartItem item) {
        Product p = item.product();
        double price = 0;
        if (p instanceof Book b) price = b.price();
        else if (p instanceof Electronics e) price = e.price();
        else if (p instanceof Clothing c) price = c.price();
        return price * item.quantity();
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Switch Expressions | Standard | Switch as expression with arrow/yield |
| Records | Preview | Immutable data carriers |
| Pattern Matching instanceof | Preview | Type patterns in instanceof |
| Helpful NPE | Standard | Detailed null pointer messages |
| Text Blocks | 2nd Preview | New escape sequences: `\s`, `\` |

[← Java 13 Features](java-13.md) | [Java 15 Features →](java-15.md)
