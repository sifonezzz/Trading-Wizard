// FILE: src/main/java/com/tdf/controllers/SetupDetailController.java
package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SetupDetailController implements Controller {

    @FXML private Label setupNameLabel;
    @FXML private TextArea descriptionArea;
    @FXML private VBox descriptionContainer;
    @FXML private Label wonCountLabel;
    @FXML private Label lostCountLabel;
    
    // --- NEW FXML FIELDS ---
    @FXML private TextField nameField;
    @FXML private TextField tagsField;
    
    private MainApp mainApp;
    private SetupSample currentSetup;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    public void setSetup(SetupSample setup) {
        this.currentSetup = setup;
        setupNameLabel.setText(setup.getName());
        
        // --- NEW: Populate new fields ---
        nameField.setText(setup.getName());
        descriptionArea.setText(setup.getDescription());
        if (setup.getTags() != null) {
            tagsField.setText(String.join(", ", setup.getTags()));
        }
        
        int wonCount = setup.getWonExamples().size();
        wonCountLabel.setText(wonCount + (wonCount == 1 ? " Example" : " Examples"));
        
        int lostCount = setup.getLostExamples().size();
        lostCountLabel.setText(lostCount + (lostCount == 1 ? " Example" : " Examples"));
    }

    // --- RENAMED & UPDATED: handleSave ---
    @FXML
    private void handleSave() {
        // Update all fields from the UI
        currentSetup.setName(nameField.getText());
        currentSetup.setDescription(descriptionArea.getText());
        currentSetup.setTags(
            Arrays.stream(tagsField.getText().split(","))
                  .map(String::trim)
                  .filter(tag -> !tag.isEmpty())
                  .collect(Collectors.toList())
        );
        currentSetup.updateLastModified();
        
        MainApp.getDataManager().saveSetupSamples();
        
        // Update the main title label after saving a new name
        setupNameLabel.setText(currentSetup.getName());

        // Show confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Setup details have been saved successfully.");
        alert.showAndWait();
    }

    @FXML
    private void showWonSetups() {
        mainApp.showExamplesView(currentSetup, "Won");
    }

    @FXML
    private void showLostSetups() {
        mainApp.showExamplesView(currentSetup, "Lost");
    }
    
    @FXML
    private void handleBack() {
        mainApp.showSetups();
    }
}