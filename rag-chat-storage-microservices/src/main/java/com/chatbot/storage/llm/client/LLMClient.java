package com.chatbot.storage.llm.client;

/**
 *
 * The interface Llm client.
 */
public interface LLMClient {
    /**
     * Gets chat completion.
     *
     * @param message the message
     * @return the chat completion
     */
    String getChatCompletion(String message);

    /**
     * Is available boolean.
     *
     * @return the boolean
     */
    boolean isAvailable();
}