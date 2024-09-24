package com.rheumera.poc.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.HashSet;

import com.rheumera.poc.constants.ExceptionCode;

@Data
@Builder
public class ErrorResponse {
    private ExceptionCode code;
    private String message;
    @Builder.Default
    private Collection<String> errors = new HashSet<>();
}