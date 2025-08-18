package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.ExampleImage;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ExamplesViewController implements Controller {

    @FXML private StackPane rootStackPane;
    @FXML private Label titleLabel;
    @FXML private FlowPane examplesPane;
    
    private MainApp mainApp;
    private SetupSample currentSetup;
    private String exampleType; // "Won" or "Lost"
    private double xOffset = 0;
    private double yOffset = 0;

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

                Button viewButton = new Button("", new FontIcon("fas-expand-arrows-alt"));
                viewButton.getStyleClass().add("image-action-button");
                viewButton.setOnAction(e -> showMaximizedImage(new Image(imageFile.toURI().toString())));

                Button deleteButton = new Button("", new FontIcon("fas-trash-alt"));
                deleteButton.getStyleClass().addAll("image-action-button", "delete-button");
                deleteButton.setOnAction(e -> handleDeleteExample(example));
                
                HBox buttonBox = new HBox(10, viewButton, deleteButton);
                buttonBox.setAlignment(Pos.CENTER);
                HBox.setMargin(buttonBox, new Insets(5, 0, 0, 0));

                imageBox.getChildren().addAll(imageView, tagsLabel, buttonBox);
                examplesPane.getChildren().add(imageBox);
            }
        }
    }
    
    private void showMaximizedImage(Image image) {
        ImageView maximizedImageView = new ImageView(image);
        maximizedImageView.setPreserveRatio(true);
        maximizedImageView.fitWidthProperty().bind(rootStackPane.widthProperty().subtract(100));
        maximizedImageView.fitHeightProperty().bind(rootStackPane.heightProperty().subtract(100));

        Button closeButton = new Button("X");
        closeButton.getStyleClass().add("window-button-close");

        BorderPane maximizedPane = new BorderPane(maximizedImageView);
        BorderPane.setAlignment(closeButton, Pos.TOP_RIGHT);
        BorderPane.setMargin(closeButton, new Insets(10));
        maximizedPane.setTop(closeButton);
        maximizedPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        closeButton.setOnAction(e -> rootStackPane.getChildren().remove(maximizedPane));

        rootStackPane.getChildren().add(maximizedPane);
    }
    
    private void handleDeleteExample(ExampleImage exampleToDelete) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Delete Example");
        // <<< FIX 1: Set a clear header text.
        confirmation.setHeaderText("This will permanently delete the example image. Are you sure?");
        confirmation.setContentText("This action cannot be undone.");

        // --- THIS IS THE FIX ---
        // Get the dialog's window (Stage)
        Stage dialogStage = (Stage) confirmation.getDialogPane().getScene().getWindow();
        
        // <<< FIX 2: Set the owner to the main app window to ensure it's centered.
        dialogStage.initOwner(mainApp.getPrimaryStage());

        // Apply the custom dark theme styling from styles.css
        DialogPane dialogPane = confirmation.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("main-view");
        // --- END FIX ---

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if ("Won".equals(exampleType)) {
                currentSetup.getWonExamples().remove(exampleToDelete);
            } else {
                currentSetup.getLostExamples().remove(exampleToDelete);
            }

            try {
                File imageFile = new File(MainApp.getDataManager().getBaseDir(), exampleToDelete.getImagePath());
                Files.deleteIfExists(imageFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }

            MainApp.getDataManager().saveSetupSamples();
            loadImages();
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