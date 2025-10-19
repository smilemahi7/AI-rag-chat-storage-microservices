package com.chatbot.storage.repository;

import com.chatbot.storage.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 *
 * The interface Chat message repository.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Find by session id order by created at asc page.
     *
     * @param sessionId the session id
     * @param pageable  the pageable
     * @return the page
     */
    Page<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId, Pageable pageable);

    /**
     * Find by session id order by created at asc list.
     *
     * @param sessionId the session id
     * @return the list
     */
    List<ChatMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    /**
     * Find recent messages list.
     *
     * @param sessionId the session id
     * @param since     the since
     * @return the list
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.session.id = :sessionId AND m.createdAt >= :since")
    List<ChatMessage> findRecentMessages(@Param("sessionId") UUID sessionId, @Param("since") LocalDateTime since);

    /**
     * Count by session id long.
     *
     * @param sessionId the session id
     * @return the long
     */
    long countBySessionId(UUID sessionId);

    /**
     * Delete by session id.
     *
     * @param sessionId the session id
     */
    void deleteBySessionId(UUID sessionId);
}