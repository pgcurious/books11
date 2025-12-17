# Java 8 Features Guide

**Release Date:** March 2014
**Type:** LTS (Long-Term Support)

Java 8 was a revolutionary release that introduced functional programming concepts to Java. It remains one of the most significant updates in Java's history.

## Table of Contents
- [Lambda Expressions](#lambda-expressions)
- [Functional Interfaces](#functional-interfaces)
- [Stream API](#stream-api)
- [Optional](#optional)
- [Default Methods](#default-methods)
- [Method References](#method-references)
- [Date/Time API](#datetime-api)
- [CompletableFuture](#completablefuture)
- [Nashorn JavaScript Engine](#nashorn-javascript-engine)

---

## Lambda Expressions

Lambda expressions provide a clear and concise way to represent a method interface using an expression.

### Syntax

```java
// Basic syntax
(parameters) -> expression

// With block
(parameters) -> { statements; }
```

### Examples

```java
// Before Java 8 - Anonymous inner class
Runnable runnable = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello from thread!");
    }
};

// Java 8 - Lambda expression
Runnable runnableLambda = () -> System.out.println("Hello from thread!");

// With parameters
Comparator<String> comparator = (s1, s2) -> s1.compareTo(s2);

// Multi-line lambda
Comparator<String> comparatorMultiLine = (s1, s2) -> {
    System.out.println("Comparing: " + s1 + " and " + s2);
    return s1.length() - s2.length();
};
```

### Hands-On Exercise 1: Basic Lambdas

```java
import java.util.*;

public class LambdaBasics {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie", "Diana");

        // Exercise 1: Sort names by length using lambda
        names.sort((a, b) -> a.length() - b.length());
        System.out.println("Sorted by length: " + names);

        // Exercise 2: Print each name using forEach with lambda
        names.forEach(name -> System.out.println("Hello, " + name));

        // Exercise 3: Filter names starting with 'A' or 'B'
        names.stream()
             .filter(name -> name.startsWith("A") || name.startsWith("B"))
             .forEach(System.out::println);
    }
}
```

---

## Functional Interfaces

A functional interface has exactly one abstract method and can be used as the target for lambda expressions.

### Built-in Functional Interfaces

| Interface | Method | Description |
|-----------|--------|-------------|
| `Predicate<T>` | `test(T t)` | Returns boolean |
| `Function<T,R>` | `apply(T t)` | Transforms T to R |
| `Consumer<T>` | `accept(T t)` | Consumes T, returns void |
| `Supplier<T>` | `get()` | Supplies T |
| `BiFunction<T,U,R>` | `apply(T t, U u)` | Two inputs, one output |
| `UnaryOperator<T>` | `apply(T t)` | T to T transformation |
| `BinaryOperator<T>` | `apply(T t1, T t2)` | Two T inputs, T output |

### Examples

```java
import java.util.function.*;

public class FunctionalInterfaceDemo {
    public static void main(String[] args) {
        // Predicate - test a condition
        Predicate<String> isEmpty = String::isEmpty;
        Predicate<String> isNotEmpty = isEmpty.negate();
        System.out.println(isNotEmpty.test("Hello")); // true

        // Function - transform data
        Function<String, Integer> length = String::length;
        Function<Integer, Integer> doubled = x -> x * 2;
        Function<String, Integer> doubledLength = length.andThen(doubled);
        System.out.println(doubledLength.apply("Hello")); // 10

        // Consumer - perform action
        Consumer<String> printer = System.out::println;
        Consumer<String> upperPrinter = s -> System.out.println(s.toUpperCase());
        printer.andThen(upperPrinter).accept("hello");

        // Supplier - provide values
        Supplier<Double> randomSupplier = Math::random;
        System.out.println(randomSupplier.get());

        // BiFunction - two inputs
        BiFunction<String, String, String> concat = (a, b) -> a + " " + b;
        System.out.println(concat.apply("Hello", "World"));
    }
}
```

### Custom Functional Interface

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);

    // Default methods are allowed
    default int add(int a, int b) {
        return a + b;
    }

    // Static methods are allowed
    static int multiply(int a, int b) {
        return a * b;
    }
}

public class CustomFunctionalInterface {
    public static void main(String[] args) {
        Calculator addition = (a, b) -> a + b;
        Calculator subtraction = (a, b) -> a - b;

        System.out.println(addition.calculate(10, 5));    // 15
        System.out.println(subtraction.calculate(10, 5)); // 5
        System.out.println(Calculator.multiply(10, 5));   // 50
    }
}
```

---

## Stream API

Streams provide a functional approach to processing collections of objects.

### Stream Operations

**Intermediate Operations** (lazy, return Stream):
- `filter()`, `map()`, `flatMap()`, `distinct()`, `sorted()`, `peek()`, `limit()`, `skip()`

**Terminal Operations** (eager, trigger processing):
- `forEach()`, `collect()`, `reduce()`, `count()`, `min()`, `max()`, `anyMatch()`, `allMatch()`, `noneMatch()`, `findFirst()`, `findAny()`

### Basic Stream Operations

```java
import java.util.*;
import java.util.stream.*;

public class StreamBasics {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // Filter: Keep only even numbers
        List<Integer> evens = numbers.stream()
            .filter(n -> n % 2 == 0)
            .collect(Collectors.toList());
        System.out.println("Evens: " + evens); // [2, 4, 6, 8, 10]

        // Map: Square each number
        List<Integer> squares = numbers.stream()
            .map(n -> n * n)
            .collect(Collectors.toList());
        System.out.println("Squares: " + squares);

        // Reduce: Sum all numbers
        int sum = numbers.stream()
            .reduce(0, Integer::sum);
        System.out.println("Sum: " + sum); // 55

        // Chaining operations
        int sumOfEvenSquares = numbers.stream()
            .filter(n -> n % 2 == 0)
            .map(n -> n * n)
            .reduce(0, Integer::sum);
        System.out.println("Sum of even squares: " + sumOfEvenSquares); // 220
    }
}
```

### Advanced Stream Operations

```java
import java.util.*;
import java.util.stream.*;

public class StreamAdvanced {
    public static void main(String[] args) {
        // FlatMap - flatten nested structures
        List<List<String>> nested = Arrays.asList(
            Arrays.asList("a", "b"),
            Arrays.asList("c", "d"),
            Arrays.asList("e", "f")
        );

        List<String> flat = nested.stream()
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        System.out.println("Flattened: " + flat); // [a, b, c, d, e, f]

        // Distinct and Sorted
        List<Integer> nums = Arrays.asList(5, 2, 8, 2, 1, 5, 8, 3);
        List<Integer> uniqueSorted = nums.stream()
            .distinct()
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Unique sorted: " + uniqueSorted); // [1, 2, 3, 5, 8]

        // Limit and Skip (pagination)
        List<Integer> page = IntStream.rangeClosed(1, 100)
            .skip(20)  // Skip first 20
            .limit(10) // Take 10
            .boxed()
            .collect(Collectors.toList());
        System.out.println("Page 3 (items 21-30): " + page);

        // Parallel Streams
        long count = IntStream.rangeClosed(1, 1_000_000)
            .parallel()
            .filter(n -> n % 2 == 0)
            .count();
        System.out.println("Even count: " + count);
    }
}
```

### Collectors

```java
import java.util.*;
import java.util.stream.*;

public class CollectorsDemo {
    record Person(String name, int age, String city) {}

    public static void main(String[] args) {
        List<Person> people = Arrays.asList(
            new Person("Alice", 30, "NYC"),
            new Person("Bob", 25, "LA"),
            new Person("Charlie", 35, "NYC"),
            new Person("Diana", 28, "LA"),
            new Person("Eve", 32, "Chicago")
        );

        // Collecting to different types
        Set<String> names = people.stream()
            .map(Person::name)
            .collect(Collectors.toSet());

        // Joining strings
        String allNames = people.stream()
            .map(Person::name)
            .collect(Collectors.joining(", "));
        System.out.println("Names: " + allNames);

        // Grouping by
        Map<String, List<Person>> byCity = people.stream()
            .collect(Collectors.groupingBy(Person::city));
        System.out.println("By city: " + byCity);

        // Grouping with counting
        Map<String, Long> countByCity = people.stream()
            .collect(Collectors.groupingBy(Person::city, Collectors.counting()));
        System.out.println("Count by city: " + countByCity);

        // Partitioning (boolean grouping)
        Map<Boolean, List<Person>> over30 = people.stream()
            .collect(Collectors.partitioningBy(p -> p.age() > 30));
        System.out.println("Over 30: " + over30.get(true));

        // Statistics
        IntSummaryStatistics ageStats = people.stream()
            .collect(Collectors.summarizingInt(Person::age));
        System.out.println("Age stats: " + ageStats);

        // Average
        double avgAge = people.stream()
            .collect(Collectors.averagingInt(Person::age));
        System.out.println("Average age: " + avgAge);

        // toMap
        Map<String, Integer> nameToAge = people.stream()
            .collect(Collectors.toMap(Person::name, Person::age));
        System.out.println("Name to age: " + nameToAge);
    }
}
```

### Hands-On Exercise 2: Stream Processing

```java
import java.util.*;
import java.util.stream.*;

public class StreamExercise {
    record Product(String name, String category, double price) {}

    public static void main(String[] args) {
        List<Product> products = Arrays.asList(
            new Product("Laptop", "Electronics", 999.99),
            new Product("Phone", "Electronics", 699.99),
            new Product("Desk", "Furniture", 299.99),
            new Product("Chair", "Furniture", 199.99),
            new Product("Headphones", "Electronics", 149.99),
            new Product("Lamp", "Furniture", 49.99)
        );

        // Exercise 1: Find all products under $200
        List<Product> affordable = products.stream()
            .filter(p -> p.price() < 200)
            .collect(Collectors.toList());
        System.out.println("Affordable: " + affordable);

        // Exercise 2: Get list of unique categories
        Set<String> categories = products.stream()
            .map(Product::category)
            .collect(Collectors.toSet());
        System.out.println("Categories: " + categories);

        // Exercise 3: Calculate total value of all electronics
        double electronicsTotal = products.stream()
            .filter(p -> p.category().equals("Electronics"))
            .mapToDouble(Product::price)
            .sum();
        System.out.println("Electronics total: $" + electronicsTotal);

        // Exercise 4: Find the most expensive product
        Optional<Product> mostExpensive = products.stream()
            .max(Comparator.comparing(Product::price));
        mostExpensive.ifPresent(p ->
            System.out.println("Most expensive: " + p.name()));

        // Exercise 5: Group products by category with average price
        Map<String, Double> avgPriceByCategory = products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.averagingDouble(Product::price)
            ));
        System.out.println("Avg price by category: " + avgPriceByCategory);
    }
}
```

---

## Optional

Optional is a container that may or may not contain a non-null value.

### Creating Optional

```java
import java.util.Optional;

public class OptionalCreation {
    public static void main(String[] args) {
        // Empty optional
        Optional<String> empty = Optional.empty();

        // Optional with value
        Optional<String> hello = Optional.of("Hello");

        // Optional that might be null
        String nullableValue = null;
        Optional<String> nullable = Optional.ofNullable(nullableValue);

        System.out.println("Empty: " + empty);
        System.out.println("Hello: " + hello);
        System.out.println("Nullable: " + nullable);
    }
}
```

### Using Optional

```java
import java.util.Optional;

public class OptionalUsage {
    public static void main(String[] args) {
        Optional<String> optional = Optional.of("Hello, World!");

        // Check if present
        if (optional.isPresent()) {
            System.out.println("Value: " + optional.get());
        }

        // ifPresent - execute if value exists
        optional.ifPresent(System.out::println);

        // orElse - provide default value
        String value = optional.orElse("Default");
        System.out.println("Value or default: " + value);

        // orElseGet - lazy default (supplier)
        String lazyValue = optional.orElseGet(() -> computeDefault());

        // orElseThrow - throw exception if empty
        Optional<String> empty = Optional.empty();
        try {
            empty.orElseThrow(() -> new RuntimeException("No value!"));
        } catch (RuntimeException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // map - transform value
        Optional<Integer> length = optional.map(String::length);
        System.out.println("Length: " + length.orElse(0));

        // flatMap - for nested optionals
        Optional<Optional<String>> nested = Optional.of(Optional.of("nested"));
        Optional<String> flat = nested.flatMap(o -> o);

        // filter - conditional check
        Optional<String> filtered = optional.filter(s -> s.length() > 5);
        System.out.println("Filtered: " + filtered);
    }

    static String computeDefault() {
        System.out.println("Computing default...");
        return "Computed Default";
    }
}
```

### Optional Best Practices

```java
import java.util.*;

public class OptionalBestPractices {

    // Good: Return Optional from methods that might not return a value
    public static Optional<User> findUserById(int id) {
        // Simulate database lookup
        if (id == 1) {
            return Optional.of(new User(1, "Alice"));
        }
        return Optional.empty();
    }

    // Bad: Don't use Optional as method parameter
    // public void process(Optional<String> name) { } // Avoid this!

    // Good: Use in method chains
    public static String getUserNameOrDefault(int id) {
        return findUserById(id)
            .map(User::name)
            .orElse("Unknown");
    }

    record User(int id, String name) {}

    public static void main(String[] args) {
        // Chaining operations
        String result = findUserById(1)
            .filter(u -> u.id() > 0)
            .map(User::name)
            .map(String::toUpperCase)
            .orElse("NOT FOUND");
        System.out.println(result); // ALICE

        // Stream of Optionals (Java 9+ has better support)
        List<Optional<String>> optionals = Arrays.asList(
            Optional.of("a"),
            Optional.empty(),
            Optional.of("b")
        );

        List<String> values = optionals.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(java.util.stream.Collectors.toList());
        System.out.println(values); // [a, b]
    }
}
```

---

## Default Methods

Default methods allow interfaces to have method implementations.

```java
interface Vehicle {
    // Abstract method
    void start();

    // Default method with implementation
    default void stop() {
        System.out.println("Vehicle stopped");
    }

    // Static method
    static void service() {
        System.out.println("Servicing vehicle");
    }
}

interface Electric {
    default void charge() {
        System.out.println("Charging...");
    }

    default void stop() {
        System.out.println("Electric vehicle stopped - battery saved");
    }
}

// Multiple inheritance with default methods
class ElectricCar implements Vehicle, Electric {
    @Override
    public void start() {
        System.out.println("Electric car started silently");
    }

    // Must override when same default method exists in multiple interfaces
    @Override
    public void stop() {
        Vehicle.super.stop();    // Call Vehicle's stop
        Electric.super.stop();   // Call Electric's stop
    }
}

public class DefaultMethodDemo {
    public static void main(String[] args) {
        ElectricCar car = new ElectricCar();
        car.start();
        car.charge();  // Default method from Electric
        car.stop();    // Overridden method

        Vehicle.service(); // Static method
    }
}
```

---

## Method References

Method references provide a shorthand for lambda expressions that call a single method.

### Types of Method References

| Type | Syntax | Lambda Equivalent |
|------|--------|-------------------|
| Static | `Class::staticMethod` | `x -> Class.staticMethod(x)` |
| Instance (specific) | `instance::method` | `x -> instance.method(x)` |
| Instance (arbitrary) | `Class::instanceMethod` | `(obj, x) -> obj.instanceMethod(x)` |
| Constructor | `Class::new` | `x -> new Class(x)` |

```java
import java.util.*;
import java.util.function.*;

public class MethodReferenceDemo {
    public static void main(String[] args) {
        List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

        // Static method reference
        names.forEach(System.out::println);
        // Equivalent: names.forEach(name -> System.out.println(name));

        // Instance method reference (specific object)
        String prefix = "Hello, ";
        Function<String, String> greeter = prefix::concat;
        System.out.println(greeter.apply("World")); // Hello, World

        // Instance method reference (arbitrary object)
        Comparator<String> comparator = String::compareToIgnoreCase;
        names.sort(comparator);

        // Constructor reference
        Supplier<List<String>> listSupplier = ArrayList::new;
        List<String> newList = listSupplier.get();

        // Constructor with parameter
        Function<String, StringBuilder> sbCreator = StringBuilder::new;
        StringBuilder sb = sbCreator.apply("Initial");
        System.out.println(sb);

        // Array constructor reference
        IntFunction<String[]> arrayCreator = String[]::new;
        String[] array = arrayCreator.apply(5); // Creates String[5]

        // Stream with method reference
        List<Integer> lengths = names.stream()
            .map(String::length)
            .collect(java.util.stream.Collectors.toList());
        System.out.println("Lengths: " + lengths);
    }
}
```

---

## Date/Time API

Java 8 introduced a new Date/Time API in `java.time` package, replacing the problematic old Date/Calendar classes.

### Core Classes

| Class | Description |
|-------|-------------|
| `LocalDate` | Date without time (year, month, day) |
| `LocalTime` | Time without date (hour, minute, second) |
| `LocalDateTime` | Date and time without timezone |
| `ZonedDateTime` | Date and time with timezone |
| `Instant` | Machine timestamp (epoch seconds) |
| `Duration` | Time-based amount (hours, minutes, seconds) |
| `Period` | Date-based amount (years, months, days) |

### Examples

```java
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

public class DateTimeDemo {
    public static void main(String[] args) {
        // LocalDate
        LocalDate today = LocalDate.now();
        LocalDate birthday = LocalDate.of(1990, Month.MARCH, 15);
        LocalDate parsed = LocalDate.parse("2024-12-25");

        System.out.println("Today: " + today);
        System.out.println("Birthday: " + birthday);
        System.out.println("Is leap year: " + today.isLeapYear());
        System.out.println("Day of week: " + today.getDayOfWeek());

        // LocalTime
        LocalTime now = LocalTime.now();
        LocalTime lunch = LocalTime.of(12, 30);
        LocalTime parsed_time = LocalTime.parse("14:30:00");

        System.out.println("Now: " + now);
        System.out.println("Lunch: " + lunch);

        // LocalDateTime
        LocalDateTime dateTime = LocalDateTime.now();
        LocalDateTime meeting = LocalDateTime.of(2024, 6, 15, 10, 30);

        // ZonedDateTime
        ZonedDateTime zonedNow = ZonedDateTime.now();
        ZonedDateTime tokyo = ZonedDateTime.now(ZoneId.of("Asia/Tokyo"));
        ZonedDateTime newYork = zonedNow.withZoneSameInstant(ZoneId.of("America/New_York"));

        System.out.println("Local: " + zonedNow);
        System.out.println("Tokyo: " + tokyo);
        System.out.println("New York: " + newYork);

        // Instant - machine timestamp
        Instant timestamp = Instant.now();
        System.out.println("Timestamp: " + timestamp);
        System.out.println("Epoch seconds: " + timestamp.getEpochSecond());

        // Duration and Period
        Duration duration = Duration.between(LocalTime.of(9, 0), LocalTime.of(17, 0));
        System.out.println("Work duration: " + duration.toHours() + " hours");

        Period period = Period.between(birthday, today);
        System.out.println("Age: " + period.getYears() + " years");

        // Date manipulation
        LocalDate nextWeek = today.plusWeeks(1);
        LocalDate lastMonth = today.minusMonths(1);
        LocalDate firstDayOfMonth = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));

        System.out.println("Next week: " + nextWeek);
        System.out.println("First day of month: " + firstDayOfMonth);
        System.out.println("Next Monday: " + nextMonday);

        // Formatting
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        System.out.println("Formatted: " + today.format(formatter));

        DateTimeFormatter isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
        System.out.println("ISO format: " + today.format(isoFormatter));
    }
}
```

### Hands-On Exercise 3: Date/Time Operations

```java
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;

public class DateTimeExercise {
    public static void main(String[] args) {
        // Exercise 1: Calculate days until next birthday
        LocalDate today = LocalDate.now();
        LocalDate birthDate = LocalDate.of(1990, 6, 15);

        LocalDate nextBirthday = birthDate.withYear(today.getYear());
        if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }
        long daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday);
        System.out.println("Days until birthday: " + daysUntilBirthday);

        // Exercise 2: Find all Fridays in current month
        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());

        System.out.println("Fridays this month:");
        LocalDate friday = firstDay.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
        while (!friday.isAfter(lastDay)) {
            System.out.println("  " + friday);
            friday = friday.plusWeeks(1);
        }

        // Exercise 3: Convert between time zones
        ZonedDateTime meeting = ZonedDateTime.of(
            LocalDateTime.of(2024, 6, 15, 14, 0),
            ZoneId.of("America/New_York")
        );

        System.out.println("Meeting times:");
        System.out.println("  New York: " + meeting);
        System.out.println("  London: " + meeting.withZoneSameInstant(ZoneId.of("Europe/London")));
        System.out.println("  Tokyo: " + meeting.withZoneSameInstant(ZoneId.of("Asia/Tokyo")));

        // Exercise 4: Calculate working days between two dates
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        long workingDays = startDate.datesUntil(endDate.plusDays(1))
            .filter(date -> {
                DayOfWeek day = date.getDayOfWeek();
                return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
            })
            .count();
        System.out.println("Working days in January 2024: " + workingDays);
    }
}
```

---

## CompletableFuture

CompletableFuture provides a powerful way to write asynchronous, non-blocking code.

### Basic Usage

```java
import java.util.concurrent.*;

