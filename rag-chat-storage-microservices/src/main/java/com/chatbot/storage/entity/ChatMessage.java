package com.chatbot.storage.entity;

import com.chatbot.storage.enums.MessageSender;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

/**
 *
 * The type Chat message.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_messages", indexes = {
        @Index(name = "idx_session_id", columnList = "session_id"),
        @Index(name = "idx_sender_type", columnList = "senderType"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 20)
    private MessageSender senderType;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    // PostgreSQL JSONB support - much better performance than TEXT
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "context_data", columnDefinition = "jsonb")
    private Map<String, Object> contextData;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * Helper methods for easy context data manipulation.
     *
     * @param key   the key
     * @param value the value
     */
    public void addContextData(String key, Object value) {
        if (contextData == null) {
            contextData = new java.util.HashMap<>();
        }
        contextData.put(key, value);
    }

    /**
     * Add metadata.
     *
     * @param key   the key
     * @param value the value
     */
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new java.util.HashMap<>();
        }
        metadata.put(key, value);
    }
}