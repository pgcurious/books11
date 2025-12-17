# Java 16 Features Guide

**Release Date:** March 2021
**Type:** Feature Release

Java 16 finalized Records and Pattern Matching for instanceof as standard features.

## Table of Contents
- [Records (Standard)](#records-standard)
- [Pattern Matching for instanceof (Standard)](#pattern-matching-for-instanceof-standard)
- [Sealed Classes (2nd Preview)](#sealed-classes-2nd-preview)
- [Stream.toList()](#streamtolist)
- [Day Period Support](#day-period-support)
- [Vector API (Incubator)](#vector-api-incubator)
- [Foreign Linker API (Incubator)](#foreign-linker-api-incubator)

---

## Records (Standard)

Records are now a standard feature in Java 16.

### Complete Record Features

```java
import java.util.*;

// Basic record
record Point(int x, int y) {}

// Record with validation
record Email(String address) {
    public Email {
        Objects.requireNonNull(address);
        if (!address.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }
        address = address.toLowerCase();
    }
}

// Record with additional methods
record Range(int start, int end) {
    public Range {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
    }

    public int length() {
        return end - start;
    }

    public boolean contains(int value) {
        return value >= start && value <= end;
    }

    public Range overlap(Range other) {
        int newStart = Math.max(this.start, other.start);
        int newEnd = Math.min(this.end, other.end);
        if (newStart > newEnd) return null;
        return new Range(newStart, newEnd);
    }
}

// Record implementing interface
interface Printable {
    String toPrettyString();
}

record Person(String name, int age) implements Printable {
    @Override
    public String toPrettyString() {
        return String.format("Name: %s, Age: %d", name, age);
    }
}

// Generic record
record Pair<T, U>(T first, U second) {
    public static <T, U> Pair<T, U> of(T first, U second) {
        return new Pair<>(first, second);
    }
}

public class RecordsStandard {
    public static void main(String[] args) {
        // Basic usage
        Point p = new Point(10, 20);
        System.out.println("Point: " + p);
        System.out.println("X: " + p.x() + ", Y: " + p.y());

        // With validation
        Email email = new Email("USER@Example.COM");
        System.out.println("Email: " + email.address());

        // With methods
        Range r1 = new Range(0, 100);
        Range r2 = new Range(50, 150);
        System.out.println("Overlap: " + r1.overlap(r2));

        // Generic record
        Pair<String, Integer> pair = Pair.of("Age", 30);
        System.out.println("Pair: " + pair);

        // Records in collections
        List<Person> people = List.of(
            new Person("Alice", 30),
            new Person("Bob", 25),
            new Person("Charlie", 35)
        );

        people.stream()
            .filter(person -> person.age() >= 30)
            .forEach(person -> System.out.println(person.toPrettyString()));

        // Records work great with streams
        var avgAge = people.stream()
            .mapToInt(Person::age)
            .average()
            .orElse(0);
        System.out.println("Average age: " + avgAge);

        // Grouping by field
        var byAge = people.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                p2 -> p2.age() >= 30 ? "30+" : "Under 30"
            ));
        System.out.println("By age group: " + byAge);
    }
}
```

### Local Records in Methods

```java
import java.util.*;
import java.util.stream.*;

public class LocalRecordsDemo {
    public static void main(String[] args) {
        List<String> csvData = List.of(
            "1,Alice,Developer,75000",
            "2,Bob,Designer,65000",
            "3,Charlie,Developer,80000",
            "4,Diana,Manager,90000"
        );

        // Local record for parsing
        record Employee(int id, String name, String role, double salary) {
            static Employee parse(String csv) {
                String[] parts = csv.split(",");
                return new Employee(
                    Integer.parseInt(parts[0]),
                    parts[1],
                    parts[2],
                    Double.parseDouble(parts[3])
                );
            }
        }

        List<Employee> employees = csvData.stream()
            .map(Employee::parse)
            .toList();

        // Analysis with local record
        record RoleStats(String role, long count, double avgSalary) {}

        Map<String, RoleStats> statsByRole = employees.stream()
            .collect(Collectors.groupingBy(
                Employee::role,
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    list -> new RoleStats(
                        list.get(0).role(),
                        list.size(),
                        list.stream().mapToDouble(Employee::salary).average().orElse(0)
                    )
                )
            ));

        System.out.println("Statistics by role:");
        statsByRole.values().forEach(stats ->
            System.out.printf("  %s: %d employees, avg $%.0f%n",
                stats.role(), stats.count(), stats.avgSalary()));
    }
}
```

---

## Pattern Matching for instanceof (Standard)

Pattern matching for instanceof is now standard in Java 16.

```java
public class PatternMatchingStandard {
    public static void main(String[] args) {
        Object[] values = {42, "Hello", 3.14, true, null, List.of(1, 2, 3)};

        for (Object value : values) {
            System.out.println(describe(value));
        }
    }

    static String describe(Object obj) {
        // Pattern matching with instanceof
        if (obj instanceof Integer i) {
            return "Integer: " + i + " (squared: " + i * i + ")";
        } else if (obj instanceof String s) {
            return "String: \"" + s + "\" (length: " + s.length() + ")";
        } else if (obj instanceof Double d) {
            return "Double: " + d + " (rounded: " + Math.round(d) + ")";
        } else if (obj instanceof Boolean b) {
            return "Boolean: " + (b ? "YES" : "NO");
        } else if (obj instanceof java.util.List<?> list) {
            return "List with " + list.size() + " elements";
        } else if (obj == null) {
            return "null value";
        } else {
            return "Unknown type: " + obj.getClass().getSimpleName();
        }
    }
}
```

### Pattern Matching with Guards

```java
public class PatternMatchingGuards {
    record Person(String name, int age) {}

    public static void main(String[] args) {
        Object[] items = {
            new Person("Alice", 25),
            new Person("Bob", 17),
            "Hello",
            42,
            -10
        };

        for (Object item : items) {
            System.out.println(categorize(item));
        }
    }

    static String categorize(Object obj) {
        // Pattern with additional condition
        if (obj instanceof Person p && p.age() >= 18) {
            return p.name() + " is an adult";
        } else if (obj instanceof Person p) {
            return p.name() + " is a minor";
        } else if (obj instanceof String s && s.length() > 0) {
            return "Non-empty string: " + s;
        } else if (obj instanceof Integer i && i > 0) {
            return "Positive integer: " + i;
        } else if (obj instanceof Integer i) {
            return "Non-positive integer: " + i;
        }
        return "Other: " + obj;
    }
}
```

---

## Stream.toList()

Java 16 added a convenient `toList()` method to Stream.

```java
import java.util.*;
import java.util.stream.*;

public class StreamToList {
    public static void main(String[] args) {
        // Before Java 16
        List<String> list1 = Stream.of("a", "b", "c")
            .collect(Collectors.toList());

        // Java 16
        List<String> list2 = Stream.of("a", "b", "c")
            .toList();

        System.out.println("List: " + list2);

        // The returned list is unmodifiable!
        try {
            list2.add("d");
        } catch (UnsupportedOperationException e) {
            System.out.println("Cannot modify - list is unmodifiable");
        }

        // For mutable list, still use Collectors
        List<String> mutableList = Stream.of("a", "b", "c")
            .collect(Collectors.toCollection(ArrayList::new));
        mutableList.add("d");
        System.out.println("Mutable list: " + mutableList);

        // Practical example
        List<Integer> evenSquares = IntStream.rangeClosed(1, 10)
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .boxed()
            .toList();
        System.out.println("Even squares: " + evenSquares);

        // With null handling
        List<String> withNulls = Stream.of("a", null, "b")
            .toList();  // Allows nulls!
        System.out.println("With nulls: " + withNulls);

        // Collectors.toList() also allows nulls in Java 16+
        // But Collectors.toUnmodifiableList() does NOT allow nulls
    }
}
```

---

## Day Period Support

Java 16 added support for day periods in date formatting (morning, afternoon, evening, night).

```java
import java.time.*;
import java.time.format.*;
import java.util.Locale;

public class DayPeriodDemo {
    public static void main(String[] args) {
        // Day period formatting with 'B' pattern
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm B");

        LocalTime[] times = {
            LocalTime.of(6, 0),   // Morning
            LocalTime.of(12, 0),  // Noon
            LocalTime.of(14, 30), // Afternoon
            LocalTime.of(18, 0),  // Evening
            LocalTime.of(22, 0),  // Night
            LocalTime.of(3, 0)    // Night
        };

        System.out.println("Day periods (US):");
        for (LocalTime time : times) {
            System.out.println("  " + time + " -> " + time.format(formatter));
        }

        // Different locales
        System.out.println("\nDay periods in different locales (14:30):");
        LocalTime afternoon = LocalTime.of(14, 30);

        Locale[] locales = {
            Locale.US,
            Locale.GERMANY,
            Locale.FRANCE,
            Locale.JAPAN,
            Locale.CHINA
        };

        for (Locale locale : locales) {
            DateTimeFormatter localFormatter =
                DateTimeFormatter.ofPattern("h:mm B", locale);
            System.out.printf("  %s: %s%n",
                locale.getDisplayCountry(),
                afternoon.format(localFormatter));
        }

        // With full date-time
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter fullFormatter =
            DateTimeFormatter.ofPattern("EEEE, MMMM d 'at' h:mm B");
        System.out.println("\nNow: " + now.format(fullFormatter));
    }
}
```

---

## Vector API (Incubator)

The Vector API enables vector computations that reliably compile at runtime to optimal vector instructions.

```java
// Note: Requires: --add-modules jdk.incubator.vector
import jdk.incubator.vector.*;

public class VectorAPIDemo {
    public static void main(String[] args) {
        // SIMD operations for better performance
        float[] a = {1, 2, 3, 4, 5, 6, 7, 8};
        float[] b = {1, 2, 3, 4, 5, 6, 7, 8};
        float[] c = new float[8];

        // Scalar (traditional)
        long start = System.nanoTime();
        for (int i = 0; i < a.length; i++) {
            c[i] = a[i] * b[i];
        }
        long scalarTime = System.nanoTime() - start;

        // Vector API
        VectorSpecies<Float> SPECIES = FloatVector.SPECIES_256;

        start = System.nanoTime();
        int i = 0;
        for (; i < SPECIES.loopBound(a.length); i += SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            FloatVector vc = va.mul(vb);
            vc.intoArray(c, i);
        }
        // Handle tail
        for (; i < a.length; i++) {
            c[i] = a[i] * b[i];
        }
        long vectorTime = System.nanoTime() - start;

        System.out.println("Result: " + java.util.Arrays.toString(c));
        System.out.println("Scalar time: " + scalarTime + " ns");
        System.out.println("Vector time: " + vectorTime + " ns");
    }
}
```

---

## Foreign Linker API (Incubator)

The Foreign Linker API allows calling native code without JNI.

```java
// Note: Requires --add-modules jdk.incubator.foreign
// --enable-native-access=ALL-UNNAMED
import jdk.incubator.foreign.*;
import java.lang.invoke.*;

public class ForeignLinkerDemo {
    public static void main(String[] args) throws Throwable {
        // Get the native linker
        CLinker linker = CLinker.getInstance();

        // Look up a native function (e.g., strlen from libc)
        MethodHandle strlen = linker.downcallHandle(
            CLinker.systemLookup().lookup("strlen").get(),
            MethodType.methodType(long.class, MemoryAddress.class),
            FunctionDescriptor.of(CLinker.C_LONG, CLinker.C_POINTER)
        );

        // Allocate native memory and call
        try (MemorySegment str = CLinker.toCString("Hello, Foreign API!")) {
            long len = (long) strlen.invoke(str.address());
            System.out.println("String length: " + len);
        }
    }
}
```

---

## Other Java 16 Features

### Unix-Domain Socket Channels

```java
import java.net.*;
import java.nio.channels.*;
import java.nio.file.*;

public class UnixDomainSocket {
    public static void main(String[] args) throws Exception {
        // Unix domain sockets for inter-process communication
        Path socketPath = Path.of("/tmp/test.socket");

        // Create server
        ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
        server.bind(UnixDomainSocketAddress.of(socketPath));
        System.out.println("Server listening on: " + socketPath);

        // In another thread/process: connect client
        // SocketChannel client = SocketChannel.open(StandardProtocolFamily.UNIX);
        // client.connect(UnixDomainSocketAddress.of(socketPath));

        // Benefits over TCP localhost:
        // - Better security (file system permissions)
        // - Better performance (no network stack)
        // - No port conflicts

        server.close();
        Files.deleteIfExists(socketPath);
    }
}
```

### Packaging Tool (Standard)

The `jpackage` tool is now standard for creating native installers.

```bash
# Create native package
jpackage --name MyApp \
         --input lib \
         --main-jar myapp.jar \
         --main-class com.myapp.Main \
         --type dmg  # or msi, deb, rpm, pkg
```

---

## Summary

| Feature | Status | Description |
|---------|--------|-------------|
| Records | Standard | Immutable data carriers |
| Pattern Matching instanceof | Standard | Type patterns in instanceof |
| Sealed Classes | 2nd Preview | Restricted class hierarchies |
| Stream.toList() | Standard | Convenient list conversion |
| Day Period Support | Standard | Morning, afternoon, evening formatting |
| Vector API | Incubator | SIMD vector operations |
| Foreign Linker | Incubator | Native code interop |
| jpackage | Standard | Native packaging tool |

[← Java 15 Features](java-15.md) | [Java 17 Features →](java-17.md)
