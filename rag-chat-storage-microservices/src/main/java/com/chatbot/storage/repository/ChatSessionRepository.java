package com.chatbot.storage.repository;

import com.chatbot.storage.entity.ChatSession;
import com.chatbot.storage.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * The interface Chat session repository.
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /**
     * Find by user id and status page.
     *
     * @param userId   the user id
     * @param status   the status
     * @param pageable the pageable
     * @return the page
     */
    Page<ChatSession> findByUserIdAndStatus(String userId, SessionStatus status, Pageable pageable);

    /**
     * Find by user id and is favorite and status list.
     *
     * @param userId     the user id
     * @param isFavorite the is favorite
     * @param status     the status
     * @return the list
     */
    List<ChatSession> findByUserIdAndIsFavoriteAndStatus(String userId, Boolean isFavorite, SessionStatus status);

    /**
     * Find active sessions by user list.
     *
     * @param userId the user id
     * @param status the status
     * @return the list
     */
    @Query("SELECT s FROM ChatSession s WHERE s.userId = :userId AND s.status = :status")
    List<ChatSession> findActiveSessionsByUser(@Param("userId") String userId, @Param("status") SessionStatus status);

    /**
     * Find by id and user id optional.
     *
     * @param id     the id
     * @param userId the user id
     * @return the optional
     */
    Optional<ChatSession> findByIdAndUserId(UUID id, String userId);

    /**
     * Count by user id and status long.
     *
     * @param userId the user id
     * @param status the status
     * @return the long
     */
    @Query("SELECT COUNT(s) FROM ChatSession s WHERE s.userId = :userId AND s.status = :status")
    long countByUserIdAndStatus(@Param("userId") String userId, @Param("status") SessionStatus status);

    /**
     * Exists by id and user id boolean.
     *
     * @param id     the id
     * @param userId the user id
     * @return the boolean
     */
    boolean existsByIdAndUserId(UUID id, String userId);
}