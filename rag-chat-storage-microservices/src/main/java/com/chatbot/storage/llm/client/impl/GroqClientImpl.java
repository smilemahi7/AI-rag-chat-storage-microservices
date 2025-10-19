package com.chatbot.storage.llm.client.impl;

import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.llm.client.LLMClient;
import com.chatbot.storage.llm.config.LLMConfig;
import com.chatbot.storage.llm.model.GroqMessage;
import com.chatbot.storage.llm.model.GroqRequest;
import com.chatbot.storage.llm.model.GroqResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * The type Groq client.
 */
@Slf4j
@RequiredArgsConstructor
public class GroqClientImpl implements LLMClient {

    private final WebClient webClient;
    private final LLMConfig config;

    @Override
    public String getChatCompletion(String message) {
        try {
            log.debug("Sending request to Groq API with model: {}", config.getModel());

            GroqRequest request = new GroqRequest(
                    config.getModel(),
                    List.of(new GroqMessage("user", message)),
                    config.getTemperature(),
                    config.getMaxTokens(),
                    config.getTopP(),
                    config.getStream()
            );

            GroqResponse response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(GroqResponse.class)
                    .block();

            return extractResponseContent(response);

        } catch (WebClientResponseException e) {
            log.error("Groq API HTTP error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to get response from Groq API: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error calling Groq API", e);
            throw new RuntimeException("Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Get chat completion with conversation context.
     *
     * @param conversationHistory the conversation history
     * @param currentMessage the current user message
     * @return the AI response
     */
    public String getChatCompletionWithContext(List<MessageResponse> conversationHistory, String currentMessage) {
        try {
            log.debug("Sending contextual request to Groq API with model: {}", config.getModel());

            List<GroqMessage> messages = buildMessagesFromHistory(conversationHistory, currentMessage);

            GroqRequest request = new GroqRequest(
                    config.getModel(),
                    messages,
                    config.getTemperature(),
                    config.getMaxTokens(),
                    config.getTopP(),
                    config.getStream()
            );

            GroqResponse response = webClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, this::handleError)
                    .bodyToMono(GroqResponse.class)
                    .block();

            return extractResponseContent(response);

        } catch (Exception e) {
            log.error("Error calling Groq API with context", e);
            throw new RuntimeException("Failed to get contextual response: " + e.getMessage());
        }
    }

    private List<GroqMessage> buildMessagesFromHistory(List<MessageResponse> history, String currentMessage) {
        List<GroqMessage> messages = new ArrayList<>();

        // Add system message
        messages.add(new GroqMessage("system", "You are a helpful AI assistant. Respond based on the conversation context."));

        // Add conversation history
        for (MessageResponse msg : history) {
            String role = msg.getSenderType() == MessageSender.USER ? "user" : "assistant";
            messages.add(new GroqMessage(role, msg.getContent()));
        }

        // Add current message
        messages.add(new GroqMessage("user", currentMessage));

        return messages;
    }

    @Override
    public boolean isAvailable() {
        return config.isConfigured() && config.getProvider().toString().equals("GROQ");
    }

    private Mono<Throwable> handleError(org.springframework.web.reactive.function.client.ClientResponse response) {
        return response.bodyToMono(String.class)
                .flatMap(body -> Mono.error(new RuntimeException(
                        "Groq API error: " + response.statusCode() + " - " + body)));
    }

    private String extractResponseContent(GroqResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Invalid response format from Groq API");
        }

        GroqResponse.Choice firstChoice = response.choices().get(0);
        if (firstChoice.message() == null || firstChoice.message().content() == null) {
            throw new RuntimeException("Empty response content from Groq API");
        }

        return firstChoice.message().content();
    }




}