package com.tk.learn.model.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO representing user details extracted from JWT and/or Redis cache.
 * Implements Serializable for Redis caching support.
 */
public class UserDetailsDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;
    private String email;
    private String username;
    private String sessionId;
    private List<String> roles;
    private List<String> permissions;
    private long issuedAt;
    private long expiresAt;

    public UserDetailsDto() {
    }

    public UserDetailsDto(String userId, String email, String username, String sessionId,
                         List<String> roles, List<String> permissions,
                         long issuedAt, long expiresAt) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.sessionId = sessionId;
        this.roles = roles;
        this.permissions = permissions;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public long getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(long issuedAt) {
        this.issuedAt = issuedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}

