package com.chatbot.storage.llm.service;

import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.llm.client.LLMClient;
import com.chatbot.storage.llm.client.impl.GroqClientImpl;
import com.chatbot.storage.llm.client.impl.NoOpLLMClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * The type Llm integration service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LLMIntegrationService {
    
    private final LLMClient llmClient;

    /**
     * Process message string.
     *
     * @param userMessage the user message
     * @return the string
     */
    public String processMessage(String userMessage) {
        if (!llmClient.isAvailable()) {
            log.debug("LLM client not available, returning informative response");
            return "This chat storage service is running without LLM integration. " +
                   "Your message has been stored successfully. " +
                   "Configure an LLM provider to enable AI responses.";
        }
        
        try {
            return llmClient.getChatCompletion(userMessage);
        } catch (Exception e) {
            log.error("Failed to get LLM response, but message was stored successfully", e);
            return "Sorry, I encountered an error generating a response. " +
                   "Your message has been stored successfully. " +
                   "Please try again later or check the LLM configuration.";
        }
    }

    /**
     * Process message with conversation context.
     *
     * @param currentMessage the current user message
     * @param conversationHistory the conversation history
     * @return the AI response
     */
    public String processMessageWithContext(String currentMessage, List<MessageResponse> conversationHistory) {
        if (!llmClient.isAvailable()) {
            log.debug("LLM client not available, returning informative response.");
            return "This chat storage service is running without LLM integration. " +
                    "Your message has been stored successfully. " +
                    "Configure an LLM provider to enable AI responses.";
        }

        try {
            // Check if the client supports contextual conversations
            if (llmClient instanceof GroqClientImpl groqClient) {
                return groqClient.getChatCompletionWithContext(conversationHistory, currentMessage);
            } else {
                // Fallback: build contextual message as string for other clients
                String contextualMessage = buildContextualMessage(currentMessage, conversationHistory);
                return llmClient.getChatCompletion(contextualMessage);
            }
        } catch (Exception e) {
            log.error("Failed to get LLM response, but message was stored successfully", e);
            return "Sorry, I encountered an error generating a response. " +
                    "Your message has been stored successfully. " +
                    "Please try again later or check the LLM configuration.";
        }
    }

    /**
     * Build contextual message with conversation history (fallback method).
     */
    private String buildContextualMessage(String currentMessage, List<MessageResponse> conversationHistory) {
        if (conversationHistory.isEmpty()) {
            return currentMessage;
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Previous conversation:\n\n");

        for (MessageResponse msg : conversationHistory) {
            String role = msg.getSenderType() == MessageSender.USER ? "Human" : "Assistant";
            contextBuilder.append(role).append(": ").append(msg.getContent()).append("\n\n");
        }

        contextBuilder.append("Human: ").append(currentMessage).append("\n\n");
        contextBuilder.append("Assistant: ");

        return contextBuilder.toString();
    }

    /**
     * Is llm available boolean.
     *
     * @return the boolean
     */
    public boolean isLlmAvailable() {
        return llmClient.isAvailable();
    }

    /**
     * Gets llm status.
     *
     * @return the llm status
     */
    public String getLlmStatus() {
        if (llmClient instanceof NoOpLLMClient) {
            return "DISABLED - No LLM configured";
        }
        return llmClient.isAvailable() ? "ACTIVE" : "UNAVAILABLE";
    }
}