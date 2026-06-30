package com.primesys.adminserviceserver.utility;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component

public class BeatFileValidator {

    // Multiple allowed time formats for STRING cells
    private static final DateTimeFormatter[] TIME_FORMATTERS = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("HH:mm"), DateTimeFormatter.ofPattern("HH:mm:ss"),
            DateTimeFormatter.ofPattern("H:mm"), DateTimeFormatter.ofPattern("h:mm a"),
            DateTimeFormatter.ofPattern("H.mm") };

    /**
     * A cell holds "no trip" when it is blank, N/A, empty text, or resolves to 00:00 (a numeric 0 / empty-as-zero).
     * Such slots are skipped, never validated — so a 0 is not mistaken for a real midnight start.
     */
    private boolean isEmptyCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK)
            return true;
        if (cell.getCellType() == CellType.STRING) {
            String s = cell.getStringCellValue().trim();
            if (s.isEmpty() || s.equalsIgnoreCase("N/A") || s.equalsIgnoreCase("NA"))
                return true;
        }
        LocalTime t = parseExcelTime(cell);
        return t != null && t.equals(LocalTime.MIDNIGHT); // 0 / 00:00 = absent trip
    }

    /**
     * Universal parser for Excel cell -> LocalTime
     */
    private LocalTime parseExcelTime(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                // Excel stores date/time as numeric
                return cell.getLocalDateTimeCellValue().toLocalTime();
            } else if (cell.getCellType() == CellType.STRING) {
                String timeStr = cell.getStringCellValue().trim();
                if (timeStr.isEmpty())
                    return null;

                for (DateTimeFormatter formatter : TIME_FORMATTERS) {
                    try {
                        return LocalTime.parse(timeStr, formatter);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error parsing time from row " + cell.getRowIndex() + ", col " + cell.getColumnIndex()
                    + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Convert column index (0-based) to Excel column name (A, B, C…)
     */
    private String convertToColumnName(int columnIndex) {
        StringBuilder colName = new StringBuilder();
        while (columnIndex > 0) {
            int rem = (columnIndex - 1) % 26;
            colName.insert(0, (char) (rem + 'A'));
            columnIndex = (columnIndex - 1) / 26;
        }
        return colName.toString();
    }

    /**
     * Main validation method
     */
    public String validateDeviceNoBeatFile(String filePath) {
        StringBuilder errorInSheet = new StringBuilder();

        try (InputStream inputStream = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // first sheet
            System.out.println("Total Rows = " + sheet.getLastRowNum());

            // Start from 9th row (index 8), skip headers
            for (int rowIndex = 8; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null)
                    continue;

                // Skip empty rows
                boolean isEmpty = true;
                for (Cell cell : row) {
                    if (cell.getCellType() != CellType.BLANK) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty)
                    continue;

                // --- Validate Device No (Col B / index 1) ---
                Cell deviceNoCell = row.getCell(1);
                boolean validDeviceNo = false;

                if (deviceNoCell != null && deviceNoCell.getCellType() != CellType.BLANK) {
                    if (deviceNoCell.getCellType() == CellType.NUMERIC) {
                        double num = deviceNoCell.getNumericCellValue();
                        // must be whole number (integer)
                        if (num == Math.floor(num)) {
                            validDeviceNo = true;
                        }
                    } else if (deviceNoCell.getCellType() == CellType.STRING) {
                        String value = deviceNoCell.getStringCellValue().trim();
                        if (!value.isEmpty() && value.matches("\\d+")) { // only digits allowed
                            validDeviceNo = true;
                        }
                    }
                }

                if (!validDeviceNo) {
                    errorInSheet.append("\nRow:").append(rowIndex + 1)
                            .append(" Col:B Invalid Device No. Device Number should be an integer like 010.");
                }

                // --- Validate Section Name (Col C / index 2) ---
                Cell sectionNameCell = row.getCell(2);
                String sectionName = "NA"; // default

                if (sectionNameCell != null && sectionNameCell.getCellType() != CellType.BLANK) {
                    if (sectionNameCell.getCellType() == CellType.STRING) {
                        String value = sectionNameCell.getStringCellValue().trim();
                        if (!value.isEmpty()) {
                            sectionName = value;
                        }
                    } else if (sectionNameCell.getCellType() == CellType.NUMERIC) {
                        sectionName = String.valueOf(sectionNameCell.getNumericCellValue());
                    } else {
                        sectionName = sectionNameCell.toString().trim(); // fallback for other types
                    }
                }

                // --- Validate Device Type Id (Col D / index 3) ---
                Cell deviceTypeIdCell = row.getCell(3);
                if (deviceTypeIdCell == null || deviceTypeIdCell.getCellType() != CellType.STRING) {
                    errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:D Invalid Device Type Id.");
                }

                // --- Validate Start KM (Col E / index 4) & End KM (Col F / index 5) ---
                for (int columnIndex = 4; columnIndex <= 5; columnIndex++) {
                    Cell kmCell = row.getCell(columnIndex);
                    boolean validKm = false;

                    if (kmCell == null || kmCell.getCellType() == CellType.BLANK) {
                        validKm = false; // blank is error
                    } else if (kmCell.getCellType() == CellType.NUMERIC) {
                        validKm = true; // valid numeric
                    } else if (kmCell.getCellType() == CellType.STRING) {
                        String val = kmCell.getStringCellValue().trim();
                        if (!val.isEmpty()) {
                            try {
                                Double.parseDouble(val);
                                validKm = true; // valid numeric string
                            } catch (NumberFormatException e) {
                                validKm = false;
                            }
                        } else {
                            validKm = false; // empty string is error
                        }
                    }

                    if (!validKm) {
                        errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                .append(convertToColumnName(columnIndex + 1))
                                .append(" Invalid or missing StartKm/EndKm value.");
                    }
                }

                // --- Validate Start/End Times (from Col G / index 6 onwards, in pairs) ---
                for (int columnIndex = 6; columnIndex <= row.getLastCellNum(); columnIndex += 2) {
                    Cell startTimeCell = row.getCell(columnIndex);
                    Cell endTimeCell = row.getCell(columnIndex + 1);

                    // A slot with no real trip time is skipped — not validated. A cell counts
                    // as "no trip" if it is N/A, blank, or resolves to 00:00 (a numeric 0 /
                    // empty-as-zero). This prevents a 0 cell being read as a midnight start and
                    // paired with a real end into a bogus 00:00→16:30 "16h" trip.
                    if (isEmptyCell(startTimeCell) || isEmptyCell(endTimeCell))
                        continue;

                    LocalTime startTime = parseExcelTime(startTimeCell);
                    LocalTime endTime = parseExcelTime(endTimeCell);

                    if (startTime != null && endTime != null) {
                        int startSeconds = startTime.toSecondOfDay();
                        int endSeconds = endTime.toSecondOfDay();

                        System.out.println("Trip Row " + (rowIndex + 1) + " Start=" + startTime + ", End=" + endTime);

                        // A beat slot may run overnight (e.g. 23:30→01:30). When end<=start,
                        // treat it as wrapping past midnight and add a full day to the duration.
                        int duration = endSeconds - startSeconds;
                        if (duration <= 0) {
                            duration += 24 * 3600;
                        }
                        if (duration > 13 * 3600) {
                            errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                    .append(convertToColumnName(columnIndex + 1)).append("-")
                                    .append(convertToColumnName(columnIndex + 2))
                                    .append(" Trip duration exceeds 13 hours.");
                        }
                    } else if (startTime == null && endTime == null) {
                        // both blank (not N/A) — treated as skip
                    } else {
                        // one is blank/invalid, the other is a valid time
                        errorInSheet.append("\nRow:").append(rowIndex + 1).append(" Col:")
                                .append(convertToColumnName(columnIndex + 1)).append(" or ")
                                .append(convertToColumnName(columnIndex + 2)).append(" Missing or invalid time value.");
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            errorInSheet.append(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            errorInSheet.append(e.getMessage());
        }
        return errorInSheet.toString();
    }
}
