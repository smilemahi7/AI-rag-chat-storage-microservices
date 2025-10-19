package com.chatbot.storage.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 *
 * The type Api response.
 *
 * @param <T> the type parameter
 */
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private LocalDateTime timestamp;

    /**
     * Success api response.
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the api response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Success api response.
     *
     * @param <T>     the type parameter
     * @param message the message
     * @param data    the data
     * @return the api response
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Error api response.
     *
     * @param <T>       the type parameter
     * @param message   the message
     * @param errorCode the error code
     * @return the api response
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }
}