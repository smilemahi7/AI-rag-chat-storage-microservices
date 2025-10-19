package com.chatbot.storage.infrastructure.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 *
 * The type Security properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {
    private String apiKey;
    private String headerName = "X-API-KEY";
}