package com.example.exceptionhandlingpoc.batch;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.io.CsvItemReader;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
    private static final Character DELIMITER = ',';
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Value("classpath:mappings/mappings-1.json")
    private Resource mappingFile;

    @Bean
    public JobListener<PatientImportDto> listener() {
        return new JobListener<>();
    }

    @Bean
    @StepScope
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public CsvItemReader<PatientImportDto> csvPatientItemReader(@Value("#{jobParameters['inputFilePath']}") String filePath) {
        if (isBlank(filePath) || !Files.exists(get(filePath))) {
            throw new RuntimeException("File Not found");
        }
        var reader = CsvItemReader.<PatientImportDto>builder()
                .mapper(jsonMapper)
                // .resource(new PathResource(filePath))
                .targetType(PatientImportDto.class)
                .columnMappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class))
                .delimiter(DELIMITER)
                .build();
        reader.setResource(new PathResource(filePath));
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> patientLineItemProcessor() {
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

    @Bean
    @StepScope
    public ItemWriter<LineItem<PatientImportDto>> patientItemWriter() {
        return chunk -> chunk.forEach(item -> System.out.println("Item = " + item));
    }

    @Bean
    @JobScope
    public Step csvPatientImportStep(JobRepository jobRepository,
                                     PlatformTransactionManager transactionManager,
                                     CsvItemReader<PatientImportDto> reader,
                                     @Qualifier("patientLineItemProcessor") ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> processor,
                                     @Qualifier("patientItemWriter") ItemWriter<LineItem<PatientImportDto>> writer
    ) {
        return new StepBuilder("CSV_PATIENT_IMPORT_STEP", jobRepository)
                .<LineItem<PatientImportDto>, LineItem<PatientImportDto>>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @Qualifier("CSV_PATIENT_IMPORT_JOB")
    public Job patientImportJob(JobRepository jobRepository,
                                JobListener<PatientImportDto> listener,
                                @Qualifier("csvPatientImportStep") Step step) {
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