package com.tk.learn;

/**
 * A tiny example library class to demonstrate multi-module publishing.
 */
public class ExampleUtil {
    public static String greet(String name) {
        if (name == null || name.isBlank()) {
            return "Hello";
        }
        return "Hello, " + name + "!";
    }
}
