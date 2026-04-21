package com.tk.learn.web.security;

import com.tk.learn.model.dto.UserDetailsDto;

/**
 * Service interface for retrieving user details from an external source.
 * Implementations can integrate with user management systems, AD, etc.
 */
public interface UserService {

    /**
     * Retrieves user details by user ID.
     *
     * @param userId the user identifier
     * @return UserDetailsDto containing user information
     */
    UserDetailsDto getUserDetailsById(String userId);

    /**
     * Retrieves user details by email.
     *
     * @param email the user email
     * @return UserDetailsDto containing user information
     */
    UserDetailsDto getUserDetailsByEmail(String email);
}

