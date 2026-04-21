package com.tk.learn.web.security.impl;

import com.tk.learn.model.dto.UserDetailsDto;
import com.tk.learn.web.security.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * No-op implementation of UserService.
 * Used when no actual user service is configured.
 * This is a fallback component to prevent NullPointerException.
 */
@Component
@ConditionalOnMissingBean(UserService.class)
public class NoOpUserService implements UserService {

    private static final Logger log = LoggerFactory.getLogger(NoOpUserService.class);

    @Override
    public UserDetailsDto getUserDetailsById(String userId) {
        log.debug("No-op user service: getUserDetailsById called for userId: {}", userId);
        return null;
    }

    @Override
    public UserDetailsDto getUserDetailsByEmail(String email) {
        log.debug("No-op user service: getUserDetailsByEmail called for email: {}", email);
        return null;
    }
}

