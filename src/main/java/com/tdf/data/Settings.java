// FILE: src/main/java/com/tdf/data/Settings.java
package com.tdf.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Settings {
    public double maxLoss;
    public int ruleIntervalSeconds;
    public int rulesScreenSeconds;
    public boolean showWinRate;
    public boolean animatedBackground;
    public List<String> rules;
    public List<String> tasks;
    public List<String> setups;
    public List<String> activeWidgets;

    // --- NEW FIELDS for Panic Button ---
    public boolean panicButtonEnabled;
    public int panicButtonDurationSeconds;

    public static Settings createDefault() {
        Settings settings = new Settings();
        settings.maxLoss = 100.00;
        settings.ruleIntervalSeconds = 10;
        settings.rulesScreenSeconds = 30;
        settings.showWinRate = false;
        settings.animatedBackground = false;
        // --- NEW DEFAULTS ---
        settings.panicButtonEnabled = true;
        settings.panicButtonDurationSeconds = 60;
        // ... (rest of the default settings)
        settings.rules = new ArrayList<>(Arrays.asList("Rule 1...", "Rule 2..."));
        settings.tasks = new ArrayList<>(Arrays.asList("Task 1...", "Task 2..."));
        settings.setups = new ArrayList<>(Arrays.asList("Setup 1...", "Setup 2..."));
        settings.activeWidgets = new ArrayList<>();
        return settings;
    }
}