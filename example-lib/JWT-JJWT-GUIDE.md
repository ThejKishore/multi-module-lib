# JWT Validation with JJWT & JWK Set - Implementation Guide

## Overview

The JWT validator has been upgraded to use **JJWT (Java JWT)** library with JWK (JSON Web Key) Set support for production-grade JWT validation.

## Key Features

✅ **JJWT Library (0.13.0)**
- Industry-standard JWT library
- Proper signature verification
- Claims validation (expiration, issuer, audience)
- Supports multiple algorithms (HS256, RS256, ES256, etc.)

✅ **JWK Set Support**
- Fetches public keys from configurable JWK endpoint
- Automatic caching with TTL
- Supports OAuth2/OIDC compliant endpoints

✅ **Production-Ready**
- Proper error handling for all JWT exceptions
- Detailed logging for troubleshooting
- Thread-safe implementation
- Configurable claim validation

---

## Configuration

### Required Properties

```yaml
jwt:
  # JWK Set URI - Endpoint that provides public keys
  jwk-set-uri: https://your-auth-provider.com/.well-known/jwks.json
  
  # Cache duration (in minutes)
  jwk-cache-ttl-minutes: 60
  
  # Optional: Validate token issuer
  issuer: https://your-auth-provider.com
  
  # Optional: Validate token audience
  audience: your-app-id
  
  # Token prefix (default: "Bearer ")
  token-prefix: "Bearer "
  
  auth:
    filter:
      enabled: true
  
  cache:
    ttl-seconds: 3600
```

### Environment Variables (Recommended)

```bash
export JWT_JWK_SET_URI=https://your-auth-provider.com/.well-known/jwks.json
export JWT_ISSUER=https://your-auth-provider.com
export JWT_AUDIENCE=your-app-id
```

---

## Common JWK Set URIs

### Azure AD
```
https://login.microsoftonline.com/{tenant}/discovery/v2.0/keys
```

### Google
```
https://www.googleapis.com/oauth2/v3/certs
```

### Auth0
```
https://{domain}/.well-known/jwks.json
```

### Keycloak
```
https://{keycloak-server}/realms/{realm}/protocol/openid-connect/certs
```

### Okta
```
https://{okta-domain}/oauth2/{auth-server-id}/v1/keys
```

### Generic OAuth2
```
https://{oauth-provider}/.well-known/jwks.json
```

---

## Classes

### 1. JwtTokenValidator (Updated)

**Location**: `com.tk.learn.web.security.JwtTokenValidator`

**Methods**:
- `validateAndExtractClaims(token)` - Main validation method
- `refreshJwkSetCache()` - Force refresh JWK Set cache

**Handles**:
- Bearer token prefix removal
- JWT signature verification using JWK Set
- Claims validation (expiration, issuer, audience)
- User details extraction from claims
- Comprehensive exception handling

### 2. JwkSetProvider (New)

**Location**: `com.tk.learn.web.security.JwkSetProvider`

**Methods**:
- `getJwkSet()` - Fetch JWK Set with caching
- `clearCache()` - Clear cached JWK Set

**Features**:
- Automatic caching with configurable TTL
- REST client for fetching JWK Sets
- Thread-safe implementation
- Detailed logging

---

## Supported JWT Exceptions

The validator handles the following exceptions from JJWT:

| Exception | Cause | HTTP Status |
|-----------|-------|-------------|
| `SignatureException` | Invalid signature | 401 |
| `ExpiredJwtException` | Token expired | 401 |
| `UnsupportedJwtException` | Unsupported format | 401 |
| `MalformedJwtException` | Malformed token | 401 |
| `IllegalArgumentException` | Invalid arguments | 401 |
| `InvalidJwtTokenException` | Custom validation failure | 401 |

---

## Expected JWT Claims

### Minimum Required
```json
{
  "userId": "user-12345",
  "exp": 1713693600,
  "iat": 1713607200
}
```

