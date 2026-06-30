package com.primesys.adminserviceserver.service.impl;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ExcelUnlockedColumnsChecker {

    public static String getColumnName(String path) throws Exception {
        File file = new File(path);
        FileInputStream fis = new FileInputStream(file);

        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheetAt(0); // first sheet

        // Track unlocked columns by column index
        Set<Integer> unlockedColumns = new HashSet<>();

        for (Row row : sheet) {
            for (Cell cell : row) {
                CellStyle style = cell.getCellStyle();
                if (style != null && !style.getLocked()) {
                    unlockedColumns.add(cell.getColumnIndex());
                }
            }
        }

        // Print header names of unlocked columns
        Row headerRow = sheet.getRow(0);
        String col = "";
        System.out.println("🔓 Unlocked columns:");
        for (Integer colIndex : unlockedColumns) {
            Cell headerCell = headerRow.getCell(colIndex);
            if (headerCell != null) {
                System.out.println("- Column " + (colIndex + 1) + ": " + headerCell.toString());
                col = headerCell.toString();
            }
        }

        workbook.close();
        fis.close();
        return col;
    }

    public static int getColumnIndexByName(Sheet sheet, String columnName) {
        Row headerRow = sheet.getRow(0); // Assuming first row is header
        if (headerRow == null) {
            throw new IllegalArgumentException("Header row is missing.");
        }

        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.STRING) {
                String cellValue = cell.getStringCellValue().trim();
                if (cellValue.equalsIgnoreCase(columnName.trim())) {
                    return cell.getColumnIndex();
                }
            }
        }

        throw new IllegalArgumentException("Column name '" + columnName + "' not found in sheet.");
    }
}
