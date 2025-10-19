package com.chatbot.storage.dto.request;

import com.chatbot.storage.constants.AppConstants;
import com.chatbot.storage.enums.MessageSender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 *
 * The type Send message request.
 */
@Data
public class SendMessageRequest {

    @NotNull(message = AppConstants.SENDER_TYPE_IS_REQUIRED)
    private MessageSender senderType;

    @NotBlank(message = AppConstants.CONTENT_IS_REQUIRED)
    private String content;

    private Map<String, Object> contextData;
    private Map<String, Object> metadata;
}