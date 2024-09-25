package com.rheumera.poc.utils;

import com.rheumera.poc.constants.AppConstants;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
public class DateUtils {
    public static LocalDate toLocalDate(String dateString) {
        for (var format : AppConstants.SUPPORTED_DATE_FORMATS) {
            try {
                return LocalDate.parse(dateString, format);
            } catch (Exception e) {
                log.warn("Could not parse with format: {}", format);
            }
        }
        return null;
    }
}