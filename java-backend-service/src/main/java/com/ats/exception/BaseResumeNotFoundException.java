package com.ats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an action requires an active Base Resume,
 * but none exists in the SQL database.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class BaseResumeNotFoundException extends RuntimeException {

    public BaseResumeNotFoundException(String message) {
        super(message);
    }

    public BaseResumeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}