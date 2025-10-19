package com.chatbot.storage.controller;

import com.chatbot.storage.config.properties.PaginationProperties;
import com.chatbot.storage.dto.request.CreateSessionRequest;
import com.chatbot.storage.dto.request.UpdateSessionRequest;
import com.chatbot.storage.dto.response.ApiResponse;
import com.chatbot.storage.dto.response.PagedResponse;
import com.chatbot.storage.dto.response.SessionResponse;
import com.chatbot.storage.service.ChatSessionService;
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
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Tag(name = "Chat Sessions", description = TagDescription.CHAT_SESSIONS)
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final PaginationProperties paginationProperties;

    @PostMapping
    @Operation(summary = OperationSummary.CREATE_SESSION)
    public ResponseEntity<ApiResponse<SessionResponse>> createSession(
            @Valid @RequestBody CreateSessionRequest request) {

        SessionResponse response = chatSessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SuccessMessages.SESSION_CREATED, response));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = OperationSummary.GET_SESSION)
    public ResponseEntity<ApiResponse<SessionResponse>> getSession(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        SessionResponse response = chatSessionService.getSessionById(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = OperationSummary.GET_USER_SESSIONS)
    public ResponseEntity<ApiResponse<PagedResponse<SessionResponse>>> getUserSessions(
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId,
            @Parameter(description = DESC_PAGE) @RequestParam(value = PARAM_PAGE, defaultValue = "0") int page,
            @Parameter(description = DESC_SIZE) @RequestParam(value = PARAM_SIZE, required = false) Integer size,
            @Parameter(description = DESC_SORT_BY) @RequestParam(value = PARAM_SORT_BY, required = false) String sortBy,
            @Parameter(description = DESC_SORT_DIR) @RequestParam(value = PARAM_SORT_DIR, required = false) String sortDir) {

        // Use configuration-based pagination
        int pageSize = size != null ? size : paginationProperties.getDefaultPageSize();
        String sortField = sortBy != null ? sortBy : paginationProperties.getDefaultSortField();
        String sortDirection = sortDir != null ? sortDir : paginationProperties.getDefaultSortDirection();

        // Validate against max page size
        if (pageSize > paginationProperties.getMaxPageSize()) {
            pageSize = paginationProperties.getMaxPageSize();
        }

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        PagedResponse<SessionResponse> response = chatSessionService.getUserSessions(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/favorites")
    @Operation(summary = OperationSummary.GET_FAVORITE_SESSIONS)
    public ResponseEntity<ApiResponse<List<SessionResponse>>> getFavoriteSessions(
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        List<SessionResponse> response = chatSessionService.getFavoriteSessions(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{sessionId}")
    @Operation(summary = OperationSummary.UPDATE_SESSION)
    public ResponseEntity<ApiResponse<SessionResponse>> updateSession(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId,
            @Valid @RequestBody UpdateSessionRequest request) {

        SessionResponse response = chatSessionService.updateSession(sessionId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.SESSION_UPDATED, response));
    }

    @PatchMapping("/{sessionId}/favorite")
    @Operation(summary = OperationSummary.TOGGLE_FAVORITE)
    public ResponseEntity<ApiResponse<SessionResponse>> toggleFavorite(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        SessionResponse response = chatSessionService.toggleFavorite(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.FAVORITE_UPDATED, response));
    }

    @DeleteMapping("/{sessionId}")
    @Operation(summary = OperationSummary.DELETE_SESSION)
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable(PARAM_SESSION_ID) UUID sessionId,
            @Parameter(description = DESC_USER_ID) @RequestParam(PARAM_USER_ID) String userId) {

        chatSessionService.deleteSession(sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessMessages.SESSION_DELETED, null));
    }
}