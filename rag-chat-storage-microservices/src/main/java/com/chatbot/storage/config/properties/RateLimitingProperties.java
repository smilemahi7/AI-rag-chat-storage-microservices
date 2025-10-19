package com.chatbot.storage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * The type Rate limiting properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.rate-limiting")
public class RateLimitingProperties {

    private int requests = 100;
    private int hours = 1;
    private String headerName = "X-RATE-LIMIT-KEY";
    private boolean enabled = true;
}