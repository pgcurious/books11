# Java 12 Features Guide

**Release Date:** March 2019
**Type:** Feature Release

Java 12 introduced Switch Expressions as a preview feature and several other improvements.

## Table of Contents
- [Switch Expressions (Preview)](#switch-expressions-preview)
- [String Methods](#string-methods)
- [Collectors Teeing](#collectors-teeing)
- [Compact Number Formatting](#compact-number-formatting)
- [Files.mismatch()](#filesmismatch)

---

## Switch Expressions (Preview)

Switch expressions allow switch to return a value using arrow syntax.

> Note: Enable with `--enable-preview` flag. Standard in Java 14.

### Traditional vs Expression Switch

```java
public class SwitchExpressionsPreview {
    public static void main(String[] args) {
        // Traditional switch statement
        String day = "MONDAY";
        String type1;
        switch (day) {
            case "MONDAY":
            case "TUESDAY":
            case "WEDNESDAY":
            case "THURSDAY":
            case "FRIDAY":
                type1 = "Weekday";
                break;
            case "SATURDAY":
            case "SUNDAY":
                type1 = "Weekend";
                break;
            default:
                type1 = "Unknown";
        }
        System.out.println("Traditional: " + type1);

        // Switch expression (Java 12 preview)
        String type2 = switch (day) {
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
            case "SATURDAY", "SUNDAY" -> "Weekend";
            default -> "Unknown";
        };
        System.out.println("Expression: " + type2);
    }
}
```

### Arrow Syntax

```java
public class SwitchArrowSyntax {
    public static void main(String[] args) {
        int dayNum = 3;

        // Arrow cases - no fall-through, no break needed
        String dayName = switch (dayNum) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Invalid";
        };
        System.out.println("Day: " + dayName);

        // Multiple labels per case
        String dayType = switch (dayNum) {
            case 1, 2, 3, 4, 5 -> "Weekday";
            case 6, 7 -> "Weekend";
            default -> "Invalid";
        };
        System.out.println("Type: " + dayType);

        // With blocks for complex logic
        String description = switch (dayNum) {
            case 1 -> {
                System.out.println("Start of week!");
                yield "Monday - Beginning";  // yield returns value from block
            }
            case 5 -> {
                System.out.println("Almost weekend!");
                yield "Friday - End";
            }
            default -> "Regular day";
        };
        System.out.println("Description: " + description);
    }
}
```

### Switch with Enums

```java
enum Status { PENDING, ACTIVE, COMPLETED, CANCELLED }

public class SwitchWithEnums {
    public static void main(String[] args) {
        Status status = Status.ACTIVE;

        // Switch expression with enum
        String message = switch (status) {
            case PENDING -> "Waiting to start";
            case ACTIVE -> "In progress";
            case COMPLETED -> "Finished successfully";
            case CANCELLED -> "Cancelled by user";
        };
        System.out.println(message);

        // Exhaustiveness check - compiler ensures all cases covered
        // If you remove a case, compiler will error

        int priority = switch (status) {
            case PENDING -> 3;
            case ACTIVE -> 1;
            case COMPLETED -> 4;
            case CANCELLED -> 5;
        };
        System.out.println("Priority: " + priority);
    }
}
```

### Hands-On Exercise: Switch Expressions

```java
public class SwitchExercise {
    record Order(String status, double amount) {}

    public static void main(String[] args) {
        // Exercise 1: Calculate discount based on customer tier
        String tier = "GOLD";

        double discount = switch (tier) {
            case "BRONZE" -> 0.05;
            case "SILVER" -> 0.10;
            case "GOLD" -> 0.15;
            case "PLATINUM" -> 0.20;
            default -> 0.0;
        };
        System.out.println("Discount: " + (discount * 100) + "%");

        // Exercise 2: Process order status with logging
        Order order = new Order("SHIPPED", 150.00);

        String notification = switch (order.status()) {
            case "NEW" -> {
                System.out.println("Processing new order");
                yield "Your order has been received";
            }
            case "PROCESSING" -> {
                System.out.println("Order in progress");
                yield "Your order is being prepared";
            }
            case "SHIPPED" -> {
                System.out.println("Order shipped");
                yield "Your order is on the way!";
            }
            case "DELIVERED" -> {
                System.out.println("Order completed");
                yield "Your order has been delivered";
            }
            default -> "Unknown status";
        };
        System.out.println("Notification: " + notification);

        // Exercise 3: Calculate shipping cost
        String region = "INTERNATIONAL";
        double weight = 2.5;

        double shippingCost = switch (region) {
            case "LOCAL" -> weight * 5;
            case "DOMESTIC" -> weight * 10;
            case "INTERNATIONAL" -> {
                double baseCost = weight * 20;
                double customsFee = 15;
                yield baseCost + customsFee;
            }
            default -> throw new IllegalArgumentException("Unknown region");
        };
        System.out.println("Shipping: $" + shippingCost);
    }
}
```

---

## String Methods

### indent()

```java
public class StringIndent {
    public static void main(String[] args) {
        String text = "Line 1\nLine 2\nLine 3";

        // Add indentation (positive number)
        System.out.println("Original:");
        System.out.println(text);

        System.out.println("\nIndented by 4:");
        System.out.println(text.indent(4));

        System.out.println("Indented by 8:");
        System.out.println(text.indent(8));

        // Remove indentation (negative number)
        String indented = "    Indented line\n    Another line";
        System.out.println("Before strip:");
        System.out.println(indented);
        System.out.println("After indent(-2):");
        System.out.println(indented.indent(-2));

        // indent() also normalizes line endings and adds trailing newline
        String noNewline = "test";
        System.out.println("With indent(0): '" + noNewline.indent(0) + "'");
        // Adds newline at end
    }
}
```

### transform()

```java
import java.util.function.*;

public class StringTransform {
    public static void main(String[] args) {
        // transform() applies a function to the string
        String result = "hello"
            .transform(s -> s + " world")
            .transform(String::toUpperCase)
            .transform(s -> s + "!");

        System.out.println(result);  // HELLO WORLD!

        // Useful for chaining operations
        String processed = "  User Input  "
            .transform(String::strip)
            .transform(String::toLowerCase)
            .transform(s -> s.replace(" ", "_"));

        System.out.println(processed);  // user_input

        // Can return different types
        Integer length = "hello".transform(String::length);
        System.out.println("Length: " + length);

        // Complex transformations
        record ProcessedText(String text, int wordCount) {}

        ProcessedText analysis = "The quick brown fox"
            .transform(s -> new ProcessedText(
                s.toUpperCase(),
                s.split("\\s+").length
            ));
        System.out.println("Text: " + analysis.text());
        System.out.println("Words: " + analysis.wordCount());
    }
}
```

---

## Collectors Teeing

`teeing` combines two collectors and merges their results.

```java
import java.util.*;
import java.util.stream.*;

public class CollectorTeeing {
    public static void main(String[] args) {
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Calculate sum and count in one pass
        record SumAndCount(long sum, long count) {
            double average() {
                return count == 0 ? 0 : (double) sum / count;
            }
        }

        SumAndCount result = numbers.stream()
            .collect(Collectors.teeing(
                Collectors.summingLong(i -> i),    // First collector
                Collectors.counting(),             // Second collector
                SumAndCount::new                   // Merger
            ));

        System.out.println("Sum: " + result.sum());
        System.out.println("Count: " + result.count());
        System.out.println("Average: " + result.average());

        // Find min and max simultaneously
        record MinMax(Optional<Integer> min, Optional<Integer> max) {}

        MinMax minMax = numbers.stream()
            .collect(Collectors.teeing(
                Collectors.minBy(Comparator.naturalOrder()),
                Collectors.maxBy(Comparator.naturalOrder()),
                MinMax::new
            ));

        System.out.println("Min: " + minMax.min().orElse(0));
        System.out.println("Max: " + minMax.max().orElse(0));

        // Practical example: Sales analysis
        record Sale(String product, double amount) {}
        List<Sale> sales = List.of(
            new Sale("Laptop", 999),
            new Sale("Phone", 699),
            new Sale("Tablet", 499),
            new Sale("Watch", 299),
            new Sale("Headphones", 199)
        );

        record SalesReport(double total, double average) {}

        SalesReport report = sales.stream()
            .map(Sale::amount)
            .collect(Collectors.teeing(
                Collectors.summingDouble(d -> d),
                Collectors.averagingDouble(d -> d),
                SalesReport::new
            ));

        System.out.println("Total Sales: $" + report.total());
        System.out.println("Average Sale: $" + report.average());
    }
}
```

---

## Compact Number Formatting

Format numbers in compact form (e.g., 1K, 1M, 1B).

```java
import java.text.*;
import java.util.*;

public class CompactNumberFormatting {
    public static void main(String[] args) {
        // Default locale
        NumberFormat shortFormat = NumberFormat.getCompactNumberInstance();
        NumberFormat longFormat = NumberFormat.getCompactNumberInstance(
            Locale.US, NumberFormat.Style.LONG);

        long[] numbers = {100, 1_000, 10_000, 100_000, 1_000_000,
                          1_000_000_000, 1_000_000_000_000L};

        System.out.println("Short format (US):");
        for (long num : numbers) {
            System.out.printf("  %,15d -> %s%n", num, shortFormat.format(num));
        }

        System.out.println("\nLong format (US):");
        for (long num : numbers) {
            System.out.printf("  %,15d -> %s%n", num, longFormat.format(num));
        }

        // Different locales
        System.out.println("\nDifferent locales for 1,000,000:");
        long million = 1_000_000;

        for (Locale locale : List.of(
                Locale.US, Locale.GERMANY, Locale.FRANCE,
                Locale.JAPAN, Locale.CHINA)) {
            NumberFormat fmt = NumberFormat.getCompactNumberInstance(
                locale, NumberFormat.Style.SHORT);
            System.out.printf("  %s: %s%n", locale.getDisplayCountry(), fmt.format(million));
        }

        // With decimal places
        NumberFormat precise = NumberFormat.getCompactNumberInstance();
        precise.setMinimumFractionDigits(1);
        precise.setMaximumFractionDigits(2);

        System.out.println("\nWith decimals:");
        System.out.println("  1,234 -> " + precise.format(1_234));
        System.out.println("  1,234,567 -> " + precise.format(1_234_567));
        System.out.println("  1,234,567,890 -> " + precise.format(1_234_567_890));

        // Parsing compact numbers
        try {
            Number parsed = shortFormat.parse("5K");
            System.out.println("\nParsed '5K': " + parsed);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
```

---

## Files.mismatch()

Compare two files and find the position of first difference.

```java
import java.nio.file.*;
import java.io.*;

public class FilesMismatch {
    public static void main(String[] args) throws IOException {
        // Create test files
        Path file1 = Path.of("test1.txt");
        Path file2 = Path.of("test2.txt");
        Path file3 = Path.of("test3.txt");

        Files.writeString(file1, "Hello World");
        Files.writeString(file2, "Hello World");
        Files.writeString(file3, "Hello Java!");

        // Compare identical files
        long mismatch1 = Files.mismatch(file1, file2);
        System.out.println("file1 vs file2: " + mismatch1);  // -1 (identical)

        // Compare different files
        long mismatch2 = Files.mismatch(file1, file3);
        System.out.println("file1 vs file3: " + mismatch2);  // 6 (first difference)

        // Show where difference is
        if (mismatch2 != -1) {
            String content1 = Files.readString(file1);
            String content2 = Files.readString(file3);
            System.out.println("At position " + mismatch2 + ":");
            System.out.println("  file1: '" + content1.charAt((int)mismatch2) + "'");
            System.out.println("  file3: '" + content2.charAt((int)mismatch2) + "'");
        }

        // Compare files of different lengths
        Path shortFile = Path.of("short.txt");
        Files.writeString(shortFile, "Hi");

        long mismatch3 = Files.mismatch(file1, shortFile);
        System.out.println("file1 vs short: " + mismatch3);  // 2 (length difference)

        // Cleanup
        Files.deleteIfExists(file1);
        Files.deleteIfExists(file2);
        Files.deleteIfExists(file3);
        Files.deleteIfExists(shortFile);
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Switch Expressions | Preview | Switch as expression with arrow syntax |
| String.indent() | Standard | Add/remove indentation |
| String.transform() | Standard | Apply function to string |
| Collectors.teeing() | Standard | Combine two collectors |
| Compact Number Format | Standard | Format numbers as 1K, 1M |
| Files.mismatch() | Standard | Compare files for differences |

[← Java 11 Features](java-11.md) | [Java 13 Features →](java-13.md)
