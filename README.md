# Java Features Guide: Java 8 to Java 25

A comprehensive hands-on guide exploring all major features introduced in Java from version 8 to version 25.

## Table of Contents

| Version | Release Date | Type | Guide |
|---------|--------------|------|-------|
| Java 8 | March 2014 | LTS | [Java 8 Features](guides/java-8.md) |
| Java 9 | September 2017 | - | [Java 9 Features](guides/java-9.md) |
| Java 10 | March 2018 | - | [Java 10 Features](guides/java-10.md) |
| Java 11 | September 2018 | LTS | [Java 11 Features](guides/java-11.md) |
| Java 12 | March 2019 | - | [Java 12 Features](guides/java-12.md) |
| Java 13 | September 2019 | - | [Java 13 Features](guides/java-13.md) |
| Java 14 | March 2020 | - | [Java 14 Features](guides/java-14.md) |
| Java 15 | September 2020 | - | [Java 15 Features](guides/java-15.md) |
| Java 16 | March 2021 | - | [Java 16 Features](guides/java-16.md) |
| Java 17 | September 2021 | LTS | [Java 17 Features](guides/java-17.md) |
| Java 18 | March 2022 | - | [Java 18 Features](guides/java-18.md) |
| Java 19 | September 2022 | - | [Java 19 Features](guides/java-19.md) |
| Java 20 | March 2023 | - | [Java 20 Features](guides/java-20.md) |
| Java 21 | September 2023 | LTS | [Java 21 Features](guides/java-21.md) |
| Java 22 | March 2024 | - | [Java 22 Features](guides/java-22.md) |
| Java 23 | September 2024 | - | [Java 23 Features](guides/java-23.md) |
| Java 24 | March 2025 | - | [Java 24 Features](guides/java-24.md) |
| Java 25 | September 2025 | LTS (Expected) | [Java 25 Features](guides/java-25.md) |

## Quick Feature Reference

### Language Features Timeline

```
Java 8  (2014) ─── Lambda Expressions, Streams, Optional, Default Methods
    │
Java 9  (2017) ─── Modules (JPMS), JShell, Private Interface Methods
    │
Java 10 (2018) ─── Local Variable Type Inference (var)
    │
Java 11 (2018) ─── var in Lambda, HTTP Client, String Methods
    │
Java 12 (2019) ─── Switch Expressions (Preview)
    │
Java 13 (2019) ─── Text Blocks (Preview)
    │
Java 14 (2020) ─── Records (Preview), Pattern Matching instanceof (Preview)
    │
Java 15 (2020) ─── Sealed Classes (Preview), Text Blocks (Standard)
    │
Java 16 (2021) ─── Records (Standard), Pattern Matching instanceof (Standard)
    │
Java 17 (2021) ─── Sealed Classes (Standard), Pattern Matching in Switch (Preview)
    │
Java 18 (2022) ─── Simple Web Server, UTF-8 by Default
    │
Java 19 (2022) ─── Virtual Threads (Preview), Record Patterns (Preview)
    │
Java 20 (2023) ─── Scoped Values (Incubator), Record Patterns (2nd Preview)
    │
Java 21 (2023) ─── Virtual Threads (Standard), Pattern Matching (Standard), Sequenced Collections
    │
Java 22 (2024) ─── Unnamed Variables, Statements Before super()
    │
Java 23 (2024) ─── Primitive Types in Patterns (Preview), Markdown Doc Comments
    │
Java 24 (2025) ─── Stream Gatherers, Flexible Constructor Bodies
    │
Java 25 (2025) ─── Expected LTS with stabilized features
```

## How to Use This Guide

### Prerequisites

- JDK 21+ installed (for most examples)
- IDE: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- Maven or Gradle for project management

### Running Examples

Each guide contains runnable code examples. You can:

1. **Use JShell** (Java 9+) for quick experiments:
   ```bash
   jshell
   ```

