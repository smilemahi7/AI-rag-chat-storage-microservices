package com.chatbot.storage.controller;

import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.request.UpdateSessionRequest;
import com.chatbot.storage.dto.response.ApiResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * The type Chat session controller test.
 */
@ExtendWith(MockitoExtension.class)
class ChatSessionControllerTest {

    @Mock
    private ChatSessionService chatSessionService;

    @InjectMocks
    private ChatSessionController chatSessionController;

    private UUID sessionId;
    private String userId;
    private CreateSessionRequest createSessionRequest;
    private UpdateSessionRequest updateSessionRequest;
    private SessionResponse sessionResponse;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        userId = "user123";

        createSessionRequest = new CreateSessionRequest();
        createSessionRequest.setSessionName("New Chat Session");
        createSessionRequest.setUserId(userId);

        updateSessionRequest = new UpdateSessionRequest();
        updateSessionRequest.setSessionName("Updated Session Title");

        sessionResponse = SessionResponse.builder()
                .id(sessionId)
                .sessionName("Test Session")
                .userId(userId)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Create session should return created with session response.
     */
    @Test
    void createSession_ShouldReturnCreatedWithSessionResponse() {
        // Given
        when(chatSessionService.createSession(any(CreateSessionRequest.class)))
                .thenReturn(sessionResponse);

        // When
        ResponseEntity<ApiResponse<SessionResponse>> result =
                chatSessionController.createSession(createSessionRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Session created successfully", result.getBody().getMessage());
        assertEquals(sessionResponse, result.getBody().getData());

        verify(chatSessionService).createSession(createSessionRequest);
    }

    /**
     * Gets session should return session response.
     */
    @Test
    void getSession_ShouldReturnSessionResponse() {
        // Given
        when(chatSessionService.getSessionById(sessionId, userId))
                .thenReturn(sessionResponse);

        // When
        ResponseEntity<ApiResponse<SessionResponse>> result =
                chatSessionController.getSession(sessionId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(sessionResponse, result.getBody().getData());

        verify(chatSessionService).getSessionById(sessionId, userId);
    }

    /**
     * Gets user sessions should return paged response.
     */
    @Test
    void getUserSessions_ShouldReturnPagedResponse() {
        // Given
        List<SessionResponse> sessions = List.of(sessionResponse);
        PagedResponse<SessionResponse> pagedResponse = PagedResponse.<SessionResponse>builder()
                .content(sessions)
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .size(20)
                .build();

        Sort expectedSort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable expectedPageable = PageRequest.of(0, 20, expectedSort);
        when(chatSessionService.getUserSessions(userId, expectedPageable))
                .thenReturn(pagedResponse);

        // When
        ResponseEntity<ApiResponse<PagedResponse<SessionResponse>>> result =
                chatSessionController.getUserSessions(userId, 0, 20, "createdAt", "desc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(pagedResponse, result.getBody().getData());
        assertEquals(1, result.getBody().getData().getContent().size());

        verify(chatSessionService).getUserSessions(userId, expectedPageable);
    }

    /**
     * Gets user sessions with ascending sort should create correct pageable.
     */
    @Test
    void getUserSessions_WithAscendingSort_ShouldCreateCorrectPageable() {
        // Given
        PagedResponse<SessionResponse> pagedResponse = PagedResponse.<SessionResponse>builder()
                .content(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(0)
                .size(10)
                .build();

        Sort expectedSort = Sort.by(Sort.Direction.ASC, "title");
        Pageable expectedPageable = PageRequest.of(1, 10, expectedSort);
        when(chatSessionService.getUserSessions(userId, expectedPageable))
                .thenReturn(pagedResponse);

        // When
        ResponseEntity<ApiResponse<PagedResponse<SessionResponse>>> result =
                chatSessionController.getUserSessions(userId, 1, 10, "title", "asc");

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(chatSessionService).getUserSessions(userId, expectedPageable);
    }

    /**
     * Gets favorite sessions should return favorite sessions list.
     */
    @Test
    void getFavoriteSessions_ShouldReturnFavoriteSessionsList() {
        // Given
        SessionResponse favoriteSession = SessionResponse.builder()
                .id(UUID.randomUUID())
                .sessionName("Favorite Session")
                .userId(userId)
                .isFavorite(true)
                .createdAt(LocalDateTime.now())
                .build();

        List<SessionResponse> favoriteSessions = List.of(favoriteSession);
        when(chatSessionService.getFavoriteSessions(userId))
                .thenReturn(favoriteSessions);

        // When
        ResponseEntity<ApiResponse<List<SessionResponse>>> result =
                chatSessionController.getFavoriteSessions(userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(favoriteSessions, result.getBody().getData());
        assertEquals(1, result.getBody().getData().size());
        assertTrue(result.getBody().getData().getFirst().getIsFavorite());

        verify(chatSessionService).getFavoriteSessions(userId);
    }

    /**
     * Update session should return updated session response.
     */
    @Test
    void updateSession_ShouldReturnUpdatedSessionResponse() {
        // Given
        SessionResponse updatedResponse = SessionResponse.builder()
                .id(sessionId)
                .sessionName("Updated Session Title")
                .userId(userId)
                .isFavorite(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(chatSessionService.updateSession(sessionId, userId, updateSessionRequest))
                .thenReturn(updatedResponse);

        // When
        ResponseEntity<ApiResponse<SessionResponse>> result =
                chatSessionController.updateSession(sessionId, userId, updateSessionRequest);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Session updated successfully", result.getBody().getMessage());
        assertEquals(updatedResponse, result.getBody().getData());

        verify(chatSessionService).updateSession(sessionId, userId, updateSessionRequest);
    }

    /**
     * Toggle favorite should return updated session with toggled status.
     */
    @Test
    void toggleFavorite_ShouldReturnUpdatedSessionWithToggledStatus() {
        // Given
        SessionResponse toggledResponse = SessionResponse.builder()
                .id(sessionId)
                .sessionName("Test Session")
                .userId(userId)
                .isFavorite(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(chatSessionService.toggleFavorite(sessionId, userId))
                .thenReturn(toggledResponse);

        // When
        ResponseEntity<ApiResponse<SessionResponse>> result =
                chatSessionController.toggleFavorite(sessionId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Favorite status updated", result.getBody().getMessage());
        assertEquals(toggledResponse, result.getBody().getData());
        assertTrue(result.getBody().getData().getIsFavorite());

        verify(chatSessionService).toggleFavorite(sessionId, userId);
    }

    /**
     * Delete session should return success response.
     */
    @Test
    void deleteSession_ShouldReturnSuccessResponse() {
        // Given
        doNothing().when(chatSessionService).deleteSession(sessionId, userId);

        // When
        ResponseEntity<ApiResponse<Void>> result =
                chatSessionController.deleteSession(sessionId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Session deleted successfully", result.getBody().getMessage());
        assertNull(result.getBody().getData());

        verify(chatSessionService).deleteSession(sessionId, userId);
    }

    /**
     * Create session when service throws exception should propagate exception.
     */
    @Test
    void createSession_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        when(chatSessionService.createSession(any(CreateSessionRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                chatSessionController.createSession(createSessionRequest));

        verify(chatSessionService).createSession(createSessionRequest);
    }

    /**
     * Update session when service throws exception should propagate exception.
     */
    @Test
    void updateSession_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        when(chatSessionService.updateSession(sessionId, userId, updateSessionRequest))
                .thenThrow(new RuntimeException("Update failed"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                chatSessionController.updateSession(sessionId, userId, updateSessionRequest));

        verify(chatSessionService).updateSession(sessionId, userId, updateSessionRequest);
    }
}