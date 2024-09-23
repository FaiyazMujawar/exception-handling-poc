package com.example.exceptionhandlingpoc.batch.io;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.utils.ParseUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.stream.Collectors.toMap;

@SuppressWarnings("all")
public class ExtendedFlatFileItemReader<T> extends FlatFileItemReader<LineItem<T>> {
    private final Resource resource;
    private final String delimiter;
    private final Map<String, String> columnMappings;
    private final JsonMapper mapper;
    private Class<T> targetType;

    @Builder
    public ExtendedFlatFileItemReader(Resource resource, String delimiter, Map<String, String> mappings, JsonMapper mapper) {
        this.resource = resource;
        this.delimiter = delimiter;
        this.columnMappings = mappings.entrySet().stream()
                .collect(toMap(Entry::getValue, Entry::getKey));
        this.mapper = mapper;
        super.setResource(this.resource);
        super.setLineMapper(getLineMapper());
        super.setLinesToSkip(1);
    }

    public void setTargetType(Class<T> type) {
        this.targetType = type;
    }

    private LineMapper<LineItem<T>> getLineMapper() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(this.delimiter);
        lineTokenizer.setNames(getHeaders());

        DefaultLineMapper<LineItem<T>> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(getFieldSetMapper());

        return lineMapper;
    }

    @SneakyThrows
    private String[] getHeaders() {
        var br = new BufferedReader(new InputStreamReader(this.resource.getInputStream()));
        var headerLine = br.readLine();
        br.close();
        if (headerLine == null) {
            throw new RuntimeException("No header line found");
        }
        var headers = headerLine.split(",");
        if (!this.columnMappings.keySet().containsAll(Set.of(headers))) {
            throw new RuntimeException("Headers must contain all columns");
        }
        return headers;
    }

    private FieldSetMapper<LineItem<T>> getFieldSetMapper() {
        return fieldSet -> {
            var raw = new HashMap<String, String>();
            fieldSet.getProperties().forEach((header, value) -> {
                raw.put(header.toString(), value.toString());
            });
            var transformed = raw.entrySet().stream().collect(
                    toMap(entry -> this.columnMappings.get(entry.getKey()), Map.Entry::getValue)
            );
            var result = ParseUtils.parseToType(this.mapper, this.targetType, transformed);
            return LineItem.<T>builder()
                    .item(result.parsed())
                    .errors(result.parseErrors())
                    .raw(raw)
                    .build();
        };
    }

}