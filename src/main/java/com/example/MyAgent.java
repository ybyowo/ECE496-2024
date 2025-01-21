package com.example;

import java.lang.instrument.Instrumentation;

public class MyAgent {
    public static void premain(String agentArgs, Instrumentation inst) {
        // start agent
        System.out.println("Agent is running");
         inst.addTransformer(new MyTransformer());
//        inst.addTransformer(new MethodCallTransformer());

        // shut hook, print out the usage for method after program
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("\n===== Method Call Statistics =====");
//            MethodCallTransformer.printCallStats();
//        }));
    }
}