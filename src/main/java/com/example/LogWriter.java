package com.example;

import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {
    private static FileWriter writer;

    static {
        try {
            writer = new FileWriter("profile_log.txt", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void log(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
