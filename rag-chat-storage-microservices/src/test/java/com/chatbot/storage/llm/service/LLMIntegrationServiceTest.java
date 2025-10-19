package com.chatbot.storage.llm.service;

import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.llm.client.LLMClient;
import com.chatbot.storage.llm.client.impl.GroqClientImpl;
import com.chatbot.storage.llm.client.impl.NoOpLLMClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 *
 * The type LLM integration service test.
 */
@ExtendWith(MockitoExtension.class)
class LLMIntegrationServiceTest {

    @Mock
    private LLMClient mockLLMClient;

    @Mock
    private GroqClientImpl mockGroqClient;

    private LLMIntegrationService llmIntegrationService;
    private LLMIntegrationService groqIntegrationService;

    private String userMessage;
    private String expectedResponse;
    private List<MessageResponse> conversationHistory;

    @BeforeEach
    void setUp() {
        // Create service instances with mocked clients
        llmIntegrationService = new LLMIntegrationService(mockLLMClient);
        groqIntegrationService = new LLMIntegrationService(mockGroqClient);
        
        userMessage = "What is machine learning?";
        expectedResponse = "Machine learning is a subset of artificial intelligence...";
        
        // Setup conversation history
        conversationHistory = new ArrayList<>();
        
        MessageResponse previousUserMsg = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Hello, I want to learn about AI")
                .senderType(MessageSender.USER)
                .sessionId(UUID.randomUUID())
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .build();
        
        MessageResponse previousAiMsg = MessageResponse.builder()
                .id(UUID.randomUUID())
                .content("Hello! I'd be happy to help you learn about AI. What would you like to know?")
                .senderType(MessageSender.ASSISTANT)
                .sessionId(UUID.randomUUID())
                .createdAt(LocalDateTime.now().minusMinutes(4))
                .build();
        
        conversationHistory.add(previousUserMsg);
        conversationHistory.add(previousAiMsg);
    }

    // =============== processMessage Tests ===============

