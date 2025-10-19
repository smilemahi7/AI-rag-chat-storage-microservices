package com.chatbot.storage.llm.controller;

import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.llm.model.LlmStatusResponse;
import com.chatbot.storage.llm.service.LLMIntegrationService;
import com.chatbot.storage.service.ChatMessageService;
import com.chatbot.storage.service.ChatSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * The type Demo chat controller test.
 */
@ExtendWith(MockitoExtension.class)
class DemoChatControllerTest {

    @Mock
    private LLMIntegrationService llmIntegrationService;

    @Mock
    private ChatSessionService chatSessionService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatController demoChatController;

    private UUID sessionId;
    private String userId;
    private String userMessage;
    private String aiResponse;
    private MessageResponse userMessageResponse;
    private MessageResponse aiMessageResponse;
    private SessionResponse sessionResponse;
    private List<MessageResponse> conversationHistory;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        userId = "demo-user-123";
        userMessage = "What is machine learning?";
        aiResponse = "Machine learning is a subset of AI...";

        userMessageResponse = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content(userMessage)
                .senderType(MessageSender.USER)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        aiMessageResponse = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content(aiResponse)
                .senderType(MessageSender.ASSISTANT)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        sessionResponse = SessionResponse.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Demo Chat Session")
                .createdAt(LocalDateTime.now())
                .build();

