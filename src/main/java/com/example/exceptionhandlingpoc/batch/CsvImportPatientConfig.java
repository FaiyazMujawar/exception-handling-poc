package com.example.exceptionhandlingpoc.batch;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.io.readers.ExtendedFlatFileItemReader;
import com.example.exceptionhandlingpoc.batch.io.writers.ClassifierItemWriter;
import com.example.exceptionhandlingpoc.batch.io.writers.ExtendedFlatFileItemWriter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.nio.file.Files;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.file.Paths.get;
import static java.util.Objects.isNull;
import static org.apache.poi.util.StringUtil.isBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsvImportPatientConfig {
    private static final String DELIMITER = ",";
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Value("classpath:mappings/mappings-1.json")
    private Resource mappingFile;

    @Bean
    public JobListener<PatientImportDto> listener() {
        return new JobListener<>();
    }

    @Bean(name = "CSV_PATIENT_IMPORT_READER")
    @StepScope
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public ExtendedFlatFileItemReader<PatientImportDto> reader(@Value("#{jobParameters['inputFilePath']}") String filePath) {
        if (isBlank(filePath) || !Files.exists(get(filePath))) {
            throw new RuntimeException("File Not found");
        }
        var reader = ExtendedFlatFileItemReader.<PatientImportDto>builder()
                .mapper(jsonMapper)
                .resource(new PathResource(filePath))
                .mappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class))
                .delimiter(DELIMITER)
                .build();
        reader.setTargetType(PatientImportDto.class);
        return reader;
    }

    @Bean(name = "CSV_PATIENT_IMPORT_PROCESSOR")
    @StepScope
    public ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> processor() {
        return item -> {
            var errors = validator.validate(item.getItem());
            if (!errors.isEmpty()) {
                var messages = errors.stream()
                        .collect(Collectors.toMap(v -> getJsonPropertyName(v.getPropertyPath().toString()), ConstraintViolation::getMessage));
                messages.forEach(item.getErrors()::putIfAbsent);
                item.setValid(item.getErrors().isEmpty());
            }
            return item;
        };
    }

    @Bean(name = "CSV_PATIENT_IMPORT_SUCCESS_WRITER")
    @StepScope
    public ItemWriter<LineItem<PatientImportDto>> successWriter() {
        return chunk -> chunk.forEach(item -> System.out.println("Item = " + item));
    }

    @Bean(name = "CSV_PATIENT_IMPORT_ERROR_WRITER")
    @SneakyThrows
    @StepScope
    @SuppressWarnings("unchecked")
    public ExtendedFlatFileItemWriter<PatientImportDto> errorWriter(@Value("#{jobParameters['errorFilePath']}") String filePath) {
        var writer = ExtendedFlatFileItemWriter.<PatientImportDto>builder()
                .resource(new PathResource(get(filePath)))
                .mappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class))
                .delimiter(DELIMITER)
                .build();
        writer.open(new ExecutionContext());
        return writer;
    }

    @Bean(name = "CSV_PATIENT_IMPORT_STATUS_WRITER")
    @SneakyThrows
    @StepScope
    @SuppressWarnings("unchecked")
    public ExtendedFlatFileItemWriter<PatientImportDto> statusWriter(@Value("#{jobParameters['statusFilePath']}") String filePath) {
        var writer = ExtendedFlatFileItemWriter.<PatientImportDto>builder()
                .resource(new PathResource(get(filePath)))
                .mappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class))
                .delimiter(DELIMITER)
                .build();
        writer.open(new ExecutionContext());
        return writer;
    }

    @Bean(name = "CSV_PATIENT_IMPORT_CLASSIFIER_WRITER")
    public ClassifierItemWriter<PatientImportDto> classifierWriter(
            @Qualifier("CSV_PATIENT_IMPORT_ERROR_WRITER") ExtendedFlatFileItemWriter<PatientImportDto> errorWriter,
            @Qualifier("CSV_PATIENT_IMPORT_SUCCESS_WRITER") ItemWriter<LineItem<PatientImportDto>> successWriter
    ) {
        return new ClassifierItemWriter<>(item -> item.getErrors().isEmpty() ? successWriter : errorWriter);
    }

    @Bean(name = "CSV_PATIENT_IMPORT_COMPOSITE_WRITER")
    public ItemWriter<LineItem<PatientImportDto>> compositeItemWriter(
            @Qualifier("CSV_PATIENT_IMPORT_CLASSIFIER_WRITER") ClassifierItemWriter<PatientImportDto> classifierWriter,
            @Qualifier("CSV_PATIENT_IMPORT_STATUS_WRITER") ExtendedFlatFileItemWriter<PatientImportDto> statusFileWriter
    ) {
        return chunk -> {
            classifierWriter.write(chunk);
            statusFileWriter.write(chunk);
        };
    }

    @Bean(name = "CSV_PATIENT_IMPORT_STEP_LISTENER")
    @StepScope
    public StepExecutionListener stepExecutionListener(
            @Qualifier("CSV_PATIENT_IMPORT_ERROR_WRITER") ExtendedFlatFileItemWriter<PatientImportDto> errorWriter,
            @Qualifier("CSV_PATIENT_IMPORT_STATUS_WRITER") ExtendedFlatFileItemWriter<PatientImportDto> statusWriter) {
        return new StepExecutionListener() {
            @Override
            public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
                System.out.println("afterStep called");
                try {
                    errorWriter.close();
                    statusWriter.close();
                    return ExitStatus.COMPLETED;
                } catch (Exception e) {
                    return ExitStatus.FAILED;
                }
            }
        };
    }

    @Bean(name = "CSV_PATIENT_IMPORT_STEP")
    @JobScope
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     @Qualifier("CSV_PATIENT_IMPORT_READER") ExtendedFlatFileItemReader<PatientImportDto> reader,
                     @Qualifier("CSV_PATIENT_IMPORT_PROCESSOR") ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> processor,
                     @Qualifier("CSV_PATIENT_IMPORT_COMPOSITE_WRITER") ItemWriter<LineItem<PatientImportDto>> writer,
                     @Qualifier("CSV_PATIENT_IMPORT_STEP_LISTENER") StepExecutionListener listener
    ) {
        return new StepBuilder("CSV_PATIENT_IMPORT_STEP", jobRepository)
                .<LineItem<PatientImportDto>, LineItem<PatientImportDto>>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(listener)
                .build();
    }

    @Bean
    @Qualifier("CSV_PATIENT_IMPORT_JOB")
    public Job job(JobRepository jobRepository,
                   JobListener<PatientImportDto> listener,
                   @Qualifier("CSV_PATIENT_IMPORT_STEP") Step step) {
        return new JobBuilder("CSV_PATIENT_IMPORT_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .listener(listener)
                .build();
    }

    @SneakyThrows
    private String getJsonPropertyName(String fieldName) {
        var field = PatientImportDto.class.getDeclaredField(fieldName);
        var annotation = field.getDeclaredAnnotation(JsonProperty.class);
        if (isNull(annotation)) {
            throw new Exception("Field {%s} does not have @JsonProperty annotation".formatted(fieldName));
        }
        return annotation.value();
    }
}