package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.MotionBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;


public class MainViewController implements Controller {
    private String currentViewFxml = "";
    @FXML private StackPane rootStackPane;
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

        // Set initial off-screen positions for UI elements
        sidebar.setTranslateX(-SIDEBAR_WIDTH);
        titleBar.setTranslateY(-50.0);
        
        // Apply theme settings on startup
        Platform.runLater(this::applyThemeSettings);
        
        // Set up the listener for the main welcome/comet animation
        ChangeListener<Number> sizeListener = new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.doubleValue() > 0) {
                    triggerWelcomeAnimation("Welcome");
                    observable.removeListener(this);
                }
            }
        };
        rootStackPane.widthProperty().addListener(sizeListener);
    }
    
    public void applyThemeSettings() {
        boolean useAnimation = MainApp.getDataManager().getSettings().animatedBackground;
        if (useAnimation) {
            rootStackPane.getStyleClass().add("animated-background");
        } else {
            rootStackPane.getStyleClass().remove("animated-background");
        }
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

        Pane animationPane = new Pane();
        animationPane.setMouseTransparent(true);
        rootStackPane.getChildren().add(animationPane);
        animationPane.toFront();

        Circle comet = new Circle(4, Color.WHITE);
        animationPane.getChildren().add(comet);

        double sceneWidth = rootStackPane.getScene().getWidth();
        double sceneHeight = rootStackPane.getScene().getHeight();

        double startX = sceneWidth + 20.0;
        double startY = -20.0;
        double endX = -20.0;
        double endY = sceneHeight + 20.0;
        
        double deltaX = endX - startX;
        double deltaY = endY - startY;
        double angle = Math.toDegrees(Math.atan2(deltaY, deltaX));
        comet.setEffect(new MotionBlur(angle, 15.0));

        comet.setTranslateX(startX);
        comet.setTranslateY(startY);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        KeyValue kvX = new KeyValue(comet.translateXProperty(), endX, Interpolator.LINEAR);
        KeyValue kvY = new KeyValue(comet.translateYProperty(), endY, Interpolator.LINEAR);
        KeyFrame kf = new KeyFrame(Duration.millis(850), kvX, kvY);
        timeline.getKeyFrames().add(kf);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition parallelTransition = new ParallelTransition(fadeIn, timeline);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1.0));

        SequentialTransition sequence = new SequentialTransition(parallelTransition, fadeOut);
        sequence.setOnFinished(e -> {
            welcomeLabel.setVisible(false);
            rootStackPane.getChildren().remove(animationPane);
            postAnimationActions();
        });
        sequence.play();
    }
    
    public void triggerSimpleWelcomeAnimation(String message) {
        welcomeLabel.setText(message);
        welcomeLabel.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), welcomeLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setDelay(Duration.seconds(1));

        SequentialTransition sequence = new SequentialTransition(fadeIn, fadeOut);
        sequence.setOnFinished(e -> {
            welcomeLabel.setVisible(false);
            postAnimationActions();
        });
        sequence.play();
    }

    private void postAnimationActions() {
        showDashboard();
        dashboardButton.setSelected(true);

        // Animate both the sidebar and the title bar into view
        animateSidebar(true);

        TranslateTransition slideInTitleBar = new TranslateTransition(Duration.millis(400), titleBar);
        slideInTitleBar.setToY(0);
        slideInTitleBar.play();
    }

    private void loadView(String fxmlFile) {
    // If the view we're trying to load is already the current one, do nothing.
    if (fxmlFile.equals(currentViewFxml)) {
        return;
    }

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

            // After loading, update the current view tracker.
            currentViewFxml = fxmlFile;

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
    // Reset the view tracker since this is not a main view loaded from the sidebar.
    currentViewFxml = ""; 

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
    @FXML public void showYearOverview() { loadView("YearOverview.fxml"); }
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
        // Reset the main layout to show the content area and sidebar
        rootPane.setCenter(contentPane);
        rootPane.setLeft(sidebar);
        contentPane.setCenter(null); // Clear any old view

        // Immediately animate the sidebar into view
        animateSidebar(true);

        // Immediately load and fade in the dashboard
        showDashboard();
        
        // Ensure the Dashboard button is selected in the sidebar
        if (dashboardButton != null) {
            dashboardButton.setSelected(true);
        }
    }

    @FXML private void handleClose() { Platform.exit(); }
    @FXML private void handleMinimize() { ((Stage) rootPane.getScene().getWindow()).setIconified(true); }
    @FXML private void handleMaximize() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
    @FXML private void handleExit() { Platform.exit(); }
}