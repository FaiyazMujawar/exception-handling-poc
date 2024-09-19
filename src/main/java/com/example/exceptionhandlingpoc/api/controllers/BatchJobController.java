package com.example.exceptionhandlingpoc.api.controllers;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.io.PoiItemReader;
import com.example.exceptionhandlingpoc.utils.PathUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {
    private final JsonMapper jsonMapper;
    private final Validator validator;

    @Value("classpath:mappings/mappings-1.json")
    private Resource mappingFile;

    @PostMapping
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public List<LineItem<PatientImportDto>> runBatchJob(@RequestParam("file") @NotNull MultipartFile file) {
        var id = UUID.randomUUID().toString();
        var inputFilePath = Paths.get(PathUtils.getInputDirPath().toString(), file.getOriginalFilename());
        file.transferTo(inputFilePath);
        var reader = new PoiItemReader<PatientImportDto>(jsonMapper);
        reader.setResource(new PathResource(inputFilePath.toString()));
        reader.setTargetType(PatientImportDto.class);
        reader.setColumnMappings(jsonMapper.readValue(mappingFile.getInputStream(), Map.class));
        reader.open(new ExecutionContext());
        var list = new ArrayList<LineItem<PatientImportDto>>();
        while (true) {
            var item = reader.read();
            if (item == null) break;
            if (item.getItem() != null) {
                var errors = validator.validate(item.getItem());
                if (!errors.isEmpty()) {
                    var messages = errors.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.toSet());
                    item.setErrors(messages);
                }
                item.setValid(errors.isEmpty());
            }
            list.add(item);
        }
        return list;
    }

}