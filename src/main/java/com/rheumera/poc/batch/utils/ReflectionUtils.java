package com.rheumera.poc.batch.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rheumera.poc.batch.ColumnFormat;
import com.rheumera.poc.batch.ExcelPattern;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.rheumera.poc.batch.ColumnFormat.from;
import static java.util.Arrays.stream;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ReflectionUtils {
    public static final String ERRORS = "Errors";

    public static final Function<Field, String> COL_HEADER = field -> {
        JsonProperty annotation = field.getDeclaredAnnotation(JsonProperty.class);
        return nonNull(annotation) ? annotation.value() : field.getName();
    };

    public static final Function<Field, ColumnFormat> COL_FORMAT = field -> {
        var annotation = field.getDeclaredAnnotation(JsonFormat.class);
        return annotation != null ? from(annotation.pattern()) : from(ExcelPattern.STRING_PATTERN);
    };

    public static <T> List<String> getColumnHeaders(Class<T> type) {
        return stream(type.getDeclaredFields())
                .map(COL_HEADER)
                .filter(col -> !col.equals(ERRORS))
                .collect(toList());
    }

    public static <T> Map<String, ColumnFormat> headerColumnFormats(Class<T> targetType) {
        return stream(targetType.getDeclaredFields())
                .collect(toMap(COL_HEADER, COL_FORMAT, (v1, v2) -> v2, LinkedHashMap::new));
    }
}