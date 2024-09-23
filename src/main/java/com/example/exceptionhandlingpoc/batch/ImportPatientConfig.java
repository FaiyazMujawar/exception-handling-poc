package com.example.exceptionhandlingpoc.batch;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.io.readers.PoiItemReader;
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
import static org.apache.poi.util.StringUtil.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImportPatientConfig {
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Value("classpath:mappings/mappings-1.json")
    private Resource mappingFile;

    @Bean
    @StepScope
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public PoiItemReader<PatientImportDto> patientImportDtoPoiItemReader(@Value("#{jobParameters['inputFilePath']}") String filePath) {
        var poiItemReader = new PoiItemReader<PatientImportDto>(jsonMapper);
        if (isNotBlank(filePath) && Files.exists(get(filePath))) {
            poiItemReader.setResource(new PathResource(filePath));
        }
        poiItemReader.setTargetType(PatientImportDto.class);
        poiItemReader.setColumnMappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class));
        return poiItemReader;
    }

    @Bean
    @StepScope
    public ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> lineItemProcessor() {
        return item -> {
            if (item.getItem() != null) {
                var errors = validator.validate(item.getItem());
                if (!errors.isEmpty()) {
                    var messages = errors.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toSet());
                    // item.setErrors(messages);
                }
                item.setValid(errors.isEmpty());
            }
            return item;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<LineItem<PatientImportDto>> patientImportDtoItemWriter() {
        return chunk -> chunk.forEach(item -> log.info("Item: {}", item));
    }

    @Bean
    @JobScope
    public Step patientImportStep(JobRepository jobRepository,
                                  @Qualifier("poiItemReader") PoiItemReader<PatientImportDto> reader,
                                  @Qualifier("lineItemProcessor") ItemProcessor<LineItem<PatientImportDto>, LineItem<PatientImportDto>> lineItemProcessor,
                                  @Qualifier("patientImportDtoItemWriter") ItemWriter<LineItem<PatientImportDto>> itemItemWriter,
                                  PlatformTransactionManager transactionManager) {
        return new StepBuilder("patientImportStep", jobRepository)
                .<LineItem<PatientImportDto>, LineItem<PatientImportDto>>chunk(10, transactionManager)
                .reader(reader)
                .processor(lineItemProcessor)
                .writer(itemItemWriter)
                .build();
    }

    @Bean
    @Qualifier("IMPORT_PATIENT_JOB")
    public Job importPatientJob(JobRepository jobRepository,
                                @Qualifier("patientImportStep") Step step
    ) {
        return new JobBuilder("IMPORT_PATIENT_JOB", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }
}