package com.chatbot.storage.service;

import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.request.UpdateSessionRequest;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 *
 * The interface Chat session service.
 */
public interface ChatSessionService {
    /**
     * Create session response.
     *
     * @param request the request
     * @return the session response
     */
    SessionResponse createSession(CreateSessionRequest request);

    /**
     * Gets session by id.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @return the session by id
     */
    SessionResponse getSessionById(UUID sessionId, String userId);

    /**
     * Gets user sessions.
     *
     * @param userId   the user id
     * @param pageable the pageable
     * @return the user sessions
     */
    PagedResponse<SessionResponse> getUserSessions(String userId, Pageable pageable);

    /**
     * Gets favorite sessions.
     *
     * @param userId the user id
     * @return the favorite sessions
     */
    List<SessionResponse> getFavoriteSessions(String userId);

    /**
     * Update session response.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @param request   the request
     * @return the session response
     */
    SessionResponse updateSession(UUID sessionId, String userId, UpdateSessionRequest request);

    /**
     * Delete session.
     *
     * @param sessionId the session id
     * @param userId    the user id
     */
    void deleteSession(UUID sessionId, String userId);

    /**
     * Toggle favorite session response.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @return the session response
     */
    SessionResponse toggleFavorite(UUID sessionId, String userId);
}