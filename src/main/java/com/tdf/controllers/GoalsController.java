package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Goal;
import com.tdf.data.PnlEntry;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class GoalsController implements Controller {

    @FXML private VBox goalsContainer;
    private MainApp mainApp;
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        loadGoals();
    }

    private void loadGoals() {
        goalsContainer.getChildren().clear();
        List<Goal> goals = dataManager.getGoals();

        updateAndResetGoals(goals);

        if (goals.isEmpty()) {
            Label emptyLabel = new Label("No goals set. Click 'Add New Goal' to start!");
            emptyLabel.getStyleClass().add("h3");
            goalsContainer.setAlignment(Pos.CENTER);
            goalsContainer.getChildren().add(emptyLabel);
            return;
        }

        goalsContainer.setAlignment(Pos.TOP_LEFT);
        for (Goal goal : goals) {
            goalsContainer.getChildren().add(createGoalCard(goal));
        }
    }

    private void updateAndResetGoals(List<Goal> goals) {
        LocalDate now = LocalDate.now();
        Map<String, PnlEntry> pnlData = dataManager.getPnlData();
        boolean needsSave = false;

        for (Goal goal : goals) {
            // --- Check for Reset ---
            LocalDate lastResetDate = new java.sql.Date(goal.getLastResetTimestamp()).toLocalDate();
            boolean shouldReset = false;
            if (goal.getType() == Goal.GoalType.PNL_MONTHLY && lastResetDate.getMonth() != now.getMonth()) {
                shouldReset = true;
            } else if (goal.getType() == Goal.GoalType.BEHAVIOR_WEEKLY) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                if (lastResetDate.get(weekFields.weekOfYear()) != now.get(weekFields.weekOfYear())) {
                    shouldReset = true;
                }
            }
            if (shouldReset) {
                goal.setCurrentValue(0);
                goal.setLastResetTimestamp(System.currentTimeMillis());
                needsSave = true;
            }

            // --- Update Current Value ---
            if (goal.getType() == Goal.GoalType.PNL_MONTHLY) {
                double monthlyPnl = pnlData.entrySet().stream()
                    .filter(entry -> YearMonth.from(LocalDate.parse(entry.getKey())).equals(YearMonth.from(now)))
                    .mapToDouble(entry -> entry.getValue().pnl)
                    .sum();
                goal.setCurrentValue(monthlyPnl);
            } else if (goal.getType() == Goal.GoalType.BEHAVIOR_WEEKLY) {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int currentWeek = now.get(weekFields.weekOfYear());
                int udCount = pnlData.entrySet().stream()
                    .filter(entry -> {
                        LocalDate date = LocalDate.parse(entry.getKey());
                        return date.get(weekFields.weekOfYear()) == currentWeek && date.getYear() == now.getYear();
                    })
                    .mapToInt(entry -> entry.getValue().undisciplineCount)
                    .sum();
                goal.setCurrentValue(udCount);
            }
        }
        if (needsSave) {
            dataManager.saveGoals();
        }
    }

    private Node createGoalCard(Goal goal) {
        BorderPane card = new BorderPane();
        card.getStyleClass().add("goal-card");

        // --- Left Side: Info ---
        Label descriptionLabel = new Label(goal.getDescription());
        descriptionLabel.getStyleClass().add("h3");
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        
        Label progressLabel = new Label();
        
        VBox infoBox = new VBox(10, descriptionLabel, progressBar, progressLabel);

        // --- Right Side: Status & Delete ---
        Label statusLabel = new Label();
        Button deleteButton = new Button("", new FontIcon("fas-trash-alt"));
        deleteButton.getStyleClass().addAll("icon-button", "delete-button");
        deleteButton.setOnAction(e -> {
            dataManager.getGoals().remove(goal);
            dataManager.saveGoals();
            loadGoals();
        });
        HBox actionBox = new HBox(10, statusLabel, deleteButton);
        actionBox.setAlignment(Pos.CENTER);

        // --- Configure based on Goal Type ---
        double progress = 0;
        Goal.GoalStatus status = Goal.GoalStatus.IN_PROGRESS;

        if (goal.getType() == Goal.GoalType.PNL_MONTHLY) {
            progress = goal.getTargetValue() <= 0 ? 1.0 : goal.getCurrentValue() / goal.getTargetValue();
            progressLabel.setText(String.format("$%.2f / $%.2f", goal.getCurrentValue(), goal.getTargetValue()));
            if (goal.getCurrentValue() >= goal.getTargetValue()) {
                status = Goal.GoalStatus.COMPLETED;
            }
        } else if (goal.getType() == Goal.GoalType.BEHAVIOR_WEEKLY) {
            progress = goal.getTargetValue() <= 0 ? 1.0 : goal.getCurrentValue() / goal.getTargetValue();
            progressLabel.setText(String.format("%d / %d Undisciplined Actions", (int) goal.getCurrentValue(), (int) goal.getTargetValue()));
            if (goal.getCurrentValue() > goal.getTargetValue()) {
                status = Goal.GoalStatus.FAILED;
            } else if (goal.getCurrentValue() == goal.getTargetValue()) {
                status = Goal.GoalStatus.COMPLETED; // Assuming target is a limit, not a goal to reach
            }
        }
        
        progressBar.setProgress(Math.max(0, Math.min(1, progress)));

        statusLabel.setText(status.toString());
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add("status-label");
        switch (status) {
            case COMPLETED -> statusLabel.getStyleClass().add("status-completed");
            case FAILED -> statusLabel.getStyleClass().add("status-failed");
            default -> statusLabel.getStyleClass().add("status-in-progress");
        }

        card.setLeft(infoBox);
        card.setRight(actionBox);
        return card;
    }

    @FXML
    private void handleAddGoal() {
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("Add New Goal");
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("main-view");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Goal.GoalType> typeComboBox = new ComboBox<>(FXCollections.observableArrayList(Goal.GoalType.values()));
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("e.g., Reach monthly profit target");
        TextField valueField = new TextField();
        valueField.setPromptText("e.g., 1500 or 5");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        grid.add(new Label("Goal Type:"), 0, 0);
        grid.add(typeComboBox, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        grid.add(new Label("Target Value:"), 0, 2);
        grid.add(valueField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(typeComboBox::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    double value = Double.parseDouble(valueField.getText());
                    return new Goal(typeComboBox.getValue(), descriptionField.getText(), value);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Goal> result = dialog.showAndWait();
        result.ifPresent(goal -> {
            dataManager.getGoals().add(goal);
            dataManager.saveGoals();
            loadGoals();
        });
    }
}