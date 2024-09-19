package com.example.exceptionhandlingpoc.batch.io;

import com.example.exceptionhandlingpoc.batch.dto.LineItem;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.exceptionhandlingpoc.batch.utils.ExcelUtils.*;
import static com.example.exceptionhandlingpoc.batch.utils.ReflectionUtils.getColumnHeaders;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;
import static org.apache.poi.ss.usermodel.CellType.BLANK;
import static org.springframework.util.Assert.notNull;

@Component
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class PoiItemReader<T> implements ResourceAwareItemReaderItemStream<LineItem<T>> {
    private final JsonMapper jsonMapper;

    private Resource resource;
    private Class<T> targetType;
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private Map<String, String> columnMappings;
    private List<String> headers = new ArrayList<>();
    private List<String> classFields = new ArrayList<>();
    private int firstRow = 0;
    private int lastRow = 0;
    private int currentRow = 0;
    private int rowsRead = 0;

    private static boolean nonEmptyRow(XSSFRow row) {
        if (nonNull(row) && row.getLastCellNum() > 0)
            return range(row.getFirstCellNum(), row.getLastCellNum())
                    .mapToObj(row::getCell)
                    .anyMatch(cell -> cell != null && cell.getCellType() != BLANK);
        return false;
    }

    public void setTargetType(@NonNull Class<T> targetType) {
        this.targetType = targetType;
        this.classFields = getColumnHeaders(this.targetType);
    }

    @SneakyThrows
    public void setColumnMappings(@NonNull Map<String, String> mappings) {
        if (isNull(targetType) || isNull(headers)) throw new Exception("targetType or headers must be present");
        if (!mappings.keySet().containsAll(this.headers)) {
            throw new Exception("Column mappings must contain all headers");
        }
        // Reversing the key->value mapping to value->key
        this.columnMappings = mappings.entrySet().stream().collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    @Override
    public void setResource(@NonNull Resource resource) {
        this.resource = resource;
    }

    @Override
    @SneakyThrows
    public LineItem<T> read() {
        while (this.currentRow <= this.lastRow) {
            var row = this.sheet.getRow(currentRow);
            if (nonEmptyRow(row)) {
                rowsRead++;
                var map = rowToMap(headers, Map.of(), row);
                var transformed = map.entrySet().stream()
                        .collect(toMap(entry -> this.columnMappings.get(entry.getKey()), Map.Entry::getValue));
                var object = jsonMapper.convertValue(transformed, this.targetType);
                return LineItem.<T>builder()
                        .row(1 + currentRow++)
                        .item(object)
                        .raw(map)
                        .build();
            }
            currentRow++;
        }
        return null;
    }

    @Override
    @SneakyThrows
    public void open(@NonNull ExecutionContext executionContext) throws ItemStreamException {
        Assert.state(this.targetType != null, "Target type must be provided.");
        Assert.state(this.resource != null, "Resource must be provided.");
        this.workbook = readWorkbook(this.resource);
        notNull(this.workbook, "Workbook cannot be null.");
        this.sheet = getSheet(this.workbook);
        this.firstRow = getFirstRowNum(this.sheet);
        this.lastRow = this.sheet.getLastRowNum();
        this.currentRow = this.firstRow + 1;

        this.headers = COLUMN_HEADERS.apply(this.sheet.getRow(this.firstRow));
    }

    @Override
    @SneakyThrows
    public void close() throws ItemStreamException {
        this.resource = null;
        this.firstRow = 0;
        this.lastRow = 0;
        this.currentRow = 0;
        this.rowsRead = 0;
        this.headers = new ArrayList<>();
        this.workbook.close();
    }

    public int getFirstRowNum(XSSFSheet sheet) {
        int row = 0;
        while (!nonEmptyRow(sheet.getRow(row))) {
            row++;
        }
        return row;
    }
}