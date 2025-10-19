package com.chatbot.storage.dto.response;

import com.chatbot.storage.enums.MessageSender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 *
 * The type Message response.
 */
@Data
@Builder
public class MessageResponse {
    private UUID id;
    private UUID sessionId;
    private MessageSender senderType;
    private String content;
    private Map<String, Object> contextData;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}