public class CompletableFutureBasics {
    public static void main(String[] args) throws Exception {
        // Create completed future
        CompletableFuture<String> completed = CompletableFuture.completedFuture("Hello");
        System.out.println(completed.get());

        // Run async task (no return value)
        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(() -> {
            System.out.println("Running in: " + Thread.currentThread().getName());
        });
        runAsync.join();

        // Supply async (with return value)
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Result from async computation";
        });
        System.out.println(supplyAsync.join());
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

### Chaining Operations

```java
import java.util.concurrent.*;

public class CompletableFutureChaining {
    public static void main(String[] args) {
        // thenApply - transform result
        CompletableFuture<Integer> future = CompletableFuture
            .supplyAsync(() -> "Hello")
            .thenApply(String::length)
            .thenApply(len -> len * 2);
        System.out.println("Result: " + future.join()); // 10

        // thenAccept - consume result
        CompletableFuture.supplyAsync(() -> "Hello World")
            .thenAccept(System.out::println)
            .join();

        // thenRun - run after completion
        CompletableFuture.supplyAsync(() -> "Processing...")
            .thenRun(() -> System.out.println("Done!"))
            .join();

        // thenCompose - flatten nested futures
        CompletableFuture<String> composed = CompletableFuture
            .supplyAsync(() -> 1)
            .thenCompose(num -> CompletableFuture.supplyAsync(
                () -> "Number: " + num
            ));
        System.out.println(composed.join());

        // thenCombine - combine two futures
        CompletableFuture<String> hello = CompletableFuture.supplyAsync(() -> "Hello");
        CompletableFuture<String> world = CompletableFuture.supplyAsync(() -> "World");

        CompletableFuture<String> combined = hello.thenCombine(
            world,
            (h, w) -> h + " " + w
        );
        System.out.println(combined.join()); // Hello World
    }
}
```

