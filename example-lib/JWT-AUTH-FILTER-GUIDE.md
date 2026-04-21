# JWT Authentication Filter - Implementation Guide

## Overview

The `JwtAuthFilter` is a Spring Security filter that validates JWT tokens, retrieves user details from cache or service, and makes them available to downstream components via `RequestContextHolder`.

## Architecture

```
HTTP Request
    ↓
JwtAuthFilter (Order: HIGHEST_PRECEDENCE + 1)
    ├─ Extract JWT from Authorization header
    ├─ Validate JWT format and claims
    ├─ Try to retrieve from UserCacheService (Redis)
    ├─ If cache miss, call UserService to enrich user details
    ├─ Cache user details for future requests
    ├─ Store in RequestContextHolder
    └─ Return 401 if invalid
    ↓
SideCarRequestContextFilter
    ↓
Application Controllers
    ↓
RequestContextHolder.get("userDetails") → UserDetailsDto
```

## Key Components

### 1. JwtAuthFilter
- Location: `com.tk.learn.web.filter.JwtAuthFilter`
- Responsibilities:
  - Extract JWT token from Authorization header
  - Validate JWT token using JwtTokenValidator
  - Manage user details caching
  - Set user context in RequestContextHolder
  - Return 401 Unauthorized for invalid tokens

### 2. JwtTokenValidator
- Location: `com.tk.learn.web.security.JwtTokenValidator`
- Responsibilities:
  - Validate JWT format (3 parts separated by dots)
  - Decode Base64-encoded payload
  - Extract claims from JWT payload
  - Throw InvalidJwtTokenException on validation failure

### 3. UserDetailsDto
- Location: `com.tk.learn.model.dto.UserDetailsDto`
- Contains user information extracted from JWT and enriched by UserService

### 4. Service Interfaces
- `UserCacheService`: Interface for caching user details (implement for Redis)
- `UserService`: Interface for retrieving user details from external systems

### 5. No-op Implementations
- `NoOpUserCacheService`: Default cache service (no caching)
- `NoOpUserService`: Default user service (no enrichment)

## Configuration

### Enable Filter by Profile

#### PCF Profile (application-pcf.yml)
```yaml
jwt:
  auth:
    filter:
      enabled: true
  secret-key: ${JWT_SECRET_KEY}
  cache:
    ttl-seconds: 3600
```

#### Azure Profile (application-azure.yml)
```yaml
jwt:
  auth:
    filter:
      enabled: false
```

### Run Application with PCF Profile
```bash
java -jar app.jar --spring.profiles.active=pcf
```

## Usage Example

### 1. Extract User Details in Controller
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDto> getCurrentUser() {
        UserDetailsDto userDetails = RequestContextHolder.get("userDetails");
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userDetails);
    }
}
```

### 2. JWT Token Format
Expected JWT payload structure:
```json
{
  "userId": "user123",
  "email": "user@example.com",
  "username": "john.doe",
  "sessionId": "session-abc-123",
  "roles": ["ADMIN", "USER"],
  "permissions": ["READ", "WRITE"],
  "issuedAt": 1629900000000,
  "expiresAt": 1629903600000
}
```

### 3. Include JWT in Request
```bash
curl -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
     http://localhost:8080/api/users/me
```

## Custom Implementations

### Implement UserCacheService with Redis
```java
@Component
public class RedisUserCacheService implements UserCacheService {

    @Autowired
    private RedisTemplate<String, UserDetailsDto> redisTemplate;

    @Override
    public UserDetailsDto getUserFromCache(String sessionId) {
        return (UserDetailsDto) redisTemplate.opsForValue().get("user:" + sessionId);
    }

    @Override
    public void cacheUser(String sessionId, UserDetailsDto userDetails, long ttlSeconds) {
        redisTemplate.opsForValue().set("user:" + sessionId, userDetails,
                Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public void removeUserFromCache(String sessionId) {
        redisTemplate.delete("user:" + sessionId);
    }
}
```

### Implement UserService with Database
```java
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetailsDto getUserDetailsById(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        return mapToDto(user);
    }

    @Override
    public UserDetailsDto getUserDetailsByEmail(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return null;
        return mapToDto(user);
    }

    private UserDetailsDto mapToDto(User user) {
        // Convert User entity to UserDetailsDto
    }
}
```

## Exception Handling

### InvalidJwtTokenException
- Thrown when JWT validation fails
- Returns 401 Unauthorized response
- Logged at WARN level

### Missing Dependencies
- If UserCacheService is not implemented, NoOpUserCacheService is used
- If UserService is not implemented, NoOpUserService is used

## Filter Order

The filter uses `@Order(Ordered.HIGHEST_PRECEDENCE + 1)` to ensure:
- Execution BEFORE SideCarRequestContextFilter
- Early validation of JWT tokens
- Prevention of invalid requests from proceeding

## Security Considerations

1. **Token Validation**: Implement proper JWT signature validation in production
2. **Token Storage**: Use secure storage for JWT secret key (environment variables)
3. **HTTPS Only**: JWT tokens should only be transmitted over HTTPS
4. **Token Expiration**: Validate token expiration time
5. **Cache Security**: Ensure Redis/cache is secured with authentication
6. **Audit Logging**: Log all authentication attempts and failures

## Testing

### Unit Test Example
```java
@ExtendWith(MockitoExtension.class)
public class JwtAuthFilterTest {

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private UserCacheService userCacheService;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Test
    public void testValidToken() throws ServletException, IOException {
        // Test implementation
    }
}
```

## Common Issues

### Filter Not Executing
- Check if `jwt.auth.filter.enabled` is true
- Verify profile is set correctly (e.g., `spring.profiles.active=pcf`)
- Check filter order via logs

### Null UserDetailsDto
- Verify UserService implementation is available
- Check if Redis cache is properly configured
- Review JwtTokenValidator payload extraction logic

### 401 Unauthorized Responses
- Verify JWT token format (3 parts separated by dots)
- Check JWT payload contains required fields
- Ensure Authorization header is correctly formatted

## References
- Spring Security Filter Chain: https://spring.io/blog/2015/06/29/spring-security-filter-chain-proxy
- JWT (JSON Web Token): https://tools.ietf.org/html/rfc7519
- RequestContextHolder: Spring Framework documentation

