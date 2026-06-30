package com.primesys.adminservicecommon.error.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum ErrorCode {
    NOT_FOUND(10000, "Unable to find record for given details"), EMPTY_FILE(10001, "No records in file"),
    FILE_PROCESSING_FAILED(10002, "File processing failed"), FILE_RENDERING_FAILED(10003, "File processing failed"),
    DEVICE_DUPLICATE_FAILED(10010, "New Device IMEI is already assigned to another device. Please remove it."),
    DIVISION_DUPLICATE_FAILED(10020, "The User-Name specified is already in use."),
    DIVISION_NOTFOUND_FAILED(10030, "The User-Name specified as parent does not found please check Parent Hierarchy."),
    INVALID_BEAT_DATA(10045, "The device beat request data invalid."),
    INVALID_FILE_TYPE(19190, "The file type is not a CSV. Please verify the file extension.");

    private final int value;
    private final String message;
}
