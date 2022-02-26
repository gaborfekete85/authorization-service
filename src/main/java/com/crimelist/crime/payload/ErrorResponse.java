package com.crimelist.crime.payload;

import java.util.Map;

public class ErrorResponse {
    private String message;
    private String errorCode;
    private Map<String, Object> values;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResponse(String errorCode, Map<String, Object> values) {
        this.errorCode = errorCode;
        this.values = values;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
}
