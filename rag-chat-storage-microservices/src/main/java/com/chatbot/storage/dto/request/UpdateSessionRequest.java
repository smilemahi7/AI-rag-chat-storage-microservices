package com.chatbot.storage.dto.request;

import com.chatbot.storage.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 *
 * The type Update session request.
 */
@Data
public class UpdateSessionRequest {

    @NotBlank(message = AppConstants.SESSION_NAME_IS_REQUIRED)
    @Size(max = 255, message = AppConstants.SESSION_NAME_MUST_NOT_EXCEED_255_CHARACTERS)
    private String sessionName;

    @Size(max = 500, message = AppConstants.NOT_EXCEED_500_CHARACTERS)
    private String description;

    private Boolean isFavorite;
}