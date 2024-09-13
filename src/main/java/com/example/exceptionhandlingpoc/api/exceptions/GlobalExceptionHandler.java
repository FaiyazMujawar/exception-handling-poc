package com.example.exceptionhandlingpoc.api.exceptions;

import com.example.exceptionhandlingpoc.api.dto.response.ErrorResponse;
import com.example.exceptionhandlingpoc.constants.ExceptionCode;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static com.example.exceptionhandlingpoc.constants.ExceptionCode.*;
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
                .code(MISSING_REQUEST_PARAMETER)
                .message(e.getMessage())
                .build();
    }

    // URI not found exception handler
    @ExceptionHandler(NoResourceFoundException.class)
    public ErrorResponse handlePathNotFound(HttpServletRequest request) {
        return ErrorResponse.builder()
                .code(INVALID_URI)
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
                .code(VALIDATION_EXCEPTION)
                .message("Validation Errors")
                .errors(errors)
                .build();
    }

    // Handler for validation failure using validator.validate method
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleConstraintViolationException(ConstraintViolationException e) {
        var errors = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        return ErrorResponse.builder()
                .code(VALIDATION_EXCEPTION)
                .message("Validation failed")
                .errors(errors)
                .build();
    }

    // Handler for JSON message parsing error
    @ExceptionHandler({JsonParseException.class, JsonMappingException.class, MismatchedInputException.class})
    public ErrorResponse handleJsonParseException(Exception e) {
        return ErrorResponse.builder()
                .code(INVALID_REQUEST_BODY)
                .message(e.getLocalizedMessage())
                .build();
    }

    // Handler for JSON message parsing error, may not handle SQS
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadableException(Exception e) {
        e.printStackTrace();
        return ErrorResponse.builder()
                .code(INVALID_REQUEST_BODY)
                .message(e.getLocalizedMessage())
                .build();
    }

    // Handler for when transaction could not commit
    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleTransactionSystemException(TransactionSystemException e) {
        return ErrorResponse.builder()
                .code(SQL_EXCEPTION)
                .message(e.getMostSpecificCause().getMessage())
                .build();
    }

    // Handler for when data integrity is violated, like duplicate primary key, or foreign key, or unique key
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ErrorResponse.builder()
                .code(SQL_EXCEPTION)
                .message(e.getMostSpecificCause().getMessage())
                .build();
    }

    // Handler for invalid SQL
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleInvalidSqlException(InvalidDataAccessResourceUsageException e) {
        return ErrorResponse.builder()
                .code(SQL_EXCEPTION)
                .message(e.getMostSpecificCause().getMessage())
                .build();
    }

    // Handler for invalid SQL
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(BAD_REQUEST)
    public ErrorResponse handleSqlException(DataAccessException e) {
        return ErrorResponse.builder()
                .code(SQL_EXCEPTION)
                .message(e.getMostSpecificCause().getMessage())
                .build();
    }

    // Handler for any exception not mapped
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Exception e) {
        log.error(e.getLocalizedMessage(), e);
        return ErrorResponse.builder()
                .code(ExceptionCode.INTERNAL_SERVER_ERROR)
                .message(e.getLocalizedMessage())
                .build();
    }
}