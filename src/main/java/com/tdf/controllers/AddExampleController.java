package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.ExampleImage;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AddExampleController implements Controller {

    @FXML private Label titleLabel;
    @FXML private ImageView imageView;
    @FXML private TextField tagsField;

    private MainApp mainApp;
    private SetupSample currentSetup;
    private String exampleType;
    private File selectedImageFile;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setContext(SetupSample setup, String type) {
        this.currentSetup = setup;
        this.exampleType = type;
        titleLabel.setText("Add " + type + " Example to " + setup.getName());
    }

    @FXML
    private void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif")
        );
        File file = fileChooser.showOpenDialog(mainApp.getPrimaryStage());
        if (file != null) {
            selectedImageFile = file;
            imageView.setImage(new Image(file.toURI().toString()));
        }
    }
    
    private void addTag(String tag) {
        String currentTags = tagsField.getText();
        if (currentTags.isEmpty()) {
            tagsField.setText(tag);
        } else {
            tagsField.setText(currentTags + ", " + tag);
        }
    }

    @FXML private void addAPlusTag() { addTag("A+"); }
    @FXML private void addATag() { addTag("A"); }
    @FXML private void addBPlusTag() { addTag("B+"); }
    @FXML private void addCPlusTag() { addTag("C+"); }

    @FXML
    private void handleSave() {
        if (selectedImageFile == null) {
            showAlert("No Image Selected", "Please select an image file first.");
            return;
        }

        try {
            // Create a unique filename and copy the image to the app's data directory
            String extension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            Path targetDir = Paths.get(MainApp.getDataManager().getBaseDir(), "images");
            Files.createDirectories(targetDir); // Ensure the 'images' directory exists
            Path targetPath = targetDir.resolve(uniqueFileName);
            Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Get tags from text field
            String relativeImagePath = "images/" + uniqueFileName;
            List<String> tags = Arrays.asList(tagsField.getText().split("\\s*,\\s*"));
            
            ExampleImage newExample = new ExampleImage(relativeImagePath, tags);
            
            // Add to the correct list (won or lost)
            if ("Won".equals(exampleType)) {
                currentSetup.getWonExamples().add(newExample);
            } else {
                currentSetup.getLostExamples().add(newExample);
            }
            
            MainApp.getDataManager().saveSetupSamples();
            handleBack(); // Go back after saving

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Save Failed", "Could not save the image file.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML
    private void handleBack() {
        mainApp.showExamplesView(currentSetup, exampleType);
    }
}