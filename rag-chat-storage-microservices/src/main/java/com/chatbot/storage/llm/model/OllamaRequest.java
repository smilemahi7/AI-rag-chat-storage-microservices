// OllamaRequest.java
package com.chatbot.storage.llm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OllamaRequest {
    private String model;
    private List<Message> messages;
    private Boolean stream = false;
    private Double temperature;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    @Data
    public static class Message {
        private String role;
        private String content;
        
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }
}