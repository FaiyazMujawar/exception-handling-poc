package com.rheumera.poc.batch.utils;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ParseUtils {

    @SneakyThrows
    public static <T> ParseResult<T> parse(@NonNull JsonMapper mapper,
                                           @NonNull Class<T> type,
                                           @NonNull Map<String, String> data) {
        var errors = new HashMap<String, String>();
        var reader = mapper.readerFor(type).withHandler(new DeserializationProblemHandler() {
            @Override
            public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
                errors.put(ctxt.getParser().currentName(), "Could not parse value: %s".formatted(valueToConvert));
                return null;
            }
        });
        var converted = reader.<T>readValue(mapper.writeValueAsBytes(data));
        return new ParseResult<>(converted, errors);
    }

    public record ParseResult<T>(T value, Map<String, String> errors) {}
}