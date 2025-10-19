package com.chatbot.storage.llm.client.impl;

import com.chatbot.storage.llm.client.LLMClient;
import com.chatbot.storage.llm.config.LLMConfig;
import com.chatbot.storage.llm.model.OllamaRequest;
import com.chatbot.storage.llm.model.OllamaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

/**
 *
 * The type Ollama client.
 */
@Slf4j
@RequiredArgsConstructor
public class OllamaClient implements LLMClient {

    private final WebClient webClient;
    private final LLMConfig config;

    @Override
    public String getChatCompletion(String message) {
        try {
            log.debug("Sending request to Ollama with model: {}", config.getModel());

            OllamaRequest request = new OllamaRequest();
            request.setModel(config.getModel());
            request.setMessages(List.of(
                    new OllamaRequest.Message("user", message)
            ));
            request.setTemperature(config.getTemperature());
            request.setMaxTokens(config.getMaxTokens());

            OllamaResponse response = webClient.post()
                    .uri("/api/chat")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofMillis(config.getTimeout()))
                    .block();

            return extractResponseContent(response);

        } catch (WebClientResponseException e) {
            log.error("Ollama API HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get response from Ollama: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Ollama API", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        return config.isConfigured() && config.getProvider().toString().equals("LOCAL_OLLAMA");
    }

    private Mono<Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException(
                        "Ollama API error: " + response.statusCode() + " - " + body)));
    }

    private String extractResponseContent(OllamaResponse response) {
        if (response == null || response.getMessage() == null) {
            throw new RuntimeException("Invalid response format from Ollama API");
        }
        return response.getMessage().getContent();
    }
}