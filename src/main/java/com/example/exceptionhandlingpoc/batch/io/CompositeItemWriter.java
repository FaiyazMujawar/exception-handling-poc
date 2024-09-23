package com.example.exceptionhandlingpoc.batch.io;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import lombok.SneakyThrows;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.Arrays;
import java.util.List;

public class CompositeItemWriter<T> implements ItemWriter<LineItem<T>> {
    private final List<ClassifierItemWriter<T>> classifierWriters;

    public CompositeItemWriter(ClassifierItemWriter<T>... classifierWriters) {
        this.classifierWriters = Arrays.asList(classifierWriters);
    }

    @Override
    @SneakyThrows
    public void write(Chunk<? extends LineItem<T>> chunk) {
        for (ClassifierItemWriter<T> writer : classifierWriters) {
            writer.write(chunk);
        }
    }
}