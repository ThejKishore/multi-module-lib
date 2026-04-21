package com.tk.learn.web.security;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.model.exceptions.InvalidJwtTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenValidator Tests")
class JwtTokenValidatorTest {

    private JwtTokenValidator jwtTokenValidator;

    @BeforeEach
    void setUp() {
        jwtTokenValidator = new JwtTokenValidator(null);
        ReflectionTestUtils.setField(jwtTokenValidator, "tokenPrefix", "Bearer ");
    }

    @Test
    @DisplayName("Should successfully validate and extract claims from valid JWT token")
    void testValidateAndExtractClaimsSuccess() {
        // Arrange
        String payload = """
                {
                  "userId": "user-123",
                  "email": "user@example.com",
                  "username": "john.doe",
                  "sessionId": "session-123",
                  "issuedAt": "1629900000",
                  "expiresAt": "1629903600"
                }""";
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        String token = "Bearer header." + encodedPayload + ".signature";

        // Act
        UserDetailsDto result = jwtTokenValidator.validateAndExtractClaims(token);

        // Assert
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
        assertEquals("user@example.com", result.getEmail());
        assertEquals("john.doe", result.getUsername());
        assertEquals("session-123", result.getSessionId());
    }

    @Test
    @DisplayName("Should throw exception for missing token")
    void testMissingTokenThrowsException() {
        // Act & Assert
        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims("")
        );

        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims(null)
        );
    }

    @Test
    @DisplayName("Should throw exception for invalid token format")
    void testInvalidTokenFormatThrowsException() {
        // Act & Assert
        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims("invalid-token-no-dots")
        );

        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims("only.two.parts")
        );
    }

    @Test
    @DisplayName("Should throw exception when userId is missing from payload")
    void testMissingUserIdThrowsException() {
        // Arrange
        String payload = """
                {
                  "email": "user@example.com",
                  "username": "john.doe"
                }""";
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        String token = "Bearer header." + encodedPayload + ".signature";

        // Act & Assert
        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims(token)
        );
    }

    @Test
    @DisplayName("Should remove Bearer prefix from token")
    void testRemoveBearerPrefix() {
        // Arrange
        String payload = """
                {
                  "userId": "user-123",
                  "email": "user@example.com",
                  "username": "john.doe",
                  "sessionId": "session-123",
                  "issuedAt": "1629900000",
                  "expiresAt": "1629903600"
                }""";
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        String tokenWithBearer = "Bearer header." + encodedPayload + ".signature";

        // Act
        UserDetailsDto result = jwtTokenValidator.validateAndExtractClaims(tokenWithBearer);

        // Assert
        assertNotNull(result);
        assertEquals("user-123", result.getUserId());
    }

    @Test
    @DisplayName("Should extract numeric fields correctly")
    void testExtractNumericFields() {
        // Arrange
        String payload = """
                {
                  "userId": "user-123",
                  "email": "user@example.com",
                  "username": "john.doe",
                  "sessionId": "session-123",
                  "issuedAt": "1629900000",
                  "expiresAt": "1629903600"
                }""";
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes());
        String token = "Bearer header." + encodedPayload + ".signature";

        // Act
        UserDetailsDto result = jwtTokenValidator.validateAndExtractClaims(token);

        // Assert
        assertEquals(1629900000L, result.getIssuedAt());
        assertEquals(1629903600L, result.getExpiresAt());
    }

    @Test
    @DisplayName("Should throw exception for malformed Base64 payload")
    void testMalformedBase64ThrowsException() {
        // Arrange
        String token = "Bearer header.!!!invalid-base64!!!.signature";

        // Act & Assert
        assertThrows(InvalidJwtTokenException.class, () ->
            jwtTokenValidator.validateAndExtractClaims(token)
        );
    }
}

