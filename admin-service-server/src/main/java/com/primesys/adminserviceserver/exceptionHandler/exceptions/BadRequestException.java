package com.primesys.adminserviceserver.exceptionHandler.exceptions;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
