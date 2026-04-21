# JWT Auth Filter - Quick Reference

## 🚀 Quick Start

### 1. Enable Filter
Set application properties for PCF profile:
```yaml
# application-pcf.yml
jwt:
  auth:
    filter:
      enabled: true
  secret-key: ${JWT_SECRET_KEY}
  cache:
    ttl-seconds: 3600
```

### 2. Run Application
```bash
java -jar app.jar --spring.profiles.active=pcf
```

### 3. Send Request with JWT
```bash
curl -H "Authorization: Bearer <jwt_token>" http://localhost:8080/api/endpoint
```

### 4. Access User Details
```java
@GetMapping("/me")
public UserDetailsDto getCurrentUser() {
    UserDetailsDto user = RequestContextHolder.get("userDetails");
    return user;
}
```

---

## 📁 Key Files

| File | Purpose |
|------|---------|
| `JwtAuthFilter.java` | Main filter - validates JWT & enriches context |
| `JwtTokenValidator.java` | Validates JWT format & extracts claims |
| `UserDetailsDto.java` | DTO for user information |
| `UserCacheService.java` | Interface for cache (Redis, etc.) |
| `UserService.java` | Interface for user details service |
| `application-pcf.yml` | Configuration for PCF profile |
| `application-azure.yml` | Configuration for Azure profile |

---

## 🔐 JWT Token Format

### Header
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

### Payload
```json
{
  "userId": "user-123",
  "email": "user@example.com",
  "username": "john.doe",
  "sessionId": "session-abc-123",
  "roles": ["ADMIN", "USER"],
  "permissions": ["READ", "WRITE"],
  "issuedAt": 1713607200000,
  "expiresAt": 1713693600000
}
```

### Format
```
Bearer header.payload.signature
```

---

## ⚙️ Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `jwt.auth.filter.enabled` | false | Enable/disable filter |
| `jwt.secret-key` | default-secret-key | JWT secret for validation |
| `jwt.header-name` | Authorization | HTTP header name |
| `jwt.token-prefix` | Bearer  | Token prefix (with space) |
| `jwt.cache.ttl-seconds` | 3600 | Cache TTL in seconds |

---

## 🎯 Request Flow

```
1. Filter receives request with Authorization header
2. Validates JWT token format
3. Extracts user claims from payload
4. Tries to get user from cache
   - If found: Use cached user details
   - If not found: Call UserService
5. Cache the user details
6. Set in RequestContextHolder
7. Continue to next filter/controller
```

---

## ✅ Response Codes

| Status | Condition |
|--------|-----------|
| 200 | Valid JWT, request processed |
| 401 | Invalid or missing JWT token |
| 400 | Malformed request |

### 401 Response
```json
{
  "error": "Unauthorized - Invalid or missing JWT token"
}
```

---

## 📝 Usage Examples

### Example 1: Get Current User
```java
@GetMapping("/api/users/me")
public ResponseEntity<UserDetailsDto> getCurrentUser() {
    UserDetailsDto user = RequestContextHolder.get("userDetails");
    if (user == null) {
        return ResponseEntity.status(401).build();
    }
    return ResponseEntity.ok(user);
}
```

### Example 2: Check User Roles
```java
@GetMapping("/api/admin/data")
public ResponseEntity<?> getAdminData() {
    UserDetailsDto user = RequestContextHolder.get("userDetails");
    if (!user.getRoles().contains("ADMIN")) {
        return ResponseEntity.status(403).build();
    }
    return ResponseEntity.ok(adminData);
}
```

### Example 3: Use Session ID
```java
@PostMapping("/api/logout")
public ResponseEntity<?> logout() {
    String sessionId = RequestContextHolder.get("jwtSessionId");
    userCacheService.removeUserFromCache(sessionId);
    return ResponseEntity.ok("Logged out");
}
```

---

## 🔧 Custom Implementations

### Redis Cache Service
```java
@Service
public class RedisUserCacheService implements UserCacheService {
    @Override
    public UserDetailsDto getUserFromCache(String sessionId) {
        return redisTemplate.opsForValue().get("user:" + sessionId);
    }
}
```

### User Service
```java
@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserDetailsDto getUserDetailsById(String userId) {
        return userRepository.findById(userId)
            .map(this::mapToDto)
            .orElse(null);
    }
}
```

---

## 🧪 Testing

### Unit Test
```java
@Test
void testValidJwtToken() throws ServletException, IOException {
    when(jwtTokenValidator.validateAndExtractClaims(token))
        .thenReturn(userDetails);
    jwtAuthFilter.doFilterInternal(request, response, chain);
    verify(chain).doFilter(request, response);
}
```

### Integration Test
```bash
# Test with valid token
curl -H "Authorization: Bearer eyJhbGc..." http://localhost:8080/api/me

# Test without token
curl http://localhost:8080/api/me

# Test with invalid token
curl -H "Authorization: Bearer invalid" http://localhost:8080/api/me
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Filter not executing | Check `jwt.auth.filter.enabled=true` and profile is `pcf` |
| Null UserDetailsDto | Verify JwtTokenValidator extracts correct fields |
| 401 on valid token | Check JWT payload has required fields (userId, sessionId) |
| Cache not working | Implement custom UserCacheService with Redis |
| User details not enriched | Implement UserService to fetch from database |

---

## 📚 References

- Full Guide: `JWT-AUTH-FILTER-GUIDE.md`
- Examples: `JWT-AUTH-FILTER-EXAMPLES.md`
- Implementation: `JWT-IMPLEMENTATION-SUMMARY.md`
- JWT Token Examples: `jwt-example.json`

---

## 🔒 Security Notes

- Always use HTTPS in production
- Store JWT secret in secure environment variables
- Validate token expiration
- Implement rate limiting on auth endpoints
- Audit all authentication attempts
- Use strong secret keys (min 256 bits for HS256)
- Consider short-lived tokens with refresh token rotation

---

## 📞 Support

For issues or questions:
1. Check JWT-AUTH-FILTER-GUIDE.md for detailed documentation
2. Review JWT-AUTH-FILTER-EXAMPLES.md for implementation patterns
3. Check unit tests for usage examples
4. Verify configuration in application-pcf.yml

