package com.example.exceptionhandlingpoc.batch.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class LineItem<T> {
    private int row;
    private boolean isValid;
    private T item;
    @Builder.Default
    private Map<String, String> raw = Map.of();
}