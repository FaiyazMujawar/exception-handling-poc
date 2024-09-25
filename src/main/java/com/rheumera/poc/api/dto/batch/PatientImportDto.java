package com.rheumera.poc.api.dto.batch;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.OptBoolean;
import com.rheumera.poc.constants.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

import static com.rheumera.poc.utils.DateUtils.toLocalDate;

@Slf4j
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
    @JsonFormat(pattern = "[dd/MM/yyyy][MM/dd/yyyy]", lenient = OptBoolean.TRUE)
    private LocalDate dob;

    @JsonProperty(value = "MRN")
    @NotBlank(message = "'MRN' cannot be blank")
    private String mrn;

    @JsonProperty(value = "STATUS")
    @NotNull(message = "Status cannot be blank")
    private Status status;

    @JsonSetter("DATE_OF_BIRTH")
    public void setDateOfBirth(String dob) {
        this.dob = toLocalDate(dob);
    }

    @Override
    public String toString() {
        return """
                PatientImportDto {
                    firstname: %s,
                    lastname: %s,
                    dob: %s,
                    mrn: %s,
                    status: %s
                }
                """.formatted(firstname, lastname, dob, mrn, status);
    }
}