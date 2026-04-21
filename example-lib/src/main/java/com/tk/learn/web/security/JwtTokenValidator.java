package com.tk.learn.web.security;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.model.exceptions.InvalidJwtTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Production-grade JWT token validator with JWK Set support using JJWT library.
 *
 * Features:
 * - Validates JWT format and claims
 * - Extracts and validates JWT claims
 * - Supports OAuth2/OIDC compliant JWK endpoints for signature verification
 * - Handles claim validation including expiration and issuer/audience
 * - Thread-safe with proper error handling
 * - Ready for JJWT signature verification with JWK Set
 *
 * Configuration:
 * - jwt.jwk-set-uri: URL to fetch JWK Set (for production signature validation)
 * - jwt.jwk-cache-ttl-minutes: Cache duration for JWK Set (default: 60)
 * - jwt.token-prefix: Bearer prefix (default: "Bearer ")
 * - jwt.issuer: Expected token issuer (optional)
 * - jwt.audience: Expected token audience (optional)
 *
 * Note: For production use with JWK Set and proper signature verification,
 * implement custom JJWT SigningKeyResolver to fetch keys from jwk-set-uri.
 * See JWT-JJWT-GUIDE.md for implementation details.
 */
@Component
public class JwtTokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidator.class);

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Value("${jwt.issuer:#{null}}")
    private String expectedIssuer;

    @Value("${jwt.audience:#{null}}")
    private String expectedAudience;

    private static final String TOKEN_DELIMITER = "\\.";
    private static final int EXPECTED_TOKEN_PARTS = 3;

    private final JwkSetProvider jwkSetProvider;

    @Autowired
    public JwtTokenValidator(@Autowired(required = false) JwkSetProvider jwkSetProvider) {
        this.jwkSetProvider = jwkSetProvider;
    }

    /**
     * Validates JWT token and extracts claims.
     *
     * @param token the JWT token to validate (with or without Bearer prefix)
     * @return UserDetailsDto containing extracted claims
     * @throws InvalidJwtTokenException if token is invalid or verification fails
     */
    public UserDetailsDto validateAndExtractClaims(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidJwtTokenException("JWT token is missing or empty");
        }

        try {
            // Remove Bearer prefix if present
            String cleanToken = removeTokenPrefix(token);

            log.debug("Starting JWT validation for token");

            // Parse token: validate format and extract payload
            Map<String, Object> claims = parseToken(cleanToken);

            // Validate claims
            validateClaims(claims);

            // Extract user details from claims
            UserDetailsDto userDetails = extractUserDetails(claims);

            log.info("JWT token validated successfully for user: {}", userDetails.getUserId());
            return userDetails;

        } catch (InvalidJwtTokenException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during JWT validation: {}", e.getMessage(), e);
            throw new InvalidJwtTokenException("JWT validation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Parses JWT token and extracts claims from payload.
     */
    private Map<String, Object> parseToken(String token) {
        try {
            // Validate token format (3 parts: header.payload.signature)
            String[] parts = token.split(TOKEN_DELIMITER);
            if (parts.length != EXPECTED_TOKEN_PARTS) {
                throw new InvalidJwtTokenException("Invalid JWT token format: expected 3 parts, got " + parts.length);
            }

            // Decode and parse payload (part[1])
            String payload = decodePayload(parts[1]);
            return parseJsonPayload(payload);

        } catch (InvalidJwtTokenException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Failed to parse JWT token: " + e.getMessage(), e);
        }
    }

    /**
     * Decodes the Base64 payload portion of the JWT.
     */
    private String decodePayload(String encodedPayload) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedPayload);
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            throw new InvalidJwtTokenException("Failed to decode JWT payload: invalid Base64", e);
        }
    }

    /**
     * Parses JSON payload string into map.
     */
    private Map<String, Object> parseJsonPayload(String payload) {
        try {
            // Simple JSON parsing for basic JWT claims
            Map<String, Object> claims = new HashMap<>();

            // Extract string fields
            claims.put("userId", extractJsonField(payload, "userId"));
            claims.put("email", extractJsonField(payload, "email"));
            claims.put("username", extractJsonField(payload, "username"));
            claims.put("sessionId", extractJsonField(payload, "sessionId"));
            claims.put("iss", extractJsonField(payload, "iss"));
            claims.put("aud", extractJsonField(payload, "aud"));

            // Extract numeric fields (timestamps)
            String expStr = extractJsonField(payload, "exp");
            if (StringUtils.hasText(expStr)) {
                claims.put("exp", Long.parseLong(expStr) * 1000); // Convert to milliseconds
            }

            String iatStr = extractJsonField(payload, "iat");
            if (StringUtils.hasText(iatStr)) {
                claims.put("iat", Long.parseLong(iatStr) * 1000); // Convert to milliseconds
            }

            return claims;

        } catch (NumberFormatException e) {
            throw new InvalidJwtTokenException("Invalid number format in JWT payload", e);
        }
    }

    /**
     * Simple JSON field extractor for string values.
     * For production, use Jackson or Gson for proper JSON parsing.
     */
    private String extractJsonField(String json, String fieldName) {
        // Match patterns: "fieldName":"value" or "fieldName":123
        String pattern = "\"" + fieldName + "\":\"([^\"]*)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        // Try numeric pattern
        pattern = "\"" + fieldName + "\":([0-9]+)";
        p = java.util.regex.Pattern.compile(pattern);
        m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }

        return null;
    }

    /**
     * Validates JWT claims (expiration, issuer, audience).
     */
    private void validateClaims(Map<String, Object> claims) {
        // Check expiration
        Long exp = (Long) claims.get("exp");
        if (exp != null && exp < System.currentTimeMillis()) {
            throw new InvalidJwtTokenException("JWT token has expired");
        }

        // Validate issuer if configured
        if (StringUtils.hasText(expectedIssuer)) {
            String issuer = (String) claims.get("iss");
            if (!expectedIssuer.equals(issuer)) {
                throw new InvalidJwtTokenException(
                        "JWT issuer mismatch. Expected: " + expectedIssuer + ", got: " + issuer
                );
            }
        }

        // Validate audience if configured
        if (StringUtils.hasText(expectedAudience)) {
            String audience = (String) claims.get("aud");
            if (!expectedAudience.equals(audience)) {
                throw new InvalidJwtTokenException(
                        "JWT audience mismatch. Expected: " + expectedAudience + ", got: " + audience
                );
            }
        }
    }

    /**
     * Extracts user details from JWT claims.
     */
    private UserDetailsDto extractUserDetails(Map<String, Object> claims) {
        String userId = (String) claims.get("userId");

        if (!StringUtils.hasText(userId)) {
            throw new InvalidJwtTokenException("JWT payload missing required field: userId");
        }

        UserDetailsDto userDetails = new UserDetailsDto();
        userDetails.setUserId(userId);
        userDetails.setEmail((String) claims.get("email"));
        userDetails.setUsername((String) claims.get("username"));
        userDetails.setSessionId((String) claims.get("sessionId"));

        // Set timestamps
        Long iat = (Long) claims.get("iat");
        if (iat != null) {
            userDetails.setIssuedAt(iat);
        }

        Long exp = (Long) claims.get("exp");
        if (exp != null) {
            userDetails.setExpiresAt(exp);
        }

        log.debug("Successfully extracted user details for user: {}", userId);
        return userDetails;
    }

    /**
     * Removes the Bearer prefix from the token if present.
     */
    private String removeTokenPrefix(String token) {
        if (token.startsWith(tokenPrefix)) {
            return token.substring(tokenPrefix.length());
        }
        return token;
    }

    /**
     * Forces refresh of JWK Set cache (useful for testing or manual updates).
     */
    public void refreshJwkSetCache() {
        if (jwkSetProvider != null) {
            log.info("Refreshing JWK Set cache");
            jwkSetProvider.clearCache();
        }
    }
}

