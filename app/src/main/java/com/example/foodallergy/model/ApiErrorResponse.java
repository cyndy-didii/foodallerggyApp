package com.example.foodallergy.model;

public class ApiErrorResponse {
    private String code;
    private String message;

    // Constructors
    public ApiErrorResponse() {}

    public ApiErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getter and Setter methods
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // toString method for printing
    @Override
    public String toString() {
        return "ApiResponse{" +
                "code='" + code + '\'' +
                ", message='" + message + '\'' +
                '}';
    }

}
