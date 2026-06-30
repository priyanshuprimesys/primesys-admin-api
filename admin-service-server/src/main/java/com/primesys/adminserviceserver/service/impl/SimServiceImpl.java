package com.primesys.adminserviceserver.service.impl;

import com.mongodb.bulk.BulkWriteResult;
import com.primesys.adminservicemongodb.entity.SimEntity;
import com.primesys.adminservicemongodb.repository.SimRepository;
import com.primesys.adminserviceserver.response.SimUploadResult;
import com.primesys.adminserviceserver.service.SimService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimServiceImpl implements SimService {

    private final MongoTemplate mongoTemplate;
    private final SimRepository simRepository;

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    @Override
    public List<SimEntity> getSimRecords(String simProvider) {
        String provider = StringUtils.upperCase(StringUtils.trimToNull(simProvider));
        return provider == null ? simRepository.findAll() : simRepository.findBySimProvider(provider);
    }

    @Override
    public SimUploadResult importSimFile(MultipartFile file, String provider, String createdBy) throws IOException {
        String fileName = file.getOriginalFilename();
        String ext = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
        String normalizedProvider = StringUtils.upperCase(StringUtils.trimToNull(provider));

        List<List<String>> table; // row 0 = header, rest = data
        try (InputStream in = file.getInputStream()) {
            if ("csv".equals(ext)) {
                table = readCsv(in);
            } else if ("xlsx".equals(ext) || "xls".equals(ext)) {
                table = readExcel(in);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported file type: " + ext + ". Upload a .csv, .xlsx or .xls file.");
            }
        }

        if (table.isEmpty()) {
            return SimUploadResult.builder().provider(normalizedProvider).totalRows(0).saved(0).skipped(0)
                    .summary("File is empty — nothing imported.").build();
        }

        // map each column index to a canonical SimEntity field using its header
        List<String> headers = table.get(0);
        String[] fieldByColumn = new String[headers.size()];
        for (int c = 0; c < headers.size(); c++) {
            fieldByColumn[c] = resolveField(headers.get(c));
        }

        List<SimEntity> toSave = new ArrayList<>();
        int skipped = 0;
        long now = System.currentTimeMillis() / 1000;
        int totalRows = table.size() - 1;

        for (int r = 1; r < table.size(); r++) {
            List<String> row = table.get(r);
            SimEntity sim = SimEntity.builder().simProvider(normalizedProvider).refFileName(fileName).createdAt(now)
                    .createdBy(createdBy).build();

            boolean hasValue = false;
            for (int c = 0; c < row.size() && c < fieldByColumn.length; c++) {
                String field = fieldByColumn[c];
                if (field == null)
                    continue;
                String value = cleanValue(row.get(c));
                if (value == null)
                    continue;
                hasValue = applyField(sim, field, value) || hasValue;
            }

            // a usable row needs at least the SIM number (ICCID / SIM_NO)
            if (!hasValue || StringUtils.isBlank(sim.getSimNo())) {
                skipped++;
                continue;
            }
            toSave.add(sim);
        }

        // Upsert keyed on sim_no (ICCID for Jio): re-uploading the same file refreshes
        // existing rows instead of creating duplicates. Only non-null values are written
        // so a partial sheet never wipes fields previously set from another export.
        int inserted = 0, updated = 0;
        if (!toSave.isEmpty()) {
            BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, SimEntity.class);
            for (SimEntity sim : toSave) {
                Query query = new Query(Criteria.where("sim_no").is(sim.getSimNo()));
                Update update = new Update();
                setIfNotNull(update, "sim_imsi", sim.getSimImsi());
                setIfNotNull(update, "mobile_number", sim.getMobileNumber());
                setIfNotNull(update, "imei", sim.getImei());
                setIfNotNull(update, "basket_name", sim.getBasketName());
                setIfNotNull(update, "sim_status", sim.getSimStatus());
                setIfNotNull(update, "plan_name", sim.getPlanName());
                setIfNotNull(update, "activation_date", sim.getActivationDate());
                setIfNotNull(update, "onboarding_date", sim.getOnboardingDate());
                setIfNotNull(update, "apn1", sim.getApn1());
                update.set("sim_provider", sim.getSimProvider());
                update.set("ref_file_name", sim.getRefFileName());
                update.set("updated_at", now);
                update.set("updated_by", createdBy);
                update.setOnInsert("created_at", now);
                update.setOnInsert("created_by", createdBy);
                bulkOps.upsert(query, update);
            }
            BulkWriteResult result = bulkOps.execute();
            inserted = result.getUpserts().size();
            updated = result.getModifiedCount();
        }

        int saved = inserted + updated;
        String summary = totalRows + (totalRows == 1 ? " row" : " rows") + " processed — " + inserted + " inserted, "
                + updated + " updated" + (skipped > 0 ? ", " + skipped + " skipped (missing SIM number)" : "") + " ["
                + normalizedProvider + "]";
        log.info("SIM import: {}", summary);

        return SimUploadResult.builder().provider(normalizedProvider).totalRows(totalRows).saved(saved)
                .inserted(inserted).updated(updated).skipped(skipped).summary(summary).build();
    }

    // --- file readers -------------------------------------------------------

    private List<List<String>> readCsv(InputStream in) throws IOException {
        List<List<String>> table = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
                CSVParser parser = CSVFormat.DEFAULT.builder().setIgnoreEmptyLines(true).setTrim(true).build()
                        .parse(reader)) {
            for (CSVRecord record : parser) {
                List<String> rowData = new ArrayList<>();
                record.forEach(rowData::add);
                table.add(rowData);
            }
        }
        return table;
    }

    private List<List<String>> readExcel(InputStream in) throws IOException {
        List<List<String>> table = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(in)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                List<String> rowData = new ArrayList<>();
                boolean isEmpty = true;
                int lastCell = row.getLastCellNum();
                for (int c = 0; c < lastCell; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    String value = DATA_FORMATTER.formatCellValue(cell).trim();
                    if (StringUtils.isNotBlank(value))
                        isEmpty = false;
                    rowData.add(value);
                }
                if (!isEmpty)
                    table.add(rowData);
            }
        }
        return table;
    }

    // --- header → field resolution -----------------------------------------

    /**
     * Map a raw column header to the canonical {@code SimEntity} field name. Headers are normalized
     * (quotes/spaces/underscores stripped, upper-cased) so both the Jio sheet (ICCID/IMSI/MSISDN) and Airtel sheet
     * (SIM_NO/ SIM_IMSI/MOBILE_NUMBER …) resolve onto the same fields. Returns {@code null} for unrecognized columns
     * (which are ignored).
     */
    private String resolveField(String rawHeader) {
        String h = normalizeHeader(rawHeader);
        if (h.isEmpty())
            return null;
        switch (h) {
        case "SIMNO":
        case "ICCID":
            return "simNo";
        case "SIMIMSI":
        case "IMSI":
            return "simImsi";
        case "MOBILENUMBER":
        case "MOBILENO":
        case "MSISDN":
            return "mobileNumber";
        case "IMEI":
            return "imei";
        case "BASKETNAME":
            return "basketName";
        case "SIMSTATUS":
            return "simStatus";
        case "PLANNAME":
            return "planName";
        case "APN1":
        case "APN":
            return "apn1";
        default:
            // date columns may be visually truncated in some exports — match by prefix
            if (h.startsWith("ACTIVATION"))
                return "activationDate";
            if (h.startsWith("ONBOARD"))
                return "onboardingDate";
            return null;
        }
    }

    /** @return true if a non-blank value was set */
    private boolean applyField(SimEntity sim, String field, String value) {
        switch (field) {
        case "simNo" -> sim.setSimNo(value);
        case "simImsi" -> sim.setSimImsi(value);
        case "mobileNumber" -> sim.setMobileNumber(value);
        case "imei" -> sim.setImei(value);
        case "basketName" -> sim.setBasketName(value);
        case "simStatus" -> sim.setSimStatus(value);
        case "planName" -> sim.setPlanName(value);
        case "activationDate" -> sim.setActivationDate(value);
        case "onboardingDate" -> sim.setOnboardingDate(value);
        case "apn1" -> sim.setApn1(value);
        default -> {
            return false;
        }
        }
        return true;
    }

    /** Add a {@code $set} for the field only when the value is present, so blanks never overwrite existing data. */
    private void setIfNotNull(Update update, String field, String value) {
        if (value != null)
            update.set(field, value);
    }

    private String normalizeHeader(String raw) {
        if (raw == null)
            return "";
        return raw.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    /** Strip surrounding single/double quotes and whitespace; blank → null. */
    private String cleanValue(String raw) {
        if (raw == null)
            return null;
        String v = raw.trim();
        if (v.length() >= 2) {
            char first = v.charAt(0), last = v.charAt(v.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"'))
                v = v.substring(1, v.length() - 1).trim();
        }
        // leading apostrophe used by spreadsheets to force text ('89918...)
        if (v.startsWith("'"))
            v = v.substring(1).trim();
        return StringUtils.isBlank(v) ? null : v;
    }
}
