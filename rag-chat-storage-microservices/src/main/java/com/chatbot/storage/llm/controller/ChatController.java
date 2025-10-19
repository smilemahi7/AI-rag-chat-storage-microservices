package com.chatbot.storage.llm.controller;

import com.chatbot.storage.constants.AppConstants;
import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.enums.MessageSender;
import com.chatbot.storage.llm.model.LlmStatusResponse;
import com.chatbot.storage.llm.service.LLMIntegrationService;
import com.chatbot.storage.service.ChatMessageService;
import com.chatbot.storage.service.ChatSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 *
 * The type chat controller to demonstarate the realtime chat with LLM.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = AppConstants.CHAT_WITH_LLM,
        description = AppConstants.LLM_INTEGRATION_OPTIONAL_FUNCTIONALITY)
public class ChatController {

    private final LLMIntegrationService llmIntegrationService;
    private final ChatSessionService chatSessionService;
    private final ChatMessageService chatMessageService;

    /**
     * Chat with session response entity.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @param message   the message
     * @return the response entity
     */
    @PostMapping("/sessions/{sessionId}")
    @Operation(summary = AppConstants.SEND_A_USER_MESSAGE_AND_GET_AI_RESPONSE)
    public ResponseEntity<MessageResponse> chatWithSession(
            @PathVariable UUID sessionId,
            @RequestParam String userId,
            @RequestParam String message) {

        // 1. Get conversation history BEFORE adding new message
        List<MessageResponse> conversationHistory = chatMessageService.getAllSessionMessages(sessionId, userId);

        // 2. Store user message
        MessageResponse userMessage = chatMessageService.addMessage(
                sessionId, userId, MessageSender.USER, message
        );

        // 3. Get AI response WITH conversation context
        String aiResponse = llmIntegrationService.processMessageWithContext(message, conversationHistory);

        // 4. Store AI response
        MessageResponse aiMessage = chatMessageService.addMessage(
                sessionId, userId, MessageSender.ASSISTANT, aiResponse
        );

        return ResponseEntity.ok(aiMessage);
    }

    /**
     * Start new chat response entity.
     *
     * @param userId  the user id
     * @param message the message
     * @param title   the title
     * @return the response entity
     */
    @PostMapping("/sessions")
    @Operation(summary = AppConstants.START_A_NEW_CHAT_WITH_INITIAL_MESSAGE)
    public ResponseEntity<SessionResponse> startNewChat(
            @RequestParam String userId,
            @RequestParam String message,
            @RequestParam(required = false) String title) {

        CreateSessionRequest createRequest = new CreateSessionRequest();
        createRequest.setUserId(userId);
        createRequest.setSessionName(title != null ? title : AppConstants.NEW_CHAT);

        SessionResponse session = chatSessionService.createSession(createRequest);

        chatMessageService.addMessage(session.getId(), userId, MessageSender.USER, message);

        String aiResponse = llmIntegrationService.processMessage(message);
        chatMessageService.addMessage(session.getId(), userId, MessageSender.ASSISTANT, aiResponse);

        return ResponseEntity.ok(session);
    }

    /**
     * Gets llm status.
     *
     * @return the llm status
     */
    @GetMapping("/status")
    @Operation(summary = AppConstants.CHECK_LLM_INTEGRATION_STATUS)
    public ResponseEntity<LlmStatusResponse> getLlmStatus() {
        return ResponseEntity.ok(new LlmStatusResponse(
                llmIntegrationService.getLlmStatus(),
                llmIntegrationService.isLlmAvailable()
        ));
    }

}