package com.tdf.data;

import java.util.UUID;

public class Goal {
    public enum GoalType { PNL_MONTHLY, BEHAVIOR_WEEKLY }
    public enum GoalStatus { IN_PROGRESS, COMPLETED, FAILED }

    private String id;
    private GoalType type;
    private String description;
    private double targetValue;
    private double currentValue;
    private long lastResetTimestamp;
    
    // FIX: Added required no-argument constructor for data loading
    public Goal() {
    }

    public Goal(GoalType type, String description, double targetValue) {
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.description = description;
        this.targetValue = targetValue;
        this.currentValue = 0;
        this.lastResetTimestamp = System.currentTimeMillis();
    }

    // Getters
    public String getId() { return id; }
    public GoalType getType() { return type; }
    public String getDescription() { return description; }
    public double getTargetValue() { return targetValue; }
    public double getCurrentValue() { return currentValue; }
    public long getLastResetTimestamp() { return lastResetTimestamp; }

    // Setters
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }
    public void setLastResetTimestamp(long lastResetTimestamp) { this.lastResetTimestamp = lastResetTimestamp; }
}