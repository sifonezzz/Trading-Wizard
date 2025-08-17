package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SettingsController implements Controller {

    @FXML private TextField maxLossField;
    @FXML private TextField intervalField;
    @FXML private TextField gifPathField;
    @FXML private TextField soundPathField;
    @FXML private TextArea tasksArea;
    @FXML private TextArea setupsArea;
    @FXML private TextArea rulesArea;

    private MainApp mainApp;
    private DataManager dataManager;
    private Settings currentSettings;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        loadSettings();
    }

    private void loadSettings() {
        currentSettings = dataManager.getSettings();
        maxLossField.setText(String.format("%.2f", currentSettings.maxLoss));
        intervalField.setText(String.valueOf(currentSettings.ruleIntervalSeconds));
        gifPathField.setText(currentSettings.gifPath);
        soundPathField.setText(currentSettings.soundPath);
        tasksArea.setText(String.join("\n", currentSettings.tasks));
        setupsArea.setText(String.join("\n", currentSettings.setups));
        rulesArea.setText(String.join("\n", currentSettings.rules));
    }

    @FXML
    private void handleSave() {
        try {
            currentSettings.maxLoss = Double.parseDouble(maxLossField.getText());
            currentSettings.ruleIntervalSeconds = Integer.parseInt(intervalField.getText());
            currentSettings.gifPath = gifPathField.getText();
            currentSettings.soundPath = soundPathField.getText();
            currentSettings.tasks = Arrays.stream(tasksArea.getText().split("\\n")).collect(Collectors.toList());
            currentSettings.setups = Arrays.stream(setupsArea.getText().split("\\n")).collect(Collectors.toList());
            currentSettings.rules = Arrays.stream(rulesArea.getText().split("\\n")).collect(Collectors.toList());

            dataManager.saveSettings(currentSettings);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Settings saved successfully!");
            alert.showAndWait();
            
            // FIX: Removed the call to mainApp.showMainMenu() which caused the crash.

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Input");
            alert.setHeaderText("Please check your numbers.");
            alert.setContentText("Max Loss and Interval must be valid numbers.");
            alert.showAndWait();
        }
    }
    
    @FXML
    private void browseGif() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select GIF File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GIF Files", "*.gif"));
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (file != null) {
            gifPathField.setText(file.getAbsolutePath());
        }
    }
    
    @FXML
    private void browseSound() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Sound File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Sound Files", "*.wav", "*.mp3"));
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (file != null) {
            soundPathField.setText(file.getAbsolutePath());
        }
    }
}