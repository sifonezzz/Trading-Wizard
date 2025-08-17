package com.tdf.widgets;

import com.tdf.MainApp;
import com.tdf.data.Settings;
import com.tdf.dialogs.EndSessionController; // THIS IS THE CRITICAL FIX
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class OnTopWidget extends Stage {
    private MainApp mainApp;
    private Settings settings;
    private Label ruleLabel;
    private int currentRuleIndex = 0;
    
    private double xOffset = 0;
    private double yOffset = 0;

    public OnTopWidget(MainApp mainApp) {
        this.mainApp = mainApp;
        this.settings = MainApp.getDataManager().getSettings();

        initStyle(StageStyle.TRANSPARENT);
        setAlwaysOnTop(true);
        
        setX(0);
        setY(0);

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

        VBox content = new VBox(10, ruleLabel, lossBox, stopButton, udButton);
        content.setPadding(new Insets(5));
        root.setCenter(content);

        Scene scene = new Scene(root, 360, 210);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        setScene(scene);

        startRuleRotation();
    }

    private void startRuleRotation() {
        if (settings.rules == null || settings.rules.isEmpty()) return;
        
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(settings.ruleIntervalSeconds), e -> {
            currentRuleIndex = (currentRuleIndex + 1) % settings.rules.size();
            ruleLabel.setText(settings.rules.get(currentRuleIndex));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        ruleLabel.setText(settings.rules.get(currentRuleIndex));
        timeline.play();
    }
    
    private void stopTrading() {
        this.close();

        try {
            Stage dialogStage = new Stage();
            dialogStage.initOwner(mainApp.getPrimaryStage());
            dialogStage.initStyle(StageStyle.UNDECORATED);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/EndSession.fxml"));
            Parent root = loader.load();
            
            EndSessionController controller = loader.getController();
            controller.initialize(dialogStage, mainApp);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            scene.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
            
            dialogStage.setScene(scene);
            dialogStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mainApp.returnToMainMenu();
        }
    }
}