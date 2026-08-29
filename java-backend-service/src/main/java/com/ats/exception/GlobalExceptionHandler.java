package com.ats.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource Not Found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorPayload(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(BaseResumeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBaseResumeNotFound(BaseResumeNotFoundException ex) {
        log.warn("Base Resume Missing: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildErrorPayload(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleFileProcessing(FileProcessingException ex) {
        log.error("File Processing Error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(buildErrorPayload(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage()));
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<Map<String, Object>> handleServiceCommunication(ServiceCommunicationException ex) {
        log.error("External Service Error (LLM / SMTP / Python Scraper): {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(buildErrorPayload(HttpStatus.BAD_GATEWAY, ex.getMessage()));
    }

    /**
     * Catches validation errors triggered by @Valid annotations in controllers
     * and returns itemized field errors for the Python client.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.warn("Payload validation failed for request: {}", ex.getBindingResult().getObjectName());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> payload = buildErrorPayload(HttpStatus.BAD_REQUEST, "Invalid request payload attributes.");
        payload.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
    }
    

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildErrorPayload(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal server error occurred."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
public ResponseEntity<Map<String, Object>> handleNoResourceFound(NoResourceFoundException ex) {
    log.warn("Path not found: {}", ex.getResourcePath());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(buildErrorPayload(HttpStatus.NOT_FOUND, "Resource or endpoint not found: /" + ex.getResourcePath()));
}

    private Map<String, Object> buildErrorPayload(HttpStatus status, String message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message != null ? message : "No detailed message provided");
        return payload;
    }
}