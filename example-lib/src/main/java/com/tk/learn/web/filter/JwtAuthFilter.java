package com.tk.learn.web.filter;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.model.exceptions.InvalidJwtTokenException;
import com.tk.learn.web.context.RequestContextHolder;
import com.tk.learn.web.security.JwtTokenValidator;
import com.tk.learn.web.security.UserCacheService;
import com.tk.learn.web.security.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JWT Authentication Filter that validates incoming JWT tokens and enriches the request context.
 *
 * Features:
 * - Validates JWT token format and claims
 * - Retrieves user details from Redis cache or User Service
 * - Sets user details in RequestContextHolder for downstream processing
 * - Returns 401 Unauthorized for invalid tokens
 * - Can be enabled/disabled via profile-based configuration
 *
 * Order: Executes before SideCarRequestContextFilter via @Order annotation.
 * Profile: Enabled for PCF profile, disabled for Azure profile.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnProperty(
    name = "jwt.auth.filter.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String USER_DETAILS_KEY = "userDetails";
    private static final String JWT_SESSION_ID_KEY = "jwtSessionId";
    private static final String UNAUTHORIZED_RESPONSE = "{\"error\":\"Unauthorized - Invalid or missing JWT token\"}";

    private final JwtTokenValidator jwtTokenValidator;
    private final UserCacheService userCacheService;
    private final UserService userService;
    private final long cacheTtlSeconds;

    @Autowired(required = false)
    public JwtAuthFilter(JwtTokenValidator jwtTokenValidator,
                        @Autowired(required = false) UserCacheService userCacheService,
                        @Autowired(required = false) UserService userService,
                        @Value("${jwt.cache.ttl-seconds:3600}") long cacheTtlSeconds) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.userCacheService = userCacheService;
        this.userService = userService;
        this.cacheTtlSeconds = cacheTtlSeconds;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractJwtToken(request);

            if (StringUtils.hasText(token)) {
                processJwtToken(token, request, response, filterChain);
            } else {
                // No JWT token present; allow filter chain to continue
                log.debug("No JWT token found in request");
                filterChain.doFilter(request, response);
            }

        } catch (InvalidJwtTokenException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            sendUnauthorizedResponse(response);
        } catch (Exception e) {
            log.error("Unexpected error in JWT filter: {}", e.getMessage(), e);
            sendUnauthorizedResponse(response);
        }
    }

    /**
     * Processes a JWT token: validates, retrieves user details, and sets in context.
     */
    private void processJwtToken(String token,
                                HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Validate JWT token and extract claims
            UserDetailsDto userDetails = jwtTokenValidator.validateAndExtractClaims(token);
            log.info("JWT token validated for user: {}", userDetails.getUserId());

            // Step 2: Try to get user details from cache
            String sessionId = userDetails.getSessionId();
            UserDetailsDto cachedUserDetails = getUserDetailsFromCache(sessionId);

            if (cachedUserDetails != null) {
                log.debug("User details retrieved from cache for session: {}", sessionId);
                userDetails = cachedUserDetails;
            } else {
                log.debug("User details not found in cache, fetching from user service");
                // Step 3: Fetch user details from User Service if cache miss
                userDetails = enrichUserDetailsFromService(userDetails);

                // Step 4: Cache the user details for future requests
                if (userCacheService != null && StringUtils.hasText(sessionId)) {
                    userCacheService.cacheUser(sessionId, userDetails, cacheTtlSeconds);
                    log.debug("User details cached for session: {} with TTL: {} seconds", sessionId, cacheTtlSeconds);
                }
            }

            // Step 5: Set user details in RequestContextHolder
            RequestContextHolder.put(USER_DETAILS_KEY, userDetails);
            RequestContextHolder.put(JWT_SESSION_ID_KEY, sessionId);
            log.debug("User details set in RequestContextHolder for session: {}", sessionId);

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // RequestContextHolder is cleared by SideCarRequestContextFilter
        }
    }

    /**
     * Retrieves user details from cache service.
     */
    private UserDetailsDto getUserDetailsFromCache(String sessionId) {
        if (userCacheService == null || !StringUtils.hasText(sessionId)) {
            return null;
        }

        try {
            return userCacheService.getUserFromCache(sessionId);
        } catch (Exception e) {
            log.warn("Error retrieving user details from cache: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Enriches user details by calling the User Service.
     */
    private UserDetailsDto enrichUserDetailsFromService(UserDetailsDto userDetails) {
        if (userService == null) {
            log.warn("UserService not available; cannot enrich user details");
            return userDetails;
        }

        try {
            UserDetailsDto enrichedDetails;
            if (StringUtils.hasText(userDetails.getUserId())) {
                enrichedDetails = userService.getUserDetailsById(userDetails.getUserId());
            } else if (StringUtils.hasText(userDetails.getEmail())) {
                enrichedDetails = userService.getUserDetailsByEmail(userDetails.getEmail());
            } else {
                log.warn("Cannot enrich user details: no userId or email available");
                return userDetails;
            }

            if (enrichedDetails != null) {
                // Merge enriched details with JWT claims
                mergeUserDetails(userDetails, enrichedDetails);
            }

            return userDetails;

        } catch (Exception e) {
            log.warn("Error enriching user details from service: {}", e.getMessage(), e);
            return userDetails;
        }
    }

    /**
     * Merges enriched user details from service with JWT claims.
     */
    private void mergeUserDetails(UserDetailsDto jwtDetails, UserDetailsDto enrichedDetails) {
        // Prefer enriched data if available
        if (enrichedDetails.getEmail() != null) {
            jwtDetails.setEmail(enrichedDetails.getEmail());
        }
        if (enrichedDetails.getUsername() != null) {
            jwtDetails.setUsername(enrichedDetails.getUsername());
        }
        if (enrichedDetails.getRoles() != null && !enrichedDetails.getRoles().isEmpty()) {
            jwtDetails.setRoles(enrichedDetails.getRoles());
        }
        if (enrichedDetails.getPermissions() != null && !enrichedDetails.getPermissions().isEmpty()) {
            jwtDetails.setPermissions(enrichedDetails.getPermissions());
        }
    }

    /**
     * Extracts JWT token from Authorization header.
     */
    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        return authHeader;
    }

    /**
     * Sends a 401 Unauthorized response.
     */
    private void sendUnauthorizedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getOutputStream().write(UNAUTHORIZED_RESPONSE.getBytes(StandardCharsets.UTF_8));
    }
}

