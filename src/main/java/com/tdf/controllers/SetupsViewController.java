package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;
import java.util.Optional;

public class SetupsViewController implements Controller {

    @FXML private FlowPane setupsPane;
    private MainApp mainApp;
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        loadSetups();
    }

    private void loadSetups() {
        setupsPane.getChildren().clear();
        boolean showWinRate = dataManager.getSettings().showWinRate;

        for (SetupSample setup : dataManager.getSetupSamples()) {
            VBox setupBox = new VBox(10);
            setupBox.getStyleClass().add("setup-box"); // Use the styled box
            setupBox.setPrefWidth(280);
            setupBox.setAlignment(Pos.TOP_CENTER);

            FontIcon icon = new FontIcon("fas-bullseye");
            icon.getStyleClass().add("setup-icon");

            String nameText = setup.getName();
            if (showWinRate) {
                nameText += String.format(" (%.1f%% WR)", setup.getWinRate());
            }

            Label nameLabel = new Label(nameText);
            nameLabel.getStyleClass().add("h3");

            String desc = setup.getDescription();
            if (desc == null || desc.isEmpty()) {
                desc = "No description provided.";
            } else if (desc.length() > 80) {
                desc = desc.substring(0, 80) + "...";
            }
            Label descriptionLabel = new Label(desc);
            descriptionLabel.getStyleClass().add("note-text");
            descriptionLabel.setWrapText(true);

            Pane spacer = new Pane();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            Button viewButton = new Button("View Details");
            viewButton.getStyleClass().add("standard-button");
            viewButton.setOnAction(event -> mainApp.showSetupDetail(setup));

            setupBox.getChildren().addAll(icon, nameLabel, descriptionLabel, spacer, viewButton);
            setupsPane.getChildren().add(setupBox);
        }
    }

    @FXML
    private void handleAddSetup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add New Setup");
        dialog.setHeaderText("Enter the name for your new setup sample.");
        dialog.setContentText("Name:");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("main-view");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.isBlank()) {
                SetupSample newSetup = new SetupSample(name);
                dataManager.getSetupSamples().add(newSetup);
                dataManager.saveSetupSamples();
                loadSetups();
            }
        });
    }
}
