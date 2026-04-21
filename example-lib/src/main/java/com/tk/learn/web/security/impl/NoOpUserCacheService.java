package com.tk.learn.web.security.impl;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.web.security.UserCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of UserCacheService.
 * Used when no actual cache implementation (e.g., Redis) is configured.
 * This is a fallback component to prevent NullPointerException.
 */
@Component
@ConditionalOnMissingBean(UserCacheService.class)
public class NoOpUserCacheService implements UserCacheService {

    private static final Logger log = LoggerFactory.getLogger(NoOpUserCacheService.class);

    @Override
    public UserDetailsDto getUserFromCache(String sessionId) {
        log.debug("No-op cache: getUserFromCache called for sessionId: {}", sessionId);
        return null;
    }

    @Override
    public void cacheUser(String sessionId, UserDetailsDto userDetails, long ttlSeconds) {
        log.debug("No-op cache: cacheUser called for sessionId: {} with TTL: {} seconds",
                sessionId, ttlSeconds);
    }

    @Override
    public void removeUserFromCache(String sessionId) {
        log.debug("No-op cache: removeUserFromCache called for sessionId: {}", sessionId);
    }
}

