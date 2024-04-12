package com.example.foodallergy.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecognisedFood {
    private String ingredients;
    private String name;

    // Getters and setters
    @JsonProperty("ingredients")
    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