### Error Handling

```java
import java.util.concurrent.*;

public class CompletableFutureErrors {
    public static void main(String[] args) {
        // exceptionally - handle errors
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {
                if (true) throw new RuntimeException("Oops!");
                return "Success";
            })
            .exceptionally(ex -> "Error: " + ex.getMessage());
        System.out.println(future.join()); // Error: Oops!

        // handle - handle both success and error
        CompletableFuture<String> handled = CompletableFuture
            .supplyAsync(() -> {
                if (Math.random() > 0.5) throw new RuntimeException("Random error");
                return "Success";
            })
            .handle((result, ex) -> {
                if (ex != null) return "Handled error: " + ex.getMessage();
                return result;
            });
        System.out.println(handled.join());

        // whenComplete - perform action on completion
        CompletableFuture.supplyAsync(() -> "Result")
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    System.out.println("Failed: " + ex);
                } else {
                    System.out.println("Succeeded: " + result);
                }
            })
            .join();
    }
}
```

### Combining Multiple Futures

```java
import java.util.concurrent.*;
import java.util.*;
import java.util.stream.*;

public class CompletableFutureCombining {
    public static void main(String[] args) {
        // allOf - wait for all futures
        CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
            sleep(1000);
            return "Result 1";
        });
        CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
            sleep(500);
            return "Result 2";
        });
        CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> {
            sleep(800);
            return "Result 3";
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(f1, f2, f3);
        allOf.join();

        // Get all results
        List<String> results = Stream.of(f1, f2, f3)
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
        System.out.println("All results: " + results);

        // anyOf - first to complete wins
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(f1, f2, f3);
        System.out.println("First result: " + anyOf.join());
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

### Hands-On Exercise 4: Async Processing

```java
import java.util.concurrent.*;
import java.util.*;

