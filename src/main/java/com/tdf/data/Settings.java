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

    public static Settings createDefault() {
        Settings settings = new Settings();
        settings.maxLoss = 100.00;
        settings.ruleIntervalSeconds = 10;
        settings.rulesScreenSeconds = 30;
        settings.showWinRate = false;
        settings.animatedBackground = false;
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