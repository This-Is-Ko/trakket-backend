package org.trakket.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String error = "INVALID_TOKEN";
        String message = "Authentication failed.";

        // Check cause to differentiate expired vs invalid JWT
        Throwable cause = authException.getCause();
        if (cause instanceof org.springframework.security.oauth2.jwt.JwtException) {
            String msg = cause.getMessage().toLowerCase();
            if (msg.contains("expired")) {
                error = "TOKEN_EXPIRED";
                message = "Your session has expired. Please log in again.";
            } else {
                error = "INVALID_TOKEN";
                message = "The provided token is invalid.";
            }
        }

        response.getWriter().write(
                objectMapper.writeValueAsString(Map.of("error", error, "message", message))
        );
    }
}

