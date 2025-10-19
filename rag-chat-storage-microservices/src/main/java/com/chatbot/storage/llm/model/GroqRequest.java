package com.chatbot.storage.llm.model;

import java.util.List;

/**
 *
 * The type Groq request.
 */
public record GroqRequest(
            String model,
            List<GroqMessage> messages,
            Double temperature,
            Integer max_tokens,
            Double top_p,
            Boolean stream
    ) {}