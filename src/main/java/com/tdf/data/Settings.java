package com.tdf.data;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

public class Settings {
    public double maxLoss;
    public int ruleIntervalSeconds;
    public String gifPath;
    public String soundPath;
    public List<String> rules;
    public List<String> tasks;
    public List<String> setups;

    public static Settings createDefault() {
        Settings settings = new Settings();
        settings.maxLoss = 100.00;
        settings.ruleIntervalSeconds = 10;
        settings.gifPath = "";
        settings.soundPath = "";
        settings.rules = new ArrayList<>(Arrays.asList(
            "Hyperscalp ONLY the dip after a strong push.",
            "Do not ignore your SPECIFIC ENTRY POINTS.",
            "Do NOT OVERSIZE.",
            "Smaller size on not so sure setups.",
            "Do NOT anticipate, WAIT for your setups and specific entry points.",
            "Remember your progressive behavioral approach into rotation analysis. (key levels and symmetry)",
            "Discretionally sized bets."
        ));
        settings.tasks = new ArrayList<>(Arrays.asList(
            "Open a stock scanner.",
            "Start your 4 chart watchlist and position it on your left monitor.",
            "Open discord and position it on your right monitor.",
            "Open Medved ladder along tastytrade interface."
        ));
        settings.setups = new ArrayList<>(Arrays.asList(
            "Accumulations (Small Caps)",
            "Wash Reclaims (Small Caps)",
            "Accumulations (Large Caps)",
            "Hyperscalping, THE DIP AFTER A STRONG PUSH (Large Caps)."
        ));
        return settings;
    }
}