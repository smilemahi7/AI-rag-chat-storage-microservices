package com.chatbot.storage.service;

import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.entity.ChatSession;
import com.chatbot.storage.enums.SessionStatus;
import com.chatbot.storage.exception.ResourceNotFoundException;
import com.chatbot.storage.mapper.SessionMapper;
import com.chatbot.storage.repository.ChatSessionRepository;
import com.chatbot.storage.service.impl.ChatSessionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * The type Chat session service impl test.
 */
@ExtendWith(MockitoExtension.class)
class ChatSessionServiceImplTest {

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private ChatSessionServiceImpl chatSessionService;

    private UUID sessionId;
    private String userId;
    private ChatSession chatSession;
    private SessionResponse sessionResponse;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        userId = "user123";

        chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Test Session")
                .description("Test Description")
                .isFavorite(false)
                .status(SessionStatus.ACTIVE)
                .build();

        sessionResponse = SessionResponse.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Test Session")
                .description("Test Description")
                .isFavorite(false)
                .status(SessionStatus.ACTIVE)
                .messageCount(0L)
                .build();
    }

    /**
     * Create session success.
     */
    @Test
    @DisplayName("Should create session successfully")
    void createSession_Success() {
        // Given
        CreateSessionRequest request = new CreateSessionRequest();
        request.setUserId(userId);
        request.setSessionName("Test Session");
        request.setDescription("Test Description");

        when(sessionRepository.save(any(ChatSession.class))).thenReturn(chatSession);
        when(sessionMapper.toResponse(chatSession)).thenReturn(sessionResponse);

        // When
        SessionResponse result = chatSessionService.createSession(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getSessionName()).isEqualTo("Test Session");

        verify(sessionRepository).save(any(ChatSession.class));
        verify(sessionMapper).toResponse(chatSession);
    }

    /**
     * Gets session by id success.
     */
    @Test
    @DisplayName("Should get session by ID successfully")
    void getSessionById_Success() {
        // Given
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(chatSession));
        when(sessionMapper.toResponse(chatSession)).thenReturn(sessionResponse);

        // When
        SessionResponse result = chatSessionService.getSessionById(sessionId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sessionId);
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verify(sessionMapper).toResponse(chatSession);
    }

    /**
     * Gets session by id not found.
     */
    @Test
    @DisplayName("Should throw ResourceNotFoundException when session not found")
    void getSessionById_NotFound() {
        // Given
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> chatSessionService.getSessionById(sessionId, userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Session not found");

        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verifyNoInteractions(sessionMapper);
    }

    /**
     * Toggle favorite success.
     */
    @Test
    @DisplayName("Should toggle favorite status successfully")
    void toggleFavorite_Success() {
        // Given
        chatSession.setIsFavorite(false);
        ChatSession updatedSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Test Session")
                .isFavorite(true)
                .status(SessionStatus.ACTIVE)
                .build();

        SessionResponse updatedResponse = SessionResponse.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Test Session")
                .isFavorite(true)
                .status(SessionStatus.ACTIVE)
                .build();

        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(chatSession));
        when(sessionRepository.save(chatSession)).thenReturn(updatedSession);
        when(sessionMapper.toResponse(updatedSession)).thenReturn(updatedResponse);

        // When
        SessionResponse result = chatSessionService.toggleFavorite(sessionId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsFavorite()).isTrue();
        assertThat(chatSession.getIsFavorite()).isTrue();

        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verify(sessionRepository).save(chatSession);
        verify(sessionMapper).toResponse(updatedSession);
    }
}