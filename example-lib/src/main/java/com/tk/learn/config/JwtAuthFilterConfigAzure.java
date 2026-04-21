package com.tk.learn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * JWT Authentication Filter configuration for Azure profile.
 * Disables JWT authentication filter in favor of Azure AD integration.
 */
@Configuration
@Profile("azure")
public class JwtAuthFilterConfigAzure {
    // Configuration properties are loaded from application-azure.yml
    // jwt.auth.filter.enabled=false
}

