package com.example.foodallergy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecognitionResponse {
    private String code;
    private String message;
    private RecognisedFood recognised_food;

    // Getters and setters
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @JsonProperty("message")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonProperty("recognised_food")
    public RecognisedFood getRecognisedFood() {
        return recognised_food;
    }

    public void setRecognisedFood(RecognisedFood recognised_food) {
        this.recognised_food = recognised_food;
    }
}
