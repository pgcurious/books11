# Java 18 Features Guide

**Release Date:** March 2022
**Type:** Feature Release

Java 18 introduced UTF-8 as default charset and a simple web server for development.

## Table of Contents
- [UTF-8 by Default](#utf-8-by-default)
- [Simple Web Server](#simple-web-server)
- [Code Snippets in JavaDoc](#code-snippets-in-javadoc)
- [Pattern Matching for switch (2nd Preview)](#pattern-matching-for-switch-2nd-preview)
- [Vector API (3rd Incubator)](#vector-api-3rd-incubator)
- [Internet-Address Resolution SPI](#internet-address-resolution-spi)

---

## UTF-8 by Default

Java 18 made UTF-8 the default charset for all standard Java APIs.

```java
import java.nio.charset.*;
import java.io.*;

public class UTF8Default {
    public static void main(String[] args) throws Exception {
        // Check default charset
        System.out.println("Default charset: " + Charset.defaultCharset());
        // Java 18+: UTF-8 (previously system-dependent)

        // All these now use UTF-8 by default:

        // 1. FileReader / FileWriter
        try (var writer = new FileWriter("test.txt")) {
            writer.write("Hello, 世界! 🌍");
        }
        try (var reader = new FileReader("test.txt")) {
            char[] buffer = new char[100];
            int length = reader.read(buffer);
            System.out.println("Read: " + new String(buffer, 0, length));
        }

        // 2. InputStreamReader / OutputStreamWriter
        // (without charset parameter)

        // 3. PrintStream (System.out)
        System.out.println("Console output: café, naïve, 日本語");

        // 4. Formatter, Scanner
        try (var scanner = new java.util.Scanner(new File("test.txt"))) {
            System.out.println("Scanned: " + scanner.nextLine());
        }

        // 5. URLEncoder / URLDecoder
        String encoded = java.net.URLEncoder.encode("hello world", Charset.defaultCharset());
        System.out.println("URL encoded: " + encoded);

        // Benefits:
        // - Consistent behavior across platforms
        // - No more encoding issues between Windows/Unix
        // - Better internationalization support

        // If you need legacy behavior, specify charset explicitly:
        try (var legacyWriter = new FileWriter("legacy.txt",
                Charset.forName(System.getProperty("native.encoding", "UTF-8")))) {
            legacyWriter.write("Legacy encoding");
        }

        // Cleanup
        new File("test.txt").delete();
        new File("legacy.txt").delete();
    }
}
```

---

## Simple Web Server

Java 18 includes a command-line tool and API for a simple HTTP file server.

### Command Line Usage

```bash
# Start simple web server (serves current directory)
jwebserver

# With options
jwebserver -p 9000 -d /path/to/directory -o verbose

# Options:
# -p, --port      Port number (default: 8000)
# -d, --directory Directory to serve (default: current)
# -o, --output    Output format: none, info, verbose
# -b, --bind      Address to bind (default: 127.0.0.1)
```

### Programmatic Usage

```java
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.file.*;

public class SimpleWebServerDemo {
    public static void main(String[] args) throws Exception {
        // Create simple file server
        Path root = Path.of(".");
        HttpServer server = SimpleFileServer.createFileServer(
            new InetSocketAddress(8080),
            root,
            SimpleFileServer.OutputLevel.VERBOSE
        );

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("Serving: " + root.toAbsolutePath());
        System.out.println("Press Enter to stop...");
        System.in.read();
        server.stop(0);
    }
}
```

### Custom Handler with File Server

```java
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.file.*;
import java.io.*;

public class CustomWebServer {
    public static void main(String[] args) throws Exception {
        // Create server with custom and file handlers
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0);

        // API endpoint
        server.createContext("/api/hello", exchange -> {
            String response = """
                {"message": "Hello, World!", "timestamp": %d}
                """.formatted(System.currentTimeMillis());

            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // Health check
        server.createContext("/health", exchange -> {
            String response = "OK";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        });

        // Static files
        HttpHandler fileHandler = SimpleFileServer.createFileHandler(
            Path.of("./static")
        );
        server.createContext("/", fileHandler);

        server.start();
        System.out.println("Server started at http://localhost:8080");
        System.out.println("API: http://localhost:8080/api/hello");
        System.out.println("Health: http://localhost:8080/health");
    }
}
```

### Filter Example

```java
import com.sun.net.httpserver.*;
import java.net.*;
import java.nio.file.*;

public class FilteredWebServer {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0);

        // Create output filter (logging)
        Filter outputFilter = SimpleFileServer.createOutputFilter(
            System.out,
            SimpleFileServer.OutputLevel.INFO
        );

        // Create context with filter
        HttpHandler fileHandler = SimpleFileServer.createFileHandler(Path.of("."));
        HttpContext context = server.createContext("/", fileHandler);
        context.getFilters().add(outputFilter);

        // Custom filter for authentication
        Filter authFilter = new Filter() {
            @Override
            public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
                String auth = exchange.getRequestHeaders().getFirst("Authorization");
                if (auth == null && exchange.getRequestURI().getPath().startsWith("/private")) {
                    exchange.sendResponseHeaders(401, -1);
                    return;
                }
                chain.doFilter(exchange);
            }

            @Override
            public String description() {
                return "Authentication filter";
            }
        };
        context.getFilters().add(authFilter);

        server.start();
        System.out.println("Server with filters started at http://localhost:8080");
    }
}
```

---

## Code Snippets in JavaDoc

Java 18 introduced the `@snippet` tag for including code examples in documentation.

```java
/**
 * A utility class for string operations.
 *
 * <h2>Basic Usage</h2>
 *
 * {@snippet :
 * StringUtils utils = new StringUtils();
 * String result = utils.reverse("hello");
 * System.out.println(result); // "olleh"
 * }
 *
 * <h2>With Highlighting</h2>
 *
 * {@snippet :
 * // @highlight substring="reverse" type="highlighted"
 * String reversed = utils.reverse("world");
 * // @highlight substring="reversed" type="italic"
 * System.out.println(reversed);
 * }
 *
 * <h2>External Snippet</h2>
 *
 * {@snippet file="StringUtilsExample.java" region="example"}
 *
 * <h2>With Replacement</h2>
 *
 * {@snippet :
 * // @replace regex='".*"' replacement="..."
 * String input = "actual input here";
 * // @end
 * }
 */
public class StringUtils {
    /**
     * Reverses the given string.
     *
     * Example:
     * {@snippet :
     * // @link substring="reverse" target="#reverse(String)"
     * String result = reverse("hello");
     * assert result.equals("olleh");
     * }
     *
     * @param input the string to reverse
     * @return the reversed string
     */
    public String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }

    /**
     * Counts occurrences of a character.
     *
     * {@snippet lang="java":
     * // Count 'l' in "hello"
     * int count = countChar("hello", 'l');
     * System.out.println(count); // @highlight substring="2"
     * }
     *
     * @param str the string to search
     * @param ch the character to count
     * @return the count
     */
    public int countChar(String str, char ch) {
        return (int) str.chars().filter(c -> c == ch).count();
    }
}
```

### External Snippet Files

Create `snippet-files/StringUtilsExample.java`:
```java
public class StringUtilsExample {
    public static void main(String[] args) {
        // @start region="example"
        StringUtils utils = new StringUtils();
        String original = "Hello, World!";
        String reversed = utils.reverse(original);
        System.out.println("Original: " + original);
        System.out.println("Reversed: " + reversed);
        // @end
    }
}
```

---

## Pattern Matching for switch (2nd Preview)

Refinements to pattern matching in switch.

```java
public class PatternMatchingSwitch2 {
    public static void main(String[] args) {
        // Dominance checking - more specific patterns first
        Object obj = "Hello";

        String result = switch (obj) {
            // String pattern must come before CharSequence
            case String s when s.length() > 10 -> "Long string";
            case String s -> "String: " + s;
            // case CharSequence cs -> "CharSequence"; // Would be unreachable
            default -> "Other";
        };
        System.out.println(result);

        // Exhaustiveness with sealed types
        testShapes();
    }

    sealed interface Shape permits Circle, Rectangle, Square {}
    record Circle(double radius) implements Shape {}
    record Rectangle(double w, double h) implements Shape {}
    record Square(double side) implements Shape {}

    static void testShapes() {
        Shape[] shapes = {
            new Circle(5),
            new Rectangle(10, 20),
            new Square(8)
        };

        for (Shape shape : shapes) {
            double area = switch (shape) {
                case Circle c -> Math.PI * c.radius() * c.radius();
                case Rectangle r -> r.w() * r.h();
                case Square s -> s.side() * s.side();
            };
            System.out.printf("%s area: %.2f%n",
                shape.getClass().getSimpleName(), area);
        }
    }
}
```

---

## Internet-Address Resolution SPI

Java 18 added a Service Provider Interface for customizing host name and address resolution.

```java
import java.net.*;
import java.net.spi.*;
import java.util.*;
import java.util.stream.*;

public class AddressResolutionDemo {
    public static void main(String[] args) throws Exception {
        // Default resolution
        InetAddress address = InetAddress.getByName("localhost");
        System.out.println("localhost resolves to: " + address);

        // Get all addresses
        InetAddress[] all = InetAddress.getAllByName("google.com");
        System.out.println("\ngoogle.com addresses:");
        for (InetAddress addr : all) {
            System.out.println("  " + addr);
        }

        // Reverse lookup
        String hostname = address.getHostName();
        System.out.println("\nReverse lookup: " + hostname);

        // Custom resolver would implement InetAddressResolver
        // and be registered via InetAddressResolverProvider
    }
}

// Custom resolver example (for testing/mocking)
class CustomAddressResolverProvider extends InetAddressResolverProvider {
    @Override
    public InetAddressResolver get(Configuration configuration) {
        return new InetAddressResolver() {
            @Override
            public Stream<InetAddress> lookupByName(String host,
                    LookupPolicy lookupPolicy) throws UnknownHostException {
                // Custom resolution logic
                if (host.equals("custom.local")) {
                    return Stream.of(InetAddress.getByAddress(
                        host, new byte[]{127, 0, 0, 99}));
                }
                // Fall back to system resolver
                return configuration.builtinResolver()
                    .lookupByName(host, lookupPolicy);
            }

            @Override
            public String lookupByAddress(byte[] addr)
                    throws UnknownHostException {
                if (Arrays.equals(addr, new byte[]{127, 0, 0, 99})) {
                    return "custom.local";
                }
                return configuration.builtinResolver().lookupByAddress(addr);
            }
        };
    }

    @Override
    public String name() {
        return "Custom Resolver";
    }
}
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| UTF-8 by Default | Standard | UTF-8 as default charset |
| Simple Web Server | Standard | `jwebserver` tool and API |
| Code Snippets | Standard | `@snippet` tag in JavaDoc |
| Pattern Matching Switch | 2nd Preview | Refinements to switch patterns |
| Vector API | 3rd Incubator | SIMD operations |
| Address Resolution SPI | Standard | Custom DNS resolution |

[← Java 17 Features](java-17.md) | [Java 19 Features →](java-19.md)
