package com.rheumera.poc.api.controllers;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rheumera.poc.utils.PathUtils;

import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping(path = "/batch")
@RequiredArgsConstructor
@Slf4j
public class BatchJobController {
    private final JobRegistry jobRegistry;
    private final JobLauncher jobLauncher;

    @PostMapping
    @SneakyThrows
    public void runBatchJob(@RequestParam("file") @NotNull MultipartFile file) {
        var inputFilePath = Paths.get(PathUtils.getInputDirPath().toString(), file.getOriginalFilename());
        var errorFilePath = Paths.get(PathUtils.getErrorDirPath().toString(), "error-" + file.getOriginalFilename());
        var statusFilePath = Paths.get(PathUtils.getStatusDir().toString(), "status-" + file.getOriginalFilename());
        file.transferTo(inputFilePath);
        var jobParameters = new JobParametersBuilder()
                .addString("id", UUID.randomUUID().toString())
                .addString("inputFilePath", inputFilePath.toString())
                .addString("errorFilePath", errorFilePath.toString())
                .addString("statusFilePath", statusFilePath.toString())
                .toJobParameters();
        var job = jobRegistry.getJob("CSV_PATIENT_IMPORT_JOB");
        runJob(job, jobParameters);
    }

    private void runJob(Job job, JobParameters jobParameters) {
        try {
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobInstanceAlreadyCompleteException | JobRestartException
                 | JobParametersInvalidException e) {
            log.error("Something went wrong while trying run job: {}", e.getMessage());
        }
    }

}