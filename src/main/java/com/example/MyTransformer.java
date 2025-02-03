package com.example;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LineNumberAttribute;
import javassist.bytecode.MethodInfo;

import javassist.*;

public class MyTransformer implements java.lang.instrument.ClassFileTransformer {
    
    private static final String COUNTER_FIELD = "__methodCounter";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            CallGraphLogger.printCallGraph();
        }));
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            java.security.ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || className.startsWith("java/") || className.startsWith("sun/") 
                || className.equals("com/example/CallGraphLogger")) {
            return null;
        }

        try {
            ClassPool classPool = ClassPool.getDefault();
            CtClass ctClass = classPool.get(className.replace("/", "."));

            // Skip instrumentation for interfaces (and optionally annotations/enums)
            if (ctClass.isInterface() || ctClass.isAnnotation() || ctClass.isEnum()) {
                return null;
            }

            // Check only for declared fields to avoid inherited ones.
            try {
                ctClass.getDeclaredField(COUNTER_FIELD);
            } catch (NotFoundException e) {
                // Field not declared in this class, so add it.
                CtField counterField = new CtField(CtClass.intType, COUNTER_FIELD, ctClass);
                counterField.setModifiers(Modifier.STATIC);
                ctClass.addField(counterField, "0");
            }

            // Instrument each declared method
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                instrumentMethod(method, ctClass);
            }

            return ctClass.toBytecode();
        } catch (Exception e) {
            // Log or handle exception as needed
        }
        return null;
    }

    private void instrumentMethod(CtMethod method, CtClass declaringClass) throws CannotCompileException {
        method.addLocalVariable("startMemory", CtClass.longType);
        method.addLocalVariable("endMemory", CtClass.longType);
        method.addLocalVariable("startTime", CtClass.longType);

        // 插入方法开始部分的代码
        method.insertBefore(
                "startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();" +
                        "startTime = System.nanoTime();" +
                        declaringClass.getName() + "." + COUNTER_FIELD + "++;" +
                        "System.out.println(\"Entering method: " + method.getLongName() + "\");" +
                        "System.out.println(\"Call count: \" + " + declaringClass.getName() + "." + COUNTER_FIELD + ");" +
                        "System.out.println(\"Call stack: \" + java.util.Arrays.toString(Thread.currentThread().getStackTrace()));" 
                        +"com.example.CallGraphLogger.logMethodEnter(\"" + declaringClass.getName() + "\", \"" + method.getName() + "\");"
        );

        // 插入方法结束部分的代码
        method.insertAfter(
                "endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();" +
                        "long duration = System.nanoTime() - startTime;" +
                        "System.out.println(\"Exiting method: " + method.getLongName() + "\");" +
                        "System.out.println(\"Execution time: \" + duration + \" ns\");" +
                        "System.out.println(\"Memory allocated: \" + (endMemory - startMemory) + \" bytes\");" 
                        +"com.example.CallGraphLogger.logMethodExit(\"" + declaringClass.getName() + "\", \"" + method.getName() + "\");"
        );
    }
}