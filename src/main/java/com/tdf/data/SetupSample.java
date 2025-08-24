// FILE: src/main/java/com/tdf/data/SetupSample.java
package com.tdf.data;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SetupSample {
    private String name;
    private String description;
    private List<ExampleImage> wonExamples;
    private List<ExampleImage> lostExamples;

    // --- NEW FIELDS ---
    private List<String> tags;
    private boolean isArchived;
    private boolean isFavorite;
    private String lastModified; // ISO 8601 format

    public SetupSample(String name) {
        this.name = name;
        this.description = "";
        this.wonExamples = new ArrayList<>();
        this.lostExamples = new ArrayList<>();
        // --- NEW INITIALIZATIONS ---
        this.tags = new ArrayList<>();
        this.isArchived = false;
        this.isFavorite = false;
        updateLastModified();
    }

    // --- GETTERS ---
    public String getName() { return name; }
    public String getDescription() { return description; }
    public List<ExampleImage> getWonExamples() { return wonExamples; }
    public List<ExampleImage> getLostExamples() { return lostExamples; }
    public List<String> getTags() { return tags; }
    public boolean isArchived() { return isArchived; }
    public boolean isFavorite() { return isFavorite; }
    public String getLastModified() { return lastModified; }

    // --- SETTERS ---
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public void setArchived(boolean archived) { isArchived = archived; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    // --- LOGIC ---
    public final void updateLastModified() {
        this.lastModified = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
    
    public double getWinRate() {
        int wins = wonExamples.size();
        int losses = lostExamples.size();
        if (wins + losses == 0) {
            return 0.0;
        }
        return ((double) wins / (wins + losses)) * 100.0;
    }
}