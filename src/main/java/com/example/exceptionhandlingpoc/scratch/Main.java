package com.example.exceptionhandlingpoc.scratch;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.example.exceptionhandlingpoc.batch.utils.ParseUtils;
import com.example.exceptionhandlingpoc.utils.PathUtils;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@SuppressWarnings("all")
public class Main {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
    private static final JsonMapper mapper = JsonMapper.builder()
            .defaultDateFormat(dateFormat)
            .findAndAddModules()
            .build();
    private static final Map<String, String> map = Map.of(
            "FIRST_NAME", "First Name",
            "LAST_NAME", "Last Name",
            "DATE_OF_BIRTH", "Date of Birth",
            "MRN", "Patient MRN",
            "STATUS", "Status"
    );
    private static final Resource resource;
    private static final Map<String, String> patientImportDtoMap = new HashMap<>();

    static {
        patientImportDtoMap.put("FIRST_NAME", "faiyaz");
        patientImportDtoMap.put("LAST_NAME", "mujawar");
        patientImportDtoMap.put("DATE_OF_BIRTH", "20/12/1999");
        patientImportDtoMap.put("MRN", null);
        patientImportDtoMap.put("STATUS", "ACT");
        try {
            resource = new PathResource(Paths.get(PathUtils.getInputDirPath().toString(), "test.csv"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    private static LineMapper<LineItem<PatientImportDto>> getLineMapper() {
        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        var br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
        var headerLine = br.readLine();
        if (headerLine == null) {
            throw new RuntimeException();
        }
        var headers = headerLine.split(",");
        lineTokenizer.setNames(headers);
        DefaultLineMapper<LineItem<PatientImportDto>> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> {
            fieldSet.getProperties().forEach((o, o2) -> {
                System.out.println("%s: %s".formatted(o, o2.toString()));
                if (o.toString().equals("Last Name")) {
                    System.out.println("o2 1st = " + o2.toString().charAt(0));
                }
            });
            System.out.println("-------");
            return LineItem.<PatientImportDto>builder().build();
        });
        return lineMapper;
    }

    public static void main(String[] args) throws Exception {
        var result = ParseUtils.parse(mapper, PatientImportDto.class, patientImportDtoMap);
        System.out.println("result = " + result.errors());
    }

    public static void main2(String[] args) {
        try {
            mapper.convertValue(patientImportDtoMap, PatientImportDto.class);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            if (ex.getCause() instanceof JsonMappingException e) {
                e.getPath().forEach(path -> {
                    System.out.printf("%s: %s%n", path.getFieldName(), path.getDescription());
                });
            }
        }
    }
}