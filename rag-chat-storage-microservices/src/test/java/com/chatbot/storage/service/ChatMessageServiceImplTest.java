package com.chatbot.storage.service;

import com.chatbot.storage.dto.request.SendMessageRequest;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.entity.ChatMessage;
import com.chatbot.storage.entity.ChatSession;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.exception.ResourceNotFoundException;
import com.chatbot.storage.mapper.MessageMapper;
import com.chatbot.storage.repository.ChatMessageRepository;
import com.chatbot.storage.repository.ChatSessionRepository;
import com.chatbot.storage.service.impl.ChatMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * The type Chat message service impl test.
 */
@ExtendWith(MockitoExtension.class)
class ChatMessageServiceImplTest {

    @Mock
    private ChatMessageRepository messageRepository;

    @Mock
    private ChatSessionRepository sessionRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private UUID sessionId;
    private UUID messageId;
    private String userId;
    private ChatSession chatSession;
    private ChatMessage chatMessage;
    private MessageResponse messageResponse;
    private SendMessageRequest sendMessageRequest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        messageId = UUID.randomUUID();
        userId = "user123";

        chatSession = ChatSession.builder()
                .id(sessionId)
                .userId(userId)
                .sessionName("Test Session")
                .createdAt(LocalDateTime.now())
                .build();

        chatMessage = ChatMessage.builder()
                .id(messageId)
                .session(chatSession)
                .senderType(MessageSender.USER)
                .content("Hello, world!")
                .createdAt(LocalDateTime.now())
                .build();

        messageResponse = MessageResponse.builder()
                .id(messageId)
                .content("Hello, world!")
                .senderType(MessageSender.USER)
                .sessionId(sessionId)
                .createdAt(LocalDateTime.now())
                .build();

        sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setSenderType(MessageSender.USER);
        sendMessageRequest.setContent("Hello, world!");
    }

    /**
     * Send message should create and return message.
     */
    @Test
    void sendMessage_ShouldCreateAndReturnMessage() {
        // Given
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(chatSession));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenReturn(chatMessage);
        when(messageMapper.toResponse(chatMessage))
                .thenReturn(messageResponse);

        // When
        MessageResponse result = chatMessageService.sendMessage(sessionId, userId, sendMessageRequest);

        // Then
        assertNotNull(result);
        assertEquals(messageResponse, result);
        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verify(messageRepository).save(any(ChatMessage.class));
        verify(messageMapper).toResponse(chatMessage);
    }

    /**
     * Send message when session not found should throw exception.
     */
    @Test
    void sendMessage_WhenSessionNotFound_ShouldThrowException() {
        // Given
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> chatMessageService.sendMessage(sessionId, userId, sendMessageRequest));

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verifyNoInteractions(messageRepository, messageMapper);
    }

    /**
     * Gets session messages should return paged response.
     */
    @Test
    void getSessionMessages_ShouldReturnPagedResponse() {
        // Given
        Pageable pageable = PageRequest.of(0, 50);
        Page<ChatMessage> messagePage = new PageImpl<>(List.of(chatMessage));
        PagedResponse<MessageResponse> pagedResponse = PagedResponse.<MessageResponse>builder()
                .content(List.of(messageResponse))
                .totalElements(1L)
                .build();

        when(sessionRepository.existsByIdAndUserId(sessionId, userId))
                .thenReturn(true);
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable))
                .thenReturn(messagePage);
        when(messageMapper.toPagedResponse(messagePage))
                .thenReturn(pagedResponse);

        // When
        PagedResponse<MessageResponse> result = chatMessageService.getSessionMessages(sessionId, userId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(pagedResponse, result);
        verify(sessionRepository).existsByIdAndUserId(sessionId, userId);
        verify(messageRepository).findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
        verify(messageMapper).toPagedResponse(messagePage);
    }

    /**
     * Gets session messages when session not owned should throw exception.
     */
    @Test
    void getSessionMessages_WhenSessionNotOwned_ShouldThrowException() {
        // Given
        Pageable pageable = PageRequest.of(0, 50);
        when(sessionRepository.existsByIdAndUserId(sessionId, userId))
                .thenReturn(false);

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> chatMessageService.getSessionMessages(sessionId, userId, pageable));

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).existsByIdAndUserId(sessionId, userId);
        verifyNoInteractions(messageRepository, messageMapper);
    }

    /**
     * Gets all session messages should return all messages.
     */
    @Test
    void getAllSessionMessages_ShouldReturnAllMessages() {
        // Given
        List<ChatMessage> messages = List.of(chatMessage);
        List<MessageResponse> messageResponses = List.of(messageResponse);

        when(sessionRepository.existsByIdAndUserId(sessionId, userId))
                .thenReturn(true);
        when(messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId))
                .thenReturn(messages);
        when(messageMapper.toResponseList(messages))
                .thenReturn(messageResponses);

        // When
        List<MessageResponse> result = chatMessageService.getAllSessionMessages(sessionId, userId);

        // Then
        assertNotNull(result);
        assertEquals(messageResponses, result);
        assertEquals(1, result.size());
        verify(sessionRepository).existsByIdAndUserId(sessionId, userId);
        verify(messageRepository).findBySessionIdOrderByCreatedAtAsc(sessionId);
        verify(messageMapper).toResponseList(messages);
    }

    /**
     * Delete message should delete message.
     */
    @Test
    void deleteMessage_ShouldDeleteMessage() {
        // Given
        when(messageRepository.findById(messageId))
                .thenReturn(Optional.of(chatMessage));
        doNothing().when(messageRepository).delete(chatMessage);

        // When
        chatMessageService.deleteMessage(messageId, userId);

        // Then
        verify(messageRepository).findById(messageId);
        verify(messageRepository).delete(chatMessage);
    }

    /**
     * Delete message when message not found should throw exception.
     */
    @Test
    void deleteMessage_WhenMessageNotFound_ShouldThrowException() {
        // Given
        when(messageRepository.findById(messageId))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> chatMessageService.deleteMessage(messageId, userId));

        assertEquals("Message not found", exception.getMessage());
        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).delete(any());
    }

    /**
     * Delete message when user not owner should throw exception.
     */
    @Test
    void deleteMessage_WhenUserNotOwner_ShouldThrowException() {
        // Given
        ChatSession otherUserSession = ChatSession.builder()
                .id(sessionId)
                .userId("otherUser")
                .build();
        
        ChatMessage otherUserMessage = ChatMessage.builder()
                .id(messageId)
                .session(otherUserSession)
                .content("Other user message")
                .build();

        when(messageRepository.findById(messageId))
                .thenReturn(Optional.of(otherUserMessage));

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> chatMessageService.deleteMessage(messageId, userId));

        assertEquals("Message not found", exception.getMessage());
        verify(messageRepository).findById(messageId);
        verify(messageRepository, never()).delete(any());
    }

    /**
     * Gets message count should return count.
     */
    @Test
    void getMessageCount_ShouldReturnCount() {
        // Given
        long expectedCount = 5L;
        when(messageRepository.countBySessionId(sessionId))
                .thenReturn(expectedCount);

        // When
        long result = chatMessageService.getMessageCount(sessionId);

        // Then
        assertEquals(expectedCount, result);
        verify(messageRepository).countBySessionId(sessionId);
    }

    /**
     * Add message without context data should create message.
     */
    @Test
    void addMessage_WithoutContextData_ShouldCreateMessage() {
        // Given
        String content = "Test message";
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(chatSession));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenReturn(chatMessage);
        when(messageMapper.toResponse(chatMessage))
                .thenReturn(messageResponse);

        // When
        MessageResponse result = chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, content);

        // Then
        assertNotNull(result);
        assertEquals(messageResponse, result);
        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verify(messageRepository).save(any(ChatMessage.class));
        verify(messageMapper).toResponse(chatMessage);
    }

    /**
     * Add message with context data should create message with context.
     */
    @Test
    void addMessage_WithContextData_ShouldCreateMessageWithContext() {
        // Given
        String content = "Test message";
        Map<String, Object> contextData = Map.of("source", "test");
        
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.of(chatSession));
        when(messageRepository.save(any(ChatMessage.class)))
                .thenReturn(chatMessage);
        when(messageMapper.toResponse(chatMessage))
                .thenReturn(messageResponse);

        // When
        MessageResponse result = chatMessageService.addMessage(sessionId, userId, MessageSender.ASSISTANT, content, contextData);

        // Then
        assertNotNull(result);
        assertEquals(messageResponse, result);
        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verify(messageRepository).save(any(ChatMessage.class));
        verify(messageMapper).toResponse(chatMessage);
    }

    /**
     * Add message when session not found should throw exception.
     */
    @Test
    void addMessage_WhenSessionNotFound_ShouldThrowException() {
        // Given
        when(sessionRepository.findByIdAndUserId(sessionId, userId))
                .thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> chatMessageService.addMessage(sessionId, userId, MessageSender.USER, "content"));

        assertEquals("Session not found", exception.getMessage());
        verify(sessionRepository).findByIdAndUserId(sessionId, userId);
        verifyNoInteractions(messageRepository, messageMapper);
    }
}