package com.example.exceptionhandlingpoc.batch.io.writers;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import lombok.Builder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.FieldExtractor;
import org.springframework.batch.item.file.transform.LineAggregator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

public class ExtendedFlatFileItemWriter<T> extends FlatFileItemWriter<LineItem<T>> {
    private final String delimiter;
    private final Map<String, String> columnMappings;
    private final List<String> headers;
    private Resource resource;

    @Builder
    public ExtendedFlatFileItemWriter(String delimiter, Map<String, String> mappings, Resource resource) {
        this.delimiter = delimiter;
        this.resource = resource;
        this.columnMappings = mappings.entrySet().stream().collect(toMap(Entry::getValue, Entry::getKey));
        this.headers = List.copyOf(this.columnMappings.keySet());
        super.setResource((WritableResource) this.resource);
        super.setLineAggregator(getLineAggregator());
        super.setHeaderCallback(writer -> writer.write(String.join(this.delimiter, getHeaders())));
    }

    private LineAggregator<LineItem<T>> getLineAggregator() {
        DelimitedLineAggregator<LineItem<T>> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(this.delimiter);
        lineAggregator.setFieldExtractor(getFieldExtractor());

        return lineAggregator;
    }

    private List<String> getHeaders() {
        var headers = new ArrayList<String>();
        headers.add("Original Row #");
        headers.addAll(this.headers);
        headers.add("Import Status");
        headers.add("Error");
        return headers;
    }

    private FieldExtractor<LineItem<T>> getFieldExtractor() {
        return item -> {
            var data = new ArrayList<String>();
            data.add(String.valueOf(item.getRow()));
            var raw = item.getRaw();
            this.headers.forEach(header -> data.add(raw.get(header)));
            data.add(item.getErrors().isEmpty() ? "SUCCESS" : "FAILED");
            data.add(String.join(",\n", item.getErrors().values()));
            return data.stream().map("\"%s\""::formatted).toArray();
        };
    }
}