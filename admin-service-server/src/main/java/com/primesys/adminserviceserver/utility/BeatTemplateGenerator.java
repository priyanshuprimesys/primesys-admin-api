package com.primesys.adminserviceserver.utility;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generates the beat upload template programmatically so it is always in sync with the reader/validator logic.
 * Eliminates stale manual template files.
 *
 * Column layout (0-indexed): 0=DeviceName 1=DeviceNo 2=SectionName 3=DeviceType 4=StartKm 5=EndKm 6,7=Trip1(Start,End)
 * 8,9=Trip2 10,11=Trip3 12,13=Trip4 ...
 *
 * Data starts at row index 8 (row 9 in Excel) — matches the reader/validator.
 */
@Component
public class BeatTemplateGenerator {

    private static final int MAX_TRIPS = 6; // Trip columns generated in template
    private static final int HEADER_ROW = 7; // 0-indexed → row 8 in Excel (reader skips rows 0-7)
    private static final int FIRST_DATA_ROW = 8;

    public byte[] generate() throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            XSSFSheet sheet = wb.createSheet("Beat Upload");

            // ─── Styles ──────────────────────────────────────────────────────
            CellStyle titleStyle = titleStyle(wb);
            CellStyle infoStyle = infoStyle(wb);
            CellStyle headerStyle = headerStyle(wb);
            CellStyle example1Style = exampleStyle(wb,
                    new XSSFColor(new byte[] { (byte) 198, (byte) 224, (byte) 180 }, null)); // light green
            CellStyle example2Style = exampleStyle(wb,
                    new XSSFColor(new byte[] { (byte) 189, (byte) 215, (byte) 238 }, null)); // light blue
            CellStyle naStyle = naStyle(wb);
            CellStyle textStyle = textStyle(wb);
            CellStyle numStyle = numericStyle(wb);

            int lastCol = 5 + MAX_TRIPS * 2; // last column index

