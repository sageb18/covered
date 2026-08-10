package com.sageb18.covered.controller;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Turns bad input into 400s. Without this, the duplicate-id checks in SchedulingService
 * would surface as 500s and read to the client as "the server is broken" rather than
 * "your request is wrong".
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ApiError(String message, List<String> details) {
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleIllegalArgument(IllegalArgumentException exception) {
        return new ApiError(exception.getMessage(), List.of());
    }

    /** Raised by @Valid. Flattens the field errors into messages the UI can display as-is. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidBody(MethodArgumentNotValidException exception) {
        List<String> details = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .distinct()
                .sorted()
                .toList();
        return new ApiError("Request is not valid", details);
    }
}
