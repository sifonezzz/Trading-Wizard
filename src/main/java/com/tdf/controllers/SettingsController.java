package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SettingsController implements Controller {

    @FXML private TextField maxLossField;
    @FXML private TextField intervalField;
    @FXML private TextField rulesScreenTimeField;
    @FXML private CheckBox winRateCheckbox; // New field
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
        rulesScreenTimeField.setText(String.valueOf(currentSettings.rulesScreenSeconds));
        winRateCheckbox.setSelected(currentSettings.showWinRate); // Load new value
        tasksArea.setText(String.join("\n", currentSettings.tasks));
        setupsArea.setText(String.join("\n", currentSettings.setups));
        rulesArea.setText(String.join("\n", currentSettings.rules));
    }

    @FXML
    private void handleSave() {
        try {
            currentSettings.maxLoss = Double.parseDouble(maxLossField.getText());
            currentSettings.ruleIntervalSeconds = Integer.parseInt(intervalField.getText());
            currentSettings.rulesScreenSeconds = Integer.parseInt(rulesScreenTimeField.getText());
            currentSettings.showWinRate = winRateCheckbox.isSelected(); // Save new value
            currentSettings.tasks = Arrays.stream(tasksArea.getText().split("\\n")).collect(Collectors.toList());
            currentSettings.setups = Arrays.stream(setupsArea.getText().split("\\n")).collect(Collectors.toList());
            currentSettings.rules = Arrays.stream(rulesArea.getText().split("\\n")).collect(Collectors.toList());

            dataManager.saveSettings(currentSettings);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Settings saved successfully!");
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Input", "All numeric fields must contain valid numbers.");
        }
    }
    
    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Backup");
        fileChooser.setInitialFileName("TradingWizard_Backup_" + LocalDate.now() + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archives", "*.zip"));
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            if (dataManager.createBackup(file)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data backed up successfully to:\n" + file.getAbsolutePath());
            } else {
                showAlert(Alert.AlertType.ERROR, "Backup Failed", "An error occurred while creating the backup.");
            }
        }
    }

    @FXML
    private void handleRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Backup");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP Archives", "*.zip"));
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());

        if (file != null) {
            if (dataManager.restoreFromBackup(file)) {
                loadSettings();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Data restored successfully from backup.\nPlease review the loaded settings.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Restore Failed", "An error occurred while restoring from the backup.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}