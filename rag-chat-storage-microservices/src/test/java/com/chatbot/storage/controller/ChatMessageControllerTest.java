package com.chatbot.storage.controller;

import com.chatbot.storage.dto.request.SendMessageRequest;
import com.chatbot.storage.dto.response.ApiResponse;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.service.ChatMessageService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 *
 * The type Chat message controller test.
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatMessageController chatMessageController;

    private UUID sessionId;
    private UUID messageId;
    private String userId;
    private SendMessageRequest sendMessageRequest;
    private MessageResponse messageResponse;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        messageId = UUID.randomUUID();
        userId = "user123";

        sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setContent("Hello, world!");

        messageResponse = MessageResponse.builder()
                .id(messageId)
                .content("Hello, world!")
                .senderType(MessageSender.USER)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Send message should return created with message response.
     */
    @Test
    void sendMessage_ShouldReturnCreatedWithMessageResponse() {
        // Given
        when(chatMessageService.sendMessage(eq(sessionId), eq(userId), any(SendMessageRequest.class)))
                .thenReturn(messageResponse);

        // When
        ResponseEntity<ApiResponse<MessageResponse>> result =
                chatMessageController.sendMessage(sessionId, userId, sendMessageRequest);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Message sent successfully", result.getBody().getMessage());
        assertEquals(messageResponse, result.getBody().getData());

        verify(chatMessageService).sendMessage(sessionId, userId, sendMessageRequest);
    }

    /**
     * Gets session messages should return paged response.
     */
    @Test
    void getSessionMessages_ShouldReturnPagedResponse() {
        // Given
        PagedResponse<MessageResponse> pagedResponse = PagedResponse.<MessageResponse>builder()
                .content(List.of(messageResponse))
                .totalElements(1L)
                .totalPages(1)
                .currentPage(0)
                .size(50)
                .build();

        Pageable expectedPageable = PageRequest.of(0, 50, Sort.by("createdAt").ascending());
        when(chatMessageService.getSessionMessages(sessionId, userId, expectedPageable))
                .thenReturn(pagedResponse);

        // When
        ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> result =
                chatMessageController.getSessionMessages(sessionId, userId, 0, 50);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(pagedResponse, result.getBody().getData());
        assertEquals(1, result.getBody().getData().getContent().size());

        verify(chatMessageService).getSessionMessages(sessionId, userId, expectedPageable);
    }

    /**
     * Gets session messages with default pagination should use default values.
     */
    @Test
    void getSessionMessages_WithDefaultPagination_ShouldUseDefaultValues() {
        // Given
        PagedResponse<MessageResponse> pagedResponse = PagedResponse.<MessageResponse>builder()
                .content(List.of())
                .totalElements(0L)
                .totalPages(0)
                .currentPage(0)
                .size(50)
                .build();

        Pageable defaultPageable = PageRequest.of(0, 50, Sort.by("createdAt").ascending());
        when(chatMessageService.getSessionMessages(sessionId, userId, defaultPageable))
                .thenReturn(pagedResponse);

        // When
        ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> result =
                chatMessageController.getSessionMessages(sessionId, userId, 0, 50);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(0, result.getBody().getData().getTotalElements());

        verify(chatMessageService).getSessionMessages(sessionId, userId, defaultPageable);
    }

    /**
     * Gets all session messages should return all messages.
     */
    @Test
    void getAllSessionMessages_ShouldReturnAllMessages() {
        // Given
        MessageResponse message1 = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("First message")
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        MessageResponse message2 = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Second message")
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        List<MessageResponse> messages = List.of(message1, message2);
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenReturn(messages);

        // When
        ResponseEntity<ApiResponse<List<MessageResponse>>> result =
                chatMessageController.getAllSessionMessages(sessionId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals(messages, result.getBody().getData());
        assertEquals(2, result.getBody().getData().size());

        verify(chatMessageService).getAllSessionMessages(sessionId, userId);
    }

    /**
     * Delete message should return success response.
     */
    @Test
    void deleteMessage_ShouldReturnSuccessResponse() {
        // Given
        doNothing().when(chatMessageService).deleteMessage(messageId, userId);

        // When
        ResponseEntity<ApiResponse<Void>> result =
                chatMessageController.deleteMessage(sessionId, messageId, userId);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("Message deleted successfully", result.getBody().getMessage());
        assertNull(result.getBody().getData());

        verify(chatMessageService).deleteMessage(messageId, userId);
    }

    /**
     * Send message when service throws exception should propagate exception.
     */
    @Test
    void sendMessage_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        when(chatMessageService.sendMessage(eq(sessionId), eq(userId), any(SendMessageRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        assertThrows(RuntimeException.class, () ->
                chatMessageController.sendMessage(sessionId, userId, sendMessageRequest));

        verify(chatMessageService).sendMessage(sessionId, userId, sendMessageRequest);
    }

    /**
     * Delete message when service throws exception should propagate exception.
     */
    @Test
    void deleteMessage_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        doThrow(new RuntimeException("Delete failed"))
                .when(chatMessageService).deleteMessage(messageId, userId);

        // When & Then
        assertThrows(RuntimeException.class, () ->
                chatMessageController.deleteMessage(sessionId, messageId, userId));

        verify(chatMessageService).deleteMessage(messageId, userId);
    }
}