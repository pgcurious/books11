package com.javafeatures;

/**
 * Main entry point for Java Features Examples.
 * Run this class to see a menu of available demonstrations.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("""
            ╔═══════════════════════════════════════════════════════════════╗
            ║           Java Features Examples (Java 8 - 25)                ║
            ╠═══════════════════════════════════════════════════════════════╣
            ║  1. StreamPipelineDemo     - Stream API operations            ║
            ║  2. RecordPatternDemo      - Records and Pattern Matching     ║
            ║  3. VirtualThreadDemo      - Virtual Threads                  ║
            ║  4. ModernJavaDemo         - Combined modern features         ║
            ╚═══════════════════════════════════════════════════════════════╝

            Run individual demos:
              mvn exec:java -Dexec.mainClass="com.javafeatures.StreamPipelineDemo"
              mvn exec:java -Dexec.mainClass="com.javafeatures.RecordPatternDemo"
              mvn exec:java -Dexec.mainClass="com.javafeatures.VirtualThreadDemo"
              mvn exec:java -Dexec.mainClass="com.javafeatures.ModernJavaDemo"

            Or run all:
              mvn exec:java -Dexec.mainClass="com.javafeatures.Main" -Dexec.args="all"
            """);

        if (args.length > 0 && args[0].equals("all")) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("Running all demos...\n");

            System.out.println("\n>>> Stream Pipeline Demo <<<\n");
            StreamPipelineDemo.main(new String[]{});

            System.out.println("\n>>> Record Pattern Demo <<<\n");
            RecordPatternDemo.main(new String[]{});

            System.out.println("\n>>> Virtual Thread Demo <<<\n");
            VirtualThreadDemo.main(new String[]{});

            System.out.println("\n>>> Modern Java Demo <<<\n");
            ModernJavaDemo.main(new String[]{});
        }
    }
}
