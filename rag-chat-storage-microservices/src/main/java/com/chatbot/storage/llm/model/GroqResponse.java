package com.chatbot.storage.llm.model;

import java.util.List;

/**
 *
 * The type Groq response.
 */
public record GroqResponse(
            String id,
            String object,
            long created,
            String model,
            List<Choice> choices,
            Usage usage
    ) {
    /**
     * The type Choice.
     */
    public record Choice(
                int index,
                GroqMessage message,
                String finish_reason,
                Object logprobs
        ) {}

    /**
     * The type Usage.
     */
    public record Usage(
                int prompt_tokens,
                int completion_tokens,
                int total_tokens
        ) {}
    }