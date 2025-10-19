package com.chatbot.storage.controller;

import com.chatbot.storage.config.properties.PaginationProperties;
import com.chatbot.storage.dto.request.SendMessageRequest;
import com.chatbot.storage.dto.response.ApiResponse;
import com.chatbot.storage.dto.response.MessageResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.chatbot.storage.constants.AppConstants.*;

@RestController
@RequestMapping("/api/v1/sessions/{sessionId}/messages")
@RequiredArgsConstructor
@Tag(name = "Chat Messages", description = TagDescription.CHAT_MESSAGES)
public class ChatMessageController {

    private final ChatMessageService chatMessageService;
    private final PaginationProperties paginationProperties;

    @PostMapping
    @Operation(summary = OperationSummary.SEND_MESSAGE)
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId,
            @Valid @RequestBody SendMessageRequest request) {

        MessageResponse response = chatMessageService.sendMessage(sessionId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.MESSAGE_SENT, response));
    }

    @GetMapping
    @Operation(summary = OperationSummary.GET_SESSION_MESSAGES)
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getSessionMessages(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId,
            @Parameter(description = DESC_PAGE) @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
            @Parameter(description = DESC_SIZE) @RequestParam(value = PARAM_SIZE, required = false) Integer size) {

        // Use configuration-based pagination
        int pageSize = size != null ? size : paginationProperties.getDefaultPageSize();

        // Validate against max page size
        if (pageSize > paginationProperties.getMaxPageSize()) {
            pageSize = paginationProperties.getMaxPageSize();
        }

        Pageable pageable = PageRequest.of(page, pageSize,
                Sort.by(paginationProperties.getDefaultSortField()).ascending());

        PagedResponse<MessageResponse> response = chatMessageService.getSessionMessages(sessionId, userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    @Operation(summary = OperationSummary.GET_ALL_SESSION_MESSAGES)
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getAllSessionMessages(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        List<MessageResponse> response = chatMessageService.getAllSessionMessages(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = OperationSummary.DELETE_MESSAGE)
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @PathVariable(PARAM_MESSAGE_ID) UUID messageId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        chatMessageService.deleteMessage(messageId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.MESSAGE_DELETED, null));
    }
}