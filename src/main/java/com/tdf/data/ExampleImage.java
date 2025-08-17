package com.tdf.data;

import java.util.List;

public class ExampleImage {
    private String imagePath; // Path to the image file relative to the app data dir
    private List<String> tags;

    public ExampleImage(String imagePath, List<String> tags) {
        this.imagePath = imagePath;
        this.tags = tags;
    }

    // Getters
    public String getImagePath() { return imagePath; }
    public List<String> getTags() { return tags; }
}