package com.rheumera.poc.batch.io.readers;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class CustomProblemHandler extends DeserializationProblemHandler {
    private final Map<String, String> errors = new HashMap<>();

    @Override
    public Object handleWeirdStringValue(DeserializationContext ctxt, Class<?> targetType, String valueToConvert, String failureMsg) throws IOException {
        this.errors.putIfAbsent(ctxt.getParser().currentName(), "Could not parse value: " + valueToConvert);
        return null;
    }

    public void clearErrors() {
        this.errors.clear();
    }
}