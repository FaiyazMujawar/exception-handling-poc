package com.example.exceptionhandlingpoc.api.dto.batch;

import com.example.exceptionhandlingpoc.batch.ExcelPattern;
import com.example.exceptionhandlingpoc.constants.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientImportDto {
    @JsonProperty(value = "FIRST_NAME")
    @NotBlank(message = "First name cannot be blank")
    private String firstname;

    @JsonProperty(value = "LAST_NAME")
    @NotBlank(message = "Last name cannot be blank")
    private String lastname;

    @JsonProperty(value = "DATE_OF_BIRTH")
    @NotNull(message = "Date of birth cannot be blank")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ExcelPattern.DATE_PATTERN)
    private LocalDate dob;

    @JsonProperty(value = "MRN")
    @NotBlank(message = "'MRN' cannot be blank")
    private String mrn;

    @JsonProperty(value = "STATUS")
    @NotNull(message = "Status cannot be blank")
    private Status status;
}