// FILE: src/main/java/com/tdf/controllers/SetupsViewController.java
package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.SetupSample;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SetupsViewController implements Controller {

    @FXML private FlowPane setupsPane;
    @FXML private VBox setupsVBox; // For list view
    @FXML private ScrollPane gridScrollPane;
    @FXML private ScrollPane listScrollPane;
    @FXML private VBox emptyStatePane;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private ToggleButton showArchivedToggle;
    @FXML private Button layoutToggleButton;

    private MainApp mainApp;
    private DataManager dataManager;
    private boolean isListView = false;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        initializeControls();
        loadSetups();
    }

    private void initializeControls() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> loadSetups());
        sortComboBox.getItems().addAll("Favorite", "Name (A-Z)", "Win Rate (High-Low)", "Last Modified");
        sortComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadSetups());
        showArchivedToggle.setOnAction(e -> loadSetups());
        layoutToggleButton.setOnAction(e -> toggleLayout());
    }

    private void toggleLayout() {
        isListView = !isListView;
        FontIcon icon = (FontIcon) layoutToggleButton.getGraphic();
        if (isListView) {
            gridScrollPane.setVisible(false);
            listScrollPane.setVisible(true);
            icon.setIconLiteral("fas-th-large");
        } else {
            gridScrollPane.setVisible(true);
            listScrollPane.setVisible(false);
            icon.setIconLiteral("fas-list");
        }
        loadSetups();
    }

    private void loadSetups() {
        setupsPane.getChildren().clear();
        setupsVBox.getChildren().clear();
        List<SetupSample> filteredAndSortedSetups = getFilteredAndSortedSetups();
        if (filteredAndSortedSetups.isEmpty()) {
            emptyStatePane.setVisible(true);
            return;
        }
        emptyStatePane.setVisible(false);
        boolean showWinRate = dataManager.getSettings().showWinRate;
        if (isListView) {
            for (SetupSample setup : filteredAndSortedSetups) {
                setupsVBox.getChildren().add(createListCard(setup, showWinRate));
            }
        } else {
            for (SetupSample setup : filteredAndSortedSetups) {
                setupsPane.getChildren().add(createGridCard(setup, showWinRate));
            }
        }
    }

    private List<SetupSample> getFilteredAndSortedSetups() {
        List<SetupSample> setups = dataManager.getSetupSamples().stream()
            .filter(s -> showArchivedToggle.isSelected() || !s.isArchived())
            .filter(s -> {
                String keyword = searchField.getText().toLowerCase();
                // FIX: Add a null check for tags to prevent crash on search
                boolean tagsMatch = s.getTags() != null && String.join(" ", s.getTags()).toLowerCase().contains(keyword);
                return keyword.isEmpty() || s.getName().toLowerCase().contains(keyword) || tagsMatch;
            })
            .collect(Collectors.toList());

        String sortType = sortComboBox.getSelectionModel().getSelectedItem();
        if (sortType != null) {
            switch (sortType) {
                case "Favorite" -> setups.sort(Comparator.comparing(SetupSample::isFavorite).reversed());
                case "Name (A-Z)" -> setups.sort(Comparator.comparing(SetupSample::getName, String.CASE_INSENSITIVE_ORDER));
                case "Win Rate (High-Low)" -> setups.sort(Comparator.comparingDouble(SetupSample::getWinRate).reversed());
                case "Last Modified" -> setups.sort(Comparator.comparing(SetupSample::getLastModified, Comparator.nullsLast(Comparator.reverseOrder())));
            }
        }
        return setups;
    }

    private Node createGridCard(SetupSample setup, boolean showWinRate) {
        VBox setupBox = new VBox(10);
        setupBox.getStyleClass().add("setup-box");
        setupBox.setPrefWidth(280);
        setupBox.setAlignment(Pos.TOP_CENTER);

        // CHANGE: Make the entire box clickable
        setupBox.setOnMouseClicked(event -> mainApp.showSetupDetail(setup));

        String nameText = setup.getName();
        if (showWinRate) { nameText += String.format(" (%.1f%% WR)", setup.getWinRate()); }
        Label nameLabel = new Label(nameText);
        nameLabel.getStyleClass().add("h3");
        String desc = setup.getDescription();
        if (desc == null || desc.isEmpty()) { desc = "No description provided."; }
        else if (desc.length() > 80) { desc = desc.substring(0, 80) + "..."; }
        Label descriptionLabel = new Label(desc);
        descriptionLabel.getStyleClass().add("note-text");
        descriptionLabel.setWrapText(true);
        Label statsLabel = new Label(String.format("%dW - %dL", setup.getWonExamples().size(), setup.getLostExamples().size()));
        statsLabel.getStyleClass().add("stats-label");
        HBox buttonBar = createButtonBar(setup);
        Pane spacer = new Pane();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // CHANGE: Removed the "View Details" button
        setupBox.getChildren().addAll(buttonBar, nameLabel, statsLabel, descriptionLabel, spacer);
        return setupBox;
    }

    private Node createListCard(SetupSample setup, boolean showWinRate) {
        BorderPane listCard = new BorderPane();
        listCard.getStyleClass().add("list-card");
        
        // CHANGE: Make the entire card clickable
        listCard.setOnMouseClicked(event -> mainApp.showSetupDetail(setup));

        VBox infoBox = new VBox(5);
        String nameText = setup.getName();
        if (showWinRate) { nameText += String.format(" (%.1f%% WR)", setup.getWinRate()); }
        Label nameLabel = new Label(nameText);
        nameLabel.getStyleClass().add("h3");
        String lastModifiedText = "N/A";
        if (setup.getLastModified() != null) {
            lastModifiedText = ZonedDateTime.parse(setup.getLastModified()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        Label statsLabel = new Label(String.format("Stats: %dW - %dL  |  Last Modified: %s",
                setup.getWonExamples().size(),
                setup.getLostExamples().size(),
                lastModifiedText
        ));
        statsLabel.getStyleClass().add("note-text");
        infoBox.getChildren().addAll(nameLabel, statsLabel);
        listCard.setLeft(infoBox);
        HBox buttonBar = createButtonBar(setup);
        buttonBar.setAlignment(Pos.CENTER);
        
        // CHANGE: Removed the "View" button, buttonBar is now the only item on the right
        listCard.setRight(buttonBar);
        
        return listCard;
    }

    private HBox createButtonBar(SetupSample setup) {
        Button favButton = new Button();
        favButton.getStyleClass().addAll("icon-button", "favorite-button");
        favButton.setGraphic(new FontIcon(setup.isFavorite() ? "fas-star" : "far-star"));
        favButton.setOnAction(e -> {
            setup.setFavorite(!setup.isFavorite());
            dataManager.saveSetupSamples();
            loadSetups();
            e.consume(); // Consume event to prevent card click-through
        });
        Button archiveButton = new Button();
        archiveButton.getStyleClass().add("icon-button");
        archiveButton.setGraphic(new FontIcon(setup.isArchived() ? "fas-undo" : "fas-archive"));
        archiveButton.setOnAction(e -> {
            setup.setArchived(!setup.isArchived());
            dataManager.saveSetupSamples();
            loadSetups();
            e.consume(); // Consume event to prevent card click-through
        });
        Button deleteButton = new Button();
        deleteButton.getStyleClass().addAll("icon-button", "delete-button");
        deleteButton.setGraphic(new FontIcon("fas-trash-alt"));
        deleteButton.setOnAction(e -> {
            handleDeleteSetup(setup);
            e.consume(); // Consume event to prevent card click-through
        });
        HBox buttonBar = new HBox(5, favButton, archiveButton, deleteButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);
        return buttonBar;
    }

    private void handleDeleteSetup(SetupSample setup) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Are you sure you want to delete the setup '" + setup.getName() + "'?");
        alert.setContentText("This action is permanent and cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            dataManager.getSetupSamples().remove(setup);
            dataManager.saveSetupSamples();
            loadSetups();
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