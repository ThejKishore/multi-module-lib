package com.tk.learn.web.filter;

import com.tk.learn.web.context.RequestContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Filter that extracts a JSON map from a configured HTTP header and stores it
 * in a ThreadLocal-backed context for the duration of the request.
 */
@Component
public class SideCarRequestContextFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SideCarRequestContextFilter.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JsonMapper objectMapper;
    private final String headerName;

    public SideCarRequestContextFilter(JsonMapper objectMapper,
                                       @Value("${request.context.header:X-Request-Context}") String headerName) {
        this.objectMapper = objectMapper;
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(headerName);
        Base64.Decoder decoder = Base64.getDecoder();
        header = new String(decoder.decode(header));
        try {
            if (StringUtils.hasText(header)) {
                try {
                    Map<String, Object> ctx = objectMapper.readValue(header, MAP_TYPE);
                    RequestContextHolder.setContext(ctx);
                } catch (JacksonException e) {
                    log.warn("Invalid JSON in header {}: {}", headerName, e.getOriginalMessage());
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    String body = "{\"error\":\"Invalid request context header JSON\"}";
                    response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            // Always clear the context at the end of the request
            RequestContextHolder.clear();
        }
    }
}