        // Setup conversation history
        conversationHistory = new ArrayList<>();
        MessageResponse previousMsg = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Hello, tell me about AI")
                .senderType(MessageSender.USER)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();
        conversationHistory.add(previousMsg);
    }

    /**
     * Chat with session should process message with context and return AI response.
     */
    @Test
    void chatWithSession_ShouldProcessMessageWithContextAndReturnAiResponse() {
        // Given
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenReturn(conversationHistory);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.USER, userMessage))
                .thenReturn(userMessageResponse);
        when(llmIntegrationService.processMessageWithContext(userMessage, conversationHistory))
                .thenReturn(aiResponse);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, aiResponse))
                .thenReturn(aiMessageResponse);

        // When
        ResponseEntity<MessageResponse> result = demoChatController.chatWithSession(sessionId, userId, userMessage);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(aiMessageResponse, result.getBody());
        assertEquals(MessageSender.ASSISTANT, result.getBody().getSenderType());
        assertEquals(aiResponse, result.getBody().getContent());

        // Verify the correct sequence of calls
        verify(chatMessageService).getAllSessionMessages(sessionId, userId);
        verify(chatMessageService).addMessage(sessionId, userId, MessageSender.USER, userMessage);
        verify(llmIntegrationService).processMessageWithContext(userMessage, conversationHistory);
        verify(chatMessageService).addMessage(sessionId, userId, MessageSender.ASSISTANT, aiResponse);
    }

    /**
     * Chat with session with empty history should still work.
     */
    @Test
    void chatWithSession_WithEmptyHistory_ShouldStillWork() {
        // Given
        List<MessageResponse> emptyHistory = new ArrayList<>();
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenReturn(emptyHistory);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.USER, userMessage))
                .thenReturn(userMessageResponse);
        when(llmIntegrationService.processMessageWithContext(userMessage, emptyHistory))
                .thenReturn(aiResponse);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, aiResponse))
                .thenReturn(aiMessageResponse);

        // When
        ResponseEntity<MessageResponse> result = demoChatController.chatWithSession(sessionId, userId, userMessage);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(llmIntegrationService).processMessageWithContext(userMessage, emptyHistory);
    }

    /**
     * Chat with session when llm returns error should still store messages.
     */
    @Test
    void chatWithSession_WhenLLMReturnsError_ShouldStillStoreMessages() {
        // Given
        String errorResponse = "Sorry, I'm having trouble processing your request.";
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenReturn(conversationHistory);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.USER, userMessage))
                .thenReturn(userMessageResponse);
        when(llmIntegrationService.processMessageWithContext(userMessage, conversationHistory))
                .thenReturn(errorResponse);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, errorResponse))
                .thenReturn(aiMessageResponse);

        // When
        ResponseEntity<MessageResponse> result = demoChatController.chatWithSession(sessionId, userId, userMessage);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(chatMessageService).getAllSessionMessages(sessionId, userId);
        verify(chatMessageService, times(2)).addMessage(eq(sessionId), eq(userId), any(MessageSender.class), anyString());
        verify(llmIntegrationService).processMessageWithContext(userMessage, conversationHistory);
    }

    /**
     * Start new chat with title should create session and process message.
     */
    @Test
    void startNewChat_WithTitle_ShouldCreateSessionAndProcessMessage() {
        // Given
        String title = "My Demo Chat";
        when(chatSessionService.createSession(any(CreateSessionRequest.class)))
                .thenReturn(sessionResponse);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.USER, userMessage))
                .thenReturn(userMessageResponse);
        when(llmIntegrationService.processMessage(userMessage))
                .thenReturn(aiResponse);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, aiResponse))
                .thenReturn(aiMessageResponse);

        // When
        ResponseEntity<SessionResponse> result = demoChatController.startNewChat(userId, userMessage, title);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(sessionResponse, result.getBody());

        verify(chatSessionService).createSession(any(CreateSessionRequest.class));
        verify(chatMessageService).addMessage(sessionId, userId, MessageSender.USER, userMessage);
        verify(llmIntegrationService).processMessage(userMessage);
        verify(chatMessageService).addMessage(sessionId, userId, MessageSender.ASSISTANT, aiResponse);
    }

    /**
     * Start new chat without title should use default title.
     */
    @Test
    void startNewChat_WithoutTitle_ShouldUseDefaultTitle() {
        // Given
        when(chatSessionService.createSession(any(CreateSessionRequest.class)))
                .thenReturn(sessionResponse);
        when(chatMessageService.addMessage(any(UUID.class), eq(userId), eq(MessageSender.USER), eq(userMessage)))
                .thenReturn(userMessageResponse);
        when(llmIntegrationService.processMessage(userMessage))
                .thenReturn(aiResponse);
        when(chatMessageService.addMessage(any(UUID.class), eq(userId), eq(MessageSender.ASSISTANT), eq(aiResponse)))
                .thenReturn(aiMessageResponse);

        // When
        ResponseEntity<SessionResponse> result = demoChatController.startNewChat(userId, userMessage, null);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());

        verify(chatSessionService).createSession(argThat(request ->
                "New Chat".equals(request.getSessionName()) && userId.equals(request.getUserId())
        ));
    }

    /**
     * Gets llm status should return status response.
     */
    @Test
    void getLlmStatus_ShouldReturnStatusResponse() {
        // Given
        String status = "Connected";
        boolean available = true;
        when(llmIntegrationService.getLlmStatus()).thenReturn(status);
        when(llmIntegrationService.isLlmAvailable()).thenReturn(available);

        // When
        ResponseEntity<LlmStatusResponse> result = demoChatController.getLlmStatus();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(status, result.getBody().status());

        verify(llmIntegrationService).getLlmStatus();
        verify(llmIntegrationService).isLlmAvailable();
    }

    /**
     * Gets llm status when llm unavailable should return unavailable status.
     */
    @Test
    void getLlmStatus_WhenLLMUnavailable_ShouldReturnUnavailableStatus() {
        // Given
        String status = "Disconnected";
        boolean available = false;
        when(llmIntegrationService.getLlmStatus()).thenReturn(status);
        when(llmIntegrationService.isLlmAvailable()).thenReturn(available);

        // When
        ResponseEntity<LlmStatusResponse> result = demoChatController.getLlmStatus();

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(status, Objects.requireNonNull(result.getBody()).status());
        assertFalse(result.getBody().available());
    }

    /**
     * Chat with session when service throws exception should propagate exception.
     */
    @Test
    void chatWithSession_WhenServiceThrowsException_ShouldPropagateException() {
        // Given
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenThrow(new RuntimeException("Session not found"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> demoChatController.chatWithSession(sessionId, userId, userMessage));

        assertEquals("Session not found", exception.getMessage());
        verify(chatMessageService).getAllSessionMessages(sessionId, userId);
        verifyNoMoreInteractions(chatMessageService);
        verifyNoInteractions(llmIntegrationService);
    }

    /**
     * Chat with session when message service throws exception during user message storage.
     */
    @Test
    void chatWithSession_WhenMessageServiceThrowsExceptionDuringUserMessageStorage_ShouldPropagateException() {
        // Given
        when(chatMessageService.getAllSessionMessages(sessionId, userId))
                .thenReturn(conversationHistory);
        when(chatMessageService.addMessage(sessionId, userId, MessageSender.USER, userMessage))
                .thenThrow(new RuntimeException("Failed to store user message"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> demoChatController.chatWithSession(sessionId, userId, userMessage));

        assertEquals("Failed to store user message", exception.getMessage());
        verify(chatMessageService).getAllSessionMessages(sessionId, userId);
        verify(chatMessageService).addMessage(sessionId, userId, MessageSender.USER, userMessage);
        verifyNoInteractions(llmIntegrationService);
    }

    /**
     * Start new chat when session creation fails should propagate exception.
     */
    @Test
    void startNewChat_WhenSessionCreationFails_ShouldPropagateException() {
        // Given
        when(chatSessionService.createSession(any(CreateSessionRequest.class)))
                .thenThrow(new RuntimeException("Unable to create session"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> demoChatController.startNewChat(userId, userMessage, "Test Chat"));

        assertEquals("Unable to create session", exception.getMessage());
        verify(chatSessionService).createSession(any(CreateSessionRequest.class));
        verifyNoInteractions(chatMessageService, llmIntegrationService);
    }
}