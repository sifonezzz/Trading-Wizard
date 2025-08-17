package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class SetupDetailController implements Controller {

    @FXML private Label setupNameLabel;
    @FXML private TextArea descriptionArea;
    
    private MainApp mainApp;
    private SetupSample currentSetup;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    // This method is called to pass the selected setup from the previous screen
    public void setSetup(SetupSample setup) {
        this.currentSetup = setup;
        setupNameLabel.setText(setup.getName());
        descriptionArea.setText(setup.getDescription());
    }

    @FXML
    private void handleSaveDescription() {
        currentSetup.setDescription(descriptionArea.getText());
        MainApp.getDataManager().saveSetupSamples();
        // Optionally, show a confirmation message
    }

    @FXML
    private void showWonSetups() {
        // Placeholder for the next phase
        System.out.println("Show Won Setups for: " + currentSetup.getName());
    }

    @FXML
    private void showLostSetups() {
        // Placeholder for the next phase
        System.out.println("Show Lost Setups for: " + currentSetup.getName());
    }
    
    @FXML
    private void handleBack() {
        mainApp.showSetups();
    }

}