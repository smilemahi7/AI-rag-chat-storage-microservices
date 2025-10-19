package com.chatbot.storage.dto.request;

import com.chatbot.storage.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * The type Create session request.
 */
@Data
public class CreateSessionRequest {

    @NotBlank(message = AppConstants.USER_ID_IS_REQUIRED)
    @Size(max = 100, message = AppConstants.USER_ID_MUST_NOT_EXCEED_100_CHARACTERS)
    private String userId;

    @NotBlank(message = AppConstants.SESSION_NAME_IS_REQUIRED)
    @Size(max = 255, message = AppConstants.SESSION_NAME_MUST_NOT_EXCEED_255_CHARACTERS)
    private String sessionName;

    @Size(max = 500, message = AppConstants.DESCRIPTION_MUST_NOT_EXCEED_500_CHARACTERS)
    private String description;
}