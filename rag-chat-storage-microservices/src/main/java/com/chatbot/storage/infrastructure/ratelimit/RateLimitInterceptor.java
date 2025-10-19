package com.chatbot.storage.infrastructure.ratelimit;

import com.chatbot.storage.config.properties.RateLimitingProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.chatbot.storage.constants.AppConstants.X_FORWARDED_FOR;

/**
 *
 * The type Rate limit interceptor.
 */
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitingProperties properties;
    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        // Add this check at the beginning
        if (!properties.isEnabled()) {
            return true;
        }

        String rateLimitKey = request.getHeader(properties.getHeaderName());
        String clientIp = getClientIpAddress(request);
        String keyToUse = (rateLimitKey != null && !rateLimitKey.trim().isEmpty())
                ? rateLimitKey.trim()
                : clientIp;

        if (!rateLimitService.tryConsume(keyToUse)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json"); // Add this line
            response.getWriter().write("{\"error\":\"Rate limit exceeded\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
            return false;
        }

        return true;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR);
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}