package com.tk.learn.web.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-local request context similar in spirit to Spring SecurityContext.
 * <p>
 * A {@link java.util.Map} of arbitrary attributes can be associated with the
 * current request thread by {@code RequestContextFilter}. Application code can
 * access these attributes anywhere within the same request thread via the
 * static accessors. Always cleared after the request completes.
 */
public final class RequestContextHolder {

    private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

    private RequestContextHolder() {
        // utility
    }

    /**
     * Returns an unmodifiable view of the current context map. Never null.
     */
    public static Map<String, Object> getContext() {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Get a specific attribute from the context.
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(String key) {
        Map<String, Object> map = CONTEXT.get();
        return map == null ? null : (T) map.get(key);
    }

    /**
     * Set a specific attribute.
     */
    public static void put(String key, Object value) {
        Map<String, Object> map = CONTEXT.get();
        if (map == null) {
            map = new HashMap<>();
            CONTEXT.set(map);
        }
        map.put(key, value);
    }

    /**
     * Replace the entire context map.
     */
    public static void setContext(Map<String, Object> newContext) {
        if (newContext == null) {
            CONTEXT.remove();
        } else {
            CONTEXT.set(new HashMap<>(newContext));
        }
    }

    /**
     * Clear the ThreadLocal context. Always call at end of request.
     */
    public static void clear() {
        CONTEXT.remove();
    }
}
