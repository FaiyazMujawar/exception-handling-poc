package com.example.exceptionhandlingpoc.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class LineItem<T> {
    private int row;
    private boolean isValid = true;
    private T item;
    @Builder.Default
    private Map<String, String> raw = Map.of();
    @Builder.Default
    private Set<String> errors = new HashSet<>();
}