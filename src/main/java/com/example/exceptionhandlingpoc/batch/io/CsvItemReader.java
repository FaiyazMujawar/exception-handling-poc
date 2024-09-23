package com.example.exceptionhandlingpoc.batch.io;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.Builder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.format.datetime.standard.DateTimeFormatterFactory;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

// TODO: use OpenCSV

@Slf4j
@SuppressWarnings("unused")
public class CsvItemReader<T> implements ResourceAwareItemReaderItemStream<LineItem<T>> {
    private final JsonMapper mapper;
    private final Class<T> targetType;
    private final Map<String, String> columnMappings;
    private final Character delimiter;
    private Resource resource;
    private List<String> headers;
    private int row;
    private CSVReader reader;

    @Builder
    public CsvItemReader(JsonMapper mapper, Class<T> targetType, Map<String, String> columnMappings, Character delimiter, Resource resource) {
        Assert.state(nonNull(targetType), "Target type must be provided");
        Assert.state(nonNull(resource), "Resource must be provided");
        Assert.state(nonNull(columnMappings), "Column mappings must be provided");
        Assert.state(nonNull(delimiter), "Delimiter must be provided");
        Assert.state(nonNull(mapper), "Mapper must be provided");

        this.mapper = mapper;
        this.targetType = targetType;
        this.delimiter = delimiter;
        this.columnMappings = columnMappings.entrySet().stream().collect(toMap(
                Map.Entry::getValue, Map.Entry::getKey
        ));
        this.resource = resource;
    }

    @Override
    public void setResource(@NonNull Resource resource) {
        this.resource = resource;
    }

    @Override
    @SneakyThrows
    public void close() throws ItemStreamException {
        ResourceAwareItemReaderItemStream.super.close();
        this.resource = null;
        this.reader.close();
    }

    @Override
    @SneakyThrows
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        Assert.state(this.targetType != null, "Target type must be provided.");
        Assert.state(this.resource != null, "Resource must be provided.");
        if (this.resource.contentLength() == 0) {
            throw new RuntimeException("File Cannot be empty");
        }
        this.reader = getReader();
        this.headers = getHeaders();
        if (!this.columnMappings.keySet().containsAll(this.headers)) {
            throw new RuntimeException("Column mappings must contain all headers");
        }
        this.row = 1;
    }

    @Override
    @SneakyThrows
    public LineItem<T> read() {
        String[] line;
        while (true) {
            line = reader.readNext();
            if (isNull(line)) break;
            if (line.length == 0) continue;
            final var split = line;
            var map = range(0, headers.size()).boxed().collect(toMap(headers::get, i -> split[i]));
            var transformed = map.entrySet().stream().collect(
                    toMap(entry -> this.columnMappings.get(entry.getKey()), Map.Entry::getValue)
            );
            var errors = new HashMap<String, String>();
            var item = convertToJavaType(transformed, errors);
            return LineItem.<T>builder()
                    .row(this.row++)
                    .item(item)
                    .raw(map)
                    .errors(errors)
                    .isValid(errors.isEmpty())
                    .build();
        }
        return null;
    }

    @SneakyThrows
    private T convertToJavaType(Map<String, String> data, HashMap<String, String> errors) {
        Assert.state(nonNull(this.mapper), "Mapper must be provided");
        var object = this.targetType.getDeclaredConstructor().newInstance();
        for (var field : this.targetType.getDeclaredFields()) {
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
        return object;
    }

    @SneakyThrows
    private <K> K convert(Class<K> type, String value, String format) {
        if (List.of(LocalDate.class, Instant.class, LocalTime.class).contains(type)) {
            // Maybe use default datetime format?
            var formatter = new DateTimeFormatterFactory(format).createDateTimeFormatter();
            var method = type.getMethod("parse", CharSequence.class, DateTimeFormatter.class);
            return type.cast(method.invoke(null, value, formatter));
        }
        throw new RuntimeException("JsonFormat not supported here yet");
    }

    @SneakyThrows
    private List<String> getHeaders() {
        String[] line;
        do {
            line = this.reader.readNext();
            if (isNull(line)) throw new RuntimeException("Parsing failed: Empty file");
        } while (line.length == 0);
        return List.of(line);
    }

    @SneakyThrows
    private CSVReader getReader() {
        return new CSVReaderBuilder(new FileReader(this.resource.getFile()))
                .withCSVParser(new CSVParserBuilder().withSeparator(this.delimiter).build())
                .build();
    }

}