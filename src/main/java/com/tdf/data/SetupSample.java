package com.tdf.data;

import java.util.ArrayList;
import java.util.List;

public class SetupSample {
    private String name;
    private String description;
    private List<ExampleImage> wonExamples;
    private List<ExampleImage> lostExamples;

    public SetupSample(String name) {
        this.name = name;
        this.description = "";
        this.wonExamples = new ArrayList<>();
        this.lostExamples = new ArrayList<>();
    }

    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<ExampleImage> getWonExamples() { return wonExamples; }
    public List<ExampleImage> getLostExamples() { return lostExamples; }

    // Setters
    public void setDescription(String description) { this.description = description; }

    // Logic
    public double getWinRate() {
        int wins = wonExamples.size();
        int losses = lostExamples.size();
        if (wins + losses == 0) {
            return 0.0;
        }
        return ((double) wins / (wins + losses)) * 100.0;
    }
}