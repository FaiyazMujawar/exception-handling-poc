package com.example.exceptionhandlingpoc.batch;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.example.exceptionhandlingpoc.batch.ExcelPattern.*;
import static java.util.Arrays.stream;

@Getter
@AllArgsConstructor
public enum ColumnFormat {
    STRING(STRING_PATTERN),
    INTEGER(INTEGER_PATTERN),
    DATE(DATE_PATTERN),
    TIME(TIME_PATTERN),
    PHONE(PHONE_PATTERN);

    private final String format;

    public static ColumnFormat from(String pattern) {
        return stream(values())
                .filter(cf -> cf.format.equals(pattern))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Not an enum value."));
    }
}