package com.example;

import java.util.ArrayList;
import java.util.List;

public class ExampleClass {
    public void doWork() {
        try {
            Thread.sleep(1000);  // simulate time-consuming task
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void anotherMethod() {
        System.out.println("Doing some other work");
    }

    public void allocateMemory() {
        // System.out.println("Starting memory allocation...");
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    
        List<byte[]> memoryHog = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            memoryHog.add(new byte[1* 1024 * 1024]); // allocate 0.5MB each time
            long currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            // System.out.println((i + 1) + " MB allocated, current heap usage: " + (currentMemory - startMemory) + " bytes");
        }
    
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        // System.out.println("Memory allocation complete. Total allocated: " + (endMemory - startMemory) + " bytes.");
    }

    public static void main(String[] args) {
        ExampleClass example = new ExampleClass();
        example.doWork();
        example.anotherMethod();
        example.allocateMemory();
        example.allocateMemory();
        example.allocateMemory();
        example.allocateMemory();
    }
}
