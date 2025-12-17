# Java 10 Features Guide

**Release Date:** March 2018
**Type:** Feature Release (Short-term support)

Java 10 was the first release under the new 6-month release cycle. Its headline feature is Local Variable Type Inference with `var`.

## Table of Contents
- [Local Variable Type Inference (var)](#local-variable-type-inference-var)
- [Unmodifiable Collections](#unmodifiable-collections)
- [Optional.orElseThrow()](#optionalorelseThrow)
- [Application Class-Data Sharing](#application-class-data-sharing)
- [Parallel Full GC for G1](#parallel-full-gc-for-g1)
- [Root Certificates](#root-certificates)

---

## Local Variable Type Inference (var)

The `var` keyword allows the compiler to infer the type of local variables.

### Basic Usage

```java
import java.util.*;
import java.io.*;
import java.net.*;

public class VarBasics {
    public static void main(String[] args) throws Exception {
        // Before Java 10
        String message = "Hello, World!";
        List<String> names = new ArrayList<>();
        Map<String, List<Integer>> complexMap = new HashMap<>();

        // With var
        var message2 = "Hello, World!";         // String
        var names2 = new ArrayList<String>();   // ArrayList<String>
        var complexMap2 = new HashMap<String, List<Integer>>(); // HashMap

        // Type is inferred at compile time
        System.out.println(message2.getClass());  // class java.lang.String
        System.out.println(names2.getClass());    // class java.util.ArrayList

        // Great for complex generic types
        var entries = complexMap2.entrySet();  // Set<Entry<String, List<Integer>>>

        // Works with try-with-resources
        try (var reader = new BufferedReader(new FileReader("test.txt"))) {
            var line = reader.readLine();
        } catch (IOException e) {
            // handle
        }

        // Works in for loops
        var numbers = List.of(1, 2, 3, 4, 5);
        for (var number : numbers) {
            System.out.println(number);
        }

        // Traditional for loop
        for (var i = 0; i < 10; i++) {
            System.out.println(i);
        }
    }
}
```

### Where var CAN Be Used

```java
public class VarValidUsages {
    public static void main(String[] args) {
        // 1. Local variables with initializers
        var name = "Alice";
        var count = 42;
        var price = 19.99;
        var active = true;

        // 2. Enhanced for loop
        var items = List.of("a", "b", "c");
        for (var item : items) {
            System.out.println(item);
        }

        // 3. Traditional for loop
        for (var i = 0; i < 10; i++) {
            System.out.println(i);
        }

        // 4. Try-with-resources
        try (var stream = java.nio.file.Files.lines(
                java.nio.file.Path.of("file.txt"))) {
            // use stream
        } catch (Exception e) {
            // handle
        }

        // 5. Anonymous classes (with some caveats)
        var runnable = new Runnable() {
            @Override
            public void run() {
                System.out.println("Running!");
            }

            public void extraMethod() {
                System.out.println("Extra!");
            }
        };
        runnable.run();
        runnable.extraMethod(); // Works! Type is the anonymous class
    }
}
```

### Where var CANNOT Be Used

```java
public class VarRestrictions {
    // NOT allowed as field
    // var field = "test"; // Compilation error

    // NOT allowed as method parameter
    // void process(var data) { } // Compilation error

    // NOT allowed as return type
    // var getData() { return "test"; } // Compilation error

    public static void main(String[] args) {
        // NOT allowed without initializer
        // var x; // Compilation error

        // NOT allowed with null (type cannot be inferred)
        // var nothing = null; // Compilation error

        // NOT allowed with lambda (no target type)
        // var func = () -> "hello"; // Compilation error
        // Fix: provide explicit functional interface type
        java.util.function.Supplier<String> func = () -> "hello";

        // NOT allowed with method reference alone
        // var ref = System.out::println; // Compilation error

        // NOT allowed with array initializer without new
        // var nums = {1, 2, 3}; // Compilation error
        var nums = new int[]{1, 2, 3}; // This works
    }
}
```

### var with Generic Methods

```java
import java.util.*;
import java.util.stream.*;

public class VarWithGenerics {
    public static void main(String[] args) {
        // var captures the inferred type from generic methods
        var list = List.of(1, 2, 3);  // List<Integer>
        var set = Set.of("a", "b");   // Set<String>
        var map = Map.of("key", 1);   // Map<String, Integer>

        // Stream operations
        var numbers = List.of(1, 2, 3, 4, 5);
        var evenSquares = numbers.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .collect(Collectors.toList());
        // Type: List<Integer>

        // Be careful with type inference
        var emptyList = Collections.emptyList(); // List<Object>
        var typedEmpty = Collections.<String>emptyList(); // List<String>

        // Diamond operator works with var
        var arrayList = new ArrayList<String>();
    }
}
```

### Best Practices for var

```java
import java.util.*;

public class VarBestPractices {
    public static void main(String[] args) {
        // GOOD: Type is obvious from the right side
        var message = "Hello, World!";
        var numbers = List.of(1, 2, 3);
        var user = new User("Alice", 30);
        var stream = numbers.stream();

        // GOOD: Reduces boilerplate with complex types
        var entrySet = Map.of("a", 1, "b", 2).entrySet();

        // BAD: Type is not clear
        var result = getData();  // What type is this?
        var x = calculate();     // Unclear

        // GOOD: Consider using explicit type when it improves readability
        List<String> names = fetchNames();  // Clear intent
        // var names = fetchNames();  // Less clear

        // BAD: Overusing var reduces readability
        var v1 = getValue1();
        var v2 = getValue2();
        var v3 = process(v1, v2);  // Types completely unclear

        // GOOD: Use descriptive variable names with var
        var customerOrders = getOrdersByCustomer(customerId);
        var activeSubscriptions = subscriptions.stream()
            .filter(Subscription::isActive)
            .collect(Collectors.toList());
    }

    static String getData() { return "data"; }
    static int calculate() { return 42; }
    static List<String> fetchNames() { return List.of(); }
    static Object getValue1() { return null; }
    static Object getValue2() { return null; }
    static Object process(Object a, Object b) { return null; }
    static List<Order> getOrdersByCustomer(int id) { return List.of(); }

    record User(String name, int age) {}
    record Order(int id) {}
    record Subscription(boolean active) {
        boolean isActive() { return active; }
    }
}
```

### Hands-On Exercise: Refactoring with var

```java
import java.util.*;
import java.util.stream.*;
import java.time.*;

public class VarRefactoringExercise {
    public static void main(String[] args) {
        // Exercise: Refactor the following code to use var where appropriate

        // Original:
        String greeting = "Hello";
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
        Map<String, Integer> scores = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // Refactored:
        var greeting2 = "Hello";
        var names2 = Arrays.asList("Alice", "Bob", "Charlie");
        var scores2 = new HashMap<String, Integer>();
        var now2 = LocalDateTime.now();

        // Stream operations - great candidate for var
        List<Integer> evenNumbers = names.stream()
            .map(String::length)
            .filter(len -> len % 2 == 0)
            .collect(Collectors.toList());

        // With var:
        var evenNumbers2 = names.stream()
            .map(String::length)
            .filter(len -> len % 2 == 0)
            .collect(Collectors.toList());

        // Nested generics - excellent candidate for var
        Map<String, List<Map<String, Object>>> complexStructure =
            new HashMap<String, List<Map<String, Object>>>();

        // Much cleaner with var:
        var complexStructure2 = new HashMap<String, List<Map<String, Object>>>();

        System.out.println("Refactoring complete!");
    }
}
```

---

## Unmodifiable Collections

Java 10 added `copyOf()` methods and stream collectors for unmodifiable collections.

```java
import java.util.*;
import java.util.stream.*;

public class UnmodifiableCollections {
    public static void main(String[] args) {
        // copyOf - creates unmodifiable copy
        List<String> original = new ArrayList<>();
        original.add("a");
        original.add("b");
        original.add("c");

        List<String> copy = List.copyOf(original);
        System.out.println("Copy: " + copy);

        // Original can still be modified
        original.add("d");
        System.out.println("Original: " + original);
        System.out.println("Copy unchanged: " + copy);

        // Copy is unmodifiable
        try {
            copy.add("x");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify copy!");
        }

        // Set.copyOf
        Set<Integer> numbers = new HashSet<>(Arrays.asList(1, 2, 3));
        Set<Integer> numbersCopy = Set.copyOf(numbers);

        // Map.copyOf
        Map<String, Integer> map = new HashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        Map<String, Integer> mapCopy = Map.copyOf(map);

        // Stream collectors for unmodifiable collections
        var unmodifiableList = Stream.of(1, 2, 3, 4, 5)
            .collect(Collectors.toUnmodifiableList());

        var unmodifiableSet = Stream.of("a", "b", "c")
            .collect(Collectors.toUnmodifiableSet());

        var unmodifiableMap = Stream.of("one", "two", "three")
            .collect(Collectors.toUnmodifiableMap(
                s -> s,
                String::length
            ));

        System.out.println("Unmodifiable List: " + unmodifiableList);
        System.out.println("Unmodifiable Set: " + unmodifiableSet);
        System.out.println("Unmodifiable Map: " + unmodifiableMap);

        // Note: copyOf returns the same instance if already unmodifiable
        List<String> immutable = List.of("x", "y", "z");
        List<String> copyOfImmutable = List.copyOf(immutable);
        System.out.println("Same instance: " + (immutable == copyOfImmutable)); // true
    }
}
```

---

## Optional.orElseThrow()

Java 10 added `orElseThrow()` as a more readable alternative to `get()`.

```java
import java.util.*;

public class OptionalOrElseThrow {
    public static void main(String[] args) {
        Optional<String> present = Optional.of("Hello");
        Optional<String> empty = Optional.empty();

        // Before Java 10: get() - throws NoSuchElementException
        // This is discouraged because intent is unclear
        String value1 = present.get(); // Works
        // String value2 = empty.get(); // Throws!

        // Java 10: orElseThrow() - same behavior, clearer intent
        String value3 = present.orElseThrow(); // Works
        // String value4 = empty.orElseThrow(); // Throws NoSuchElementException

        // The method name makes it clear an exception is expected
        // when the Optional is empty

        // Compare readability:
        // present.get()           - What happens if empty? Not obvious
        // present.orElseThrow()   - Clearly throws if empty

        // With custom exception (from Java 8)
        try {
            String value = empty.orElseThrow(
                () -> new IllegalStateException("Value required!")
            );
        } catch (IllegalStateException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // Practical example
        User user = findUserById(1)
            .orElseThrow(); // Throws if not found

        User user2 = findUserById(999)
            .orElseThrow(() -> new UserNotFoundException("User 999 not found"));
    }

    static Optional<User> findUserById(int id) {
        if (id == 1) return Optional.of(new User("Alice"));
        return Optional.empty();
    }

    record User(String name) {}
    static class UserNotFoundException extends RuntimeException {
        UserNotFoundException(String msg) { super(msg); }
    }
}
```

---

## Application Class-Data Sharing

Application Class-Data Sharing (AppCDS) allows sharing class metadata between JVM instances, improving startup time.

### Creating and Using CDS Archive

```bash
# Step 1: Create a list of classes to archive
java -Xshare:off -XX:DumpLoadedClassList=classes.lst -jar myapp.jar

# Step 2: Create the archive
java -Xshare:dump -XX:SharedClassListFile=classes.lst \
    -XX:SharedArchiveFile=app-cds.jsa -jar myapp.jar

# Step 3: Run with the archive
java -Xshare:on -XX:SharedArchiveFile=app-cds.jsa -jar myapp.jar
```

### Benefits

- **Faster startup**: Classes are memory-mapped from the archive
- **Reduced memory**: Shared archive can be used across JVM instances
- **Lower footprint**: Container deployments benefit significantly

---

## Parallel Full GC for G1

Java 10 improved G1 garbage collector with parallel full GC, reducing worst-case latencies.

```java
// Enable G1 (default in Java 9+)
// java -XX:+UseG1GC MyApp

// Monitor GC
// java -Xlog:gc* MyApp

public class GCDemo {
    public static void main(String[] args) {
        // G1 Full GC is now parallel, improving pause times
        // when a full GC is needed

        // Print GC info
        System.out.println("Max memory: " + Runtime.getRuntime().maxMemory());
        System.out.println("Total memory: " + Runtime.getRuntime().totalMemory());
        System.out.println("Free memory: " + Runtime.getRuntime().freeMemory());

        // Allocate memory to trigger GC
        var lists = new java.util.ArrayList<byte[]>();
        for (int i = 0; i < 100; i++) {
            lists.add(new byte[1024 * 1024]); // 1MB
            if (i % 10 == 0) {
                System.out.println("Allocated " + (i + 1) + "MB");
            }
        }
    }
}
```

---

## Root Certificates

Java 10 includes a set of root certificates in the cacerts keystore, enabling TLS out of the box.

```java
import javax.net.ssl.*;
import java.security.*;

public class RootCertificatesDemo {
    public static void main(String[] args) throws Exception {
        // Java 10+ includes root certificates by default
        // HTTPS connections work without additional configuration

        // List root certificates
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(null, null);

        // Get the default trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm()
        );
        tmf.init((KeyStore) null);

        System.out.println("Trusted certificate authorities:");
        for (TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager x509tm) {
                var issuers = x509tm.getAcceptedIssuers();
                System.out.println("Number of trusted CAs: " + issuers.length);

                // Print first 5
                for (int i = 0; i < Math.min(5, issuers.length); i++) {
                    System.out.println("  " + issuers[i].getSubjectX500Principal());
                }
            }
        }

        // HTTPS works out of the box
        var url = new java.net.URL("https://www.google.com");
        var connection = (HttpsURLConnection) url.openConnection();
        System.out.println("\nConnected to: " + url);
        System.out.println("Response code: " + connection.getResponseCode());
    }
}
```

---

## Summary

| Feature | Description |
|---------|-------------|
| Local Variable Type Inference | `var` for local variables |
| Unmodifiable Collections | `List.copyOf()`, `Set.copyOf()`, `Map.copyOf()` |
| Optional.orElseThrow() | Clearer alternative to `get()` |
| Application CDS | Faster startup with shared class data |
| Parallel Full GC for G1 | Better G1 garbage collection performance |
| Root Certificates | TLS works out of the box |

---

## Hands-On Challenge

**Code Modernization Challenge:**

Refactor this pre-Java 10 code to use `var` appropriately:

```java
// Before
public class LegacyCode {
    public void process() {
        ArrayList<String> names = new ArrayList<String>();
        names.add("Alice");
        names.add("Bob");

        HashMap<String, List<Integer>> scores =
            new HashMap<String, List<Integer>>();

        for (String name : names) {
            ArrayList<Integer> nameScores = new ArrayList<Integer>();
            for (int i = 0; i < 5; i++) {
                nameScores.add((int)(Math.random() * 100));
            }
            scores.put(name, nameScores);
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("data.txt"));
            String line = reader.readLine();
            while (line != null) {
                System.out.println(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) {}
            }
        }
    }
}
```

[← Java 9 Features](java-9.md) | [Java 11 Features →](java-11.md)