public class AsyncExercise {
    public static void main(String[] args) {
        // Simulate async API calls
        ExecutorService executor = Executors.newFixedThreadPool(3);

        // Exercise: Fetch user, orders, and recommendations in parallel
        CompletableFuture<String> userFuture = CompletableFuture
            .supplyAsync(() -> fetchUser(1), executor);

        CompletableFuture<List<String>> ordersFuture = CompletableFuture
            .supplyAsync(() -> fetchOrders(1), executor);

        CompletableFuture<List<String>> recommendationsFuture = CompletableFuture
            .supplyAsync(() -> fetchRecommendations(1), executor);

        // Combine all results
        CompletableFuture<String> dashboard = userFuture
            .thenCombine(ordersFuture, (user, orders) ->
                user + "\nOrders: " + orders)
            .thenCombine(recommendationsFuture, (partial, recs) ->
                partial + "\nRecommendations: " + recs);

        System.out.println("Dashboard:\n" + dashboard.join());

        executor.shutdown();
    }

    static String fetchUser(int id) {
        sleep(1000);
        return "User: Alice (ID: " + id + ")";
    }

    static List<String> fetchOrders(int userId) {
        sleep(1500);
        return Arrays.asList("Order-1", "Order-2", "Order-3");
    }

    static List<String> fetchRecommendations(int userId) {
        sleep(800);
        return Arrays.asList("Product-A", "Product-B");
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {}
    }
}
```

---

## Nashorn JavaScript Engine

Java 8 included Nashorn, a JavaScript engine (deprecated in Java 11, removed in Java 15).

```java
import javax.script.*;

