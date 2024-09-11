package com.example.exceptionhandlingpoc.api.exceptions;

import com.example.exceptionhandlingpoc.api.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Request param missing exception handler
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMissingRequestParameterException(MissingServletRequestParameterException e) {
        return ErrorResponse.builder()
                .code("BAD_REQUEST")
                .message(e.getMessage())
                .build();
    }

    // URI not found exception handler
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handlePathNotFound(HttpServletRequest request) {
        return ErrorResponse.builder()
                .code("NOT_FOUND")
                .message("Path {%s} not found".formatted(request.getRequestURI()))
                .build();
    }

    // Handler for validation failure using @Valid annotation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ErrorResponse.builder()
                .code("BAD_REQUEST")
                .message("Validation Errors")
                .errors(errors)
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(Exception e) {
        return ErrorResponse.builder()
                .code("BAD_REQUEST")
                .message(e.getMessage())
                .build();
    }

    // Handler for any exception not mapped
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.info("Exception Class: {}", e.getClass());
        return ErrorResponse.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message(e.getMessage())
                .build();
    }
}