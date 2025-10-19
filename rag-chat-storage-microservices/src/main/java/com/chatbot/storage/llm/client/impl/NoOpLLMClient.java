package com.chatbot.storage.llm.client.impl;

import com.chatbot.storage.llm.client.LLMClient;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * The type No op llm client.
 */
@Slf4j
public class NoOpLLMClient implements LLMClient {

    @Override
    public String getChatCompletion(String message) {
        log.warn("LLM integration is not configured. Returning placeholder response.");
        // Return a friendly message instead of throwing an exception
        return "I'm a RAG chat storage service. LLM integration is not currently configured. " +
               "You can still store and retrieve chat messages using the API endpoints.";
    }

    @Override
    public boolean isAvailable() {
        return false; // Explicitly mark as unavailable
    }

    /**
     * Is configured boolean.
     *
     * @return the boolean
     */
    public boolean isConfigured() {
        return false;
    }
}