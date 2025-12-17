package com.javafeatures;

import java.util.*;

/**
 * Demonstrates Records and Pattern Matching features.
 *
 * Features covered:
 * - Records (Java 16)
 * - Pattern Matching for instanceof (Java 16)
 * - Sealed classes (Java 17)
 * - Pattern Matching for switch (Java 21)
 * - Record Patterns (Java 21)
 */
public class RecordPatternDemo {

    // Basic records
    record Point(int x, int y) {
        // Compact constructor for validation
        public Point {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException("Coordinates must be non-negative");
            }
        }

        // Additional methods
        public double distanceFromOrigin() {
            return Math.sqrt(x * x + y * y);
        }
    }

    record Rectangle(Point topLeft, Point bottomRight) {
        public int width() {
            return bottomRight.x() - topLeft.x();
        }

        public int height() {
            return bottomRight.y() - topLeft.y();
        }

        public int area() {
            return width() * height();
        }
    }

    // Sealed interface hierarchy
    sealed interface Shape permits Circle, Square, Triangle {
        double area();
        String description();
    }

    record Circle(double radius) implements Shape {
        @Override
        public double area() {
            return Math.PI * radius * radius;
        }

        @Override
        public String description() {
            return "Circle with radius " + radius;
        }
    }

    record Square(double side) implements Shape {
        @Override
        public double area() {
            return side * side;
        }

        @Override
        public String description() {
            return "Square with side " + side;
        }
    }

    record Triangle(double base, double height) implements Shape {
        @Override
        public double area() {
            return 0.5 * base * height;
        }

        @Override
        public String description() {
            return "Triangle with base " + base + " and height " + height;
        }
    }

    // Algebraic Data Type pattern
    sealed interface Result<T> permits Success, Failure {
    }

    record Success<T>(T value) implements Result<T> {
    }

    record Failure<T>(String error, Exception cause) implements Result<T> {
        Failure(String error) {
            this(error, null);
        }
    }

    // Expression evaluator using sealed types
    sealed interface Expr permits Num, Add, Mul, Var {
    }

    record Num(int value) implements Expr {
    }

    record Add(Expr left, Expr right) implements Expr {
    }

    record Mul(Expr left, Expr right) implements Expr {
    }

    record Var(String name) implements Expr {
    }

    public static void main(String[] args) {
        System.out.println("=== Record Pattern Demo ===\n");

        // 1. Basic records
        basicRecords();

        // 2. Pattern matching for instanceof
        instanceofPatterns();

        // 3. Pattern matching for switch
        switchPatterns();

        // 4. Record patterns (deconstruction)
        recordPatterns();

        // 5. Practical example: Expression evaluator
        expressionEvaluator();

        // 6. Result type pattern
        resultTypeDemo();
    }

    static void basicRecords() {
        System.out.println("1. Basic Records (Java 16)");
        System.out.println("-".repeat(40));

        var point = new Point(10, 20);
        System.out.println("Point: " + point);
        System.out.println("X: " + point.x() + ", Y: " + point.y());
        System.out.println("Distance from origin: " + point.distanceFromOrigin());

        var rect = new Rectangle(new Point(0, 0), new Point(100, 50));
        System.out.println("\nRectangle: " + rect);
        System.out.println("Width: " + rect.width() + ", Height: " + rect.height());
        System.out.println("Area: " + rect.area());

        // Records are immutable
        // point.x = 5; // Compilation error

        // Records have automatic equals/hashCode
        var point2 = new Point(10, 20);
        System.out.println("\npoint.equals(point2): " + point.equals(point2));
        System.out.println("point.hashCode() == point2.hashCode(): " +
            (point.hashCode() == point2.hashCode()) + "\n");
    }

    static void instanceofPatterns() {
        System.out.println("2. Pattern Matching for instanceof (Java 16)");
        System.out.println("-".repeat(40));

        Object[] values = {"Hello", 42, 3.14, List.of(1, 2, 3), new Point(5, 10)};

        for (Object obj : values) {
            // Pattern matching eliminates explicit casting
            if (obj instanceof String s) {
                System.out.println("String of length " + s.length() + ": " + s);
            } else if (obj instanceof Integer i && i > 0) {
                System.out.println("Positive integer: " + i);
            } else if (obj instanceof Double d) {
                System.out.println("Double value: " + d);
            } else if (obj instanceof List<?> list) {
                System.out.println("List with " + list.size() + " elements");
            } else if (obj instanceof Point p) {
                System.out.println("Point at (" + p.x() + ", " + p.y() + ")");
            }
        }
        System.out.println();
    }

    static void switchPatterns() {
        System.out.println("3. Pattern Matching for switch (Java 21)");
        System.out.println("-".repeat(40));

        Shape[] shapes = {
            new Circle(5),
            new Square(4),
            new Triangle(6, 8)
        };

        for (Shape shape : shapes) {
            // Pattern matching in switch with exhaustiveness checking
            String desc = switch (shape) {
                case Circle c when c.radius() > 10 -> "Large circle";
                case Circle c -> "Circle (radius=" + c.radius() + ")";
                case Square s when s.side() == s.side() -> "Square (side=" + s.side() + ")";
                case Triangle(double b, double h) -> "Triangle (base=" + b + ", h=" + h + ")";
                // No default needed - sealed types ensure exhaustiveness
            };

            System.out.printf("%s -> Area: %.2f%n", desc, shape.area());
        }
        System.out.println();
    }

    static void recordPatterns() {
        System.out.println("4. Record Patterns (Java 21)");
        System.out.println("-".repeat(40));

        var rect = new Rectangle(new Point(10, 20), new Point(50, 80));

        // Nested deconstruction
        if (rect instanceof Rectangle(Point(int x1, int y1), Point(int x2, int y2))) {
            System.out.println("Rectangle corners:");
            System.out.println("  Top-left: (" + x1 + ", " + y1 + ")");
            System.out.println("  Bottom-right: (" + x2 + ", " + y2 + ")");
            System.out.println("  Diagonal: " + Math.sqrt(
                Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)));
        }

        // In switch
        Object obj = new Rectangle(new Point(0, 0), new Point(100, 100));
        String result = switch (obj) {
            case Rectangle(Point(int x, int y), Point p2)
                    when x == 0 && y == 0 -> "Rectangle at origin";
            case Rectangle r -> "Rectangle elsewhere";
            default -> "Not a rectangle";
        };
        System.out.println("\n" + result + "\n");
    }

    static void expressionEvaluator() {
        System.out.println("5. Expression Evaluator");
        System.out.println("-".repeat(40));

        // Build expression: (3 + x) * (2 + y)
        Expr expr = new Mul(
            new Add(new Num(3), new Var("x")),
            new Add(new Num(2), new Var("y"))
        );

        Map<String, Integer> env = Map.of("x", 5, "y", 4);

        System.out.println("Expression: (3 + x) * (2 + y)");
        System.out.println("With x=5, y=4: " + evaluate(expr, env));
        System.out.println("Simplified form: " + stringify(expr));
        System.out.println();
    }

    static int evaluate(Expr expr, Map<String, Integer> env) {
        return switch (expr) {
            case Num(int n) -> n;
            case Var(String name) -> env.getOrDefault(name, 0);
            case Add(Expr left, Expr right) ->
                evaluate(left, env) + evaluate(right, env);
            case Mul(Expr left, Expr right) ->
                evaluate(left, env) * evaluate(right, env);
        };
    }

    static String stringify(Expr expr) {
        return switch (expr) {
            case Num(int n) -> String.valueOf(n);
            case Var(String name) -> name;
            case Add(Expr left, Expr right) ->
                "(" + stringify(left) + " + " + stringify(right) + ")";
            case Mul(Expr left, Expr right) ->
                "(" + stringify(left) + " * " + stringify(right) + ")";
        };
    }

    static void resultTypeDemo() {
        System.out.println("6. Result Type Pattern");
        System.out.println("-".repeat(40));

        List<String> inputs = List.of("42", "hello", "100", "-5");

        for (String input : inputs) {
            Result<Integer> result = parsePositiveInt(input);

            // Pattern matching on Result type
            String message = switch (result) {
                case Success(Integer value) when value > 50 ->
                    "Large positive number: " + value;
                case Success(Integer value) ->
                    "Positive number: " + value;
                case Failure(String error, Exception _) ->
                    "Error: " + error;
            };

            System.out.println("Input '" + input + "' -> " + message);
        }
    }

    static Result<Integer> parsePositiveInt(String input) {
        try {
            int value = Integer.parseInt(input);
            if (value <= 0) {
                return new Failure<>("Value must be positive");
            }
            return new Success<>(value);
        } catch (NumberFormatException e) {
            return new Failure<>("Invalid number format", e);
        }
    }
}
