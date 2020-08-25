package io.four.utils;

public class SysUtils {
    public static void printMem() {
        Runtime run = Runtime.getRuntime();
        long max = run.maxMemory() / 1024 / 1024;

        long total = run.totalMemory() / 1024 / 1024;

        long free = run.freeMemory() / 1024 / 1024;

        long usable = max - total + free;
        System.out.println("Max = " + max);
        System.out.println("Total = " + total);
        System.out.println("free = " + free);
        System.out.println("Usable = " + usable);
    }

    public static void main(String[] args) {
        printMem();
    }
}
