package com.tk.learn.web.security;

import com.tk.learn.model.dto.UserDetailsDto;

/**
 * Service interface for user details caching (e.g., Redis, Memcached).
 * Implementations can use Azure Redis Cache, local cache, or other backends.
 */
public interface UserCacheService {

    /**
     * Retrieves user details from cache by session ID.
     *
     * @param sessionId the JWT session ID
     * @return UserDetailsDto if found in cache, null otherwise
     */
    UserDetailsDto getUserFromCache(String sessionId);

    /**
     * Stores user details in cache with the session ID as key.
     *
     * @param sessionId the JWT session ID
     * @param userDetails the user details to cache
     * @param ttlSeconds time-to-live in seconds (0 or negative for no expiry)
     */
    void cacheUser(String sessionId, UserDetailsDto userDetails, long ttlSeconds);

    /**
     * Removes user details from cache.
     *
     * @param sessionId the JWT session ID
     */
    void removeUserFromCache(String sessionId);
}

