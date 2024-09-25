package com.rheumera.poc.scratch;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rheumera.poc.api.dto.batch.PatientImportDto;
import com.rheumera.poc.batch.dto.LineItem;
import com.rheumera.poc.batch.io.readers.ExtendedFlatFileItemReader;
import com.rheumera.poc.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static com.rheumera.poc.utils.DateUtils.toLocalDate;

@Slf4j
@SuppressWarnings("all")
public class Main {
    private static final JsonMapper mapper = JsonMapper.builder()
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
        var module = new JavaTimeModule();
        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {
            @Override
            public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
                return toLocalDate(jsonParser.getText());
            }
        });
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

    public static void main(String[] args) throws Exception {
        var reader = ExtendedFlatFileItemReader.<PatientImportDto>builder()
                .resource(resource)
                .mapper(mapper)
                .type(PatientImportDto.class)
                .mappings(map)
                .delimiter(",")
                .build();
        reader.open(new ExecutionContext());
        LineItem<PatientImportDto> lineItem = null;
        while ((lineItem = reader.read()) != null) {
            System.out.println(lineItem.getItem());
        }
        reader.close();
    }

    public static void main1(String[] args) {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
    }
}