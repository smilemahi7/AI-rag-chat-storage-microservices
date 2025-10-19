package com.chatbot.storage.dto.response;

import com.chatbot.storage.enums.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 *
 * The type Session response.
 */
@Data
@Builder
public class SessionResponse {
    private UUID id;
    private String userId;
    private String sessionName;
    private String description;
    private Boolean isFavorite;
    private SessionStatus status;
    private Long messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}