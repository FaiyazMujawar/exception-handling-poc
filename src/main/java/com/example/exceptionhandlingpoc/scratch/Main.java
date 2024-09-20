package com.example.exceptionhandlingpoc.scratch;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class Main {
    private static final JsonMapper mapper = JsonMapper.builder()
            .findAndAddModules()
            .build();

    public static void main(String[] args) throws Exception {
        Map<String, String> patientImportDtoMap = new HashMap<>();
        patientImportDtoMap.put("FIRST_NAME", null);
        patientImportDtoMap.put("LAST_NAME", "Doe");
        patientImportDtoMap.put("DATE_OF_BIRTH", "123");
        patientImportDtoMap.put("MRN", "12345");
        patientImportDtoMap.put("STATUS", "NO");
    }

    @SneakyThrows
    private static <T> LineItem<T> convertToJavaType(Map<String, String> data, Class<T> clazz) {
        var object = clazz.getDeclaredConstructor().newInstance();
        var errors = new HashMap<String, String>();
        for (var field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            var annotation = field.getDeclaredAnnotation(JsonProperty.class);
            if (annotation == null) continue;
            var name = annotation.value();
            if (!data.containsKey(name)) continue;
            try {
                field.set(object, mapper.convertValue(data.get(name), field.getType()));
            } catch (Exception e) {
                errors.put(name, "Cannot parse value: %s".formatted(data.get(name)));
            }
        }
        return LineItem.<T>builder()
                .item(object)
                .errors(errors)
                .build();
    }
}