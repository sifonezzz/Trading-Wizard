package com.tdf.data;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Settings {
    public double maxLoss;
    public int ruleIntervalSeconds;
    public int rulesScreenSeconds; // NEW FIELD
    public List<String> rules;
    public List<String> tasks;
    public List<String> setups;
    public List<String> activeWidgets;
    public boolean showWinRate; 

    public static Settings createDefault() {
        Settings settings = new Settings();
        settings.maxLoss = 100.00;
        settings.ruleIntervalSeconds = 10;
        settings.rulesScreenSeconds = 30; // Default to 30 seconds
        settings.showWinRate = false;
        settings.rules = new ArrayList<>(Arrays.asList(
            "Hyperscalp ONLY the dip after a strong push.",
            "Do not ignore your SPECIFIC ENTRY POINTS."
        ));
        settings.tasks = new ArrayList<>(Arrays.asList(
            "Open a stock scanner.",
            "Start your 4 chart watchlist."
        ));
        settings.setups = new ArrayList<>(Arrays.asList(
            "Accumulations (Small Caps)",
            "Wash Reclaims (Small Caps)"
        ));
        settings.activeWidgets = new ArrayList<>();
        return settings;
    }
}