package com.chatbot.storage.entity;


import com.chatbot.storage.constants.AppConstants;
import com.chatbot.storage.enums.SessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * The type Chat session.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_sessions", indexes = {
        @Index(name = "idx_user_id", columnList = "userId"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_favorite", columnList = "isFavorite")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Data
@SuperBuilder
@ToString(exclude = "messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSession extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 100)
    private String userId;

    @Column(name = "session_name", nullable = false, length = 255)
    private String sessionName;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column(name = "description", length = 500)
    private String description;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * Add message.
     *
     * @param message the message
     */
    public void addMessage(ChatMessage message) {
        messages.add(message);
        message.setSession(this);
    }

    /**
     * Remove message.
     *
     * @param message the message
     */
    public void removeMessage(ChatMessage message) {
        messages.remove(message);
        message.setSession(null);
    }
}