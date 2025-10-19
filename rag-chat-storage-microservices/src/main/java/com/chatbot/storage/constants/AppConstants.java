package com.chatbot.storage.constants;

public final class AppConstants {

    // API Constants
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String API_CLIENT = "api-client";

    // Cache Names
    public static final String CACHE_SESSIONS = "sessions";
    public static final String CACHE_FAVORITE_SESSIONS = "favoriteSessions";

    public static final String USER_ID_IS_REQUIRED = "User ID is required";
    public static final String USER_ID_MUST_NOT_EXCEED_100_CHARACTERS = "User ID must not exceed 100 characters";
    public static final String SESSION_NAME_IS_REQUIRED = "Session name is required";
    public static final String SESSION_NAME_MUST_NOT_EXCEED_255_CHARACTERS = "Session name must not exceed 255 characters";
    public static final String DESCRIPTION_MUST_NOT_EXCEED_500_CHARACTERS = "Description must not exceed 500 characters";
    public static final String SENDER_TYPE_IS_REQUIRED = "Sender type is required";
    public static final String CONTENT_IS_REQUIRED = "Content is required";
    public static final String NOT_EXCEED_500_CHARACTERS = "Description must not exceed 500 characters";
    public static final String CHAT_WITH_LLM = "Chat with LLM";
    public static final String LLM_INTEGRATION_OPTIONAL_FUNCTIONALITY = "Chat endpoints for testing LLM integration. Optional functionality.";
    public static final String SEND_A_USER_MESSAGE_AND_GET_AI_RESPONSE = "Send a user message and get AI response";
    public static final String START_A_NEW_CHAT_WITH_INITIAL_MESSAGE = "Start a new chat with initial message";
    public static final String NEW_CHAT = "New Chat";
    public static final String CHECK_LLM_INTEGRATION_STATUS = "Check LLM integration status";

    // Request Parameters
    public static final String PARAM_USER_ID = "userId";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SORT_BY = "sortBy";
    public static final String PARAM_SORT_DIR = "sortDir";
    public static final String PARAM_SESSION_ID = "sessionId";
    public static final String PARAM_MESSAGE_ID = "messageId";

    // Parameter Descriptions
    public static final String DESC_USER_ID = "User ID";
    public static final String DESC_PAGE = "Page number (0-based)";
    public static final String DESC_SIZE = "Page size";
    public static final String DESC_SORT_BY = "Sort field";
    public static final String DESC_SORT_DIR = "Sort direction (asc/desc)";

    // Error Codes
    public static final String ERROR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERROR_VALIDATION_FAILED = "VALIDATION_FAILED";
    public static final String ERROR_INTERNAL_SERVER = "INTERNAL_SERVER_ERROR";

    // API Operation Summaries
    public static final class OperationSummary {
        public static final String SEND_MESSAGE = "Send a message to a session";
        public static final String GET_SESSION_MESSAGES = "Get session messages with pagination";
        public static final String GET_ALL_SESSION_MESSAGES = "Get all messages for a session";
        public static final String DELETE_MESSAGE = "Delete a specific message";
        public static final String CREATE_SESSION = "Create a new chat session";
        public static final String GET_SESSION = "Get session by ID";
        public static final String GET_USER_SESSIONS = "Get user sessions with pagination";
        public static final String GET_FAVORITE_SESSIONS = "Get user's favorite sessions";
        public static final String UPDATE_SESSION = "Update session details";
        public static final String TOGGLE_FAVORITE = "Toggle session favorite status";
        public static final String DELETE_SESSION = "Delete a session";
    }

    // Tag Descriptions
    public static final class TagDescription {
        public static final String CHAT_MESSAGES = "Manage chat messages within sessions";
        public static final String CHAT_SESSIONS = "Manage chat sessions";
    }

    // Success Messages
    public static final class SuccessMessages {
        public static final String MESSAGE_SENT = "Message sent successfully";
        public static final String SESSION_CREATED = "Session created successfully";
        public static final String SESSION_UPDATED = "Session updated successfully";
        public static final String SESSION_DELETED = "Session deleted successfully";
        public static final String MESSAGE_DELETED = "Message deleted successfully";
        public static final String FAVORITE_UPDATED = "Favorite status updated";
    }

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}