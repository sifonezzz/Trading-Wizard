package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddWidgetsController implements Controller {

    @FXML private FlowPane widgetSelectionPane;
    private MainApp mainApp;
    private final Map<String, CheckBox> widgetCheckBoxes = new HashMap<>();
    private static final String[] AVAILABLE_WIDGETS = {
        "Latest Journal Note",
        "Previous Day PNL",
        "Current Month Calendar",
        "Monthly Undiscipline Counter"
    };

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        populateWidgetSelection();
    }
    
    private void populateWidgetSelection() {
        Settings settings = MainApp.getDataManager().getSettings();
        List<String> activeWidgets = settings.activeWidgets != null ? settings.activeWidgets : new ArrayList<>();

        for (String widgetName : AVAILABLE_WIDGETS) {
            VBox previewBox = new VBox(10);
            previewBox.getStyleClass().add("note-box");
            previewBox.setPrefSize(250, 180);
            previewBox.setAlignment(Pos.TOP_CENTER);

            Label previewLabel = new Label("Preview: " + widgetName);
            previewLabel.getStyleClass().add("h3");
            previewLabel.setWrapText(true);
            
            CheckBox checkBox = new CheckBox("Add to Dashboard");
            checkBox.setSelected(activeWidgets.contains(widgetName));
            checkBox.setMaxWidth(Double.MAX_VALUE);
            // --- THIS IS THE FIX ---
            // 1. Apply a new, smaller style class
            checkBox.getStyleClass().add("widget-checkbox");
            
            // 2. Create a spacer pane that will grow and push the checkbox to the bottom
            Pane spacer = new Pane();
            VBox.setVgrow(spacer, Priority.ALWAYS);
            
            // 3. Add a small margin to the bottom of the checkbox
            VBox.setMargin(checkBox, new Insets(0, 10, 10, 10));
            // --------------------
            
            widgetCheckBoxes.put(widgetName, checkBox);
            
            // Add elements in the new order: title, spacer, checkbox
            previewBox.getChildren().addAll(previewLabel, spacer, checkBox);
            widgetSelectionPane.getChildren().add(previewBox);
        }
    }
    
    @FXML
    private void handleSave() {
        Settings settings = MainApp.getDataManager().getSettings();
        List<String> newActiveWidgets = new ArrayList<>();
        
        for (Map.Entry<String, CheckBox> entry : widgetCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                newActiveWidgets.add(entry.getKey());
            }
        }
        
        settings.activeWidgets = newActiveWidgets;
        MainApp.getDataManager().saveSettings(settings);
        
        mainApp.showDashboard();
    }
}