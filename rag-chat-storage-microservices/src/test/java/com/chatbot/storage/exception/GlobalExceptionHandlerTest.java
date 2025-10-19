package com.chatbot.storage.exception;

import com.chatbot.storage.dto.response.ApiResponse;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.Objects;

import static com.chatbot.storage.constants.AppConstants.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * The type Global exception handler test.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodParameter methodParameter;

    private ResourceNotFoundException resourceNotFoundException;
    private ValidationException validationException;
    private Exception generalException;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        resourceNotFoundException = new ResourceNotFoundException("Session not found");
        validationException = new ValidationException("Invalid input data");
        generalException = new RuntimeException("Unexpected error");
    }

    /**
     * Handle resource not found should return not found response.
     */
    @Test
    void handleResourceNotFound_ShouldReturnNotFoundResponse() {
        // When
        ResponseEntity<ApiResponse<Void>> result = 
                globalExceptionHandler.handleResourceNotFound(resourceNotFoundException);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals("Session not found", result.getBody().getMessage());
        assertEquals(ERROR_RESOURCE_NOT_FOUND, result.getBody().getErrorCode());
        assertNull(result.getBody().getData());
    }

    /**
     * Handle validation should return bad request response.
     */
    @Test
    void handleValidation_ShouldReturnBadRequestResponse() {
        // When
        ResponseEntity<ApiResponse<Void>> result = 
                globalExceptionHandler.handleValidation(validationException);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals("Invalid input data", result.getBody().getMessage());
        assertEquals(ERROR_VALIDATION_FAILED, result.getBody().getErrorCode());
        assertNull(result.getBody().getData());
    }

    /**
     * Handle method argument not valid should return validation errors.
     */
    @Test
    void handleMethodArgumentNotValid_ShouldReturnValidationErrors() {
        // Given
        MethodArgumentNotValidException ex = getMethodArgumentNotValidException();

        // When
        ResponseEntity<ApiResponse<Map<String, String>>> result = 
                globalExceptionHandler.handleMethodArgumentNotValid(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals("Validation failed", result.getBody().getMessage());
        assertEquals(ERROR_VALIDATION_FAILED, result.getBody().getErrorCode());
        
        Map<String, String> errors = result.getBody().getData();
        assertNotNull(errors);
        assertEquals(2, errors.size());
        assertEquals("Email is required", errors.get("email"));
        assertEquals("Name cannot be blank", errors.get("name"));
    }

    private @NotNull MethodArgumentNotValidException getMethodArgumentNotValidException() {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");

        FieldError fieldError1 = new FieldError("testObject", "email", "Email is required");
        FieldError fieldError2 = new FieldError("testObject", "name", "Name cannot be blank");
        bindingResult.addError(fieldError1);
        bindingResult.addError(fieldError2);

        return new MethodArgumentNotValidException(methodParameter, bindingResult);
    }

    /**
     * Handle method argument not valid with single field error should return single error.
     */
    @Test
    void handleMethodArgumentNotValid_WithSingleFieldError_ShouldReturnSingleError() {
        // Given
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        FieldError fieldError = new FieldError("testObject", "username", "Username is required");
        bindingResult.addError(fieldError);
        
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<ApiResponse<Map<String, String>>> result = 
                globalExceptionHandler.handleMethodArgumentNotValid(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        Map<String, String> errors = Objects.requireNonNull(result.getBody()).getData();
        assertEquals(1, errors.size());
        assertEquals("Username is required", errors.get("username"));
    }

    /**
     * Handle general should return internal server error response.
     */
    @Test
    void handleGeneral_ShouldReturnInternalServerErrorResponse() {
        // When
        ResponseEntity<ApiResponse<Void>> result = 
                globalExceptionHandler.handleGeneral(generalException);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals("An unexpected error occurred", result.getBody().getMessage());
        assertEquals(ERROR_INTERNAL_SERVER, result.getBody().getErrorCode());
        assertNull(result.getBody().getData());
    }

    /**
     * Handle general with null pointer exception should return generic error.
     */
    @Test
    void handleGeneral_WithNullPointerException_ShouldReturnGenericError() {
        // Given
        NullPointerException npe = new NullPointerException("Null value encountered");

        // When
        ResponseEntity<ApiResponse<Void>> result = 
                globalExceptionHandler.handleGeneral(npe);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("An unexpected error occurred", Objects.requireNonNull(result.getBody()).getMessage());
        assertEquals(ERROR_INTERNAL_SERVER, result.getBody().getErrorCode());
    }

    /**
     * Handle resource not found with different message should return custom message.
     */
    @Test
    void handleResourceNotFound_WithDifferentMessage_ShouldReturnCustomMessage() {
        // Given
        ResourceNotFoundException customException = new ResourceNotFoundException("User profile not found");

        // When
        ResponseEntity<ApiResponse<Void>> result = 
                globalExceptionHandler.handleResourceNotFound(customException);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
        assertEquals("User profile not found", Objects.requireNonNull(result.getBody()).getMessage());
        assertEquals(ERROR_RESOURCE_NOT_FOUND, result.getBody().getErrorCode());
    }
}