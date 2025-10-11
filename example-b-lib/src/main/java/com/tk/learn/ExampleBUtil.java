package com.tk.learn;

/**
 * Example-B library utility class.
 */
public class ExampleBUtil {
    public static String shout(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.toUpperCase();
    }
}
