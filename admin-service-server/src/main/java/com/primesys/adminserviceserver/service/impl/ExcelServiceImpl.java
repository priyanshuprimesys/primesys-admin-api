package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import com.primesys.adminserviceserver.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    private static final DataFormatter DATE_FORMATTER = new DataFormatter();

    public List<List<String>> readBeatExcelData(InputStream inputStream) throws IOException {
        List<List<String>> excelData = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming we are reading from the first sheet

            for (Row row : sheet) {
                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }
                List<String> rowData = new ArrayList<>();
                Iterator<Cell> cellIterator = row.cellIterator();

                if (!isEmpty) {

                    while (cellIterator.hasNext()) {

                        Cell cell = cellIterator.next();
                        // log.info(cell.getCellType() + "--" + cell.getColumnIndex());
                        switch (cell.getCellType()) {
                        case STRING -> {
                            if (StringUtils.isNotBlank(cell.getStringCellValue())) {
                                rowData.add(cell.getStringCellValue());
                            }
                        }
                        case NUMERIC -> {
                            if (cell.getColumnIndex() > 6) {
                                rowData.add(DATE_FORMATTER.formatCellValue(cell));
                            } else {
                                // rowData.add(String.valueOf(cell.getNumericCellValue()));
                                double numericValue = cell.getNumericCellValue();
                                // Convert numeric value to BigDecimal to preserve precision
                                BigDecimal bigDecimalValue = BigDecimal.valueOf(numericValue);
                                // Print decimal representation without scientific notation
                                // System.out.println("Decimal representation: " + bigDecimalValue.toPlainString());
                                rowData.add(bigDecimalValue.toPlainString());
                            }
                        }
                        case BLANK -> {
                            // log.info(
                            // "Blank Cell---" + cell.getColumnIndex() + "---row--" + cell.getRowIndex() + "----");
                            if (cell.getColumnIndex() == 0) {
                                rowData.add("");
                            }

                            if (cell.getColumnIndex() == 2) {
                                rowData.add("Na");
                            }
                        }

                        default -> {
                            log.error("Invalid type found Column Type : {} :: Row Number : {} :: Column Number : {}",
                                    cell.getCellType(), cell.getRowIndex(), cell.getColumnIndex());
                        }
                        }
                    }
                    // log.info("Row Data--" + rowData);
                    excelData.add(rowData);
                }
            }
        }
        return excelData;
    }

    public String validateBeatFile(String filePath) {
        StringBuilder errorInSheet = new StringBuilder();

        try (InputStream inputStream = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming it's the first sheet
            // log.info("getLastRowNum--------"+sheet.getLastRowNum());
            // Start from the second row (skipping header row)
            for (int rowIndex = 8; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // log.info(rowIndex + "----rowIndex");
                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }

                if (!isEmpty) {

                    // Validate Device Name, IMEI No, Section Name
                    for (int columnIndex = 1; columnIndex <= 2; columnIndex++) {
                        Cell cell = row.getCell(columnIndex);
                        if (cell == null || cell.getCellType() == CellType.BLANK) {
                            System.out.println("Missing data in column " + (convertToColumnName(columnIndex + 1))
                                    + " at row " + (rowIndex + 1));
                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                    .append(convertToColumnName(3 + 1))
                                    .append(" Invalid IMEI or Section name value in column value.");
                        }
                    }

                    // Validate Device Type Id
                    Cell deviceTypeIdCell = row.getCell(3);
                    if ((deviceTypeIdCell == null || deviceTypeIdCell.getCellType() == CellType.BLANK)
                            || (deviceTypeIdCell.getCellType() != CellType.NUMERIC
                                    && deviceTypeIdCell.getNumericCellValue() > 2)) {
                        System.out.println("Invalid Device Type Id at row " + (rowIndex + 1));
                        errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                .append(convertToColumnName(3 + 1)).append(" Invalid Device value in column value.");
                    }

                    // Validate Day/Night
                    Cell dayNightCell = row.getCell(4);
                    if ((deviceTypeIdCell == null || deviceTypeIdCell.getCellType() == CellType.BLANK)
                            || (deviceTypeIdCell.getCellType() != CellType.NUMERIC)) {
                        errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:").append(4 + 1)
                                .append(" Invalid Day/Night value in column value.");
                    } else {
                        double dayNightValue = (dayNightCell.getNumericCellValue());
                        // log.info(dayNightValue+"----------");
                        if (!(dayNightValue == 1.0 || dayNightValue == 2.0 || dayNightValue == 0.0
                                || dayNightValue == 3.0)) {
                            System.out.println("Invalid Day/Night value at row " + (rowIndex + 1));
                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:").append(4 + 1)
                                    .append(" Invalid Day/Night value in column value.");
                            // Handle invalid data as needed
                        }
                    }

                    // Validate Start KM, End KM
                    for (int columnIndex = 5; columnIndex <= 6; columnIndex++) {
                        Cell cell = row.getCell(columnIndex);
                        if (cell != null && cell.getCellType() != CellType.NUMERIC) {
                            System.out.println("Invalid value in column " + (convertToColumnName(columnIndex + 1))
                                    + " at row " + (rowIndex + 1));
                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                    .append(convertToColumnName(columnIndex + 1))
                                    .append(" Invalid StartKm value in column value.");
                            // Handle invalid data as needed
                        }
                    }

                    // Validate Start Time and End Time
                    Map<Integer, Cell> timeCells = new HashMap<>();
                    for (int columnIndex = 7; columnIndex <= row.getLastCellNum(); columnIndex += 2) {
                        Cell startTimeCell = row.getCell(columnIndex);
                        Cell endTimeCell = row.getCell(columnIndex + 1);
                        timeCells.put(columnIndex, startTimeCell);

                        if (startTimeCell != null && startTimeCell.getCellType() == CellType.STRING) {
                            if (endTimeCell == null || endTimeCell.getCellType() == CellType.BLANK) {
                                System.out.println("End Time is missing for Start Time in column "
                                        + (convertToColumnName(columnIndex + 1)) + " at row " + (rowIndex + 1));
                                // Handle missing data as needed

                                errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                        .append(convertToColumnName(columnIndex + 1))
                                        .append("End Time is missing for Start Time in column.");
                            }
                        }
                    }
                    // Validate Start Time, End Time
                    for (int columnIndex = 7; columnIndex <= row.getLastCellNum(); columnIndex += 2) {
                        Cell startTimeCell = row.getCell(columnIndex);
                        Cell endTimeCell = row.getCell(columnIndex + 1);
                        // log.info(startTimeCell.getLocalDateTimeCellValue()+"----------"+endTimeCell.getLocalDateTimeCellValue().toLocalTime());
                        if ((startTimeCell != null && endTimeCell != null
                                && startTimeCell.getCellType() != CellType.BLANK
                                && (startTimeCell.getCellType() != CellType.NUMERIC
                                        || endTimeCell.getCellType() != CellType.NUMERIC))

                                && (endTimeCell.getCellType() != CellType.NUMERIC
                                        || !isValidTime(
                                                startTimeCell.getLocalDateTimeCellValue().toLocalTime().toString())
                                        || !isValidTime(
                                                endTimeCell.getLocalDateTimeCellValue().toLocalTime().toString()))) {
                            System.out.println("Invalid time value in columns " + (convertToColumnName(columnIndex + 1))
                                    + " and " + (columnIndex + 2) + " at row " + (rowIndex + 1));
                            // Handle invalid data as needed

                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                    .append(convertToColumnName(columnIndex + 1) + " or "
                                            + convertToColumnName(columnIndex + 2))
                                    .append(" Invalid time value. please enter valid data.");
                        }
                    }

                    // Continue validation for other rows similarly...
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorInSheet.append(e.getMessage());
            return errorInSheet.toString();

        } catch (Exception e) {
            errorInSheet.append(e.getMessage());

            e.printStackTrace();
            return errorInSheet.toString();

        }
        return errorInSheet.toString();

    }

    @Override
    public List<List<String>> readDeviceExcelData(InputStream inputStream) throws IOException {
        List<List<String>> excelData = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming we are reading from the first sheet

            for (Row row : sheet) {
                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }
                List<String> rowData = new ArrayList<>();
                Iterator<Cell> cellIterator = row.cellIterator();

                if (!isEmpty) {

                    while (cellIterator.hasNext()) {

                        Cell cell = cellIterator.next();
                        log.info(cell.getCellType() + "--" + cell.getColumnIndex());
                        switch (cell.getCellType()) {
                        case STRING -> {
                            if (StringUtils.isNotBlank(cell.getStringCellValue())) {
                                rowData.add(cell.getStringCellValue());
                            }
                        }
                        case NUMERIC -> {

                            // rowData.add(String.valueOf(cell.getNumericCellValue()));
                            double numericValue = cell.getNumericCellValue();
                            // Convert numeric value to BigDecimal to preserve precision
                            BigDecimal bigDecimalValue = BigDecimal.valueOf(numericValue);
                            // Print decimal representation without scientific notation
                            // System.out.println("Decimal representation: " + bigDecimalValue.toPlainString());
                            rowData.add(bigDecimalValue.toPlainString());

                        }
                        case BLANK -> {
                            // log.info("Blank Cell---" + cell.getColumnIndex());
                            if (cell.getColumnIndex() == 0) {
                                rowData.add("");
                            }

                            if (cell.getColumnIndex() == 2) {
                                rowData.add("Na");
                            }
                        }

                        default -> {
                            log.error("Invalid type found Column Type : {} :: Row Number : {} :: Column Number : {}",
                                    cell.getCellType(), cell.getRowIndex(), cell.getColumnIndex());
                        }
                        }
                    }
                    // log.info("Row Data--" + rowData);
                    excelData.add(rowData);
                }
            }
        }
        return excelData;
    }

    @Override
    public String validateSendCommandFile(String filePath) {
        StringBuilder errorInSheet = new StringBuilder();

        try (InputStream inputStream = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // Assuming it's the first sheet
            // log.info("getLastRowNum--------"+sheet.getLastRowNum());
            // Start from the second row (skipping header row)
            for (int rowIndex = 8; rowIndex < sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                // log.info(rowIndex + "----rowIndex");
                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }

                if (!isEmpty) {

                    // Validate Device Name, IMEI No, Section Name
                    for (int columnIndex = 0; columnIndex <= 3; columnIndex++) {
                        Cell cell = row.getCell(columnIndex);
                        if (cell == null || cell.getCellType() == CellType.BLANK) {
                            System.out.println("Missing data in column " + (convertToColumnName(columnIndex + 1))
                                    + " at row " + (rowIndex + 1));
                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                    .append(convertToColumnName(columnIndex + 1))
                                    .append(" Invalid value in column value.");
                        }
                    }

                    // Continue validation for other rows similarly...
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            errorInSheet.append(e.getMessage());
            return errorInSheet.toString();

        } catch (Exception e) {
            errorInSheet.append(e.getMessage());

            e.printStackTrace();
            return errorInSheet.toString();

        }
        return errorInSheet.toString();

    }

    @Override
    public List<DeviceCommandHistoryEntity> readSendCommandExcelData(FileInputStream fileInputStream) {
        List<List<String>> excelData = new ArrayList<>();
        List<DeviceCommandHistoryEntity> excelDataList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // Assuming we are reading from the first sheet

            for (int rowIndex = sheet.getFirstRowNum(); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }
                List<String> rowData = new ArrayList<>();
                Iterator<Cell> cellIterator = row.cellIterator();

                if (!isEmpty && rowIndex > 0) {
                    DeviceCommandHistoryEntity rowObj = new DeviceCommandHistoryEntity();
                    rowObj.setDivisionId(row.getCell(0).getStringCellValue());
                    rowObj.setDeviceImei(Long.parseLong(row.getCell(1).getStringCellValue()));
                    rowObj.setCommand(row.getCell(2).getStringCellValue());
                    rowObj.setLoginName(row.getCell(3).getStringCellValue());
                    rowObj.setTimestamp((System.currentTimeMillis() / 1000));

                    excelDataList.add(rowObj);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return excelDataList;
    }

    public List<List<String>> readBeatDeviceNoExcelData(FileInputStream fileInputStream) throws IOException {
        List<List<String>> excelData = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(); // Always get cell value as String

        // --- Time formatters ---
        List<DateTimeFormatter> timeFormatters = List.of(DateTimeFormatter.ofPattern("HH:mm"), // 24-hour (15:30)
                DateTimeFormatter.ofPattern("H:mm"), // 24-hour single digit hour
                DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH), // 12-hour (03:30 PM)
                DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH) // 12-hour (3:30 pm)
        );

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try (Workbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0); // first sheet

            for (Row row : sheet) {
                if (row.getRowNum() > 7) { // skip headers
                    boolean isEmpty = true;
                    for (Cell cell : row) {
                        String value = formatter.formatCellValue(cell);
                        if (StringUtils.isNotBlank(value)) {
                            isEmpty = false;
                            break;
                        }
                    }

                    List<String> rowData = new ArrayList<>();
                    if (!isEmpty) {
                        for (Cell cell : row) {
                            String rawValue = formatter.formatCellValue(cell).trim();

                            // Skip empty cells
                            if (StringUtils.isBlank(rawValue)) {
                                continue;
                            }

                            // Handle time columns (after index 5 → Start Time, End Time)
                            if (cell.getColumnIndex() > 5) {
                                rowData.add(normalizeTime(rawValue, timeFormatters, outputFormatter));
                            }
                            // Handle numeric fields like StartKm, EndKm
                            else if (cell.getColumnIndex() == 4 || cell.getColumnIndex() == 5) {
                                if (StringUtils.isNumeric(rawValue) || isDouble(rawValue)) {
                                    rowData.add(rawValue);
                                } else {
                                    rowData.add("INVALID"); // flag invalid numeric
                                }
                            }
                            // Everything else → add as-is
                            else {
                                rowData.add(rawValue);
                            }
                        }
                        excelData.add(rowData);
                    }
                }
            }
        }
        return excelData;
    }

    /**
     * Normalize time string into HH:mm (24-hour format).
     */
    private String normalizeTime(String rawValue, List<DateTimeFormatter> timeFormatters,
            DateTimeFormatter outputFormatter) {
        if (StringUtils.isBlank(rawValue)) {
            return "";
        }
        rawValue = rawValue.trim().toUpperCase(Locale.ENGLISH); // normalize AM/PM

        for (DateTimeFormatter formatter : timeFormatters) {
            try {
                LocalTime time = LocalTime.parse(rawValue, formatter);
                return outputFormatter.format(time);
            } catch (DateTimeParseException ignored) {
                // try next formatter
            }
        }

        log.warn("Unrecognized time format: {}", rawValue);
        return rawValue; // fallback
    }

    /**
     * Check if string is a valid double.
     */
    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Method to validate time format (HH:mm)
    private static boolean isValidTime(String time) {
        return time.matches("^(([0-9]{1})|([0-1]{1}[0-9]{1})|([2]{1}[0-3]{1}))([:]{1})([0-5]{1}[0-9]{1})$");
    }

    public static String convertToColumnName(int columnNumber) {
        StringBuilder columnName = new StringBuilder();

        while (columnNumber > 0) {
            int remainder = (columnNumber - 1) % 26;
            columnName.insert(0, (char) ('A' + remainder));
            columnNumber = (columnNumber - 1) / 26;
        }

        return columnName.toString();
    }

    public static String toName(int number) {
        StringBuilder sb = new StringBuilder();
        while (number-- > 0) {
            sb.append((char) ('A' + (number % 26)));
            number /= 26;
        }
        return sb.reverse().toString();
    }

}
