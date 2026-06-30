package com.primesys.adminserviceserver.response;

import com.primesys.adminservicecommon.error.message.ErrorCode;
import lombok.Data;

import java.io.Serializable;

@Data
public class ErrorResponse implements Serializable {
    private Integer code;
    private String message;

    public ErrorResponse(ErrorCode errorCode) {
        this.code = errorCode.getValue();
        this.message = errorCode.getMessage();
    }

    public ErrorResponse(int errorCode, String error_msg) {
        this.code = errorCode;
        this.message = error_msg;
    }

    public ErrorResponse() {

    }
}
