package com.tdf.widgets;

import com.tdf.MainApp;
import com.tdf.controllers.EndSessionController;
import com.tdf.data.Settings;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OnTopWidget extends Stage {
    private MainApp mainApp;
    private Settings settings;
    private Label ruleLabel;
    private int currentRuleIndex = 0;
    private Timeline ruleTimeline;
    private VBox content;

    private Timeline sessionTimer;
    private long sessionStartTime;

    private double xOffset = 0;
    private double yOffset = 0;

    public OnTopWidget(MainApp mainApp) {
        this.mainApp = mainApp;
        this.settings = MainApp.getDataManager().getSettings();
        this.sessionStartTime = System.currentTimeMillis();

        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        setX(100);
        setY(100);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("on-top-widget");

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            setX(event.getScreenX() - xOffset);
            setY(event.getScreenY() - yOffset);
        });

        content = new VBox(10);
        buildNormalContent();
        root.setCenter(content);

        // FIX: Adjusted widget height to be more compact
        double widgetHeight = settings.panicButtonEnabled ? 320 : 270;
        
        Scene scene = new Scene(root, 420, widgetHeight);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        setScene(scene);
    }

    private void buildNormalContent() {
        content.getChildren().clear();
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(5));
        
        ruleLabel = new Label();
        ruleLabel.setWrapText(true);
        VBox.setMargin(ruleLabel, new Insets(10));

        HBox lossBox = new HBox(5);
        lossBox.setAlignment(Pos.CENTER);
        Label maxLossTitle = new Label("MAX LOSS:");
        Label maxLossValue = new Label(String.format("$%.2f", settings.maxLoss));
        maxLossValue.getStyleClass().add("negative-text");
        lossBox.getChildren().addAll(maxLossTitle, maxLossValue);

        Button stopButton = new Button("Stop Trading");
        stopButton.getStyleClass().add("exit-button");
        stopButton.setMaxWidth(Double.MAX_VALUE);
        stopButton.setOnAction(e -> stopTrading());

        Button udButton = new Button("Undisciplined Action");
        udButton.getStyleClass().add("ud-button");
        udButton.setMaxWidth(Double.MAX_VALUE);
        udButton.setOnAction(e -> {
            String today = LocalDate.now().toString();
            MainApp.getDataManager().incrementUndisciplineCount(today);
            udButton.setText("Counted!");
            new Timeline(new KeyFrame(Duration.seconds(1), event -> udButton.setText("Undisciplined Action"))).play();
        });
        VBox.setMargin(udButton, new Insets(10, 0, 0, 0));

        // FIX: Moved session timer to be above the panic button
        Label sessionTimerLabel = new Label("Session Time: 00:00:00");
        sessionTimerLabel.getStyleClass().add("note-text");
        VBox.setMargin(sessionTimerLabel, new Insets(10, 0, 0, 0));
        startSessionTimer(sessionTimerLabel);

        content.getChildren().addAll(ruleLabel, lossBox, stopButton, udButton, sessionTimerLabel);

        if (settings.panicButtonEnabled) {
            Button panicButton = new Button("Panic");
            panicButton.getStyleClass().add("exit-button");
            panicButton.setMaxWidth(Double.MAX_VALUE);
            panicButton.setOnAction(e -> handlePanic());
            VBox.setMargin(panicButton, new Insets(15, 0, 0, 0));
            content.getChildren().add(panicButton); // Add panic button last
        }
        
        startRuleRotation();
    }
    
    private void startRuleRotation() {
        if (settings.rules == null || settings.rules.isEmpty()) {
            if(ruleLabel != null) ruleLabel.setText("No rules found in settings.");
            return;
        };
        if(ruleTimeline != null) ruleTimeline.stop();
        ruleTimeline = new Timeline(new KeyFrame(Duration.seconds(settings.ruleIntervalSeconds), e -> {
            currentRuleIndex = (currentRuleIndex + 1) % settings.rules.size();
            ruleLabel.setText(settings.rules.get(currentRuleIndex));
        }));
        ruleTimeline.setCycleCount(Timeline.INDEFINITE);
        if(!settings.rules.isEmpty()) ruleLabel.setText(settings.rules.get(currentRuleIndex));
        ruleTimeline.play();
    }

    private void startSessionTimer(Label timerLabel) {
        if (sessionTimer != null) sessionTimer.stop();
        sessionTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            long elapsedMillis = System.currentTimeMillis() - sessionStartTime;
            long hours = TimeUnit.MILLISECONDS.toHours(elapsedMillis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) % 60;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) % 60;
            timerLabel.setText(String.format("Session Time: %02d:%02d:%02d", hours, minutes, seconds));
        }));
        sessionTimer.setCycleCount(Timeline.INDEFINITE);
        sessionTimer.play();
    }

    private void stopTrading() {
        if (ruleTimeline != null) ruleTimeline.stop();
        if (sessionTimer != null) sessionTimer.stop();
        this.close();
        
        try {
            Stage dialogStage = new Stage();
            dialogStage.initOwner(mainApp.getPrimaryStage());
            dialogStage.initStyle(StageStyle.TRANSPARENT);
            dialogStage.setAlwaysOnTop(true);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/EndSession.fxml"));
            Parent root = loader.load();
            EndSessionController controller = loader.getController();
            controller.initialize(dialogStage, mainApp);
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(
                Objects.requireNonNull(
                    mainApp.getClass().getResource("/com/tdf/styles.css")
                ).toExternalForm()
            );
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            mainApp.returnToMainMenu();
        }
    }

    private void handlePanic() {
        if(ruleTimeline != null) ruleTimeline.stop();
        this.hide();

        Stage lockStage = new Stage(StageStyle.TRANSPARENT);
        lockStage.initOwner(this.getOwner());
        lockStage.setOpacity(0.85);

        Label panicTitle = new Label("Panic Mode");
        panicTitle.getStyleClass().add("h2");

        Label panicDesc = new Label("Relax, reflect, and think of your next action properly.\nHonor your rules.");
        panicDesc.setWrapText(true);
        panicDesc.setStyle("-fx-text-align: center;");

        Label countdownLabel = new Label();
        countdownLabel.setStyle("-fx-font-size: 80px; -fx-text-fill: white; -fx-font-family: 'Fira Code Bold';");

        VBox panicContainer = new VBox(20, panicTitle, panicDesc, countdownLabel);
        panicContainer.setAlignment(Pos.CENTER);

        StackPane lockRoot = new StackPane(panicContainer);
        lockRoot.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Scene lockScene = new Scene(lockRoot);
        lockScene.setFill(Color.TRANSPARENT);

        var screenBounds = Screen.getPrimary().getVisualBounds();
        lockStage.setX(screenBounds.getMinX());
        lockStage.setY(screenBounds.getMinY());
        lockStage.setWidth(screenBounds.getWidth());
        lockStage.setHeight(screenBounds.getHeight());

        lockStage.setScene(lockScene);
        lockStage.show();

        int duration = settings.panicButtonDurationSeconds;
        IntegerProperty countdown = new SimpleIntegerProperty(duration);
        countdownLabel.textProperty().bind(countdown.asString());

        Timeline countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            countdown.set(countdown.get() - 1);
            if (countdown.get() <= 0) {
                lockStage.close();
                this.show();
                startRuleRotation();
            }
        }));
        countdownTimeline.setCycleCount(duration);
        countdownTimeline.play();

        lockScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F12) {
                countdownTimeline.stop();
                lockStage.close();
                this.show();
                startRuleRotation();
            }
        });
    }
}