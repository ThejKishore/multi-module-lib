package com.tk.learn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        // Set JVM default timezone (use IANA zone to handle DST)
        TimeZone tz = TimeZone.getTimeZone("America/Chicago");
        TimeZone.setDefault(tz);
        // Also set the system property so libraries that read user.timezone pick it up
        System.setProperty("user.timezone", "America/Chicago");
        SpringApplication.run(Application.class, args);
    }
}
