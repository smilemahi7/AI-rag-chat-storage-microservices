package com.chatbot.storage.llm.config;

import com.chatbot.storage.llm.client.LLMClient;
import com.chatbot.storage.llm.client.impl.GroqClientImpl;
import com.chatbot.storage.llm.client.impl.NoOpLLMClient;
import com.chatbot.storage.llm.client.impl.OllamaClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 *
 * The type Llm client config.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class LLMClientConfig {

    private final LLMConfig llmConfig;

    /**
     * Llm client llm client.
     *
     * @return the llm client
     */
    @Bean
    public LLMClient llmClient() {
        try {
            if (!llmConfig.isConfigured()) {
                log.warn("LLM is not configured. Chat storage service will operate without LLM integration.");
                return new NoOpLLMClient();
            }

            WebClient webClient = createWebClient();

            log.info("Configuring {} client for model: {}",
                    llmConfig.getProvider(), llmConfig.getModel());

            return switch (llmConfig.getProvider()) {
                case GROQ -> new GroqClientImpl(webClient, llmConfig);
                case LOCAL_OLLAMA -> new OllamaClient(webClient, llmConfig);
                default -> {
                    log.warn("Unsupported LLM provider: {}. Falling back to no-op client.",
                            llmConfig.getProvider());
                    yield new NoOpLLMClient();
                }
            };
        } catch (Exception e) {
            log.error("Failed to configure LLM client. Falling back to no-op mode. Error: {}",
                    e.getMessage());
            return new NoOpLLMClient();
        }
    }

    private WebClient createWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(llmConfig.getEffectiveBaseUrl());

        if (llmConfig.requiresApiKey()) {
            if (llmConfig.getApiKey() == null || llmConfig.getApiKey().isBlank()) {
                throw new IllegalStateException("API key is required for provider: " + llmConfig.getProvider());
            }
            builder.defaultHeader("Authorization", "Bearer " + llmConfig.getApiKey());
        }

        builder.defaultHeader("Content-Type", "application/json");

        return builder.build();
    }
}