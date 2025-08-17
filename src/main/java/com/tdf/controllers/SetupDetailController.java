package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SetupDetailController implements Controller {

    @FXML private Label setupNameLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Button editDescriptionButton;
    @FXML private Button saveDescriptionButton;
    @FXML private VBox descriptionContainer;
    
    private MainApp mainApp;
    private SetupSample currentSetup;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    public void setSetup(SetupSample setup) {
        this.currentSetup = setup;
        setupNameLabel.setText(setup.getName());
        descriptionArea.setText(setup.getDescription());
        setEditMode(false); // Start in view mode
    }

    @FXML
    private void handleEditDescription() {
        setEditMode(true);
    }

    @FXML
    private void handleSaveDescription() {
        currentSetup.setDescription(descriptionArea.getText());
        MainApp.getDataManager().saveSetupSamples();
        setEditMode(false);
    }

    private void setEditMode(boolean editing) {
        descriptionArea.setEditable(editing);
        editDescriptionButton.setVisible(!editing);
        saveDescriptionButton.setVisible(editing);
        
        if (editing) {
            descriptionArea.getStyleClass().remove("non-editable-text");
            descriptionArea.getStyleClass().add("editable-text");
        } else {
            descriptionArea.getStyleClass().remove("editable-text");
            descriptionArea.getStyleClass().add("non-editable-text");
        }
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