    @Test
    void processMessage_WhenLLMAvailable_ShouldReturnLLMResponse() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(userMessage)).thenReturn(expectedResponse);

        // When
        String result = llmIntegrationService.processMessage(userMessage);

        // Then
        assertEquals(expectedResponse, result);
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient).getChatCompletion(userMessage);
    }

    @Test
    void processMessage_WhenLLMNotAvailable_ShouldReturnInformativeMessage() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(false));

        // When
        String result = llmIntegrationService.processMessage(userMessage);

        // Then
        assertTrue(result.contains("without LLM integration"));
        assertTrue(result.contains("stored successfully"));
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient, never()).getChatCompletion(anyString());
    }

    @Test
    void processMessage_WhenLLMThrowsException_ShouldReturnErrorMessage() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(userMessage))
                .thenThrow(new RuntimeException("API connection failed"));

        // When
        String result = llmIntegrationService.processMessage(userMessage);

        // Then
        assertTrue(result.contains("encountered an error"));
        assertTrue(result.contains("stored successfully"));
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient).getChatCompletion(userMessage);
    }

    // =============== processMessageWithContext Tests ===============

    @Test
    void processMessageWithContext_WhenLLMNotAvailable_ShouldReturnInformativeMessage() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(false));

        // When
        String result = llmIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertTrue(result.contains("without LLM integration"));
        assertTrue(result.contains("stored successfully"));
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient, never()).getChatCompletion(anyString());
    }

    @Test
    void processMessageWithContext_WithGroqClient_ShouldUseContextualMethod() {
        // Given
        when(mockGroqClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockGroqClient.getChatCompletionWithContext(conversationHistory, userMessage))
                .thenReturn(expectedResponse);

        // When
        String result = groqIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertEquals(expectedResponse, result);
        verify(mockGroqClient).isAvailable();
        verify(mockGroqClient).getChatCompletionWithContext(conversationHistory, userMessage);
        verify(mockGroqClient, never()).getChatCompletion(anyString());
    }

    @Test
    void processMessageWithContext_WithNonGroqClient_ShouldUseFallbackMethod() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(anyString())).thenReturn(expectedResponse);

        // When
        String result = llmIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertEquals(expectedResponse, result);
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient).getChatCompletion(argThat(contextualMessage -> 
                contextualMessage.contains("Previous conversation") && 
                contextualMessage.contains(userMessage) &&
                contextualMessage.contains("Hello, I want to learn about AI")
        ));
    }

    @Test
    void processMessageWithContext_WithEmptyHistory_ShouldUseCurrentMessage() {
        // Given
        List<MessageResponse> emptyHistory = new ArrayList<>();
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(userMessage)).thenReturn(expectedResponse);

        // When
        String result = llmIntegrationService.processMessageWithContext(userMessage, emptyHistory);

        // Then
        assertEquals(expectedResponse, result);
        verify(mockLLMClient).getChatCompletion(userMessage);
    }

    @Test
    void processMessageWithContext_WhenRegularLLMThrowsException_ShouldReturnErrorMessage() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(anyString()))
                .thenThrow(new RuntimeException("Context processing failed"));

        // When
        String result = llmIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertTrue(result.contains("encountered an error"));
        assertTrue(result.contains("stored successfully"));
        verify(mockLLMClient).isAvailable();
        verify(mockLLMClient).getChatCompletion(anyString());
    }

    @Test
    void processMessageWithContext_WhenGroqClientThrowsException_ShouldReturnErrorMessage() {
        // Given
        when(mockGroqClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockGroqClient.getChatCompletionWithContext(conversationHistory, userMessage))
                .thenThrow(new RuntimeException("Groq API error"));

        // When
        String result = groqIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertTrue(result.contains("encountered an error"));
        verify(mockGroqClient).getChatCompletionWithContext(conversationHistory, userMessage);
    }

    // =============== Utility Method Tests ===============

    @Test
    void isLlmAvailable_ShouldReturnClientAvailability() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));

        // When
        boolean result = llmIntegrationService.isLlmAvailable();

        // Then
        assertTrue(result);
        verify(mockLLMClient).isAvailable();
    }

    @Test
    void getLlmStatus_WithNoOpClient_ShouldReturnDisabledStatus() {
        // Given
        LLMIntegrationService serviceWithNoOp = new LLMIntegrationService(new NoOpLLMClient());

        // When
        String status = serviceWithNoOp.getLlmStatus();

        // Then
        assertEquals("DISABLED - No LLM configured", status);
    }

    @Test
    void getLlmStatus_WithAvailableClient_ShouldReturnActiveStatus() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));

        // When
        String status = llmIntegrationService.getLlmStatus();

        // Then
        assertEquals("ACTIVE", status);
        verify(mockLLMClient).isAvailable();
    }

    @Test
    void getLlmStatus_WithUnavailableClient_ShouldReturnUnavailableStatus() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(false));

        // When
        String status = llmIntegrationService.getLlmStatus();

        // Then
        assertEquals("UNAVAILABLE", status);
        verify(mockLLMClient).isAvailable();
    }

    // =============== Context Building Tests ===============

    @Test
    void processMessageWithContext_ShouldBuildCorrectContextFormat() {
        // Given
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(anyString())).thenReturn(expectedResponse);

        // When
        llmIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        verify(mockLLMClient).getChatCompletion(argThat(contextualMessage -> {
            // Verify context contains expected elements
            boolean containsPreviousConversation = contextualMessage.contains("Previous conversation");
            boolean containsUserMessage = contextualMessage.contains("Hello, I want to learn about AI");
            boolean containsAssistantMessage = contextualMessage.contains("I'd be happy to help you learn about AI");
            boolean containsCurrentMessage = contextualMessage.contains(userMessage);
            boolean endsWithAssistantPrompt = contextualMessage.contains("Assistant:");
            
            return containsPreviousConversation && containsUserMessage && 
                   containsAssistantMessage && containsCurrentMessage && endsWithAssistantPrompt;
        }));
    }

    @Test
    void processMessageWithContext_WithMixedMessageTypes_ShouldFormatCorrectly() {
        // Given
        List<MessageResponse> mixedHistory = new ArrayList<>();
        
        MessageResponse userMsg1 = MessageResponse.builder()
                .content("First user message")
                .senderType(MessageSender.USER)
                .build();
        MessageResponse aiMsg1 = MessageResponse.builder()
                .content("First AI response")
                .senderType(MessageSender.ASSISTANT)
                .build();
        MessageResponse userMsg2 = MessageResponse.builder()
                .content("Second user message")
                .senderType(MessageSender.USER)
                .build();
        
        mixedHistory.add(userMsg1);
        mixedHistory.add(aiMsg1);
        mixedHistory.add(userMsg2);
        
        when(mockLLMClient.isAvailable()).thenReturn(Boolean.valueOf(true));
        when(mockLLMClient.getChatCompletion(anyString())).thenReturn(expectedResponse);

        // When
        llmIntegrationService.processMessageWithContext("Current message", mixedHistory);

        // Then
        verify(mockLLMClient).getChatCompletion(argThat(contextualMessage -> {
            // Verify proper role formatting
            boolean hasHumanLabels = contextualMessage.contains("Human: First user message") &&
                                   contextualMessage.contains("Human: Second user message") &&
                                   contextualMessage.contains("Human: Current message");
            boolean hasAssistantLabels = contextualMessage.contains("Assistant: First AI response");
            
            return hasHumanLabels && hasAssistantLabels;
        }));
    }

    @Test 
    void processMessageWithContext_WithGroqClient_WhenNotAvailable_ShouldReturnInformativeMessage() {
        // Given
        when(mockGroqClient.isAvailable()).thenReturn(Boolean.valueOf(false));

        // When
        String result = groqIntegrationService.processMessageWithContext(userMessage, conversationHistory);

        // Then
        assertTrue(result.contains("without LLM integration"));
        verify(mockGroqClient).isAvailable();
        verify(mockGroqClient, never()).getChatCompletionWithContext(any(), anyString());
        verify(mockGroqClient, never()).getChatCompletion(anyString());
    }
}