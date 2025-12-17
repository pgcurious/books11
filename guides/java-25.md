# Java 25 Features Guide

**Expected Release Date:** September 2025
**Type:** LTS (Long-Term Support)

Java 25 is expected to be the next LTS release, bringing together many features that have matured through the preview process.

> Note: This guide covers expected features based on the progression of preview features. Actual features may vary.

## Table of Contents
- [Expected Standard Features](#expected-standard-features)
- [Flexible Constructor Bodies (Expected Standard)](#flexible-constructor-bodies-expected-standard)
- [Primitive Types in Patterns (Expected Standard)](#primitive-types-in-patterns-expected-standard)
- [Module Import Declarations (Expected Standard)](#module-import-declarations-expected-standard)
- [Implicitly Declared Classes (Expected Standard)](#implicitly-declared-classes-expected-standard)
- [Value Classes (Preview/Incubator)](#value-classes-previewincubator)
- [Migration Guide from Java 21](#migration-guide-from-java-21)

---

## Expected Standard Features

Java 25 as an LTS release is expected to finalize several preview features:

| Feature | Preview in | Expected Status |
|---------|-----------|-----------------|
| Flexible Constructor Bodies | Java 22-24 | Standard |
| Primitive Types in Patterns | Java 23-24 | Standard |
| Module Import Declarations | Java 23-24 | Standard |
| Implicitly Declared Classes | Java 21-24 | Standard |
| String Templates | Java 21-24 | TBD |

---

## Flexible Constructor Bodies (Expected Standard)

Full flexibility in constructor execution before super/this calls.

```java
public class FlexibleConstructorsStandard {
    public static void main(String[] args) {
        // All patterns now work without --enable-preview

        // Validation
        var validated = new ValidatedUser("alice@example.com", 25);
        System.out.println("User: " + validated);

        // Complex parsing
        var uri = new ParsedUri("https://example.com:8080/path?query=value");
        System.out.println("Scheme: " + uri.scheme());
        System.out.println("Host: " + uri.host());
        System.out.println("Port: " + uri.port());

        // Conditional initialization
        var optional = new OptionalConfig("DEBUG");
        System.out.println("Config: " + optional);
    }
}

// Base user class
class User {
    final String email;
    final int age;

    User(String email, int age) {
        this.email = email;
        this.age = age;
    }
}

// Validated subclass
class ValidatedUser extends User {
    ValidatedUser(String email, int age) {
        // Validation before super
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age: " + age);
        }
        // Normalize
        email = email.toLowerCase().trim();

        super(email, age);
    }
}

// Base URI class
class UriComponents {
    final String scheme;
    final String host;
    final int port;
    final String path;

    UriComponents(String scheme, String host, int port, String path) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.path = path;
    }

    String scheme() { return scheme; }
    String host() { return host; }
    int port() { return port; }
    String path() { return path; }
}

// Parsing subclass
class ParsedUri extends UriComponents {
    ParsedUri(String uri) {
        // Complex parsing before super()
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
            "^(\\w+)://([^:/]+)(?::(\\d+))?(/[^?]*)?");
        java.util.regex.Matcher matcher = pattern.matcher(uri);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid URI: " + uri);
        }

        String scheme = matcher.group(1);
        String host = matcher.group(2);
        int port = matcher.group(3) != null
            ? Integer.parseInt(matcher.group(3))
            : (scheme.equals("https") ? 443 : 80);
        String path = matcher.group(4) != null ? matcher.group(4) : "/";

        super(scheme, host, port, path);
    }
}

// Conditional initialization
class Config {
    final String level;
    final boolean verbose;

    Config(String level, boolean verbose) {
        this.level = level;
        this.verbose = verbose;
    }
}

class OptionalConfig extends Config {
    OptionalConfig(String input) {
        // Compute values before super
        String level;
        boolean verbose;

        if (input == null || input.isBlank()) {
            level = "INFO";
            verbose = false;
        } else {
            level = input.toUpperCase();
            verbose = level.equals("DEBUG") || level.equals("TRACE");
        }

        super(level, verbose);
    }
}
```

---

## Primitive Types in Patterns (Expected Standard)

Full pattern matching support for primitive types.

```java
public class PrimitivePatternsStandard {
    public static void main(String[] args) {
        // Integer patterns
        demonstrateIntPatterns();

        // Boolean patterns
        demonstrateBooleanPatterns();

        // All primitive types
        demonstrateAllPrimitives();

        // Record patterns with primitives
        demonstrateRecordPatterns();
    }

    static void demonstrateIntPatterns() {
        System.out.println("=== Integer Patterns ===");

        for (int i : new int[]{-10, 0, 50, 100, 200}) {
            String category = switch (i) {
                case int n when n < 0 -> "Negative";
                case int n when n == 0 -> "Zero";
                case int n when n <= 100 -> "Low";
                case int n -> "High";
            };
            System.out.println(i + " -> " + category);
        }
    }

    static void demonstrateBooleanPatterns() {
        System.out.println("\n=== Boolean Patterns ===");

        for (boolean b : new boolean[]{true, false}) {
            String result = switch (b) {
                case true -> "Affirmative";
                case false -> "Negative";
            };
            System.out.println(b + " -> " + result);
        }
    }

    static void demonstrateAllPrimitives() {
        System.out.println("\n=== All Primitive Patterns ===");

        Object[] values = {
            42,
            42L,
            3.14f,
            3.14159,
            (byte) 127,
            (short) 1000,
            'A',
            true
        };

        for (Object val : values) {
            String type = switch (val) {
                case Integer i -> "int: " + i;
                case Long l -> "long: " + l;
                case Float f -> "float: " + f;
                case Double d -> "double: " + d;
                case Byte b -> "byte: " + b;
                case Short s -> "short: " + s;
                case Character c -> "char: " + c;
                case Boolean b -> "boolean: " + b;
                default -> "unknown";
            };
            System.out.println(type);
        }
    }

    record Temperature(double celsius) {
        String classification() {
            return switch (celsius) {
                case double c when c < 0 -> "Freezing";
                case double c when c < 10 -> "Cold";
                case double c when c < 20 -> "Cool";
                case double c when c < 30 -> "Warm";
                case double c -> "Hot";
            };
        }
    }

    record Score(int points, int total) {
        String grade() {
            double percentage = (points * 100.0) / total;
            return switch ((int) percentage) {
                case int p when p >= 90 -> "A";
                case int p when p >= 80 -> "B";
                case int p when p >= 70 -> "C";
                case int p when p >= 60 -> "D";
                case int p -> "F";
            };
        }
    }

    static void demonstrateRecordPatterns() {
        System.out.println("\n=== Record Patterns with Primitives ===");

        var temps = java.util.List.of(
            new Temperature(-5),
            new Temperature(15),
            new Temperature(35)
        );

        for (var temp : temps) {
            System.out.println(temp.celsius() + "°C -> " + temp.classification());
        }

        var scores = java.util.List.of(
            new Score(95, 100),
            new Score(82, 100),
            new Score(55, 100)
        );

        for (var score : scores) {
            System.out.println(score.points() + "/" + score.total() +
                " -> " + score.grade());
        }
    }
}
```

---

## Module Import Declarations (Expected Standard)

Import all packages from a module with a single declaration.

```java
// Import entire module
import module java.base;  // Imports all java.base packages

public class ModuleImportsStandard {
    public static void main(String[] args) {
        // All these classes are available without individual imports:

        // From java.util
        List<String> list = List.of("a", "b", "c");
        Map<String, Integer> map = Map.of("one", 1, "two", 2);
        Set<Integer> set = Set.of(1, 2, 3);
        Optional<String> optional = Optional.of("value");

        // From java.time
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        LocalDateTime dateTime = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);

        // From java.io
        // File file = new File("test.txt");

        // From java.nio.file
        Path path = Path.of("test.txt");

        // From java.util.concurrent
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // From java.util.stream
        Stream<String> stream = list.stream();

        // Print examples
        System.out.println("List: " + list);
        System.out.println("Map: " + map);
        System.out.println("Today: " + today);
        System.out.println("Now: " + now);
        System.out.println("Path: " + path);

        executor.close();
    }
}
```

---

## Implicitly Declared Classes (Expected Standard)

Simplified main method declaration for simple programs.

```java
// File: HelloWorld.java
// No explicit class declaration needed
void main() {
    println("Hello, Java 25!");
}

// Helper method available without System.out
void println(Object obj) {
    System.out.println(obj);
}
```

```java
// File: Interactive.java
// More complex implicit class
import java.util.Scanner;

String name;
int age;

void main() {
    greet();
    askName();
    askAge();
    summarize();
}

void greet() {
    println("Welcome to Java 25!");
    println("Let me ask you some questions.\n");
}

void askName() {
    print("What's your name? ");
    name = readLine();
}

void askAge() {
    print("How old are you? ");
    age = Integer.parseInt(readLine());
}

void summarize() {
    println("\nNice to meet you, " + name + "!");
    println("You are " + age + " years old.");
    if (age >= 18) {
        println("You are an adult.");
    } else {
        println("You are a minor.");
    }
}

void print(Object obj) {
    System.out.print(obj);
}

void println(Object obj) {
    System.out.println(obj);
}

String readLine() {
    return new Scanner(System.in).nextLine();
}
```

---

## Value Classes (Preview/Incubator)

Value classes are expected to continue development, providing identity-free classes for performance.

```java
// Expected syntax (may change)
value class Point {
    private int x;
    private int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() { return x; }
    public int y() { return y; }
}

// Benefits:
// - No object identity (no ==)
// - Can be inlined by JVM
// - Better cache locality
// - Reduced memory overhead

public class ValueClassDemo {
    public static void main(String[] args) {
        // Value classes behave like primitives
        Point p1 = new Point(10, 20);
        Point p2 = new Point(10, 20);

        // Equality based on values, not identity
        System.out.println(p1.equals(p2)); // true

        // Arrays of value classes are flat in memory
        Point[] points = new Point[1000];
        // More efficient than Object[] with references
    }
}
```

---

## Migration Guide from Java 21

### Step 1: Update Build Configuration

```xml
<!-- Maven pom.xml -->
<properties>
    <java.version>25</java.version>
    <maven.compiler.source>25</maven.compiler.source>
    <maven.compiler.target>25</maven.compiler.target>
</properties>
```

```groovy
// Gradle build.gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
```

### Step 2: Remove Preview Flags

Features that were preview in Java 21-24 are now standard:
- Remove `--enable-preview` from compiler and runtime
- Remove `@SuppressWarnings("preview")` annotations

### Step 3: Update Code Patterns

```java
// Before: Complex super() workaround
class OldWay extends Parent {
    OldWay(String input) {
        super(parse(input)); // Had to use static helper
    }
    private static String parse(String s) { return s.trim(); }
}

// After: Flexible constructor bodies
class NewWay extends Parent {
    NewWay(String input) {
        String parsed = input.trim();  // Direct code before super
        if (parsed.isEmpty()) throw new IllegalArgumentException();
        super(parsed);
    }
}
```

### Step 4: Leverage New Features

```java
// Use module imports for cleaner code
import module java.base;

// Use primitive patterns
String classify(Object obj) {
    return switch (obj) {
        case Integer i when i > 0 -> "Positive int";
        case Double d when d > 0 -> "Positive double";
        default -> "Other";
    };
}

// Use simplified main for scripts
// void main() { ... }
```

---

## Summary

Java 25 as an LTS brings stability and finalizes many features:

| Feature | Status | Impact |
|---------|--------|--------|
| Flexible Constructor Bodies | Standard | Cleaner validation |
| Primitive Types in Patterns | Standard | Exhaustive matching |
| Module Import Declarations | Standard | Simplified imports |
| Implicitly Declared Classes | Standard | Script-like programs |
| Scoped Values | Standard | Better than ThreadLocal |
| Structured Concurrency | Standard | Safe concurrent code |
| Stream Gatherers | Standard | Custom stream ops |
| Virtual Threads | Mature | High-scale servers |

### Recommended Actions

1. **Start planning migration** from Java 17/21 to Java 25
2. **Test with early access builds** when available
3. **Review deprecated APIs** that may be removed
4. **Update dependencies** to versions supporting Java 25
5. **Train team** on new features and patterns

---

## Complete Feature Summary: Java 8 to 25

```
Java 8  ──── Lambdas, Streams, Optional, Date/Time API
Java 9  ──── Modules, JShell, Collection Factory Methods
Java 10 ──── var (Local Variable Type Inference)
Java 11 ──── HTTP Client, String Methods, var in Lambda
Java 12 ──── Switch Expressions (Preview)
Java 13 ──── Text Blocks (Preview)
Java 14 ──── Records (Preview), Pattern Matching instanceof (Preview)
Java 15 ──── Sealed Classes (Preview), Text Blocks (Standard)
Java 16 ──── Records (Standard), Pattern Matching instanceof (Standard)
Java 17 ──── Sealed Classes (Standard), Pattern Matching Switch (Preview)
Java 18 ──── Simple Web Server, UTF-8 Default
Java 19 ──── Virtual Threads (Preview), Record Patterns (Preview)
Java 20 ──── Scoped Values (Incubator)
Java 21 ──── Virtual Threads (Standard), Pattern Matching (Standard), Sequenced Collections
Java 22 ──── Unnamed Variables (Standard), Stream Gatherers (Preview)
Java 23 ──── Markdown Doc Comments, Primitive Patterns (Preview)
Java 24 ──── Stream Gatherers (Standard), Scoped Values (Standard)
Java 25 ──── LTS with all mature features
```

[← Java 24 Features](java-24.md) | [Back to Index](../README.md)