2. **Compile and run** individual files:
   ```bash
   javac Example.java
   java Example
   ```

3. **Use the examples project**:
   ```bash
   cd examples
   mvn compile exec:java -Dexec.mainClass="com.example.FeatureName"
   ```

### Enable Preview Features

For preview features, use:
```bash
javac --enable-preview --release 24 Example.java
java --enable-preview Example
```

## Feature Categories

### 1. Language Enhancements
- [Lambda Expressions](guides/java-8.md#lambda-expressions) (Java 8)
- [Local Variable Type Inference](guides/java-10.md#local-variable-type-inference) (Java 10)
- [Switch Expressions](guides/java-14.md#switch-expressions) (Java 14)
- [Text Blocks](guides/java-15.md#text-blocks) (Java 15)
- [Records](guides/java-16.md#records) (Java 16)
- [Sealed Classes](guides/java-17.md#sealed-classes) (Java 17)
- [Pattern Matching](guides/java-21.md#pattern-matching) (Java 21)

### 2. Concurrency & Performance
- [CompletableFuture](guides/java-8.md#completablefuture) (Java 8)
- [Virtual Threads](guides/java-21.md#virtual-threads) (Java 21)
- [Structured Concurrency](guides/java-21.md#structured-concurrency) (Java 21)
- [Scoped Values](guides/java-21.md#scoped-values) (Java 21)

### 3. API Improvements
- [Stream API](guides/java-8.md#stream-api) (Java 8)
- [Optional](guides/java-8.md#optional) (Java 8)
- [HTTP Client](guides/java-11.md#http-client) (Java 11)
- [Sequenced Collections](guides/java-21.md#sequenced-collections) (Java 21)
- [Stream Gatherers](guides/java-24.md#stream-gatherers) (Java 24)

### 4. Tooling & Runtime
- [Module System](guides/java-9.md#module-system) (Java 9)
- [JShell](guides/java-9.md#jshell) (Java 9)
- [Simple Web Server](guides/java-18.md#simple-web-server) (Java 18)
- [Foreign Function & Memory API](guides/java-22.md#foreign-function-memory-api) (Java 22)

## Hands-On Projects

Practice your skills with these progressive projects:

| Project | Features Used | Difficulty |
|---------|---------------|------------|
| [Stream Processing Pipeline](examples/stream-pipeline/) | Streams, Lambdas, Optional | Beginner |
| [REST API Client](examples/rest-client/) | HTTP Client, Records, var | Intermediate |
| [Concurrent Data Processor](examples/concurrent-processor/) | Virtual Threads, Structured Concurrency | Intermediate |
| [Pattern Matching Demo](examples/pattern-matching/) | Records, Sealed Classes, Switch Patterns | Advanced |
| [Modular Application](examples/modular-app/) | JPMS, Services | Advanced |

## Recommended Learning Path

### Week 1-2: Foundation (Java 8)
- Master Lambda expressions and functional interfaces
- Deep dive into Stream API
- Understand Optional and when to use it

### Week 3: Modularity (Java 9-11)
- Learn the module system basics
- Explore JShell for prototyping
- Practice with new String and Collection methods

### Week 4: Modern Syntax (Java 12-17)
- Use var for cleaner code
- Write records for data classes
- Implement sealed class hierarchies
- Master switch expressions and text blocks

### Week 5: Concurrency Revolution (Java 19-21)
- Understand virtual threads
- Practice structured concurrency
- Learn when to use scoped values

### Week 6: Latest Features (Java 22-25)
- Explore pattern matching enhancements
- Use Stream Gatherers
- Experiment with preview features

## Additional Resources

- [Oracle Java Documentation](https://docs.oracle.com/en/java/)
- [OpenJDK JEPs](https://openjdk.org/jeps/0)
- [Java Language Specification](https://docs.oracle.com/javase/specs/)

## Contributing

Feel free to submit issues and enhancement requests!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
