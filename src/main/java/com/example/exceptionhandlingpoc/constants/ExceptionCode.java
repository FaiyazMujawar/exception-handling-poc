package com.example.exceptionhandlingpoc.constants;

import lombok.Getter;

@Getter
public enum ExceptionCode {
    INVALID_URI,
    MISSING_REQUEST_PARAMETER,
    INVALID_REQUEST_BODY,
    VALIDATION_EXCEPTION,
    SQL_EXCEPTION,
    INTERNAL_SERVER_ERROR
}