// OllamaResponse.java
package com.chatbot.storage.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OllamaResponse {
    private String model;
    private Message message;
    private Boolean done;
    private Usage usage;
    
    @Data
    public static class Message {
        private String role;
        private String content;
    }
    
    @Data
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}