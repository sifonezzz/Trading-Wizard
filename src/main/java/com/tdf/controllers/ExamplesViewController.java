package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.ExampleImage;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ExamplesViewController implements Controller {

    @FXML private Label titleLabel;
    @FXML private FlowPane examplesPane;
    
    private MainApp mainApp;
    private SetupSample currentSetup;
    private String exampleType; // "Won" or "Lost"

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void setExamples(SetupSample setup, String type) {
        this.currentSetup = setup;
        this.exampleType = type;
        titleLabel.setText(setup.getName() + " - " + type + " Examples");
        loadImages();
    }
    
    private void loadImages() {
        examplesPane.getChildren().clear();
        List<ExampleImage> examples = "Won".equals(exampleType) ? currentSetup.getWonExamples() : currentSetup.getLostExamples();

        if (examples.isEmpty()) {
            examplesPane.getChildren().add(new Label("No examples added yet."));
            return;
        }

        for (ExampleImage example : examples) {
            File imageFile = new File(MainApp.getDataManager().getBaseDir(), example.getImagePath());
            if (imageFile.exists()) {
                VBox imageBox = new VBox(5);
                imageBox.getStyleClass().add("note-box");
                imageBox.setAlignment(Pos.CENTER);

                ImageView imageView = new ImageView(new Image(imageFile.toURI().toString()));
                imageView.setFitWidth(300);
                imageView.setPreserveRatio(true);
                
                Label tagsLabel = new Label("Tags: " + String.join(", ", example.getTags()));
                tagsLabel.setWrapText(true);

                imageBox.getChildren().addAll(imageView, tagsLabel);
                examplesPane.getChildren().add(imageBox);
            }
        }
    }
    
    @FXML
    private void handleAddExample() {
        mainApp.showAddExampleView(currentSetup, exampleType);
    }
    
    @FXML
    private void handleBack() {
        mainApp.showSetupDetail(currentSetup);
    }
}