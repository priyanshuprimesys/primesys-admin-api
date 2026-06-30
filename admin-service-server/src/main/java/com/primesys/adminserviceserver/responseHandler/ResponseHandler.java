package com.primesys.adminserviceserver.responseHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResponseHandler {

    private ResponseHandler() {
        throw new UnsupportedOperationException("Response Utility");
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(List<T> data, boolean success,
            String message, HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", data);
        response.put("data", result);
        response.put("message", message);
        response.put("code", httpStatus.value());
        response.put("success", success);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(Object data, boolean success, String message,
            HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", data);
        response.put("data", result);
        response.put("message", message);
        response.put("code", httpStatus.value());
        response.put("success", success);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(List<T> data, boolean success,
            HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", data);
        response.put("data", result);
        response.put("message", data.size() + " Data fetched successfully");
        response.put("code", httpStatus.value());
        response.put("success", success);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(String message, boolean success,
            HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", message);
        response.put("data", result);
        response.put("message", message);
        response.put("code", httpStatus.value());
        response.put("success", success);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(Object error, String message,
            HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", error);
        response.put("data", result);
        response.put("message", message);
        response.put("code", httpStatus.value());
        response.put("success", false);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(String message, String error,
            HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", message);
        response.put("data", result);
        response.put("message", error);
        response.put("code", httpStatus.value());
        response.put("success", false);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }

    public static <T> ResponseEntity<Map<String, Object>> generateResponse(String message, HttpStatus httpStatus) {
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> result = new HashMap<>();
        result.put("result", message);
        response.put("data", result);
        response.put("message", "Error Occurred");
        response.put("code", httpStatus.value());
        response.put("success", false);
        return new ResponseEntity<Map<String, Object>>(response, httpStatus);
    }
}