### Recommended
```json
{
  "userId": "user-12345",
  "email": "user@example.com",
  "username": "john.doe",
  "sessionId": "session-abc-123",
  "roles": ["USER", "ADMIN"],
  "permissions": ["READ", "WRITE"],
  "iss": "https://auth-provider.com",
  "aud": "your-app-id",
  "exp": 1713693600,
  "iat": 1713607200
}
```

---

## JWT Token Format

```
Bearer <base64-encoded-header>.<base64-encoded-payload>.<base64-encoded-signature>
```

Example:
```
Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLTEyMyIsImVtYWlsIjoiam9obkBleGFtcGxlLmNvbSIsImV4cCI6MTcxMzY5MzYwMH0.signature...
```

---

## Configuration Examples

### PCF with Custom OAuth2 Provider

```yaml
# application-pcf.yml
jwt:
  auth:
    filter:
      enabled: true
  jwk-set-uri: https://oauth.mycompany.com/.well-known/jwks.json
  issuer: https://oauth.mycompany.com
  audience: my-app
  jwk-cache-ttl-minutes: 120
```

### Azure with Azure AD

```yaml
# application-azure.yml
jwt:
  auth:
    filter:
      enabled: true  # Can be enabled for Azure AD tokens
  jwk-set-uri: https://login.microsoftonline.com/common/discovery/v2.0/keys
  issuer: https://sts.windows.net/{tenant-id}/
  audience: ${AZURE_APP_ID}
  jwk-cache-ttl-minutes: 60
```

### Development/Testing

```yaml
jwt:
  auth:
    filter:
      enabled: true
  # Use a test OAuth provider or mock endpoint
  jwk-set-uri: http://localhost:8080/api/jwks
  issuer: http://localhost:8080
  audience: test-app
```

---

## Usage

### In Controllers

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<UserDetailsDto> getCurrentUser() {
        // User details already set by JwtAuthFilter
        UserDetailsDto user = RequestContextHolder.get("userDetails");
        return ResponseEntity.ok(user);
    }
}
```

### Client Request

```bash
# Get JWT token from auth provider
JWT_TOKEN=$(curl -X POST https://auth-provider.com/token \
  -d "client_id=..." \
  -d "client_secret=..." \
  -d "grant_type=client_credentials" | jq -r '.access_token')

# Use token in request
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $JWT_TOKEN"
```

---

## Caching

### JWK Set Caching

JWK Sets are cached to improve performance:

- **Cache Duration**: Configurable via `jwt.jwk-cache-ttl-minutes` (default: 60)
- **Cache Key**: JWK Set URI
- **Cache Invalidation**: Automatic based on TTL
- **Manual Refresh**: Call `jwtTokenValidator.refreshJwkSetCache()`

### Cache Hit Example
```
Request 1: Fetches JWK Set from URL (cache miss)
Request 2-100: Uses cached JWK Set
After 60 minutes: Refreshes JWK Set from URL
```

---

## Error Handling

### Signature Verification Failed
```
log.warn("JWT signature verification failed: ...")
HTTP Status: 401 Unauthorized
```

### Token Expired
```
log.warn("JWT token has expired: ...")
HTTP Status: 401 Unauthorized
```

### Issuer Mismatch
```
log.warn("JWT issuer mismatch. Expected: ..., got: ...")
HTTP Status: 401 Unauthorized
```

---

## Troubleshooting

### JWK Set Not Found
**Error**: `java.net.UnknownHostException` or `HTTP 404`

**Solution**: Verify JWK Set URI is correct and accessible

```bash
# Test JWK Set endpoint
curl -X GET https://auth-provider.com/.well-known/jwks.json
```

### JWT Signature Invalid
**Error**: `SignatureException: JWT signature does not match`

**Solution**: 
1. Ensure JWK Set contains correct public keys
2. Verify token is signed with correct private key
3. Check JWK Set is being fetched correctly

### Token Expired
**Error**: `ExpiredJwtException: JWT expired`

**Solution**: 
1. Get a fresh token from auth provider
2. Check system clock is synchronized

### Issuer/Audience Mismatch
**Error**: `JWT issuer/audience mismatch`

**Solution**: 
1. Verify `jwt.issuer` matches token's `iss` claim
2. Verify `jwt.audience` matches token's `aud` claim
3. Or remove these validations if not needed

---

## Testing

### Unit Tests

Test with mock JWK Set:

```java
@Test
void testValidJwtWithJwkSet() {
    // Create a test JWT token
    String token = createTestJwt();
    
    // Mock JwkSetProvider
    JwkSetProvider mockProvider = mock(JwkSetProvider.class);
    when(mockProvider.getJwkSet()).thenReturn(testJwkSet);
    
    // Validate
    UserDetailsDto result = jwtTokenValidator.validateAndExtractClaims(token);
    
    // Assert
    assertEquals("user-123", result.getUserId());
}
```

### Integration Tests

Test against real OAuth provider:

```bash
# Get real token
TOKEN=$(curl -s -X POST https://auth-provider.com/token \
  -d "..." | jq -r '.access_token')

