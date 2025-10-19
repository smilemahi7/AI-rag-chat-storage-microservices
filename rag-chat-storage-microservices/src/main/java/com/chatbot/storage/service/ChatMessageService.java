package com.chatbot.storage.service;

import com.chatbot.storage.dto.request.SendMessageRequest;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.enums.MessageSender;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * The interface Chat message service.
 */
public interface ChatMessageService {
    /**
     * Send message response.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @param request   the request
     * @return the message response
     */
    MessageResponse sendMessage(UUID sessionId, String userId, SendMessageRequest request);

    /**
     * Gets session messages.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @param pageable  the pageable
     * @return the session messages
     */
    PagedResponse<MessageResponse> getSessionMessages(UUID sessionId, String userId, Pageable pageable);

    /**
     * Gets all session messages.
     *
     * @param sessionId the session id
     * @param userId    the user id
     * @return the all session messages
     */
    List<MessageResponse> getAllSessionMessages(UUID sessionId, String userId);

    /**
     * Delete message.
     *
     * @param messageId the message id
     * @param userId    the user id
     */
    void deleteMessage(UUID messageId, String userId);

    /**
     * Gets message count.
     *
     * @param sessionId the session id
     * @return the message count
     */
    long getMessageCount(UUID sessionId);

    /**
     * Add message response.
     *
     * @param sessionId  the session id
     * @param userId     the user id
     * @param senderType the sender type
     * @param content    the content
     * @return the message response
     */
    MessageResponse addMessage(UUID sessionId, String userId, MessageSender senderType, String content);

    /**
     * Add message response.
     *
     * @param sessionId   the session id
     * @param userId      the user id
     * @param senderType  the sender type
     * @param content     the content
     * @param contextData the context data
     * @return the message response
     */
    MessageResponse addMessage(UUID sessionId, String userId, MessageSender senderType, String content, Map<String, Object> contextData);
}