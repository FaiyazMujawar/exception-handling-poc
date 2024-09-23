package com.example.exceptionhandlingpoc.scratch;

import com.example.exceptionhandlingpoc.api.dto.batch.PatientImportDto;
import com.example.exceptionhandlingpoc.batch.io.CsvItemReader;
import com.example.exceptionhandlingpoc.utils.PathUtils;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.PathResource;

import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Map;

@Slf4j
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

    public static void main(String[] args) throws Exception {
        var resource = new PathResource(Paths.get(PathUtils.getInputDirPath().toString(), "test.csv"));
        var reader = CsvItemReader.<PatientImportDto>builder()
                .resource(resource)
                .mapper(mapper)
                .delimiter(',')
                .columnMappings(map)
                .targetType(PatientImportDto.class)
                .build();
        reader.open(new ExecutionContext());
        while (true) {
            var item = reader.read();
            if (item == null) break;
            System.out.println("item = " + item);
        }
        reader.close();
    }
}