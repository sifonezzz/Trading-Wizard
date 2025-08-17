package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.Settings;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TradingFlowController implements Controller {

    @FXML private VBox contentBox;

    private MainApp mainApp;
    private Settings settings;
    private int currentStep = 0;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.settings = MainApp.getDataManager().getSettings();
        // Changed from showStep() to nextStep() to trigger the first fade-in
        nextStep();
    }
    
    private void nextStep() {
        Node currentContent = contentBox.getChildren().isEmpty() ? null : contentBox.getChildren().get(0);
        
        Runnable loadNextAction = () -> {
            showStep(); // Build the content for the *next* step
            Node newContent = contentBox.getChildren().isEmpty() ? null : contentBox.getChildren().get(0);
            if (newContent != null) {
                newContent.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), newContent);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            }
            currentStep++; // Increment step *after* showing it
        };

        if (currentContent != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), currentContent);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> loadNextAction.run());
            fadeOut.play();
        } else {
            loadNextAction.run();
        }
    }

    private void showStep() {
        contentBox.getChildren().clear();
        switch (currentStep) {
            case 0 -> showIntro();
            case 1 -> showTasks();
            case 2 -> showSetups();
            case 3 -> showRules();
            case 4 -> startTrading();
        }
    }

    private void showIntro() {
        Label introLabel = new Label("Let's start trading.");
        introLabel.getStyleClass().add("h1");
        contentBox.getChildren().add(introLabel);
        
        new Timeline(new KeyFrame(Duration.seconds(2), e -> nextStep())).play();
    }

    private void showTasks() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        Label title = new Label("Pre-Trading Checklist");
        title.getStyleClass().add("h2");
        VBox tasksVBox = new VBox(10);
        
        List<String> tasks = settings.tasks;
        for (String task : tasks) {
            tasksVBox.getChildren().add(new CheckBox(task));
        }
        
        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("standard-button");
        nextButton.setDisable(true);
        
        tasksVBox.getChildren().forEach(node -> {
            if (node instanceof CheckBox) {
                ((CheckBox) node).setOnAction(e -> {
                    boolean allChecked = tasksVBox.getChildren().stream()
                        .filter(n -> n instanceof CheckBox)
                        .allMatch(n -> ((CheckBox) n).isSelected());
                    nextButton.setDisable(!allChecked);
                });
            }
        });

        nextButton.setOnAction(e -> nextStep());
        container.getChildren().addAll(title, tasksVBox, nextButton);
        contentBox.getChildren().add(container);
    }
    
    private void showSetups() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        Label title = new Label("THESE ARE YOUR SETUPS, DO NOT IGNORE THEM");
        title.getStyleClass().add("h2");
        title.setWrapText(true);
        
        VBox setupsVBox = new VBox(10);
        for(String setup : settings.setups) {
            Button setupButton = new Button(setup);
            setupButton.getStyleClass().add("standard-button");
            setupButton.setDisable(true);
            setupButton.setMaxWidth(Double.MAX_VALUE);
            setupsVBox.getChildren().add(setupButton);
        }
        
        Button nextButton = new Button("Next");
        nextButton.getStyleClass().add("standard-button");
        nextButton.setDisable(true);
        nextButton.setOnAction(e -> nextStep());
        
        new Timeline(new KeyFrame(Duration.seconds(5), e -> {
            setupsVBox.getChildren().forEach(node -> node.setDisable(false));
        })).play();
        
        AtomicInteger clickedCount = new AtomicInteger();
        setupsVBox.getChildren().forEach(node -> {
            ((Button)node).setOnAction(e -> {
                node.setDisable(true);
                node.setStyle("-fx-background-color: -positive-color;");
                if (clickedCount.incrementAndGet() == settings.setups.size()) {
                    nextButton.setDisable(false);
                }
            });
        });
        
        container.getChildren().addAll(title, setupsVBox, nextButton);
        contentBox.getChildren().add(container);
    }

    private void showRules() {
        VBox container = new VBox(20);
        container.setAlignment(Pos.CENTER);
        Label title = new Label("Your Rules");
        title.getStyleClass().add("h2");
        
        VBox rulesVBox = new VBox(10);
        for(String rule : settings.rules) {
            Label ruleLabel = new Label("- " + rule);
            ruleLabel.getStyleClass().add("rule-text");
            rulesVBox.getChildren().add(ruleLabel);
        }
        
        Button nextButton = new Button();
        nextButton.getStyleClass().add("standard-button");
        nextButton.setDisable(true);
        
        IntegerProperty countdown = new SimpleIntegerProperty(15);
        nextButton.textProperty().bind(countdown.asString("Next (%d)"));
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown.set(countdown.get() - 1);
            if (countdown.get() <= 0) {
                nextButton.textProperty().unbind();
                nextButton.setText("Next");
                nextButton.setDisable(false);
            }
        }));
        timeline.setCycleCount(15);
        timeline.play();
        
        nextButton.setOnAction(e -> {
            timeline.stop();
            nextStep();
        });

        container.getChildren().addAll(title, rulesVBox, nextButton);
        contentBox.getChildren().add(container);
    }

    private void startTrading() {
        mainApp.startOnTopWidget();
    }
}