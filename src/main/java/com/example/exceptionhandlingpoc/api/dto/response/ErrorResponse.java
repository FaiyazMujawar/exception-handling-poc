package com.example.exceptionhandlingpoc.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

@Data
@Builder
public class ErrorResponse {
    private String code;
    private String message;
    @Builder.Default
    private Collection<String> errors = new HashSet<>();
}