package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.Goal;
import com.tdf.data.Settings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.*;

public class AddWidgetsController implements Controller {

    private record Cell(int x, int y) {}

    @FXML private FlowPane widgetSelectionPane;
    private MainApp mainApp;
    private final Map<String, CheckBox> widgetCheckBoxes = new HashMap<>();
    
    private static final List<String> AVAILABLE_WIDGETS = new ArrayList<>(Arrays.asList(
        "Latest Journal Note",
        "Previous Day PNL",
        "Current Month Calendar",
        "Monthly Undiscipline Counter",
        "Weekly/Monthly PNL Tracker",
        "PNL Streak Counter",
        "Best/Worst Day (Monthly)",
        "Rule of the Day"
    ));

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        populateWidgetSelection();
    }
    
    private void populateWidgetSelection() {
        Settings settings = MainApp.getDataManager().getSettings();
        List<String> activeWidgets = settings.activeWidgets != null ? settings.activeWidgets : new ArrayList<>();
        Set<String> activeWidgetNames = new HashSet<>();
        for (String layoutInfo : activeWidgets) {
            activeWidgetNames.add(layoutInfo.split(";")[0]);
        }

        List<String> dynamicWidgetList = new ArrayList<>(AVAILABLE_WIDGETS);
        List<Goal> goals = MainApp.getDataManager().getGoals();
        
        // DEBUG: Print number of loaded goals to the console
        System.out.println("Found " + goals.size() + " goals.");

        Set<Goal.GoalType> existingGoalTypes = new HashSet<>();
        for (Goal goal : goals) {
            if (goal != null && !existingGoalTypes.contains(goal.getType())) {
                dynamicWidgetList.add("Goal: " + goal.getDescription());
                existingGoalTypes.add(goal.getType());
            }
        }
        
        for (String widgetName : dynamicWidgetList) {
            VBox previewBox = new VBox(10);
            previewBox.getStyleClass().add("note-box");
            previewBox.setPrefSize(250, 180);
            previewBox.setAlignment(Pos.TOP_CENTER);

            Label previewLabel = new Label("Preview: " + widgetName);
            previewLabel.getStyleClass().add("h3");
            previewLabel.setWrapText(true);
            
            CheckBox checkBox = new CheckBox("Add to Dashboard");
            checkBox.setSelected(activeWidgetNames.contains(widgetName));
            checkBox.setMaxWidth(Double.MAX_VALUE);
            checkBox.getStyleClass().add("widget-checkbox");
            
            Pane spacer = new Pane();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            VBox.setMargin(checkBox, new Insets(0, 10, 10, 10));
            
            widgetCheckBoxes.put(widgetName, checkBox);
            previewBox.getChildren().addAll(previewLabel, spacer, checkBox);
            widgetSelectionPane.getChildren().add(previewBox);
        }
    }
    
    @FXML
    private void handleSave() {
        Settings settings = MainApp.getDataManager().getSettings();
        List<String> newActiveWidgets = new ArrayList<>();
        
        Set<Cell> occupiedCells = new HashSet<>();
        if (settings.activeWidgets != null) {
            for (String layoutInfo : settings.activeWidgets) {
                String[] parts = layoutInfo.split(";");
                if (parts.length == 3) {
                    occupiedCells.add(new Cell(Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
                }
            }
        }

        for (Map.Entry<String, CheckBox> entry : widgetCheckBoxes.entrySet()) {
            String widgetName = entry.getKey();
            boolean isSelected = entry.getValue().isSelected();
            
            String existingEntry = Optional.ofNullable(settings.activeWidgets).orElse(new ArrayList<>()).stream()
                .filter(s -> s.startsWith(widgetName + ";"))
                .findFirst().orElse(null);
                
            if (isSelected) {
                if (existingEntry != null) {
                    newActiveWidgets.add(existingEntry);
                } else {
                    Cell emptyCell = findFirstEmptyCell(occupiedCells);
                    newActiveWidgets.add(String.format("%s;%d;%d", widgetName, emptyCell.x(), emptyCell.y()));
                    occupiedCells.add(emptyCell);
                }
            }
        }
        
        settings.activeWidgets = newActiveWidgets;
        MainApp.getDataManager().saveSettings(settings);
        
        mainApp.showDashboard();
    }

    private Cell findFirstEmptyCell(Set<Cell> occupiedCells) {
        for (int r = 0; r < 10; r++) { 
            for (int c = 0; c < 3; c++) {
                Cell cell = new Cell(c, r);
                if (!occupiedCells.contains(cell)) {
                    return cell;
                }
            }
        }
        return new Cell(0, 0);
    }
}