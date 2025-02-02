package com.example;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class MyAgent {
    public static void premain(String agentArgs, Instrumentation inst) throws IOException {
        // start agent
        System.out.println("Agent is running");
        File agentJar = new File("/home/fred/ece496/target/my-instrumentation-project-1.0-SNAPSHOT.jar");
        inst.appendToBootstrapClassLoaderSearch(new JarFile(agentJar));
         inst.addTransformer(new MyTransformer());
//        inst.addTransformer(new MethodCallTransformer());

        // shut hook, print out the usage for method after program
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("\n===== Method Call Statistics =====");
//            MethodCallTransformer.printCallStats();
//        }));
    }
}