# Test validation
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Dependencies

### Added to build.gradle.kts

```groovy
implementation("io.jsonwebtoken:jjwt-api:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
```

### Why Three Artifacts?

- **jjwt-api**: Core JWT interfaces and exceptions
- **jjwt-impl**: Implementation (runtime only)
- **jjwt-jackson**: JSON serialization via Jackson (runtime only)

---

## Performance Considerations

### JWK Set Caching
- **Without Cache**: 100ms+ per request (network call)
- **With Cache**: <1ms per request (in-memory)
- **Cache TTL**: 60 minutes (configurable)

### Recommendation
- Set `jwk-cache-ttl-minutes` to 60 for most cases
- Reduce to 15-30 minutes if JWK Set changes frequently
- Increase to 240+ minutes in high-volume scenarios

---

## Security Best Practices

✅ **Always use HTTPS** for JWK Set endpoint
✅ **Validate issuer and audience** if possible
✅ **Monitor cache invalidation** - implement alerts
✅ **Log all validation failures** for audit trail
✅ **Use strong algorithms** (RS256, ES256 preferred over HS256)
✅ **Rotate keys regularly** in auth provider
✅ **Implement rate limiting** on auth endpoints
✅ **Validate token expiration** (already done)

---

## Migration from Simple Validator

### Old Implementation (Simple Base64)
```java
// Old: Just decoded Base64, no signature verification
String payload = Base64.decode(token);
parseJsonManually(payload);
```

### New Implementation (JJWT with JWK)
```java
// New: Proper JWT validation with signature verification
Claims claims = jwtParser.parseSignedClaims(token).getPayload();
extractUserDetailsFromClaims(claims);
```

**Benefits**:
- ✅ Signature verification
- ✅ Claims validation
- ✅ Exception handling
- ✅ Production-ready
- ✅ Industry standard

---

## Next Steps

1. **Configure JWK Set URI**
   ```bash
   export JWT_JWK_SET_URI=https://your-auth-provider.com/.well-known/jwks.json
   ```

2. **Test with real token**
   ```bash
   curl -H "Authorization: Bearer <token>" http://localhost:8080/api/users/me
   ```

3. **Monitor logs**
   ```
   Watch for JWT validation errors and cache hits
   ```

4. **Tune cache TTL**
   ```yaml
   jwt.jwk-cache-ttl-minutes: 60  # Adjust based on your needs
   ```

---

## References

- [JJWT Documentation](https://github.com/jwtk/jjwt)
- [JWT.io - JWT Introduction](https://jwt.io/introduction)
- [RFC 7519 - JSON Web Token](https://tools.ietf.org/html/rfc7519)
- [RFC 7517 - JSON Web Key](https://tools.ietf.org/html/rfc7517)
- [RFC 7518 - JSON Web Algorithms](https://tools.ietf.org/html/rfc7518)

