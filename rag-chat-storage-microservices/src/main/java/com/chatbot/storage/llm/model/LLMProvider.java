// LLMProvider.java
package com.chatbot.storage.llm.model;

public enum LLMProvider {
    GROQ("groq"),
    OPENAI("openai"),
    GEMINI("gemini"),
    LOCAL_OLLAMA("local_ollama");

    private final String value;

    LLMProvider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static LLMProvider fromString(String value) {
        for (LLMProvider provider : LLMProvider.values()) {
            if (provider.value.equalsIgnoreCase(value)) {
                return provider;
            }
        }
        throw new IllegalArgumentException("Unknown LLM provider: " + value);
    }
}