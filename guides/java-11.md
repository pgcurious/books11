# Java 11 Features Guide

**Release Date:** September 2018
**Type:** LTS (Long-Term Support)

Java 11 is a critical LTS release that removed many deprecated features and added important new capabilities, including the standardized HTTP Client.

## Table of Contents
- [HTTP Client API](#http-client-api)
- [String Methods](#string-methods)
- [Collection to Array](#collection-to-array)
- [Local-Variable Syntax for Lambda Parameters](#local-variable-syntax-for-lambda-parameters)
- [Files Methods](#files-methods)
- [Optional.isEmpty()](#optionalisempty)
- [Running Java Files Directly](#running-java-files-directly)
- [Nest-Based Access Control](#nest-based-access-control)
- [Removed Features](#removed-features)

---

## HTTP Client API

The HTTP Client API (introduced as incubator in Java 9) is now standardized in Java 11.

### Basic GET Request

```java
import java.net.http.*;
import java.net.URI;

public class HttpClientBasics {
    public static void main(String[] args) throws Exception {
        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.github.com/users/octocat"))
            .header("Accept", "application/json")
            .GET()  // GET is default
            .build();

        // Send request synchronously
        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Status: " + response.statusCode());
        System.out.println("Headers: " + response.headers().map());
        System.out.println("Body: " + response.body().substring(0, 200));
    }
}
```

### Asynchronous Requests

```java
import java.net.http.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class HttpClientAsync {
    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get"))
            .build();

        // Async request
        CompletableFuture<HttpResponse<String>> futureResponse =
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        // Process response when ready
        futureResponse
            .thenApply(HttpResponse::body)
            .thenAccept(System.out::println)
            .join();

        // Multiple async requests
        var request1 = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get?id=1"))
            .build();
        var request2 = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get?id=2"))
            .build();
        var request3 = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get?id=3"))
            .build();

        var futures = java.util.List.of(request1, request2, request3).stream()
            .map(req -> client.sendAsync(req, HttpResponse.BodyHandlers.ofString()))
            .toList();

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        futures.forEach(f -> System.out.println("Status: " + f.join().statusCode()));
    }
}
```

### POST Request with Body

```java
import java.net.http.*;
import java.net.URI;

public class HttpClientPost {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        // JSON body
        String json = """
            {
                "name": "John",
                "email": "john@example.com"
            }
            """;

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/post"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("Status: " + response.statusCode());
        System.out.println("Body: " + response.body());
    }
}
```

### HTTP Client Configuration

```java
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import java.net.*;

public class HttpClientConfig {
    public static void main(String[] args) throws Exception {
        // Configured client
        HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)       // Prefer HTTP/2
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(10))
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        "user",
                        "password".toCharArray()
                    );
                }
            })
            // .proxy(ProxySelector.of(new InetSocketAddress("proxy.example.com", 8080)))
            .build();

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://httpbin.org/get"))
            .timeout(Duration.ofSeconds(30))
            .header("User-Agent", "Java HttpClient")
            .build();

        HttpResponse<String> response = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("HTTP Version: " + response.version());
        System.out.println("Status: " + response.statusCode());
    }
}
```

### Response Body Handlers

```java
import java.net.http.*;
import java.net.URI;
import java.nio.file.*;
import java.util.stream.*;

public class BodyHandlersDemo {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        var uri = URI.create("https://httpbin.org/get");
        var request = HttpRequest.newBuilder(uri).build();

        // String body
        HttpResponse<String> stringResponse = client.send(
            request,
            HttpResponse.BodyHandlers.ofString()
        );

        // Byte array
        HttpResponse<byte[]> byteResponse = client.send(
            request,
            HttpResponse.BodyHandlers.ofByteArray()
        );
        System.out.println("Bytes: " + byteResponse.body().length);

        // Stream of lines
        HttpResponse<Stream<String>> lineResponse = client.send(
            request,
            HttpResponse.BodyHandlers.ofLines()
        );
        lineResponse.body().limit(5).forEach(System.out::println);

        // Save to file
        HttpResponse<Path> fileResponse = client.send(
            request,
            HttpResponse.BodyHandlers.ofFile(Path.of("response.txt"))
        );
        System.out.println("Saved to: " + fileResponse.body());

        // Discard body
        HttpResponse<Void> discarded = client.send(
            request,
            HttpResponse.BodyHandlers.discarding()
        );
        System.out.println("Status only: " + discarded.statusCode());

        // Clean up
        Files.deleteIfExists(Path.of("response.txt"));
    }
}
```

### Hands-On Exercise 1: REST API Client

```java
import java.net.http.*;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

public class RestApiClient {
    private final HttpClient client;
    private final String baseUrl;

    public RestApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
    }

    public String get(String path) throws Exception {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Accept", "application/json")
            .GET()
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public CompletableFuture<String> getAsync(String path) {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Accept", "application/json")
            .GET()
            .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApply(HttpResponse::body);
    }

    public String post(String path, String json) throws Exception {
        var request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    public static void main(String[] args) throws Exception {
        var client = new RestApiClient("https://jsonplaceholder.typicode.com");

        // GET request
        System.out.println("GET /posts/1:");
        System.out.println(client.get("/posts/1"));

        // Multiple async requests
        System.out.println("\nAsync requests:");
        var futures = List.of("/posts/1", "/posts/2", "/posts/3").stream()
            .map(client::getAsync)
            .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        futures.forEach(f -> System.out.println(f.join().substring(0, 50) + "..."));
    }
}
```

---

## String Methods

Java 11 added several useful String methods.

```java
public class StringMethods {
    public static void main(String[] args) {
        // isBlank() - true if empty or only whitespace
        System.out.println("isBlank():");
        System.out.println("  ''.isBlank(): " + "".isBlank());           // true
        System.out.println("  '   '.isBlank(): " + "   ".isBlank());     // true
        System.out.println("  'hi'.isBlank(): " + "hi".isBlank());       // false
        System.out.println("  '\\n\\t'.isBlank(): " + "\n\t".isBlank()); // true

        // lines() - split into stream of lines
        System.out.println("\nlines():");
        String multiline = "Line 1\nLine 2\nLine 3";
        multiline.lines()
            .map(line -> "  > " + line)
            .forEach(System.out::println);

        // strip(), stripLeading(), stripTrailing()
        // Unlike trim(), handles Unicode whitespace
        System.out.println("\nstrip() vs trim():");
        String withWhitespace = "  \u2005Hello World\u2005  "; // Unicode whitespace
        System.out.println("  Original: '" + withWhitespace + "'");
        System.out.println("  trim(): '" + withWhitespace.trim() + "'");
        System.out.println("  strip(): '" + withWhitespace.strip() + "'");
        System.out.println("  stripLeading(): '" + withWhitespace.stripLeading() + "'");
        System.out.println("  stripTrailing(): '" + withWhitespace.stripTrailing() + "'");

        // repeat(n) - repeat string n times
        System.out.println("\nrepeat():");
        System.out.println("  'Ha'.repeat(3): " + "Ha".repeat(3));   // HaHaHa
        System.out.println("  '-'.repeat(10): " + "-".repeat(10));   // ----------
        System.out.println("  'abc'.repeat(0): '" + "abc".repeat(0) + "'"); // empty

        // Practical examples
        System.out.println("\nPractical examples:");

        // Filtering blank lines
        String text = """
            First line

            Second line

            Third line
            """;
        long nonBlankLines = text.lines()
            .filter(line -> !line.isBlank())
            .count();
        System.out.println("Non-blank lines: " + nonBlankLines);

        // Creating separators
        String separator = "=".repeat(40);
        System.out.println(separator);
        System.out.println("        CENTERED TITLE");
        System.out.println(separator);

        // Processing CSV-like data
        String csvData = "name,age,city\nAlice,30,NYC\nBob,25,LA";
        csvData.lines()
            .skip(1) // Skip header
            .map(line -> line.split(","))
            .forEach(parts -> System.out.printf(
                "  Name: %s, Age: %s, City: %s%n",
                parts[0], parts[1], parts[2]
            ));
    }
}
```

---

## Collection to Array

Java 11 added a new `toArray` method to Collection that takes an IntFunction.

```java
import java.util.*;

public class CollectionToArray {
    public static void main(String[] args) {
        List<String> list = List.of("a", "b", "c");

        // Before Java 11
        String[] array1 = list.toArray(new String[0]);
        String[] array2 = list.toArray(new String[list.size()]);

        // Java 11 - with method reference
        String[] array3 = list.toArray(String[]::new);

        System.out.println("Array: " + Arrays.toString(array3));

        // Works with any collection
        Set<Integer> set = Set.of(1, 2, 3, 4, 5);
        Integer[] intArray = set.toArray(Integer[]::new);
        System.out.println("Set array: " + Arrays.toString(intArray));

        // With custom types
        record Person(String name) {}
        List<Person> people = List.of(
            new Person("Alice"),
            new Person("Bob")
        );
        Person[] personArray = people.toArray(Person[]::new);
        System.out.println("People: " + Arrays.toString(personArray));
    }
}
```

---

## Local-Variable Syntax for Lambda Parameters

Java 11 allows `var` in lambda parameters, enabling annotations.

```java
import java.util.*;
import java.util.function.*;

public class VarInLambda {
    public static void main(String[] args) {
        // Without var (implicit types)
        BiFunction<String, String, String> concat1 =
            (a, b) -> a + b;

        // With explicit types
        BiFunction<String, String, String> concat2 =
            (String a, String b) -> a + b;

        // With var (Java 11)
        BiFunction<String, String, String> concat3 =
            (var a, var b) -> a + b;

        // Why use var? For annotations!
        // @Nullable var is valid, @Nullable alone without type is not
        BiFunction<String, String, String> concat4 =
            (@Deprecated var a, @Deprecated var b) -> a + b;

        // All parameters must use var or none
        // (var a, b) -> a + b;  // Invalid!
        // (var a, String b) -> a + b;  // Invalid!

        // Example with custom annotation
        processWithAnnotation();
    }

    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @java.lang.annotation.Target(java.lang.annotation.ElementType.PARAMETER)
    @interface NotNull {}

    static void processWithAnnotation() {
        List<String> names = List.of("Alice", "Bob", "Charlie");

        // Using var with annotation
        names.stream()
            .map((@NotNull var name) -> name.toUpperCase())
            .forEach(System.out::println);
    }
}
```

---

## Files Methods

Java 11 added convenient methods for reading and writing files.

```java
import java.nio.file.*;
import java.io.*;

public class FilesMethods {
    public static void main(String[] args) throws IOException {
        Path path = Path.of("test-file.txt");

        // writeString - write string directly to file
        String content = "Hello, Java 11!\nThis is a test file.";
        Files.writeString(path, content);
        System.out.println("File written");

        // readString - read entire file as string
        String read = Files.readString(path);
        System.out.println("File content:\n" + read);

        // With charset
        Files.writeString(path, "UTF-8 content: café",
            java.nio.charset.StandardCharsets.UTF_8);
        String utf8Content = Files.readString(path,
            java.nio.charset.StandardCharsets.UTF_8);
        System.out.println("UTF-8 content: " + utf8Content);

        // With OpenOption
        Files.writeString(path, "\nAppended line",
            StandardOpenOption.APPEND);
        System.out.println("After append:\n" + Files.readString(path));

        // Combining with other operations
        Path source = Path.of("source.txt");
        Path dest = Path.of("dest.txt");

        Files.writeString(source, "Source content");

        // Read, transform, write
        String transformed = Files.readString(source).toUpperCase();
        Files.writeString(dest, transformed);

        System.out.println("Transformed content: " + Files.readString(dest));

        // Cleanup
        Files.deleteIfExists(path);
        Files.deleteIfExists(source);
        Files.deleteIfExists(dest);
    }
}
```

---

## Optional.isEmpty()

Java 11 added `isEmpty()` as the opposite of `isPresent()`.

```java
import java.util.*;

public class OptionalIsEmpty {
    public static void main(String[] args) {
        Optional<String> present = Optional.of("Hello");
        Optional<String> empty = Optional.empty();

        // Before Java 11
        if (!present.isPresent()) {
            System.out.println("Not present");
        }

        // Java 11
        if (empty.isEmpty()) {
            System.out.println("Empty!");
        }

        // Cleaner conditionals
        Optional<String> maybeValue = findValue();

        // Before
        if (!maybeValue.isPresent()) {
            // handle missing
        }

        // After
        if (maybeValue.isEmpty()) {
            // handle missing - more readable
        }

        // In validation
        public record User(String name, String email) {
            public User {
                if (Optional.ofNullable(name).isEmpty()) {
                    throw new IllegalArgumentException("Name required");
                }
            }
        }

        // Stream filtering
        List<Optional<String>> optionals = List.of(
            Optional.of("a"),
            Optional.empty(),
            Optional.of("b")
        );

        long emptyCount = optionals.stream()
            .filter(Optional::isEmpty)
            .count();
        System.out.println("Empty optionals: " + emptyCount);
    }

    static Optional<String> findValue() {
        return Optional.empty();
    }
}
```

---

## Running Java Files Directly

Java 11 allows running single-file Java programs directly without explicit compilation.

### Single File Execution

```bash
# Create a Java file
cat > Hello.java << 'EOF'
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello from Java 11!");
    }
}
EOF

# Run directly without javac
java Hello.java

# Output: Hello from Java 11!
```

### Shebang Scripts (Unix/Linux/macOS)

```java
#!/usr/bin/java --source 11

public class Script {
    public static void main(String[] args) {
        System.out.println("Running as script!");
        System.out.println("Arguments: " + java.util.Arrays.toString(args));
        System.out.println("Java version: " + System.getProperty("java.version"));
    }
}
```

```bash
# Make executable
chmod +x Script

# Run as script
./Script arg1 arg2
```

### Practical Script Example

```java
#!/usr/bin/java --source 11

import java.nio.file.*;
import java.util.stream.*;

public class FindLargeFiles {
    public static void main(String[] args) throws Exception {
        Path dir = args.length > 0 ? Path.of(args[0]) : Path.of(".");
        long minSize = args.length > 1 ? Long.parseLong(args[1]) : 1_000_000;

        System.out.println("Finding files larger than " + minSize + " bytes in " + dir);

        try (Stream<Path> paths = Files.walk(dir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> {
                     try {
                         return Files.size(p) > minSize;
                     } catch (Exception e) {
                         return false;
                     }
                 })
                 .forEach(p -> {
                     try {
                         System.out.printf("%,d bytes - %s%n", Files.size(p), p);
                     } catch (Exception e) {
                         // ignore
                     }
                 });
        }
    }
}
```

---

## Nest-Based Access Control

Java 11 improved access between nested classes at the JVM level.

```java
import java.lang.reflect.*;

public class NestBasedAccess {
    private String secret = "hidden";

    class Inner {
        void accessOuter() {
            // Direct access to outer's private members
            // Now works at bytecode level too (not just compiler sugar)
            System.out.println("Secret: " + secret);
        }
    }

    public static void main(String[] args) {
        // New reflection methods for nests
        Class<?> outer = NestBasedAccess.class;
        Class<?> inner = Inner.class;

        // Get nest host (top-level class of the nest)
        System.out.println("Nest host of outer: " + outer.getNestHost());
        System.out.println("Nest host of inner: " + inner.getNestHost());

        // Get nest members
        System.out.println("\nNest members:");
        for (Class<?> member : outer.getNestMembers()) {
            System.out.println("  " + member.getSimpleName());
        }

        // Check if classes are nestmates
        System.out.println("\nAre nestmates: " + outer.isNestmateOf(inner));

        // Demonstration of access
        var obj = new NestBasedAccess();
        var innerObj = obj.new Inner();
        innerObj.accessOuter();
    }
}
```

---

## Removed Features

Java 11 removed several deprecated features:

### Removed Modules/APIs

```java
// These are NO LONGER available in Java 11:

// 1. Java EE modules (use external dependencies)
// - java.xml.ws (JAX-WS)
// - java.xml.bind (JAXB)
// - java.activation
// - java.xml.ws.annotation
// - java.corba
// - java.transaction

// 2. JavaFX (now separate project)
// - javafx.base
// - javafx.controls
// - etc.

// 3. Nashorn JavaScript Engine (deprecated)
// import jdk.nashorn.api.scripting.*; // Deprecated

// 4. Pack200 tools (deprecated, removed in Java 14)

// Migration: Add dependencies via Maven/Gradle
/*
<!-- For JAXB -->
<dependency>
    <groupId>jakarta.xml.bind</groupId>
    <artifactId>jakarta.xml.bind-api</artifactId>
    <version>4.0.0</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jaxb</groupId>
    <artifactId>jaxb-runtime</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- For JavaFX -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>21</version>
</dependency>
*/
```

---

## Summary

| Feature | Description |
|---------|-------------|
| HTTP Client API | Modern HTTP/2 client (standardized) |
| String Methods | `isBlank()`, `lines()`, `strip()`, `repeat()` |
| Collection.toArray() | `toArray(IntFunction)` for cleaner syntax |
| var in Lambda | Allow annotations on lambda parameters |
| Files Methods | `readString()`, `writeString()` |
| Optional.isEmpty() | Opposite of `isPresent()` |
| Single-File Execution | Run `.java` files directly |
| Nest-Based Access | Better nested class access at JVM level |

---

## Hands-On Challenge

**HTTP Client Challenge:**

Build a simple REST client that:
1. Fetches posts from JSONPlaceholder API
2. Supports both sync and async operations
3. Handles errors gracefully
4. Parses JSON responses (using simple string parsing or add a JSON library)

```java
// Starter code
public class RestClientChallenge {
    private final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";

    // Implement: getPost(int id), getPosts(), createPost(String title, String body)
    // Implement: async versions of above
    // Implement: error handling for 404, network errors, etc.

    public static void main(String[] args) throws Exception {
        var client = new RestClientChallenge();
        // Test your implementation
    }
}
```

[← Java 10 Features](java-10.md) | [Java 12 Features →](java-12.md)