            // ─── Row 0 — Title ───────────────────────────────────────────────
            Row r0 = sheet.createRow(0);
            r0.setHeightInPoints(24);
            Cell title = r0.createCell(0);
            title.setCellValue("Beat Upload Template");
            title.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastCol));

            // ─── Rows 1-6 — Instructions ─────────────────────────────────────
            String[] instructions = { "PURPOSE: Upload GPS beat (trip) schedules for patrolmen / keyman devices.",
                    "MULTI-ROW: Same device can appear in multiple rows with DIFFERENT Start/End KM ranges.",
                    "           → Use row 1 for trips 1 & 3 (set KM A); row 2 for trips 2 & 4 (set KM B).",
                    "N/A RULE:  Enter N/A in BOTH start AND end time cells to skip a trip slot intentionally.",
                    "           → Asymmetric N/A (one filled, one N/A) is a validation error.",
                    "TIME FORMAT: 24-hour HH:MM   e.g. 06:30  14:00  23:45   (do NOT use Excel time format)" };
            for (int i = 0; i < instructions.length; i++) {
                Row r = sheet.createRow(i + 1);
                r.setHeightInPoints(15);
                Cell c = r.createCell(0);
                c.setCellValue(instructions[i]);
                c.setCellStyle(infoStyle);
                sheet.addMergedRegion(new CellRangeAddress(i + 1, i + 1, 0, lastCol));
            }

            // ─── Row 7 — Column headers ───────────────────────────────────────
            Row headerRow = sheet.createRow(HEADER_ROW);
            headerRow.setHeightInPoints(18);
            String[] fixedHeaders = { "Device Name", "Device No", "Section Name", "Device Type", "Start KM", "End KM" };
            for (int col = 0; col < fixedHeaders.length; col++) {
                Cell c = headerRow.createCell(col);
                c.setCellValue(fixedHeaders[col]);
                c.setCellStyle(headerStyle);
            }
            for (int t = 1; t <= MAX_TRIPS; t++) {
                Cell cs = headerRow.createCell(4 + t * 2);
                cs.setCellValue("Trip" + t + " Start");
                cs.setCellStyle(headerStyle);
                Cell ce = headerRow.createCell(5 + t * 2);
                ce.setCellValue("Trip" + t + " End");
                ce.setCellStyle(headerStyle);
            }

            // ─── Row 8 — Example 1: device 1, trips 1 & 3 filled, 2 & 4 = N/A ─
            Row ex1 = sheet.createRow(FIRST_DATA_ROW);
            setFixed(ex1, example1Style, numStyle, "Example-Loco", "1", "RNC-HTE", "PatrolMan", "100", "200");
            String[] ex1Times = { "06:00", "08:00", "N/A", "N/A", "14:00", "16:00", "N/A", "N/A" };
            setTimes(ex1, ex1Times, example1Style, naStyle, 6);

            // ─── Row 9 — Example 2: device 1, trips 2 & 4 filled, 1 & 3 = N/A ─
            Row ex2 = sheet.createRow(FIRST_DATA_ROW + 1);
            setFixed(ex2, example2Style, numStyle, "Example-Loco", "1", "RNC-HTE", "PatrolMan", "200", "100");
            String[] ex2Times = { "N/A", "N/A", "10:00", "12:00", "N/A", "N/A", "18:00", "20:00" };
            setTimes(ex2, ex2Times, example2Style, naStyle, 6);

            // ─── Rows 10-29 — Empty data rows with text format on time columns ──
            for (int r = FIRST_DATA_ROW + 2; r < FIRST_DATA_ROW + 22; r++) {
                Row row = sheet.createRow(r);
                for (int col = 0; col < 6; col++)
                    row.createCell(col).setCellStyle(textStyle);
                for (int col = 6; col <= lastCol; col++)
                    row.createCell(col).setCellStyle(textStyle);
            }

            // ─── Column widths ───────────────────────────────────────────────
            sheet.setColumnWidth(0, 4000); // Device Name
            sheet.setColumnWidth(1, 2800); // Device No
            sheet.setColumnWidth(2, 4000); // Section
            sheet.setColumnWidth(3, 3200); // Device Type
            sheet.setColumnWidth(4, 2800); // Start KM
            sheet.setColumnWidth(5, 2800); // End KM
            for (int col = 6; col <= lastCol; col++)
                sheet.setColumnWidth(col, 2800);

            // ─── Freeze header rows (rows 0-7 frozen) ────────────────────────
            sheet.createFreezePane(0, FIRST_DATA_ROW);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private void setFixed(Row row, CellStyle baseStyle, CellStyle numStyle, String name, String devNo, String section,
            String type, String startKm, String endKm) {
        cell(row, 0, name, baseStyle);
        cell(row, 1, devNo, baseStyle);
        cell(row, 2, section, baseStyle);
        cell(row, 3, type, baseStyle);
        cell(row, 4, startKm, baseStyle);
        cell(row, 5, endKm, baseStyle);
    }

    private void setTimes(Row row, String[] times, CellStyle normal, CellStyle naStyle, int startCol) {
        for (int i = 0; i < times.length; i++) {
            Cell c = row.createCell(startCol + i);
            c.setCellValue(times[i]);
            c.setCellStyle("N/A".equals(times[i]) ? naStyle : normal);
        }
    }

    private void cell(Row row, int col, String value, CellStyle style) {
        Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    // ─── Style factories ─────────────────────────────────────────────────────

    private CellStyle titleStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 14);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBackground(s, new XSSFColor(new byte[] { (byte) 68, (byte) 114, (byte) 196 }, null));
        f.setColor(IndexedColors.WHITE.getIndex());
        return s;
    }

    private CellStyle infoStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setFontHeightInPoints((short) 9);
        s.setFont(f);
        s.setWrapText(false);
        setBackground(s, new XSSFColor(new byte[] { (byte) 255, (byte) 242, (byte) 204 }, null));
        return s;
    }

    private CellStyle headerStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 10);
        s.setFont(f);
        s.setAlignment(HorizontalAlignment.CENTER);
        setBackground(s, new XSSFColor(new byte[] { (byte) 68, (byte) 114, (byte) 196 }, null));
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setBorderBottom(BorderStyle.THIN);
        return s;
    }

    private CellStyle exampleStyle(XSSFWorkbook wb, XSSFColor color) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setItalic(true);
        f.setFontHeightInPoints((short) 9);
        s.setFont(f);
        setBackground(s, color);
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle naStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f = wb.createFont();
        f.setItalic(true);
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        f.setFontHeightInPoints((short) 9);
        s.setFont(f);
        setBackground(s, new XSSFColor(new byte[] { (byte) 242, (byte) 242, (byte) 242 }, null));
        s.setAlignment(HorizontalAlignment.CENTER);
        return s;
    }

    private CellStyle textStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        // force text format so Excel doesn't auto-convert 06:30 to a time decimal
        DataFormat df = wb.createDataFormat();
        s.setDataFormat(df.getFormat("@"));
        s.setBorderBottom(BorderStyle.HAIR);
        s.setBorderRight(BorderStyle.HAIR);
        return s;
    }

    private CellStyle numericStyle(XSSFWorkbook wb) {
        return wb.createCellStyle();
    }

    private void setBackground(CellStyle style, XSSFColor color) {
        if (style instanceof XSSFCellStyle xStyle) {
            xStyle.setFillForegroundColor(color);
            xStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
    }
}
