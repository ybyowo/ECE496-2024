package com.example;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

public class MethodCallTransformer implements ClassFileTransformer {

    // record the method called in hash map
    private static final ConcurrentHashMap<String, Integer> methodCallCount = new ConcurrentHashMap<>();

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // filter out system classes
        if (className == null || className.startsWith("jdk") || className.startsWith("java/") || className.startsWith("sun/") || className.startsWith("com/example/MethodCallTransformer")) {
            return null;
        }

        try {
            // System.out.println("1");
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className.replace("/", "."));

            // System.out.println(ctClass.getName());
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                instrumentMethod(method);
                instrumentHeapAllocation(method);
            }

            // System.out.println("3");

            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("5");
        }
        return null;
    }

    private void instrumentMethod(CtMethod method) throws CannotCompileException {
        String methodName = method.getLongName();
        // System.out.println(methodName);
        // insert increment call count
        try{
            method.insertBefore(
                    "{ com.example.MethodCallTransformer.incrementCallCount(\"" + methodName + "\"); }"
            );
            // methodCallCount.merge(methodName, 1, Integer::sum);
            // method.insertBefore(
            //         "{ incrementCallCount(\"" + methodName + "\"); }"
            // );

            // System.out.println("4");
        } catch (Exception e) {
            // System.out.println(methodName);
            methodCallCount.merge(methodName, 1, Integer::sum);
            // System.out.println("4");
            // method.insertBefore(
            //         "{ com.example.MethodCallTransformer.incrementCallCount(\"" + methodName + "\"); }"
            // );
        }
        // System.out.println("4");

    }

    // increment call count
    public static void incrementCallCount(String methodName) {
        try{
            methodCallCount.merge(methodName, 1, Integer::sum);
            // System.out.println("44");
        } catch (Exception e) {
            System.out.println("45");
        }
    }

    // print the usage for each method
    public static void printCallStats() {
        System.out.println("Method Call Statistics:");
        methodCallCount.forEach((method, count) -> {
            if (count >= 1) {
                System.out.println(method + " was called " + count + " times.");
            }
        });
    }

    private void instrumentHeapAllocation(CtMethod method) throws CannotCompileException {
        method.addLocalVariable("startMemory", CtClass.longType);
        method.insertBefore("startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();");
        method.insertAfter(
                "{ long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();" +
                "if (endMemory - startMemory!=0) {System.out.println(\"Heap allocated by " + method.getLongName() + ": \" + (endMemory - startMemory) + \" bytes.\");} }"
        );
    }
}