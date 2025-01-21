package com.example;

import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CallGraphLogger {
    private static final ThreadLocal<Stack<String>> callStack = ThreadLocal.withInitial(Stack::new);
    private static final ConcurrentMap<String, ConcurrentMap<String, Integer>> callGraph = new ConcurrentHashMap<>();

    public static void logMethodEnter(String className, String methodName) {
        String currentMethod = className + "." + methodName;
        Stack<String> stack = callStack.get();

        if (!stack.isEmpty()) {
            String caller = stack.peek();
            callGraph
                    .computeIfAbsent(caller, k -> new ConcurrentHashMap<>())
                    .merge(currentMethod, 1, Integer::sum);
        }

        stack.push(currentMethod);
    }

    public static void logMethodExit(String className, String methodName) {
        Stack<String> stack = callStack.get();
        if (!stack.isEmpty()) {
            stack.pop();
        }
    }

    public static void printCallGraph() {
        System.out.println("Call Graph:");
        callGraph.forEach((caller, callees) -> {
            callees.forEach((callee, count) -> {
                // System.out.printf("%d", count);
                System.out.printf("%s -> %s [count=%d]%n", caller, callee, count);
            });
        });
    }
}

