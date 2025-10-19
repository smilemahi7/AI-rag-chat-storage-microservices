package com.chatbot.storage.service.impl;


import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.request.UpdateSessionRequest;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.entity.ChatSession;
import com.chatbot.storage.enums.SessionStatus;
import com.chatbot.storage.exception.ResourceNotFoundException;
import com.chatbot.storage.mapper.SessionMapper;
import com.chatbot.storage.repository.ChatSessionRepository;
import com.chatbot.storage.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.chatbot.storage.constants.AppConstants.CACHE_FAVORITE_SESSIONS;
import static com.chatbot.storage.constants.AppConstants.CACHE_SESSIONS;

/**
 *
 * The type Chat session service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatSessionServiceImpl implements ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final SessionMapper sessionMapper;

    @Override
    public SessionResponse createSession(CreateSessionRequest request) {
        log.info("Creating new chat session for user: {}", request.getUserId());

        ChatSession session = ChatSession.builder()
                .userId(request.getUserId())
                .sessionName(request.getSessionName())
                .description(request.getDescription())
                .build();

        ChatSession savedSession = sessionRepository.save(session);
        log.info("Created chat session with ID: {}", savedSession.getId());

        return sessionMapper.toResponse(savedSession);
    }

    @Override
    @Cacheable(value = CACHE_SESSIONS, key = "#sessionId + '_' + #userId")
    public SessionResponse getSessionById(UUID sessionId, String userId) {
        log.info("Fetching session {} for user {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        return sessionMapper.toResponse(session);
    }

    @Override
    public PagedResponse<SessionResponse> getUserSessions(String userId, Pageable pageable) {
        log.info("Fetching sessions for user: {} with pagination", userId);

        Page<ChatSession> sessions = sessionRepository.findByUserIdAndStatus(
                userId, SessionStatus.ACTIVE, pageable);

        return sessionMapper.toPagedResponse(sessions);
    }

    @Override
    @Cacheable(value = CACHE_FAVORITE_SESSIONS, key = "#userId")
    public List<SessionResponse> getFavoriteSessions(String userId) {
        log.info("Fetching favorite sessions for user: {}", userId);

        List<ChatSession> favoriteSessions = sessionRepository
                .findByUserIdAndIsFavoriteAndStatus(userId, true, SessionStatus.ACTIVE);

        return sessionMapper.toResponseList(favoriteSessions);
    }

    @Override
    @CacheEvict(value = {CACHE_SESSIONS, CACHE_FAVORITE_SESSIONS}, key = "#sessionId + '_' + #userId")
    public SessionResponse updateSession(UUID sessionId, String userId, UpdateSessionRequest request) {
        log.info("Updating session {} for user {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setSessionName(request.getSessionName());
        session.setDescription(request.getDescription());

        if (request.getIsFavorite() != null) {
            session.setIsFavorite(request.getIsFavorite());
        }

        ChatSession updatedSession = sessionRepository.save(session);
        return sessionMapper.toResponse(updatedSession);
    }

    @Override
    @CacheEvict(value = {CACHE_SESSIONS, CACHE_FAVORITE_SESSIONS}, key = "#sessionId + '_' + #userId")
    public void deleteSession(UUID sessionId, String userId) {
        log.info("Deleting session {} for user {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setStatus(SessionStatus.DELETED);
        sessionRepository.save(session);

        log.info("Session {} marked as deleted", sessionId);
    }

    @Override
    @CacheEvict(value = {CACHE_SESSIONS, CACHE_FAVORITE_SESSIONS}, key = "#sessionId + '_' + #userId")
    public SessionResponse toggleFavorite(UUID sessionId, String userId) {
        log.info("Toggling favorite status for session {} and user {}", sessionId, userId);

        ChatSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setIsFavorite(!session.getIsFavorite());
        ChatSession updatedSession = sessionRepository.save(session);

        return sessionMapper.toResponse(updatedSession);
    }
}