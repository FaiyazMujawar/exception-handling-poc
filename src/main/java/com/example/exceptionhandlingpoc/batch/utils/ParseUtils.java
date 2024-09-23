package com.example.exceptionhandlingpoc.batch.utils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.springframework.format.datetime.standard.DateTimeFormatterFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ParseUtils {
    @SneakyThrows
    public static <T> ParseResult<T> parseToType(@NonNull JsonMapper mapper,
                                                 @NonNull Class<T> type,
                                                 @NonNull Map<String, String> data) {
        var errors = new HashMap<String, String>();
        var object = type.getDeclaredConstructor().newInstance();
        for (var field : type.getDeclaredFields()) {
            field.setAccessible(true);
            var annotation = field.getDeclaredAnnotation(JsonProperty.class);
            if (isNull(annotation)) continue;
            var name = annotation.value();
            if (!data.containsKey(name)) continue;
            try {
                var format = field.getDeclaredAnnotation(JsonFormat.class);
                var value = data.get(name);
                var convertedValue = nonNull(format)
                        ? convert(field.getType(), value, format.pattern())
                        : mapper.convertValue(value, field.getType());
                field.set(object, convertedValue);
            } catch (Exception e) {
                errors.put(name, "Cannot parse value: %s".formatted(data.get(name)));
            }
        }
        return new ParseResult<>(object, errors);
    }

    @SneakyThrows
    private static <K> K convert(Class<K> type, String value, String format) {
        if (List.of(LocalDate.class, Instant.class, LocalTime.class).contains(type)) {
            // Maybe use default datetime format?
            var formatter = new DateTimeFormatterFactory(format).createDateTimeFormatter();
            var method = type.getMethod("parse", CharSequence.class, DateTimeFormatter.class);
            return type.cast(method.invoke(null, value, formatter));
        }
        throw new RuntimeException("JsonFormat not supported here yet");
    }

    public record ParseResult<T>(T parsed, Map<String, String> parseErrors) {}
}