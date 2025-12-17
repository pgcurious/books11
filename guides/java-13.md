# Java 13 Features Guide

**Release Date:** September 2019
**Type:** Feature Release

Java 13 continued the switch expression preview and introduced text blocks as a preview feature.

## Table of Contents
- [Text Blocks (Preview)](#text-blocks-preview)
- [Switch Expressions (2nd Preview)](#switch-expressions-2nd-preview)
- [Dynamic CDS Archives](#dynamic-cds-archives)
- [ZGC: Uncommit Unused Memory](#zgc-uncommit-unused-memory)

---

## Text Blocks (Preview)

Text blocks allow multi-line string literals without escape sequences.

> Note: Enable with `--enable-preview`. Standard in Java 15.

### Basic Text Blocks

```java
public class TextBlocksBasics {
    public static void main(String[] args) {
        // Before text blocks - HTML
        String htmlOld = "<html>\n" +
                         "    <body>\n" +
                         "        <h1>Hello</h1>\n" +
                         "    </body>\n" +
                         "</html>";

        // With text blocks
        String html = """
                <html>
                    <body>
                        <h1>Hello</h1>
                    </body>
                </html>
                """;
        System.out.println("HTML:");
        System.out.println(html);

        // JSON example
        String jsonOld = "{\n" +
                         "    \"name\": \"John\",\n" +
                         "    \"age\": 30,\n" +
                         "    \"city\": \"New York\"\n" +
                         "}";

        String json = """
                {
                    "name": "John",
                    "age": 30,
                    "city": "New York"
                }
                """;
        System.out.println("JSON:");
        System.out.println(json);

        // SQL example
        String sql = """
                SELECT id, name, email
                FROM users
                WHERE status = 'ACTIVE'
                    AND created_at > '2024-01-01'
                ORDER BY name
                """;
        System.out.println("SQL:");
        System.out.println(sql);
    }
}
```

### Indentation Rules

```java
public class TextBlockIndentation {
    public static void main(String[] args) {
        // Indentation is relative to closing """
        // The closing """ position determines base indentation

        // No indentation (closing at column 0)
        String noIndent = """
line 1
line 2
""";

        // With indentation (closing indented)
        String indented = """
                line 1
                line 2
                """;

        // Extra indentation preserved
        String extraIndent = """
                line 1
                    indented line
                        more indented
                """;

        System.out.println("No indent:\n" + noIndent);
        System.out.println("Indented:\n'" + indented + "'");
        System.out.println("Extra indent:\n" + extraIndent);

        // Tip: Use indent() to see the indentation
        String block = """
                First
                    Second
                Third
                """;
        System.out.println("Lines with markers:");
        block.lines().forEach(line -> System.out.println("|" + line + "|"));
    }
}
```

### Escape Sequences in Text Blocks

```java
public class TextBlockEscapes {
    public static void main(String[] args) {
        // Standard escapes work
        String withEscapes = """
                Tab:\there
                Newline in middle\nof line
                Quote: \"quoted\"
                Backslash: \\
                """;
        System.out.println("Escapes:");
        System.out.println(withEscapes);

        // Triple quotes need escaping
        String tripleQuote = """
                Text with \"""triple quotes\"""
                """;
        System.out.println("Triple quotes: " + tripleQuote);

        // New escape: \ at end of line prevents newline (Java 14+)
        // This allows long lines to be split
        String longLine = """
                This is a very long line that \
                continues on the next line but \
                appears as one line in output.
                """;
        System.out.println("Long line: " + longLine);

        // New escape: \s for explicit space (Java 14+)
        String withTrailingSpace = """
                Line with trailing space:\s
                Next line
                """;
        System.out.println("With \\s:");
        withTrailingSpace.lines().forEach(line ->
            System.out.println("|" + line + "|"));
    }
}
```

### Text Block Methods

```java
public class TextBlockMethods {
    public static void main(String[] args) {
        String block = """
                    First line
                    Second line
                    Third line
                """;

        // stripIndent() - remove incidental indentation
        String manual = "    First\n    Second\n    Third";
        System.out.println("Before stripIndent:");
        manual.lines().forEach(l -> System.out.println("|" + l + "|"));
        System.out.println("After stripIndent:");
        manual.stripIndent().lines().forEach(l -> System.out.println("|" + l + "|"));

        // translateEscapes() - process escape sequences
        String withEscapes = "Hello\\nWorld\\t!";
        System.out.println("\nBefore translateEscapes: " + withEscapes);
        System.out.println("After translateEscapes: " + withEscapes.translateEscapes());

        // formatted() - like String.format but as instance method
        String template = """
                Dear %s,

                Your order #%d has been shipped.
                Expected delivery: %s

                Thank you!
                """;

        String formatted = template.formatted("Alice", 12345, "Monday");
        System.out.println("\nFormatted letter:");
        System.out.println(formatted);
    }
}
```

### Practical Examples

```java
public class TextBlockPractical {
    public static void main(String[] args) {
        // API Response template
        String responseTemplate = """
                {
                    "status": "%s",
                    "code": %d,
                    "message": "%s",
                    "data": %s
                }
                """;

        String response = responseTemplate.formatted(
            "success", 200, "Operation completed",
            """
            {"id": 1, "name": "Test"}"""
        );
        System.out.println("API Response:\n" + response);

        // Email template
        String emailTemplate = """
                Subject: Welcome to %s!

                Hello %s,

                Thank you for joining us. Your account has been created.

                Username: %s
                Temporary Password: %s

                Please log in and change your password.

                Best regards,
                The Team
                """;

        String email = emailTemplate.formatted(
            "MyApp", "John", "john@example.com", "temp123"
        );
        System.out.println("Email:\n" + email);

        // Code generation
        String classTemplate = """
                public class %s {
                    private final %s %s;

                    public %s(%s %s) {
                        this.%s = %s;
                    }

                    public %s get%s() {
                        return %s;
                    }
                }
                """;

        String generatedClass = classTemplate.formatted(
            "Person",           // class name
            "String", "name",   // field
            "Person",           // constructor
            "String", "name",   // param
            "name", "name",     // assignment
            "String", "Name",   // getter return & name
            "name"              // return field
        );
        System.out.println("Generated class:\n" + generatedClass);
    }
}
```

---

## Switch Expressions (2nd Preview)

Java 13 refined switch expressions with the `yield` keyword.

```java
public class SwitchYield {
    public static void main(String[] args) {
        int day = 3;

        // Arrow case with block requires yield
        String dayType = switch (day) {
            case 1, 2, 3, 4, 5 -> {
                System.out.println("Processing weekday");
                yield "Weekday";  // yield returns value from block
            }
            case 6, 7 -> {
                System.out.println("Processing weekend");
                yield "Weekend";
            }
            default -> {
                System.out.println("Invalid day");
                yield "Unknown";
            }
        };
        System.out.println("Day type: " + dayType);

        // yield also works with colon-style cases
        String result = switch (day) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
                System.out.println("It's a weekday");
                yield "Work day";  // yield instead of break
            case 6:
            case 7:
                System.out.println("It's the weekend");
                yield "Rest day";
            default:
                yield "Invalid";
        };
        System.out.println("Result: " + result);
    }
}
```

---

## Dynamic CDS Archives

Java 13 simplified Application Class-Data Sharing.

```bash
# Create archive at application exit
java -XX:ArchiveClassesAtExit=app.jsa -cp myapp.jar MyApp

# Use the archive
java -XX:SharedArchiveFile=app.jsa -cp myapp.jar MyApp
```

Benefits:
- No need to pre-generate class list
- Archives created automatically
- Faster subsequent startups

---

## ZGC: Uncommit Unused Memory

ZGC can now return unused memory to the operating system.

```bash
# Enable ZGC (experimental in Java 13)
java -XX:+UnlockExperimentalVMOptions -XX:+UseZGC MyApp

# Configure uncommit delay (milliseconds)
java -XX:+UnlockExperimentalVMOptions -XX:+UseZGC \
     -XX:ZUncommitDelay=300 MyApp
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Text Blocks | Preview | Multi-line string literals |
| Switch Expressions | 2nd Preview | Added yield keyword |
| Dynamic CDS | Standard | Simplified class-data sharing |
| ZGC Uncommit | Standard | Return memory to OS |

---

## Hands-On Challenge

Create a template engine using text blocks:

```java
public class TemplateEngine {
    // Implement a simple template system that:
    // 1. Loads templates as text blocks
    // 2. Replaces {{placeholder}} with values
    // 3. Supports nested templates

    public static void main(String[] args) {
        String template = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>{{title}}</title>
                </head>
                <body>
                    <h1>{{heading}}</h1>
                    <p>{{content}}</p>
                    {{#each items}}
                    <li>{{item}}</li>
                    {{/each}}
                </body>
                </html>
                """;

        // Your implementation here
    }
}
```

[← Java 12 Features](java-12.md) | [Java 14 Features →](java-14.md)
