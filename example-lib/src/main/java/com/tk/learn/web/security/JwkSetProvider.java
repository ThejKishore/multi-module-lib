package com.tk.learn.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * JWK (JSON Web Key) Set provider that fetches and caches JWK Sets from configured URLs.
 * Used by JwtTokenValidator for JWT signature verification using keys from JWK endpoint.
 */
@Component
public class JwkSetProvider {

    private static final Logger log = LoggerFactory.getLogger(JwkSetProvider.class);

    @Value("${jwt.jwk-set-uri:#{null}}")
    private String jwkSetUri;

    @Value("${jwt.jwk-cache-ttl-minutes:60}")
    private long jwkCacheTtlMinutes;

    private final RestTemplate restTemplate;

    private volatile String cachedJwkSet;
    private volatile long cachedJwkSetTime;

    public JwkSetProvider() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Fetches JWK Set from the configured URI with caching.
     *
     * @return JWK Set JSON string
     * @throws IllegalStateException if JWK Set URI is not configured
     * @throws RuntimeException if fetching JWK Set fails
     */
    public String getJwkSet() {
        if (jwkSetUri == null || jwkSetUri.isEmpty()) {
            throw new IllegalStateException("JWK Set URI (jwt.jwk-set-uri) is not configured");
        }

        // Check cache validity
        if (isCacheValid()) {
            log.debug("Using cached JWK Set");
            return cachedJwkSet;
        }

        try {
            log.debug("Fetching JWK Set from: {}", jwkSetUri);
            String jwkSet = restTemplate.getForObject(new URI(jwkSetUri), String.class);

            if (jwkSet == null || jwkSet.isEmpty()) {
                throw new RuntimeException("JWK Set response is empty");
            }

            // Update cache
            this.cachedJwkSet = jwkSet;
            this.cachedJwkSetTime = System.currentTimeMillis();

            log.info("Successfully fetched JWK Set from: {}", jwkSetUri);
            return jwkSet;

        } catch (Exception e) {
            log.error("Failed to fetch JWK Set from {}: {}", jwkSetUri, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch JWK Set: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if the cached JWK Set is still valid.
     */
    private boolean isCacheValid() {
        if (cachedJwkSet == null) {
            return false;
        }

        long ageMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - cachedJwkSetTime);
        return ageMinutes < jwkCacheTtlMinutes;
    }

    /**
     * Clears the JWK Set cache (useful for testing or manual refresh).
     */
    public void clearCache() {
        log.info("Clearing JWK Set cache");
        this.cachedJwkSet = null;
        this.cachedJwkSetTime = 0;
    }
}

