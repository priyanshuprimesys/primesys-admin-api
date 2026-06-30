package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicemongodb.entity.RdpsGeometryEntity;
import com.primesys.adminservicemongodb.model.GeoLocation;
import com.primesys.adminservicemongodb.repository.RdpsGeometryRepository;
import com.primesys.adminserviceserver.response.FileUploadResultResponse;
import com.primesys.adminserviceserver.service.RdpsGeometryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class RdpsGeometryServiceImpl implements RdpsGeometryService {

    private final RdpsGeometryRepository rdpsGeometryRepository;

    @Override
    public List<RdpsGeometryEntity> saveRdps(List<RdpsGeometryEntity> rdps) {
        List<RdpsGeometryEntity> savedEntities = new ArrayList<>();
        for (RdpsGeometryEntity entity : rdps) {
            savedEntities.add(rdpsGeometryRepository.save(entity));
        }
        return savedEntities;
    }

    @Override
    public Optional<String> uploadRdpsFile(String filePath, String divisionId) {

        // return addRdpsData(filePath,divisionId);

        try {
            // Load the script from resources
            InputStream scriptStream = getClass().getClassLoader()
                    .getResourceAsStream("python/upload_rdps_in_geometry_file.py");

            if (scriptStream == null) {
                throw new IOException("Python script not found in resources.");
            }

            // Create a temporary file to store the script
            File tempScriptFile = File.createTempFile("upload_rdps_in_geometry_file", ".py");
            tempScriptFile.deleteOnExit(); // Ensure it is deleted when the JVM exits

            // Copy the script content from resources to the temporary file
            try (FileOutputStream outputStream = new FileOutputStream(tempScriptFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = scriptStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }

            // Get the path to the temp script file
            String scriptPath = tempScriptFile.getAbsolutePath();

            // Command to execute the Python script with arguments
            String[] command = { "python3", // or "python3" depending on your setup
                    scriptPath, // Use the path of the script in resources
                    filePath, divisionId };

            // Run the Python script using ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Merge stdout and stderr
            Process process = processBuilder.start();
            // Join command array into a single string for logging
            String commandStr = String.join(" ", command);

            log.info("🚀 Executing command: {}", commandStr);
            // Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Python script executed successfully!");
                log.info(output.toString());
                return Optional.of(output.toString());
            } else {
                System.out.println("Python script failed with exit code " + exitCode);
                return Optional.of(output.toString());

            }

        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(e.getMessage());
        }

    }

    @Override
    public List<RdpsGeometryEntity> getDivisionRdpsData(String divisionId) {
        final List<RdpsGeometryEntity> rdpsEntityList = rdpsGeometryRepository
                .findByDivisionIdAndActiveStatusTrue(divisionId);
        if (CollectionUtils.isEmpty(rdpsEntityList)) {
            return Collections.emptyList();
        }

        return rdpsEntityList;
    }

    @Override
    public String deleteRdpsData(String rdpsId) {
        // Fetch the entity by rdpsId
        Optional<RdpsGeometryEntity> rdpsEntityOptional = rdpsGeometryRepository.findById((new ObjectId(rdpsId)));

        if (rdpsEntityOptional.isPresent()) {
            RdpsGeometryEntity rdpsEntity = rdpsEntityOptional.get();

            // Set active_status to false to perform a soft delete
            rdpsEntity.setActiveStatus(false);

            // Save the updated entity back to the database
            rdpsGeometryRepository.save(rdpsEntity);

            return "Record with rdpsId " + rdpsId + " has been successfully deleted.";
        } else {
            // Handle the case where the rdpsId does not exist
            return "Record with rdpsId " + rdpsId + " not found.";
        }
    }

    @Override
    // Find an entity by ID
    public Optional<RdpsGeometryEntity> findById(String id) {
        return rdpsGeometryRepository.findById(new ObjectId(id));
    }

    @Override
    // Save an entity to the database
    public RdpsGeometryEntity save(RdpsGeometryEntity entity) {
        return rdpsGeometryRepository.save(entity);
    }

    public Optional<FileUploadResultResponse> addRdpsData(String filePath, String divisionId) {
        int successRow = 0;
        int errorRow = 0;

        try (BufferedReader fileReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(fileReader);

            List<RdpsGeometryEntity> rdpsData = new ArrayList<>();

            for (CSVRecord record : parser) {
                if (!validateRecord(record)) {
                    System.out.println("Invalid record found in file: " + filePath);
                    errorRow++;
                    continue;
                }

                RdpsGeometryEntity rdpsRecord = new RdpsGeometryEntity();
                rdpsRecord.setActiveStatus(true);
                rdpsRecord.setKilometer(Long.valueOf(record.get("Km")));
                rdpsRecord.setDistance(Long.valueOf(record.get("Dist")));
                rdpsRecord.setLatitude(dmsToDecimal(record.get("Latitude")));
                rdpsRecord.setLongitude(dmsToDecimal(record.get("Longitude")));
                rdpsRecord.setFeatureDetail(record.get("Feature Detail"));
                rdpsRecord.setFeatureCode(parseFeatureCode(record.get("Feature Code")));
                rdpsRecord.setSection(record.get("Section Name"));
                rdpsRecord.setDivisionId(divisionId);

                // Set GeoLocation
                GeoLocation geo = new GeoLocation();
                List<Double> coordinates = new ArrayList<>();
                coordinates.add(dmsToDecimal(record.get("Longitude")));
                coordinates.add(dmsToDecimal(record.get("Latitude")));
                geo.setCoordinates((ArrayList<Double>) coordinates); // Set longitude and latitude
                geo.setType("Point");
                rdpsRecord.setGeoLocation(geo); // Set the GeoLocation in rdpsRecord

                // Set Feature Image
                rdpsRecord.setFeatureImage(getFeatureImage(parseFeatureCode(record.get("Feature Code"))));

                rdpsData.add(rdpsRecord);
                successRow++;
            }

            if (!rdpsData.isEmpty()) {
                rdpsGeometryRepository.saveAll(rdpsData);
                System.out.println("RDPS data added successfully for file: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error processing file " + filePath + ": " + e.getMessage());
        }
        return Optional
                .of(FileUploadResultResponse.builder().validRecords(successRow).invalidRecords(errorRow).build());
    }

    // private boolean validateRecord(CSVRecord record) {
    // try {
    // return record.isMapped("Km") && record.isMapped("Dist") && record.isMapped("Latitude")
    // && record.isMapped("Longitude") && record.isMapped("Feature Detail")
    // && record.isMapped("Section Name") && Double.parseDouble(record.get("Km")) >= 0
    // && Double.parseDouble(record.get("Dist")) >= 0 && isValidDMS(record.get("Latitude"))
    // && isValidDMS(record.get("Longitude"));
    // } catch (Exception e) {
    // return false;
    // }
    // }

    private boolean validateRecord(CSVRecord record) {
        try {
            // Check if required fields are mapped
            if (!record.isMapped("Km")) {
                System.err.println("Validation error: Missing 'Km' column.");
                return false;
            }
            if (!record.isMapped("Dist")) {
                System.err.println("Validation error: Missing 'Dist' column.");
                return false;
            }
            if (!record.isMapped("Latitude")) {
                System.err.println("Validation error: Missing 'Latitude' column.");
                return false;
            }
            if (!record.isMapped("Longitude")) {
                System.err.println("Validation error: Missing 'Longitude' column.");
                return false;
            }
            if (!record.isMapped("Feature Detail")) {
                System.err.println("Validation error: Missing 'Feature Detail' column.");
                return false;
            }
            if (!record.isMapped("Feature Code")) {
                System.err.println("Validation error: Missing 'Feature Code' column.");
                return false;
            }
            if (!record.isMapped("Section Name")) {
                System.err.println("Validation error: Missing 'Section Name' column.");
                return false;
            }

            // Validate numeric fields
            try {
                double kilometer = Double.parseDouble(record.get("Km"));
                if (kilometer < 0) {
                    System.err.println("Validation error: 'Km' contains a negative value.");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.err.println("Validation error: 'Km' contains invalid numeric data.");
                return false;
            }

            try {
                double distance = Double.parseDouble(record.get("Dist"));
                if (distance < 0) {
                    System.err.println("Validation error: 'Dist' contains a negative value.");
                    return false;
                }
            } catch (NumberFormatException e) {
                System.err.println("Validation error: 'Dist' contains invalid numeric data.");
                return false;
            }

            // Validate DMS fields
            String latitude = record.get("Latitude");
            if (!isValidDMS(latitude)) {
                System.err.println("Validation error: 'Latitude' contains invalid DMS format: " + latitude);
                return false;
            }

            String longitude = record.get("Longitude");
            if (!isValidDMS(longitude)) {
                System.err.println("Validation error: 'Longitude' contains invalid DMS format: " + longitude);
                return false;
            }

            return true;

        } catch (Exception e) {
            // Log unexpected validation exceptions
            System.err.println("Unexpected validation error: " + e.getMessage());
            return false;
        }
    }

    // private boolean isValidDMS(String dms) {
    // String pattern = "\\d+°\\d+'\\d+\\.\\d+\"[NSEW]";
    // return dms.matches(pattern);
    // }

    private boolean isValidDMS(String dms) {
        // Regex to match DMS format: 123°45'67.89"N or similar
        String dmsPattern = "^(\\d{1,3})°(\\d{1,2})'(\\d{1,2}(\\.\\d+)?)\"?[NSEW]$";
        return dms != null && dms.matches(dmsPattern);
    }

    private double dmsToDecimal(String dms) {
        try {
            Pattern pattern = Pattern.compile("(\\d+)°(\\d+)'(\\d+\\.\\d+)\"([NSEW])");
            Matcher matcher = pattern.matcher(dms);

            if (!matcher.find()) {
                throw new IllegalArgumentException("Invalid DMS format: " + dms);
            }

            int degrees = Integer.parseInt(matcher.group(1));
            int minutes = Integer.parseInt(matcher.group(2));
            double seconds = Double.parseDouble(matcher.group(3));
            String direction = matcher.group(4);

            double decimal = degrees + minutes / 60.0 + seconds / 3600.0;
            if ("SW".contains(direction)) {
                decimal *= -1;
            }
            return decimal;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting DMS to decimal: " + dms, e);
        }
    }

    private int parseFeatureCode(String featureCode) {
        try {
            return Integer.parseInt(featureCode);
        } catch (NumberFormatException e) {
            return 999; // Default invalid feature code
        }
    }

    private Document createGeoLocation(double longitude, double latitude) {
        return new Document("type", "Point").append("coordinates", Arrays.asList(longitude, latitude));
    }

    private String getFeatureImage(int featureCode) {
        String imageFolderPath = "~/Images/FeatureCodePhoto/";
        switch (featureCode) {
        case 1:
            return imageFolderPath + "fc_150.png";
        case 2:
            return imageFolderPath + "fc_2.png";
        case 3:
            return imageFolderPath + "fc_11.png";
        case 9:
            return imageFolderPath + "fc_42.png";
        case 10:
        case 11:
            return imageFolderPath + "fc_102.png";
        case 21:
            return imageFolderPath + "fc_12.png";
        case 22:
        case 23:
            return imageFolderPath + "fc_107.png";
        case 24:
            return imageFolderPath + "fc_24.png";
        case 26:
            return imageFolderPath + "fc_44.png";
        case 27:
            return imageFolderPath + "fc_109.png";
        default:
            return imageFolderPath + "ic_default_featurecode.png";
        }
    }
}
