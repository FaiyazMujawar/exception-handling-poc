package com.example.exceptionhandlingpoc.batch.io.writers;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.LinkedHashMap;

@RequiredArgsConstructor
public class ClassifierItemWriter<T> implements ItemWriter<LineItem<T>> {
    private final Classifier<LineItem<T>, ItemWriter<LineItem<T>>> classifier;
    private int total;
    private int success;
    private int failed;

    @Override
    public void write(@NonNull Chunk<? extends LineItem<T>> chunk) throws Exception {
        var writers = new LinkedHashMap<ItemWriter<LineItem<T>>, Chunk<LineItem<T>>>();
        for (LineItem<T> item : chunk) {
            var key = this.classifier.classify(item);
            if (!writers.containsKey(key)) {
                writers.put(key, new Chunk<>());
            }
            writers.get(key).add(item);
        }
        for (var entry : writers.entrySet()) {
            var writer = entry.getKey();
            var items = entry.getValue();
            if (writer instanceof CsvItemWriter<T>) failed += items.size();
            else success += items.size();
            total += items.size();
            writer.write(items);
        }
    }

    public void resetCounts() {
        this.total = 0;
        this.success = 0;
        this.failed = 0;
    }
}