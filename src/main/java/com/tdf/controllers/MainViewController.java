package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PathTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.QuadCurve;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MainViewController implements Controller {

    @FXML private StackPane rootStackPane; // <-- Crucial new field
    @FXML private BorderPane rootPane;
    @FXML private BorderPane contentPane;
    @FXML private HBox titleBar;
    @FXML private VBox sidebar;
    @FXML private ToggleButton dashboardButton;
    @FXML private Label welcomeLabel;

    private MainApp mainApp;
    private double xOffset = 0;
    private double yOffset = 0;
    private static final double SIDEBAR_WIDTH = 220.0;
    
    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        sidebar.setTranslateX(-SIDEBAR_WIDTH);
        
        Platform.runLater(() -> triggerWelcomeAnimation("Welcome"));
    }

    private void animateSidebar(boolean show) {
        if (show) {
            sidebar.setVisible(true);
        }
        TranslateTransition slide = new TranslateTransition(Duration.millis(350), sidebar);
        slide.setToX(show ? 0 : -SIDEBAR_WIDTH);
        if (!show) {
            slide.setOnFinished(e -> sidebar.setVisible(false));
        }
        slide.play();
    }

    public void triggerWelcomeAnimation(String message) {
        welcomeLabel.setText(message);
        welcomeLabel.setVisible(true);

        // --- Comet Animation ---
        Circle comet = new Circle(4, Color.WHITE);
        comet.setEffect(new DropShadow(15, Color.CYAN));
        rootStackPane.getChildren().add(comet);

        // Define the curved path for the comet
        QuadCurve curve = new QuadCurve();
        curve.setStartX(rootStackPane.getWidth() + 20); // Start off-screen top-right
        curve.setStartY(-20);
        curve.setControlX(rootStackPane.getWidth() * 0.5); // Control point for curvature
        curve.setControlY(rootStackPane.getHeight() * 0.5);
        curve.setEndX(-20); // End off-screen bottom-left
        curve.setEndY(rootStackPane.getHeight() + 20);

        PathTransition pathTransition = new PathTransition();
        pathTransition.setNode(comet);
        pathTransition.setPath(curve);
        pathTransition.setDuration(Duration.millis(800)); // Fast animation
        pathTransition.setCycleCount(1);
        
        // --- Original Welcome Label Animation ---
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        // Run comet and label fade-in at the same time
        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, pathTransition);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1));

        // Create the final animation sequence
        SequentialTransition sequence = new SequentialTransition(parallelTransition, fadeOut);
        sequence.setOnFinished(e -> {
            welcomeLabel.setVisible(false);
            rootStackPane.getChildren().remove(comet); // Clean up the comet node
            showDashboard();
            dashboardButton.setSelected(true);
            animateSidebar(true);
        });
        sequence.play();
    }
    
    private void loadView(String fxmlFile) {
        Node currentView = contentPane.getCenter();
        Runnable loadAction = () -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/" + fxmlFile));
                Node newView = loader.load();
                
                Object controller = loader.getController();
                if (controller instanceof Controller) {
                    ((Controller) controller).setMainApp(mainApp);
                }

                newView.setOpacity(0);
                contentPane.setCenter(newView);
                Platform.runLater(() -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newView);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        if (currentView != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentView);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> loadAction.run());
            fadeOut.play();
        } else {
            loadAction.run();
        }
    }
    
    public void setContent(Node node) {
        Node currentView = contentPane.getCenter();
        Runnable setAction = () -> {
            node.setOpacity(0);
            contentPane.setCenter(node);
            Platform.runLater(() -> {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(250), node);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
        };

        if (currentView != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(250), currentView);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> setAction.run());
            fadeOut.play();
        } else {
            setAction.run();
        }
    }

    @FXML public void showDashboard() { loadView("Dashboard.fxml"); }
    @FXML private void showJournal() { loadView("NotesView.fxml"); }
    @FXML public void showCalendar() { loadView("PnlCalendar.fxml"); }
    @FXML public void showSetups() { loadView("SetupsView.fxml"); }
    @FXML private void showSettings() { loadView("Settings.fxml"); }
    @FXML public void showYearOverview() { load_View("YearOverview.fxml"); }
    public void showAddWidgets() { loadView("AddWidgets.fxml"); }

    @FXML 
    private void showTradingFlow() {
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(300), sidebar);
        slideOut.setToX(-SIDEBAR_WIDTH);
        slideOut.setOnFinished(e -> {
            sidebar.setVisible(false);
            rootPane.setLeft(null); 
            loadTradingFlowInRoot();
        });
        slideOut.play();
    }
    
    private void loadTradingFlowInRoot() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/TradingFlow.fxml"));
            Node newView = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof Controller) {
                ((Controller) controller).setMainApp(mainApp);
            }

            newView.setOpacity(0);
            rootPane.setCenter(newView);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), newView);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSidebarAndDashboard() {
        rootPane.setCenter(contentPane);
        rootPane.setLeft(sidebar);
        contentPane.setCenter(null);
        triggerWelcomeAnimation("Welcome Back");
    }

    @FXML private void handleClose() { Platform.exit(); }
    @FXML private void handleMinimize() { ((Stage) rootPane.getScene().getWindow()).setIconified(true); }
    @FXML private void handleMaximize() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
    @FXML private void handleExit() { Platform.exit(); }
}