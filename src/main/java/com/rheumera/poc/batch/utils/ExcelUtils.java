package com.rheumera.poc.batch.utils;

import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.Resource;

import com.rheumera.poc.batch.ColumnFormat;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static com.rheumera.poc.batch.ColumnFormat.*;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.usermodel.DateUtil.getJavaDate;
import static org.apache.poi.ss.usermodel.DateUtil.isValidExcelDate;

public class ExcelUtils {
    public static final Function<XSSFRow, List<String>> COLUMN_HEADERS = headerRow ->
            range(headerRow.getFirstCellNum(), headerRow.getLastCellNum())
                    .mapToObj(headerRow::getCell)
                    .filter(Objects::nonNull)
                    .map(cell -> getCellValue(cell, STRING))
                    .collect(toList());

    public static String getCellValue(XSSFCell cell, ColumnFormat columnFormat) {
        if (cell == null)
            return null;
        if (isDateTimeCell(cell, columnFormat)) {
            var excelDate = cell.getNumericCellValue();
            if (isValidExcelDate(excelDate)) {
                var pattern = excelDate < 1 ? "h:mm a" : "M/d/yyyy";
                var formatted = formatExcelDate(excelDate, pattern);
                if (formatted != null) {
                    return formatted;
                }
            }
        }
        return new DataFormatter().formatCellValue(cell);
    }

    private static boolean isDateTimeCell(XSSFCell cell, ColumnFormat format) {
        return cell.getCellType() == CellType.NUMERIC && (format == DATE || format == TIME);
    }

    @SneakyThrows
    public static XSSFWorkbook readWorkbook(Resource resource) {
        try {
            return new XSSFWorkbook(resource.getInputStream());
        } catch (IOException e) {
            throw new Exception("Failed to read from xlsx file", e);
        }
    }

    @SneakyThrows
    public static XSSFSheet getSheet(XSSFWorkbook workbook) {
        var sheet = workbook.getSheetAt(0);
        if (sheet.getLastRowNum() < 1)
            throw new Exception("Xlsx file has no rows");
        return sheet;
    }

    public static Map<String, String> rowToMap(List<String> headers, Map<String, ColumnFormat> columnConfig, XSSFRow row) {
        return range(0, headers.size())
                .boxed()
                .collect(
                        LinkedHashMap::new,
                        (map, i) -> {
                            var header = headers.get(i);
                            map.put(header, getCellValue(row.getCell(i), columnConfig.get(header)));
                        },
                        LinkedHashMap::putAll
                );
    }

    // Helpers

    private static String formatExcelDate(double excelDate, String pattern) {
        if (excelDate < 1 || (excelDate > 360 && excelDate < maxDays())) {
            var date = getJavaDate(excelDate);
            return new SimpleDateFormat(pattern).format(date);
        }
        return null;
    }

    private static double maxDays() {
        var startDate = LocalDate.of(1900, 1, 1);
        var endDate = LocalDate.now().plusYears(10);
        return DAYS.between(startDate, endDate);
    }
}