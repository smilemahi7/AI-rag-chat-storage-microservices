package com.chatbot.storage.llm.config;

import com.chatbot.storage.llm.model.LLMProvider;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 *
 * The type Llm config.
 */
@Data
@ConfigurationProperties(prefix = "app.llm")
public class LLMConfig {

    private final LLMProvider provider;
    private final String model;
    private final Double temperature;
    private final Integer maxTokens;
    private final Integer timeout;
    private final String baseUrl;
    private final String apiUrl;
    private final String apiKey;
    private final Double topP;
    private final Boolean stream;

    /**
     * Is configured boolean.
     *
     * @return the boolean
     */
    public boolean isConfigured() {
        return switch (provider) {
            case GROQ, OPENAI, GEMINI -> apiKey != null && !apiKey.isBlank()
                    && apiUrl != null && !apiUrl.isBlank();
            case LOCAL_OLLAMA -> baseUrl != null && !baseUrl.isBlank();
        };
    }

    /**
     * Gets effective base url.
     *
     * @return the effective base url
     */
    public String getEffectiveBaseUrl() {
        return switch (provider) {
            case GROQ, OPENAI, GEMINI -> apiUrl;
            case LOCAL_OLLAMA -> baseUrl;
            default -> throw new IllegalArgumentException("Unsupported provider: " + provider);
        };
    }

    /**
     * Requires api key boolean.
     *
     * @return the boolean
     */
    public boolean requiresApiKey() {
        return provider == LLMProvider.GROQ ||
                provider == LLMProvider.OPENAI ||
                provider == LLMProvider.GEMINI;
    }
}