public class NashornDemo {
    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");

        // Execute JavaScript
        engine.eval("print('Hello from JavaScript!')");

        // Evaluate expression
        Object result = engine.eval("10 + 20");
        System.out.println("Result: " + result);

        // Pass Java objects to JavaScript
        engine.put("name", "Java");
        engine.eval("print('Hello, ' + name + '!')");

        // Call JavaScript functions
        engine.eval("function greet(name) { return 'Hello, ' + name; }");
        Invocable invocable = (Invocable) engine;
        String greeting = (String) invocable.invokeFunction("greet", "World");
        System.out.println(greeting);
    }
}
```

---

## Summary

Java 8 introduced fundamental changes that modernized the language:

| Feature | Impact |
|---------|--------|
| Lambda Expressions | Enabled functional programming style |
| Stream API | Declarative data processing |
| Optional | Null-safety improvements |
| Date/Time API | Modern, immutable date handling |
| Default Methods | Interface evolution without breaking changes |
| CompletableFuture | Powerful async programming |

These features form the foundation for modern Java development and are essential knowledge for any Java developer.

---

## Practice Challenges

1. **Stream Pipeline**: Create a stream pipeline that reads a list of transactions, filters by amount > $100, groups by category, and calculates the sum per category.

2. **Optional Chain**: Write a method that safely navigates a nested object structure (User -> Address -> City -> Name) using Optional.

3. **Async Aggregator**: Build an async service that fetches data from 3 simulated APIs in parallel and combines the results.

4. **Date Calculator**: Create a utility that calculates business days between two dates, excluding weekends and a list of holidays.

[Next: Java 9 Features →](java-9.md)
