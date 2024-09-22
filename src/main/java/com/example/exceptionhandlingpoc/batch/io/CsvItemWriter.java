package com.example.exceptionhandlingpoc.batch.io;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import lombok.Builder;
import lombok.SneakyThrows;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemWriterItemStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

public class CsvItemWriter<T> implements ResourceAwareItemWriterItemStream<LineItem<T>> {
    private final Character delimiter;
    private final Map<String, String> columnMappings;
    private final List<String> headers;
    private Resource resource;
    private ICSVWriter writer;

    @Builder
    public CsvItemWriter(Character delimiter, Map<String, String> mappings, Resource resource) {
        this.delimiter = delimiter;
        this.columnMappings = mappings.entrySet().stream().collect(toMap(Entry::getValue, Entry::getKey));
        this.headers = List.copyOf(this.columnMappings.keySet());
        this.resource = resource;
    }

    @Override
    public void setResource(@NonNull WritableResource resource) {
        this.resource = resource;
    }

    @Override
    @SneakyThrows
    public void open(@NonNull ExecutionContext executionContext) {
        Assert.state(Objects.nonNull(this.resource), "Resource must be provided");
        this.writer = getWriter();
        this.writer.writeNext(getHeaderRow().toArray(String[]::new));
    }

    @Override
    @SneakyThrows
    public void close() throws ItemStreamException {
        ResourceAwareItemWriterItemStream.super.close();
        this.writer.flush();
    }

    @Override
    @SneakyThrows
    public void write(Chunk<? extends LineItem<T>> chunk) {
        var items = chunk.getItems();
        items.forEach(item -> this.writer.writeNext(getData(item).toArray(String[]::new)));
    }

    // Helpers

    private List<String> getData(LineItem<T> item) {
        var data = new ArrayList<String>();
        data.add(String.valueOf(item.getRow()));
        var raw = item.getRaw();
        this.headers.forEach(header -> data.add(raw.get(header)));
        data.add(String.valueOf(item.getErrors().isEmpty() ? "SUCCESS" : "FAILED"));
        data.add(String.join("\n", item.getErrors().values()));
        return data;
    }

    @SneakyThrows
    private ICSVWriter getWriter() {
        var file = this.resource.getFile();
        if (!file.exists()) file.createNewFile();
        return new CSVWriterBuilder(new FileWriter(file))
                .withParser(new CSVParserBuilder().withSeparator(this.delimiter).build())
                .build();
    }

    private List<String> getHeaderRow() {
        var headers = new ArrayList<String>();
        headers.add("Original Row #");
        headers.addAll(this.headers);
        headers.add("Status");
        headers.add("Error");
        return headers;
    }
}