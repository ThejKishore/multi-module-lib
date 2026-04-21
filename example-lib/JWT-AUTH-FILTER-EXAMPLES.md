# JWT Auth Filter - Example Implementations

## 1. Redis Cache Service Implementation

```java
package com.tk.learn.web.security.impl;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.web.security.UserCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnBean(name = "redisTemplate")
public class RedisUserCacheService implements UserCacheService {

    private static final String CACHE_KEY_PREFIX = "user:jwt:";

    @Autowired
    private RedisTemplate<String, UserDetailsDto> redisTemplate;

    @Override
    public UserDetailsDto getUserFromCache(String sessionId) {
        String key = CACHE_KEY_PREFIX + sessionId;
        return (UserDetailsDto) redisTemplate.opsForValue().get(key);
    }

    @Override
    public void cacheUser(String sessionId, UserDetailsDto userDetails, long ttlSeconds) {
        String key = CACHE_KEY_PREFIX + sessionId;
        if (ttlSeconds > 0) {
            redisTemplate.opsForValue().set(key, userDetails, ttlSeconds, TimeUnit.SECONDS);
        } else {
            redisTemplate.opsForValue().set(key, userDetails);
        }
    }

    @Override
    public void removeUserFromCache(String sessionId) {
        String key = CACHE_KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
```

## 2. User Service Implementation with Database

```java
package com.tk.learn.web.security.impl;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.web.security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(name = "userRepository")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetailsDto getUserDetailsById(String userId) {
        return userRepository.findById(userId)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    public UserDetailsDto getUserDetailsByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToDto)
                .orElse(null);
    }

    private UserDetailsDto mapToDto(User user) {
        return new UserDetailsDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getSessionId(),
                user.getRoles(),
                user.getPermissions(),
                user.getCreatedAt().getTime(),
                user.getExpiresAt().getTime()
        );
    }
}

interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}
```

## 3. JWT Token with io.jsonwebtoken (Production)

```java
package com.tk.learn.web.security;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.model.exceptions.InvalidJwtTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;

/**
 * Production-grade JWT validator using io.jsonwebtoken library.
 * Add dependency: implementation("io.jsonwebtoken:jjwt-api:0.11.5")
 */
@Component
public class JwtTokenValidatorProd {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenValidatorProd.class);

    @Value("${jwt.secret-key}")
    private String secretKey;

    public UserDetailsDto validateAndExtractClaims(String token) {
        if (!StringUtils.hasText(token)) {
            throw new InvalidJwtTokenException("JWT token is missing");
        }

        try {
            String cleanToken = removeTokenPrefix(token);
            SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(cleanToken)
                    .getBody();

            return new UserDetailsDto(
                    claims.get("userId", String.class),
                    claims.get("email", String.class),
                    claims.get("username", String.class),
                    claims.get("sessionId", String.class),
                    claims.get("roles", java.util.List.class),
                    claims.get("permissions", java.util.List.class),
                    claims.getIssuedAt().getTime(),
                    claims.getExpiration().getTime()
            );

        } catch (SignatureException e) {
            throw new InvalidJwtTokenException("Invalid JWT signature", e);
        } catch (MalformedJwtException e) {
            throw new InvalidJwtTokenException("Invalid JWT format", e);
        } catch (ExpiredJwtException e) {
            throw new InvalidJwtTokenException("JWT token expired", e);
        } catch (JwtException e) {
            throw new InvalidJwtTokenException("JWT validation failed", e);
        }
    }

    private String removeTokenPrefix(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }
}
```

## 4. Azure Key Vault Secret Integration

```java
package com.tk.learn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("azure")
@ConfigurationProperties(prefix = "jwt")
public class JwtAzureConfig {

    private String secretKey; // Retrieved from Azure Key Vault

    // Spring loads this from environment variable: SPRING_JWT_SECRETKEY
    // Or from Azure Key Vault via @RefreshScope

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
```

## 5. Usage in Controller

```java
package com.tk.learn.controller;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.web.context.RequestContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserDetailsDto> getCurrentUser() {
        UserDetailsDto userDetails = RequestContextHolder.get("userDetails");
        
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(userDetails);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        UserDetailsDto userDetails = RequestContextHolder.get("userDetails");
        String sessionId = RequestContextHolder.get("jwtSessionId");

        return ResponseEntity.ok(Map.of(
                "userId", userDetails.getUserId(),
                "email", userDetails.getEmail(),
                "roles", userDetails.getRoles(),
                "sessionId", sessionId
        ));
    }
}
```

## 6. Application Properties - PCF Profile

```yaml
# application-pcf.yml
jwt:
  auth:
    filter:
      enabled: true
  secret-key: ${JWT_SECRET_KEY:default-pcf-secret}
  header-name: Authorization
  token-prefix: "Bearer "
  cache:
    ttl-seconds: 3600

redis:
  host: ${REDIS_HOST:localhost}
  port: ${REDIS_PORT:6379}

spring:
  data:
    redis:
      host: ${redis.host}
      port: ${redis.port}
```

## 7. Application Properties - Azure Profile

```yaml
# application-azure.yml
jwt:
  auth:
    filter:
      enabled: false
  secret-key: ${JWT_SECRET_KEY}

azure:
  key-vault:
    endpoint: https://${AZURE_KEYVAULT_NAME}.vault.azure.net/

spring:
  cloud:
    azure:
      keyvault:
        secret:
          enabled: true
```

## 8. Gradle Dependencies

Add to build.gradle.kts:

```kotlin
dependencies {
    // JWT - Choose one based on your needs
    
    // Option 1: Simple validation (included in example-lib)
    // No additional dependency
    
    // Option 2: Production-grade JWT library
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    
    // Redis support
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    
    // Azure Key Vault support
    implementation("com.azure.spring:spring-cloud-azure-starter-keyvault")
}
```

## 9. Testing with Bruno/Postman

```json
{
  "info": {
    "name": "JWT Auth Filter Tests",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Get Current User - With Valid JWT",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            "type": "text"
          }
        ],
        "url": "{{base_url}}/api/users/me"
      }
    },
    {
      "name": "Get Current User - Without JWT",
      "request": {
        "method": "GET",
        "url": "{{base_url}}/api/users/me"
      }
    },
    {
      "name": "Get Current User - With Invalid JWT",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer invalid.token.here",
            "type": "text"
          }
        ],
        "url": "{{base_url}}/api/users/me"
      }
    }
  ]
}
```

