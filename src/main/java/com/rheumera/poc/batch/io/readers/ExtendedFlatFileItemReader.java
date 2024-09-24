package com.rheumera.poc.batch.io.readers;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.rheumera.poc.batch.dto.LineItem;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.rheumera.poc.batch.utils.ParseUtils.parse;
import static com.rheumera.poc.batch.utils.ReflectionUtils.getColumnHeaders;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("all")
public class ExtendedFlatFileItemReader<T> extends FlatFileItemReader<LineItem<T>> {
    private final Resource resource;
    private final String delimiter;
    private final Map<String, String> columnMappings;
    private final JsonMapper mapper;
    private Class<T> targetType;

    @Builder
    public ExtendedFlatFileItemReader(Resource resource, String delimiter, Class<T> type, Map<String, String> mappings, JsonMapper mapper) {
        this.mapper = mapper;
        this.resource = resource;
        this.delimiter = delimiter;
        this.targetType = type;
        var classFields = getColumnHeaders(type);
        if (!classFields.containsAll(mappings.keySet())) {
            throw new RuntimeException("Column mappings must contain all headers");
        }
        this.columnMappings = mappings.entrySet().stream()
                .collect(toMap(Entry::getValue, Entry::getKey));
        super.setResource(this.resource);
        super.setLineMapper(getLineMapper());
        super.setLinesToSkip(1);
    }

    public void setTargetType(Class<T> type) {
        this.targetType = type;
    }

    private LineMapper<LineItem<T>> getLineMapper() {
        var lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(this.delimiter);
        lineTokenizer.setNames(getHeaders());

        var fieldSetMapper = getFieldSetMapper();

        LineMapper<LineItem<T>> lineMapper = new LineMapper<LineItem<T>>() {
            @Override
            public LineItem<T> mapLine(String line, int lineNumber) throws Exception {
                var item = fieldSetMapper.mapFieldSet(lineTokenizer.tokenize(line));
                item.setRow(lineNumber);
                return item;
            }
        };
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
        var headers = headerLine.split(this.delimiter);
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
                    toMap(entry -> this.columnMappings.get(entry.getKey()), Entry::getValue)
            );
            var result = parse(this.mapper, this.targetType, transformed);
            return LineItem.<T>builder()
                    .item(result.value())
                    .errors(result.errors())
                    .raw(raw)
                    .build();
        };
    }

}