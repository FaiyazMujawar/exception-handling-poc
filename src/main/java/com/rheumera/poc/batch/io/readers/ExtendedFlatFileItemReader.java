package com.rheumera.poc.batch.io.readers;

import com.fasterxml.jackson.databind.ObjectReader;
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

import static com.rheumera.poc.batch.utils.ReflectionUtils.getColumnHeaders;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("all")
public class ExtendedFlatFileItemReader<T> extends FlatFileItemReader<LineItem<T>> {
    private final Resource resource;
    private final String delimiter;
    private final Map<String, String> columnMappings;
    private final ObjectReader reader;
    private final CustomProblemHandler problemHandler;
    private final JsonMapper mapper;
    private Class<T> targetType;

    @Builder
    public ExtendedFlatFileItemReader(JsonMapper mapper, Resource resource, Class<T> type, String delimiter, Map<String, String> mappings) {
        this.targetType = type;
        this.resource = resource;
        this.delimiter = delimiter;
        this.mapper = mapper;
        this.problemHandler = new CustomProblemHandler();
        this.reader = mapper.readerFor(this.targetType).withHandler(this.problemHandler);
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

    private LineMapper<LineItem<T>> getLineMapper() {
        var lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(this.delimiter);
        lineTokenizer.setNames(getHeaders());

        var fieldSetMapper = getFieldSetMapper();

        return new LineMapper<LineItem<T>>() {
            @Override
            public LineItem<T> mapLine(String line, int lineNumber) throws Exception {
                var item = fieldSetMapper.mapFieldSet(lineTokenizer.tokenize(line));
                item.setRow(lineNumber);
                return item;
            }
        };
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
            return parseObject(transformed);
        };
    }

    @SneakyThrows
    private LineItem<T> parseObject(Map<String, String> data) {
        T converted = null;
        this.problemHandler.clearErrors();
        converted = this.reader.<T>readValue(mapper.writeValueAsString(data));
        return LineItem.<T>builder()
                .item(converted)
                .errors(this.problemHandler.getErrors())
                .raw(data)
                .build();
    }

}