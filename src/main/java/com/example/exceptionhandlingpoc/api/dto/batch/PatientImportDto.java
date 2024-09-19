package com.example.exceptionhandlingpoc.api.dto.batch;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class PatientImportDto {
    @JsonProperty(value = "FIRST_NAME")
    @NotBlank(message = "First name cannot be blank")
    private String firstname;

    @JsonProperty(value = "LAST_NAME")
    @NotBlank(message = "Last name cannot be blank")
    private String lastname;

    @JsonProperty(value = "DATE_OF_BIRTH")
    @NotBlank(message = "Date of birth cannot be blank")
    private String dob;

    @JsonProperty(value = "MRN")
    @NotBlank(message = "MRN cannot be blank")
    private String mrn;

    @JsonProperty(value = "STATUS")
    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "ACTIVE|INACTIVE", flags = {Pattern.Flag.CASE_INSENSITIVE}, message = "Status can only be ACTIVE or INACTIVE")
    private String status;

    private Set<String> errors = new HashSet<>();
}