package com.tk.learn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * JWT Authentication Filter configuration for PCF (Pivotal Cloud Foundry) profile.
 * Enables JWT authentication filter.
 */
@Configuration
@Profile("pcf")
public class JwtAuthFilterConfigPcf {
    // Configuration properties are loaded from application-pcf.yml
    // jwt.auth.filter.enabled=true
}

