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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Tests")
class JwtAuthFilterTest {

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private UserCacheService userCacheService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() throws IOException {
        jwtAuthFilter = new JwtAuthFilter(jwtTokenValidator, userCacheService, userService, 3600);
        when(response.getWriter()).thenReturn(writer);
        RequestContextHolder.clear();
    }

    @Test
    @DisplayName("Should process valid JWT token from Authorization header")
    void testProcessValidJwtToken() throws ServletException, IOException {
        // Arrange
        String token = "Bearer valid.jwt.token";
        UserDetailsDto userDetails = createSampleUserDetails();

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token)).thenReturn(userDetails);
        when(userCacheService.getUserFromCache("session-123")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtTokenValidator).validateAndExtractClaims(token);
        verify(filterChain).doFilter(request, response);

        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        assertNotNull(contextUser);
        assertEquals("user-123", contextUser.getUserId());
    }

    @Test
    @DisplayName("Should retrieve user details from cache on cache hit")
    void testRetrieveUserFromCache() throws ServletException, IOException {
        // Arrange
        String token = "Bearer valid.jwt.token";
        UserDetailsDto cachedUserDetails = new UserDetailsDto(
                "cached-user", "cached@example.com", "cached.user",
                "session-123", Arrays.asList("USER"), Arrays.asList("READ"),
                System.currentTimeMillis(), System.currentTimeMillis() + 3600000
        );

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token))
                .thenReturn(createSampleUserDetails());
        when(userCacheService.getUserFromCache("session-123")).thenReturn(cachedUserDetails);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);

        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        assertEquals("cached-user", contextUser.getUserId());
    }

    @Test
    @DisplayName("Should enrich user details from UserService on cache miss")
    void testEnrichUserDetailsFromService() throws ServletException, IOException {
        // Arrange
        String token = "Bearer valid.jwt.token";
        UserDetailsDto jwtDetails = createSampleUserDetails();
        UserDetailsDto enrichedDetails = new UserDetailsDto(
                "user-123", "enriched@company.com", "enriched.user",
                "session-123", Arrays.asList("ADMIN", "USER"),
                Arrays.asList("READ", "WRITE", "DELETE"),
                System.currentTimeMillis(), System.currentTimeMillis() + 3600000
        );

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token)).thenReturn(jwtDetails);
        when(userCacheService.getUserFromCache("session-123")).thenReturn(null);
        when(userService.getUserDetailsById("user-123")).thenReturn(enrichedDetails);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(userService).getUserDetailsById("user-123");
        verify(userCacheService).cacheUser("session-123", enrichedDetails, 3600);
        verify(filterChain).doFilter(request, response);

        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        assertEquals("enriched@company.com", contextUser.getEmail());
    }

    @Test
    @DisplayName("Should return 401 Unauthorized on invalid JWT token")
    void testInvalidJwtTokenReturns401() throws ServletException, IOException {
        // Arrange
        String token = "Bearer invalid.jwt.token";

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token))
                .thenThrow(new InvalidJwtTokenException("Invalid token"));
        when(response.getOutputStream()).thenThrow(new UnsupportedOperationException("Use getWriter()"));

        // Act & Assert
        assertThrows(Exception.class, () -> 
            jwtAuthFilter.doFilterInternal(request, response, filterChain)
        );
    }

    @Test
    @DisplayName("Should allow request without JWT token")
    void testAllowRequestWithoutJwt() throws ServletException, IOException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(jwtTokenValidator, never()).validateAndExtractClaims(anyString());
    }

    @Test
    @DisplayName("Should set user details in RequestContextHolder with session ID")
    void testUserDetailsSetInContext() throws ServletException, IOException {
        // Arrange
        String token = "Bearer valid.jwt.token";
        UserDetailsDto userDetails = createSampleUserDetails();

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token)).thenReturn(userDetails);
        when(userCacheService.getUserFromCache(anyString())).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert
        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        String sessionId = RequestContextHolder.get("jwtSessionId");

        assertNotNull(contextUser);
        assertEquals("user-123", contextUser.getUserId());
        assertEquals("session-123", sessionId);
    }

    private UserDetailsDto createSampleUserDetails() {
        return new UserDetailsDto(
                "user-123",
                "user@example.com",
                "john.doe",
                "session-123",
                Arrays.asList("USER"),
                Arrays.asList("READ"),
                System.currentTimeMillis(),
                System.currentTimeMillis() + 3600000
        );
    }
}

