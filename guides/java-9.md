# Java 9 Features Guide

**Release Date:** September 2017
**Type:** Feature Release

Java 9 introduced the Java Platform Module System (JPMS), also known as Project Jigsaw, along with many other improvements.

## Table of Contents
- [Module System (JPMS)](#module-system-jpms)
- [JShell (REPL)](#jshell-repl)
- [Collection Factory Methods](#collection-factory-methods)
- [Stream API Improvements](#stream-api-improvements)
- [Optional Improvements](#optional-improvements)
- [Private Interface Methods](#private-interface-methods)
- [Try-With-Resources Enhancement](#try-with-resources-enhancement)
- [Process API Improvements](#process-api-improvements)
- [HTTP/2 Client (Incubator)](#http2-client-incubator)
- [Multi-Release JAR Files](#multi-release-jar-files)

---

## Module System (JPMS)

The Java Platform Module System provides strong encapsulation and reliable configuration.

### What is a Module?

A module is a named, self-describing collection of code and data. It defines:
- What packages it exports (makes public)
- What other modules it requires (dependencies)

### Creating a Module

**module-info.java** (in module root):
```java
module com.myapp.core {
    // Export packages for other modules to use
    exports com.myapp.core.api;
    exports com.myapp.core.model;

    // Require other modules
    requires java.logging;
    requires java.sql;

    // Transitive - consumers get this dependency too
    requires transitive com.myapp.utils;

    // Open for reflection (frameworks like Spring)
    opens com.myapp.core.internal to com.framework;

    // Open entire module for reflection
    // open module com.myapp.core { ... }

    // Provide service implementation
    provides com.myapp.spi.DataProvider
        with com.myapp.core.DefaultDataProvider;

    // Use service
    uses com.myapp.spi.DataProvider;
}
```

### Module Structure

```
myapp/
├── com.myapp.core/
│   ├── module-info.java
│   └── com/myapp/core/
│       ├── api/
│       │   └── CoreService.java
│       ├── model/
│       │   └── User.java
│       └── internal/
│           └── CoreServiceImpl.java
├── com.myapp.web/
│   ├── module-info.java
│   └── com/myapp/web/
│       └── WebController.java
```

### Example: Simple Modular Application

**com.myapp.api/module-info.java:**
```java
module com.myapp.api {
    exports com.myapp.api;
}
```

**com.myapp.api/com/myapp/api/Greeting.java:**
```java
package com.myapp.api;

public interface Greeting {
    String greet(String name);
}
```

**com.myapp.impl/module-info.java:**
```java
module com.myapp.impl {
    requires com.myapp.api;
    exports com.myapp.impl;
}
```

**com.myapp.impl/com/myapp/impl/SimpleGreeting.java:**
```java
package com.myapp.impl;

import com.myapp.api.Greeting;

public class SimpleGreeting implements Greeting {
    @Override
    public String greet(String name) {
        return "Hello, " + name + "!";
    }
}
```

**com.myapp.main/module-info.java:**
```java
module com.myapp.main {
    requires com.myapp.api;
    requires com.myapp.impl;
}
```

### Compiling and Running Modules

```bash
# Compile modules
javac -d out --module-source-path src \
    $(find src -name "*.java")

# Run application
java --module-path out -m com.myapp.main/com.myapp.main.Main

# Create modular JAR
jar --create --file=myapp.jar \
    --main-class=com.myapp.main.Main \
    -C out/com.myapp.main .
```

### Services with JPMS

**Service Provider Interface (SPI):**
```java
// com.myapp.spi module
module com.myapp.spi {
    exports com.myapp.spi;
}

package com.myapp.spi;

public interface MessageService {
    String getMessage();
}
```

**Service Provider:**
```java
// com.myapp.provider module
module com.myapp.provider {
    requires com.myapp.spi;
    provides com.myapp.spi.MessageService
        with com.myapp.provider.EnglishMessageService;
}

package com.myapp.provider;

import com.myapp.spi.MessageService;

public class EnglishMessageService implements MessageService {
    @Override
    public String getMessage() {
        return "Hello, World!";
    }
}
```

**Service Consumer:**
```java
// com.myapp.consumer module
module com.myapp.consumer {
    requires com.myapp.spi;
    uses com.myapp.spi.MessageService;
}

package com.myapp.consumer;

import com.myapp.spi.MessageService;
import java.util.ServiceLoader;

public class Consumer {
    public static void main(String[] args) {
        ServiceLoader<MessageService> loader =
            ServiceLoader.load(MessageService.class);

        for (MessageService service : loader) {
            System.out.println(service.getMessage());
        }
    }
}
```

---

## JShell (REPL)

JShell is Java's Read-Eval-Print Loop for interactive programming.

### Starting JShell

```bash
$ jshell
|  Welcome to JShell -- Version 21
|  For an introduction type: /help intro

jshell>
```

### Basic Usage

```java
jshell> int x = 10
x ==> 10

jshell> int y = 20
y ==> 20

jshell> x + y
$3 ==> 30

jshell> String greeting = "Hello, JShell!"
greeting ==> "Hello, JShell!"

jshell> greeting.toUpperCase()
$5 ==> "HELLO, JSHELL!"
```

### Defining Methods and Classes

```java
jshell> int add(int a, int b) {
   ...>     return a + b;
   ...> }
|  created method add(int,int)

jshell> add(5, 3)
$7 ==> 8

jshell> record Person(String name, int age) {}
|  created record Person

jshell> var p = new Person("Alice", 30)
p ==> Person[name=Alice, age=30]

jshell> p.name()
$10 ==> "Alice"
```

### JShell Commands

```bash
jshell> /help
|  /list [<name or id>...]    - list the source you have typed
|  /edit <name or id>         - edit a source entry
|  /drop <name or id>         - delete a source entry
|  /save <file>               - save snippets to a file
|  /open <file>               - open a file as source input
|  /vars                      - list declared variables
|  /methods                   - list declared methods
|  /types                     - list type declarations
|  /imports                   - list imported items
|  /exit                      - exit jshell
|  /reset                     - reset jshell
|  /reload                    - reload all snippets
|  /history                   - history of what you have typed

jshell> /vars
|    int x = 10
|    int y = 20
|    String greeting = "Hello, JShell!"

jshell> /methods
|    int add(int,int)

jshell> /imports
|    import java.io.*
|    import java.math.*
|    import java.net.*
|    import java.util.*
|    ...
```

### Hands-On Exercise 1: JShell Exploration

```bash
# Start jshell and try these:

# 1. Create a list and stream it
jshell> var numbers = List.of(1, 2, 3, 4, 5)
jshell> numbers.stream().map(n -> n * n).toList()

# 2. Work with dates
jshell> import java.time.*
jshell> LocalDate.now()
jshell> LocalDate.now().plusDays(30)

# 3. Test regular expressions
jshell> "hello@world.com".matches(".*@.*\\..*")

# 4. Quick JSON-like structure
jshell> record JsonObject(Map<String, Object> data) {}
jshell> new JsonObject(Map.of("name", "Alice", "age", 30))

# 5. Save your session
jshell> /save mysession.jsh
jshell> /exit

# Later: reload session
$ jshell mysession.jsh
```

---

## Collection Factory Methods

Java 9 introduced convenient factory methods to create immutable collections.

```java
import java.util.*;

public class CollectionFactoryMethods {
    public static void main(String[] args) {
        // Immutable List
        List<String> list = List.of("a", "b", "c");
        System.out.println("List: " + list);
        // list.add("d"); // UnsupportedOperationException!

        // Immutable Set
        Set<Integer> set = Set.of(1, 2, 3, 4, 5);
        System.out.println("Set: " + set);
        // Set.of(1, 1, 2); // IllegalArgumentException - duplicates!

        // Immutable Map (up to 10 entries)
        Map<String, Integer> map = Map.of(
            "one", 1,
            "two", 2,
            "three", 3
        );
        System.out.println("Map: " + map);

        // Immutable Map (more than 10 entries)
        Map<String, Integer> largeMap = Map.ofEntries(
            Map.entry("one", 1),
            Map.entry("two", 2),
            Map.entry("three", 3),
            Map.entry("four", 4),
            Map.entry("five", 5),
            Map.entry("six", 6),
            Map.entry("seven", 7),
            Map.entry("eight", 8),
            Map.entry("nine", 9),
            Map.entry("ten", 10),
            Map.entry("eleven", 11)
        );
        System.out.println("Large Map size: " + largeMap.size());

        // Copy methods (Java 10+)
        List<String> mutableList = new ArrayList<>(Arrays.asList("x", "y", "z"));
        List<String> immutableCopy = List.copyOf(mutableList);
        System.out.println("Immutable copy: " + immutableCopy);
    }
}
```

---

## Stream API Improvements

### takeWhile and dropWhile

```java
import java.util.stream.*;

public class StreamImprovements {
    public static void main(String[] args) {
        // takeWhile - take elements while predicate is true
        Stream.of(1, 2, 3, 4, 5, 4, 3, 2, 1)
            .takeWhile(n -> n < 4)
            .forEach(System.out::println); // 1, 2, 3

        System.out.println("---");

        // dropWhile - skip elements while predicate is true
        Stream.of(1, 2, 3, 4, 5, 4, 3, 2, 1)
            .dropWhile(n -> n < 4)
            .forEach(System.out::println); // 4, 5, 4, 3, 2, 1

        // Note: Best used with ordered streams
    }
}
```

### Stream.ofNullable

```java
import java.util.stream.*;

public class StreamOfNullable {
    public static void main(String[] args) {
        // Before Java 9
        String value = getValue();
        Stream<String> stream1 = value == null
            ? Stream.empty()
            : Stream.of(value);

        // Java 9+
        Stream<String> stream2 = Stream.ofNullable(getValue());

        // Useful in flatMap
        Stream.of("a", null, "b", null, "c")
            .flatMap(Stream::ofNullable)
            .forEach(System.out::println); // a, b, c
    }

    static String getValue() {
        return Math.random() > 0.5 ? "value" : null;
    }
}
```

### Stream.iterate Enhancement

```java
import java.util.stream.*;

public class StreamIterate {
    public static void main(String[] args) {
        // Before Java 9: iterate + limit
        Stream.iterate(1, n -> n + 1)
            .limit(10)
            .forEach(System.out::println);

        // Java 9: iterate with predicate (like for loop)
        Stream.iterate(1, n -> n <= 10, n -> n + 1)
            .forEach(System.out::println);

        // Equivalent to: for(int n=1; n<=10; n++)

        // Example: Powers of 2 up to 1000
        Stream.iterate(1, n -> n < 1000, n -> n * 2)
            .forEach(n -> System.out.print(n + " "));
        // 1 2 4 8 16 32 64 128 256 512
    }
}
```

---

## Optional Improvements

```java
import java.util.*;
import java.util.stream.*;

public class OptionalImprovements {
    public static void main(String[] args) {
        Optional<String> optional = Optional.of("Hello");
        Optional<String> empty = Optional.empty();

        // ifPresentOrElse - handle both cases
        optional.ifPresentOrElse(
            value -> System.out.println("Found: " + value),
            () -> System.out.println("Not found")
        );

        empty.ifPresentOrElse(
            value -> System.out.println("Found: " + value),
            () -> System.out.println("Not found")
        );

        // or - lazy alternative Optional
        Optional<String> result = empty.or(() -> Optional.of("Default"));
        System.out.println("Result: " + result.get());

        // stream - convert to Stream (0 or 1 element)
        Stream<String> stream = optional.stream();
        stream.forEach(System.out::println);

        // Useful with flatMap
        List<Optional<String>> optionals = List.of(
            Optional.of("a"),
            Optional.empty(),
            Optional.of("b"),
            Optional.empty(),
            Optional.of("c")
        );

        List<String> values = optionals.stream()
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
        System.out.println("Values: " + values); // [a, b, c]
    }
}
```

---

## Private Interface Methods

Java 9 allows private methods in interfaces to share code between default methods.

```java
interface DataProcessor {
    // Public abstract method
    void process(String data);

    // Default methods
    default void processWithLogging(String data) {
        log("Starting processing: " + data);
        process(data);
        log("Finished processing");
    }

    default void processWithTiming(String data) {
        long start = System.currentTimeMillis();
        log("Timer started");
        process(data);
        long duration = System.currentTimeMillis() - start;
        log("Processing took " + duration + "ms");
    }

    // Private method - shared implementation
    private void log(String message) {
        System.out.println("[LOG] " + java.time.LocalTime.now() + " - " + message);
    }

    // Private static method
    private static String formatData(String data) {
        return data.trim().toUpperCase();
    }
}

class SimpleProcessor implements DataProcessor {
    @Override
    public void process(String data) {
        System.out.println("Processing: " + data);
    }
}

public class PrivateInterfaceMethodDemo {
    public static void main(String[] args) {
        DataProcessor processor = new SimpleProcessor();
        processor.processWithLogging("test data");
        System.out.println();
        processor.processWithTiming("more data");
    }
}
```

---

## Try-With-Resources Enhancement

Java 9 allows effectively final variables in try-with-resources.

```java
import java.io.*;

public class TryWithResourcesEnhancement {
    public static void main(String[] args) throws IOException {
        // Before Java 9: Must declare in try statement
        try (BufferedReader reader = new BufferedReader(
                new FileReader("file.txt"))) {
            // use reader
        } catch (FileNotFoundException e) {
            // handle
        }

        // Java 9+: Can use effectively final variables
        BufferedReader reader = new BufferedReader(
            new StringReader("Hello, World!")
        );
        // reader is effectively final (not reassigned)
        try (reader) {
            System.out.println(reader.readLine());
        }

        // Multiple resources
        BufferedReader r1 = new BufferedReader(new StringReader("Line 1"));
        BufferedReader r2 = new BufferedReader(new StringReader("Line 2"));
        try (r1; r2) {
            System.out.println(r1.readLine());
            System.out.println(r2.readLine());
        }
    }
}
```

---

## Process API Improvements

Java 9 enhanced the Process API for better process management.

```java
import java.io.*;
import java.time.*;

public class ProcessApiDemo {
    public static void main(String[] args) throws Exception {
        // Get current process info
        ProcessHandle current = ProcessHandle.current();
        System.out.println("Current PID: " + current.pid());

        ProcessHandle.Info info = current.info();
        System.out.println("Command: " + info.command().orElse("N/A"));
        System.out.println("Arguments: " + info.arguments().map(java.util.Arrays::toString).orElse("N/A"));
        System.out.println("Start time: " + info.startInstant().orElse(null));
        System.out.println("User: " + info.user().orElse("N/A"));

        // List all processes
        System.out.println("\nAll Java processes:");
        ProcessHandle.allProcesses()
            .filter(p -> p.info().command().orElse("").contains("java"))
            .limit(5)
            .forEach(p -> System.out.println(
                "  PID: " + p.pid() + " - " + p.info().command().orElse("unknown")
            ));

        // Start and manage a process
        ProcessBuilder builder = new ProcessBuilder("echo", "Hello from process");
        Process process = builder.start();

        // Get process handle
        ProcessHandle handle = process.toHandle();
        System.out.println("\nSpawned process PID: " + handle.pid());

        // Wait for completion with CompletableFuture
        handle.onExit().thenAccept(ph -> {
            System.out.println("Process " + ph.pid() + " exited");
        });

        // Read output
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        process.waitFor();

        // Process hierarchy
        System.out.println("\nParent process:");
        current.parent().ifPresent(parent -> {
            System.out.println("  PID: " + parent.pid());
            parent.info().command().ifPresent(cmd ->
                System.out.println("  Command: " + cmd));
        });

        // Children processes
        System.out.println("\nChildren of current process:");
        current.children().forEach(child ->
            System.out.println("  Child PID: " + child.pid())
        );

        // Descendants (all levels)
        System.out.println("\nAll descendants:");
        current.descendants().forEach(desc ->
            System.out.println("  Descendant PID: " + desc.pid())
        );
    }
}
```

---

## HTTP/2 Client (Incubator)

Java 9 introduced an HTTP client as an incubator module (standardized in Java 11).

```java
// Note: This is the incubator API. Use Java 11+ for the standard API.
// Add: --add-modules jdk.incubator.httpclient

import jdk.incubator.http.*;
import java.net.URI;

public class HttpClientDemo {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com"))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandler.asString()
        );

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body().substring(0, 200));
    }
}
```

---

## Multi-Release JAR Files

Multi-release JARs allow different class versions for different Java versions.

### JAR Structure

```
myapp.jar
├── META-INF/
│   ├── MANIFEST.MF
│   └── versions/
│       ├── 9/
│       │   └── com/myapp/Util.class  (Java 9+ version)
│       └── 11/
│           └── com/myapp/Util.class  (Java 11+ version)
└── com/
    └── myapp/
        └── Util.class  (Base version, Java 8)
```

### MANIFEST.MF

```
Manifest-Version: 1.0
Multi-Release: true
```

### Example Implementation

**Base version (Java 8):**
```java
package com.myapp;

public class Util {
    public static String getJavaVersion() {
        return "Running on Java 8 or earlier";
    }

    public static Runtime.Version version() {
        // Not available in Java 8
        throw new UnsupportedOperationException();
    }
}
```

**Java 9+ version:**
```java
package com.myapp;

public class Util {
    public static String getJavaVersion() {
        return "Running on Java 9+: " + Runtime.version();
    }

    public static Runtime.Version version() {
        return Runtime.version();
    }
}
```

### Building Multi-Release JAR

```bash
# Compile base version
javac -d out/base src/main/java/com/myapp/Util.java

# Compile Java 9 version
javac --release 9 -d out/9 src/main/java9/com/myapp/Util.java

# Create JAR
jar --create --file myapp.jar \
    --manifest=MANIFEST.MF \
    -C out/base . \
    --release 9 -C out/9 .
```

---

## Other Java 9 Features

### Underscore as Identifier Removed

```java
// No longer valid in Java 9+
// int _ = 10;  // Compilation error

// Use meaningful names instead
int unused = 10;
```

### Diamond Operator with Anonymous Classes

```java
import java.util.*;

public class DiamondOperator {
    public static void main(String[] args) {
        // Java 9: Diamond operator works with anonymous classes
        List<String> list = new ArrayList<>() {
            @Override
            public boolean add(String s) {
                System.out.println("Adding: " + s);
                return super.add(s);
            }
        };

        list.add("Hello");
        list.add("World");
    }
}
```

### @SafeVarargs on Private Methods

```java
public class SafeVarargsDemo {
    // Java 9: @SafeVarargs allowed on private methods
    @SafeVarargs
    private void process(List<String>... lists) {
        for (List<String> list : lists) {
            System.out.println(list);
        }
    }

    @SafeVarargs
    private static <T> void print(T... items) {
        for (T item : items) {
            System.out.println(item);
        }
    }
}
```

### Compact Strings

Java 9 internally uses byte[] instead of char[] for strings with only Latin-1 characters, reducing memory usage.

```java
public class CompactStringsDemo {
    public static void main(String[] args) {
        // ASCII string - uses 1 byte per character internally
        String ascii = "Hello, World!";

        // Unicode string - uses 2 bytes per character internally
        String unicode = "Hello, 世界!";

        // This is transparent to the programmer
        // but reduces memory for ASCII-only strings by ~50%
    }
}
```

---

## Summary

| Feature | Description |
|---------|-------------|
| Module System | Strong encapsulation, reliable configuration |
| JShell | Interactive REPL for experimentation |
| Collection Factory Methods | `List.of()`, `Set.of()`, `Map.of()` |
| Stream Improvements | `takeWhile`, `dropWhile`, `ofNullable`, enhanced `iterate` |
| Optional Improvements | `ifPresentOrElse`, `or`, `stream` |
| Private Interface Methods | Code sharing between default methods |
| Process API | Better process management and information |
| HTTP/2 Client | Modern HTTP client (incubator) |

---

## Hands-On Challenge

**Module Migration Challenge:**

1. Take an existing non-modular application
2. Analyze dependencies with `jdeps`
3. Create `module-info.java` files
4. Handle split packages and automatic modules
5. Test the modular application

```bash
# Analyze dependencies
jdeps --module-path libs -s myapp.jar

# Generate module-info
jdeps --generate-module-info out myapp.jar
```

[← Java 8 Features](java-8.md) | [Java 10 Features →](java-10.md)
