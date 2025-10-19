package com.chatbot.storage.service.impl;

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
import com.chatbot.storage.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * The type Chat message service.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatSessionRepository sessionRepository;
    private final MessageMapper messageMapper;

    @Override
    public MessageResponse sendMessage(UUID sessionId, String userId, SendMessageRequest request) {
        ChatSession session = getSessionForUser(sessionId, userId);

        ChatMessage message = buildMessage(session, request.getSenderType(), request.getContent(), request.getContextData(), request.getMetadata());
        return saveAndMapMessage(message);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getSessionMessages(UUID sessionId, String userId, Pageable pageable) {
        verifySessionOwnership(sessionId, userId);

        Page<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId, pageable);
        return messageMapper.toPagedResponse(messages);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getAllSessionMessages(UUID sessionId, String userId) {
        verifySessionOwnership(sessionId, userId);

        List<ChatMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
        return messageMapper.toResponseList(messages);
    }

    @Override
    public void deleteMessage(UUID messageId, String userId) {
        ChatMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        if (!message.getSession().getUserId().equals(userId)) {
            throw new ResourceNotFoundException("Message not found");
        }

        messageRepository.delete(message);
        log.info("Message {} deleted", messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public long getMessageCount(UUID sessionId) {
        return messageRepository.countBySessionId(sessionId);
    }

    @Override
    public MessageResponse addMessage(UUID sessionId, String userId, MessageSender senderType, String content) {
        return addMessage(sessionId, userId, senderType, content, null);
    }

    @Override
    public MessageResponse addMessage(UUID sessionId, String userId, MessageSender senderType, String content, Map<String, Object> contextData) {
        ChatSession session = getSessionForUser(sessionId, userId);

        ChatMessage message = buildMessage(session, senderType, content, contextData, null);
        return saveAndMapMessage(message);
    }

    // ---------- Helper Methods ----------

    private ChatSession getSessionForUser(UUID sessionId, String userId) {
        log.info("Verifying session {} for user {}", sessionId, userId);
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
    }

    private void verifySessionOwnership(UUID sessionId, String userId) {
        if (!sessionRepository.existsByIdAndUserId(sessionId, userId)) {
            throw new ResourceNotFoundException("Session not found");
        }
    }

    private ChatMessage buildMessage(ChatSession session, MessageSender senderType, String content,
                                     Map<String, Object> contextData, Map<String, Object> metadata) {
        return ChatMessage.builder()
                .session(session)
                .senderType(senderType)
                .content(content)
                .contextData(contextData)
                .metadata(metadata)
                .build();
    }

    private MessageResponse saveAndMapMessage(ChatMessage message) {
        ChatMessage savedMessage = messageRepository.save(message);
        log.info("Message saved with ID: {}", savedMessage.getId());
        return messageMapper.toResponse(savedMessage);
    }
}
