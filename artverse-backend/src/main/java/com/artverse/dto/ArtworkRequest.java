package com.artverse.dto;

import jakarta.validation.constraints.NotBlank;

public class ArtworkRequest {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String category;

    public ArtworkRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}