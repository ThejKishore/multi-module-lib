package com.tk.learn.web.filter;

import com.tk.learn.model.dto.UserDetailsDto;
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
import java.util.Base64;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Extended integration test for JwtAuthFilter.
 * Tests cache hit and miss scenarios with RequestContextHolder.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter Extended Integration Tests")
class JwtAuthFilterIntegrationTest {

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
    @DisplayName("Should set user details in RequestContextHolder for subsequent request processing")
    void testUserDetailsSetInContextForDownstreamProcessing() throws ServletException, IOException {
        // Arrange
        String token = createValidToken();
        UserDetailsDto expectedUser = new UserDetailsDto(
                "user-456", "john@example.com", "john", "session-456",
                Arrays.asList("USER", "ADMIN"), Arrays.asList("READ", "WRITE"),
                System.currentTimeMillis(), System.currentTimeMillis() + 7200000
        );

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token)).thenReturn(expectedUser);
        when(userCacheService.getUserFromCache("session-456")).thenReturn(null);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert - User details should be available in context for controller
        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        assert contextUser != null;
        assert contextUser.getUserId().equals("user-456");
        assert contextUser.getEmail().equals("john@example.com");
        assert contextUser.getRoles().contains("ADMIN");
    }

    @Test
    @DisplayName("Should use cached user details when available")
    void testCachedUserDetailsUsedWhenAvailable() throws ServletException, IOException {
        // Arrange
        String token = createValidToken();
        UserDetailsDto jwtUser = new UserDetailsDto(
                "user-789", "jwt@example.com", "jwtuser", "session-789",
                Arrays.asList("USER"), null, System.currentTimeMillis(), 
                System.currentTimeMillis() + 3600000
        );
        UserDetailsDto cachedUser = new UserDetailsDto(
                "user-789", "cache@example.com", "cacheduser", "session-789",
                Arrays.asList("USER", "MANAGER"), Arrays.asList("READ", "WRITE", "DELETE"),
                System.currentTimeMillis(), System.currentTimeMillis() + 3600000
        );

        when(request.getHeader("Authorization")).thenReturn(token);
        when(jwtTokenValidator.validateAndExtractClaims(token)).thenReturn(jwtUser);
        when(userCacheService.getUserFromCache("session-789")).thenReturn(cachedUser);

        // Act
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Assert - Cached user details should be used
        UserDetailsDto contextUser = RequestContextHolder.get("userDetails");
        assert contextUser != null;
        assert contextUser.getEmail().equals("cache@example.com");
        assert contextUser.getRoles().size() == 2;
    }

    private String createValidToken() {
        String payload = "{\"userId\":\"user-123\",\"email\":\"user@example.com\"," +
                "\"username\":\"john.doe\",\"sessionId\":\"session-123\"," +
                "\"issuedAt\":\"1629900000\",\"expiresAt\":\"1629903600\"}";
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        return "Bearer header." + encodedPayload + ".signature";
    }
}

