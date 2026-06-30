package com.primesys.adminserviceserver.service;

import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface ExcelService {
    List<List<String>> readBeatExcelData(InputStream inputStream) throws IOException;

    String validateBeatFile(String filePath);

    List<List<String>> readDeviceExcelData(InputStream inputStream) throws IOException;

    String validateSendCommandFile(String s);

    List<DeviceCommandHistoryEntity> readSendCommandExcelData(FileInputStream fileInputStream);

    // String validateDeviceNoBeatFile(String s);

    List<List<String>> readBeatDeviceNoExcelData(FileInputStream fileInputStream) throws IOException;
}
