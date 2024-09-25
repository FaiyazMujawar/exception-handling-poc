package com.rheumera.poc.constants;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ofPattern;

public class AppConstants {
    public static final DateTimeFormatter[] SUPPORTED_DATE_FORMATS = {
            ofPattern("dd/MM/yyyy"),
            ofPattern("MM/dd/yyyy")
    };
}