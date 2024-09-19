package com.example.exceptionhandlingpoc.batch.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

public class ReflectionUtils {
    public static final String ERRORS = "Errors";

    public static final Function<Field, String> COL_HEADER = field -> {
        JsonProperty annotation = field.getDeclaredAnnotation(JsonProperty.class);
        return nonNull(annotation) ? annotation.value() : field.getName();
    };

    public static <T> List<String> getColumnHeaders(Class<T> type) {
        return stream(type.getDeclaredFields())
                .map(COL_HEADER)
                .filter(col -> !col.equals(ERRORS))
                .collect(toList());
    }
}