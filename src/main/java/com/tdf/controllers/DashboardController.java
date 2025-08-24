package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Goal;
import com.tdf.data.Note;
import com.tdf.data.PnlEntry;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DashboardController implements Controller {

    @FXML private VBox welcomePane;
    @FXML private GridPane widgetPane;
    @FXML private Button smallAddButton;
    @FXML private Button modifyLayoutButton;
    @FXML private ScrollPane dashboardScrollPane;

    private MainApp mainApp;
    private DataManager dataManager;
    private boolean isModifyMode = false;
    private VBox draggedWidget = null;
    private int sourceCol, sourceRow;
    private boolean gridInitialized = false;
    private static final int NUM_COLUMNS = 4;
    private static final int NUM_ROWS = 4;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
    }

    @FXML
    public void initialize() {
        ChangeListener<Bounds> sizeListener = (obs, oldVal, newVal) -> {
            if (newVal.getWidth() > 0 && !gridInitialized) {
                initializeGrid();
                buildDashboard();
                gridInitialized = true;
            }
        };
        dashboardScrollPane.viewportBoundsProperty().addListener(sizeListener);
    }

    private void initializeGrid() {
        widgetPane.getColumnConstraints().clear();
        for (int i = 0; i < NUM_COLUMNS; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / NUM_COLUMNS);
            widgetPane.getColumnConstraints().add(colConst);
        }

        widgetPane.getRowConstraints().clear();
        for (int i = 0; i < NUM_ROWS; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / NUM_ROWS);
            widgetPane.getRowConstraints().add(rowConst);
        }
    }

    private void buildDashboard() {
        widgetPane.getChildren().clear();
        List<String> activeWidgets = dataManager.getSettings().activeWidgets;

        if (activeWidgets == null || activeWidgets.isEmpty()) {
            welcomePane.setVisible(true);
            welcomePane.setManaged(true);
            smallAddButton.setVisible(true);
            modifyLayoutButton.setVisible(false);
        } else {
            welcomePane.setVisible(false);
            welcomePane.setManaged(false);
            smallAddButton.setVisible(true);
            modifyLayoutButton.setVisible(true);

            for (String layoutInfo : activeWidgets) {
                String[] parts = layoutInfo.split(";");
                if (parts.length == 3) {
                    String widgetName = parts[0];
                    int col = Integer.parseInt(parts[1]);
                    int row = Integer.parseInt(parts[2]);
                    VBox widget = createWidget(widgetName);
                    if (widget != null) {
                        widget.setUserData(widgetName);
                        GridPane.setValignment(widget, VPos.TOP);
                        widgetPane.add(widget, col, row);
                    }
                }
            }
        }
    }

    @FXML
    private void handleModifyLayout() {
        isModifyMode = !isModifyMode;
        if (isModifyMode) {
            modifyLayoutButton.setText("Save Layout");
            smallAddButton.setDisable(true);
            populatePlaceholders();
            widgetPane.getChildren().stream()
                .filter(node -> node.getUserData() != null)
                .forEach(this::enableDragAndDrop);
        } else {
            List<String> newWidgetLayout = new ArrayList<>();
            for (Node node : widgetPane.getChildren()) {
                if (node.getUserData() != null) {
                    String widgetName = (String) node.getUserData();
                    Integer col = GridPane.getColumnIndex(node);
                    Integer row = GridPane.getRowIndex(node);
                    newWidgetLayout.add(String.format("%s;%d;%d", widgetName, col, row));
                }
            }
            dataManager.getSettings().activeWidgets = newWidgetLayout;
            dataManager.saveSettings(dataManager.getSettings());
            
            modifyLayoutButton.setText("Modify Layout");
            smallAddButton.setDisable(false);
            buildDashboard();
        }
    }

    private void populatePlaceholders() {
        Set<String> occupiedCells = new HashSet<>();
        for (Node node : widgetPane.getChildren()) {
            if (node.getUserData() != null) {
                occupiedCells.add(GridPane.getColumnIndex(node) + ":" + GridPane.getRowIndex(node));
            }
        }
        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLUMNS; col++) {
                if (!occupiedCells.contains(col + ":" + row)) {
                    Pane placeholder = new Pane();
                    placeholder.getStyleClass().add("drop-target-pane");
                    widgetPane.add(placeholder, col, row);
                    enableDropTarget(placeholder);
                }
            }
        }
    }
    
    private void enableDragAndDrop(Node widget) {
        widget.getStyleClass().add("draggable-widget");
        widget.setOnDragDetected(event -> {
            draggedWidget = (VBox) widget;
            sourceCol = GridPane.getColumnIndex(draggedWidget);
            sourceRow = GridPane.getRowIndex(draggedWidget);

            Dragboard db = widget.startDragAndDrop(TransferMode.MOVE);
            SnapshotParameters params = new SnapshotParameters();
            params.setFill(Color.TRANSPARENT);
            Image snapshot = widget.snapshot(params, null);
            db.setDragView(snapshot, event.getX(), event.getY());

            ClipboardContent content = new ClipboardContent();
            content.putString("dragging");
            db.setContent(content);

            widget.setOpacity(0.4);
            event.consume();
        });
        widget.setOnDragDone(event -> {
            widget.setOpacity(1.0);
            widgetPane.getChildren().forEach(node -> node.getStyleClass().remove("drag-over-widget"));
            draggedWidget = null;
        });
    }

    private void enableDropTarget(Node target) {
        target.setOnDragOver(event -> {
            if (event.getGestureSource() != target && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        target.setOnDragEntered(event -> target.getStyleClass().add("drag-over-widget"));
        target.setOnDragExited(event -> target.getStyleClass().remove("drag-over-widget"));
        target.setOnDragDropped(event -> {
            if (draggedWidget != null) {
                int newCol = GridPane.getColumnIndex(target);
                int newRow = GridPane.getRowIndex(target);
                GridPane.setColumnIndex(draggedWidget, newCol);
                GridPane.setRowIndex(draggedWidget, newRow);
    
                Pane newPlaceholder = new Pane();
                newPlaceholder.getStyleClass().add("drop-target-pane");
                widgetPane.add(newPlaceholder, sourceCol, sourceRow);
                enableDropTarget(newPlaceholder);
                widgetPane.getChildren().remove(target);
                event.setDropCompleted(true);
            }
            event.consume();
        });
    }
    
    private VBox createWidget(String widgetName) {
        if (widgetName.startsWith("Goal: ")) {
            String goalDescription = widgetName.substring("Goal: ".length());
            Goal goal = dataManager.getGoals().stream()
                .filter(g -> g.getDescription().equals(goalDescription))
                .findFirst()
                .orElse(null);
            
            if (goal != null) {
                return createGoalWidget(goal);
            }
        }
        
        VBox widget = switch (widgetName) {
            case "Latest Journal Note" -> createLatestNoteWidget();
            case "Previous Day PNL" -> createPrevDayPnlWidget();
            case "Current Month Calendar" -> createCurrentMonthWidget();
            case "Monthly Undiscipline Counter" -> createUdCounterWidget();
            case "Weekly/Monthly PNL Tracker" -> createWeeklyMonthlyPnlWidget();
            case "PNL Streak Counter" -> createPnlStreakWidget();
            case "Best/Worst Day (Monthly)" -> createBestWorstDayWidget();
            case "Rule of the Day" -> createRuleOfTheDayWidget();
            default -> null;
        };
        
        if (widget != null) {
            GridPane.setValignment(widget, VPos.TOP);
        }
        return widget;
    }

    private VBox createGoalWidget(Goal goal) {
        VBox box = createWidgetContainer(goal.getDescription());
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        
        Label progressLabel = new Label();
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        double progress = 0;
        Goal.GoalStatus status = Goal.GoalStatus.IN_PROGRESS;

        if (goal.getType() == Goal.GoalType.PNL_MONTHLY) {
            progress = goal.getTargetValue() <= 0 ? 1.0 : goal.getCurrentValue() / goal.getTargetValue();
            progressLabel.setText(String.format("$%.2f / $%.2f", goal.getCurrentValue(), goal.getTargetValue()));
            if (goal.getCurrentValue() >= goal.getTargetValue()) status = Goal.GoalStatus.COMPLETED;
        } else if (goal.getType() == Goal.GoalType.BEHAVIOR_WEEKLY) {
            progress = goal.getTargetValue() <= 0 ? 1.0 : goal.getCurrentValue() / goal.getTargetValue();
            progressLabel.setText(String.format("%d / %d Undisciplined Actions", (int) goal.getCurrentValue(), (int) goal.getTargetValue()));
            // FIX: Added full status logic to match the Goals screen
            if (goal.getCurrentValue() > goal.getTargetValue()) {
                status = Goal.GoalStatus.FAILED;
            } else if (goal.getCurrentValue() == goal.getTargetValue()) {
                status = Goal.GoalStatus.COMPLETED;
            }
        }
        
        progressBar.setProgress(Math.max(0, Math.min(1, progress)));
        statusLabel.setText(status.toString());

        switch (status) {
            case COMPLETED -> statusLabel.getStyleClass().add("status-completed");
            case FAILED -> statusLabel.getStyleClass().add("status-failed");
            default -> statusLabel.getStyleClass().add("status-in-progress");
        }
        
        box.getChildren().addAll(progressBar, progressLabel, statusLabel);
        return box;
    }

    private VBox createWeeklyMonthlyPnlWidget() {
        VBox box = createWidgetContainer("PNL Tracker");
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int currentWeek = today.get(weekFields.weekOfWeekBasedYear());
        double weeklyPnl = 0;
        double monthlyPnl = 0;
        for (Map.Entry<String, PnlEntry> entry : dataManager.getPnlData().entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            if (YearMonth.from(date).equals(currentMonth)) {
                monthlyPnl += entry.getValue().pnl;
                if (date.get(weekFields.weekOfWeekBasedYear()) == currentWeek && date.getYear() == today.getYear()) {
                    weeklyPnl += entry.getValue().pnl;
                }
            }
        }
        Label weeklyLabel = new Label(String.format("This Week: $%.2f", weeklyPnl));
        weeklyLabel.getStyleClass().add(weeklyPnl >= 0 ? "positive-text" : "negative-text");
        Label monthlyLabel = new Label(String.format("This Month: $%.2f", monthlyPnl));
        monthlyLabel.getStyleClass().add(monthlyPnl >= 0 ? "positive-text" : "negative-text");
        box.getChildren().addAll(weeklyLabel, monthlyLabel);
        return box;
    }

    private VBox createPnlStreakWidget() {
        VBox box = createWidgetContainer("PNL Streak");
        int streak = 0;
        LocalDate day = LocalDate.now().minusDays(1);
        Map<String, PnlEntry> pnlData = dataManager.getPnlData();
        while (true) {
            PnlEntry entry = pnlData.get(day.toString());
            if (entry != null && entry.pnl >= 0) {
                streak++;
                day = day.minusDays(1);
            } else {
                break;
            }
        }
        Label streakLabel = new Label(String.valueOf(streak));
        streakLabel.setStyle("-fx-font-size: 24px;");
        if (streak > 0) streakLabel.getStyleClass().add("positive-text");
        Label descriptionLabel = new Label("Consecutive Green Days");
        box.getChildren().addAll(streakLabel, descriptionLabel);
        return box;
    }

    private VBox createBestWorstDayWidget() {
        VBox box = createWidgetContainer("Best/Worst Day (Month)");
        YearMonth currentMonth = YearMonth.now();
        double bestPnl = Double.NEGATIVE_INFINITY;
        double worstPnl = Double.POSITIVE_INFINITY;
        for (Map.Entry<String, PnlEntry> entry : dataManager.getPnlData().entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            if (YearMonth.from(date).equals(currentMonth)) {
                double pnl = entry.getValue().pnl;
                if (pnl > bestPnl) bestPnl = pnl;
                if (pnl < worstPnl) worstPnl = pnl;
            }
        }
        if (bestPnl == Double.NEGATIVE_INFINITY) {
            box.getChildren().add(new Label("No PNL data for this month."));
        } else {
            Label bestLabel = new Label(String.format("Best: $%.2f", bestPnl));
            bestLabel.getStyleClass().add("positive-text");
            Label worstLabel = new Label(String.format("Worst: $%.2f", worstPnl));
            worstLabel.getStyleClass().add("negative-text");
            box.getChildren().addAll(bestLabel, worstLabel);
        }
        return box;
    }

    private VBox createRuleOfTheDayWidget() {
        VBox box = createWidgetContainer("Rule of the Day");
        List<String> rules = dataManager.getSettings().rules;
        if (rules == null || rules.isEmpty() || rules.stream().allMatch(String::isBlank)) {
            box.getChildren().add(new Label("No trading rules found in settings."));
        } else {
            List<String> nonEmptyRules = rules.stream().filter(r -> !r.isBlank()).toList();
            int dayOfYear = LocalDate.now().getDayOfYear();
            int ruleIndex = (dayOfYear - 1) % nonEmptyRules.size();
            Label ruleLabel = new Label('"' + nonEmptyRules.get(ruleIndex) + '"');
            ruleLabel.setWrapText(true);
            ruleLabel.setStyle("-fx-font-style: italic;");
            box.getChildren().add(ruleLabel);
        }
        return box;
    }

    private VBox createLatestNoteWidget() {
        VBox box = createWidgetContainer("Latest Journal Note");
        List<Note> notes = dataManager.getNotes();
        if (notes.isEmpty()) {
            box.getChildren().add(new Label("No journal notes found."));
        } else {
            Note latestNote = notes.get(0);
            Label noteText = new Label(latestNote.text);
            noteText.setWrapText(true);
            box.getChildren().add(noteText);
        }
        return box;
    }

    private VBox createPrevDayPnlWidget() {
        VBox box = createWidgetContainer("Previous Day PNL");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        PnlEntry entry = dataManager.getPnlData().get(yesterday.toString());
        if (entry == null) {
            box.getChildren().add(new Label("No PNL data for yesterday."));
        } else {
            Label pnlLabel = new Label(String.format("$%.2f", entry.pnl));
            if (entry.pnl >= 0) pnlLabel.getStyleClass().add("positive-text");
            else pnlLabel.getStyleClass().add("negative-text");
            box.getChildren().add(pnlLabel);
        }
        return box;
    }

    private VBox createCurrentMonthWidget() {
        VBox box = createWidgetContainer("");
        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        Label monthNameLabel = new Label(currentMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)));
        monthNameLabel.getStyleClass().add("h3");
        box.getChildren().add(0, monthNameLabel);
        GridPane miniCalendar = new GridPane();
        miniCalendar.setAlignment(Pos.CENTER);
        miniCalendar.setHgap(4);
        miniCalendar.setVgap(4);
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (int i = 0; i < days.length; i++) {
            miniCalendar.add(new Label(days[i]), i, 0);
        }
        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        double monthTotalPnl = 0;
        for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
            LocalDate date = currentMonth.atDay(day);
            int row = (day + firstDayOfWeek - 2) / 7 + 1;
            int col = (day + firstDayOfWeek - 2) % 7;
            Circle dayCircle = new Circle(8);
            PnlEntry entry = dataManager.getPnlData().get(date.toString());
            if (entry != null) {
                monthTotalPnl += entry.pnl;
                dayCircle.getStyleClass().add(entry.pnl >= 0 ? "positive-day-circle" : "negative-day-circle");
            } else {
                dayCircle.getStyleClass().add("no-pnl-day-circle");
            }
            miniCalendar.add(dayCircle, col, row);
        }
        Label monthTotalLabel = new Label(String.format("$%.2f", monthTotalPnl));
        if (monthTotalPnl >= 0) monthTotalLabel.getStyleClass().add("positive-text");
        else monthTotalLabel.getStyleClass().add("negative-text");
        box.getChildren().addAll(miniCalendar, monthTotalLabel);
        return box;
    }

    private VBox createUdCounterWidget() {
        VBox box = createWidgetContainer("Monthly Undiscipline");
        YearMonth currentMonth = YearMonth.now();
        int totalUndisciplined = 0;
        for (Map.Entry<String, PnlEntry> entry : dataManager.getPnlData().entrySet()) {
            LocalDate date = LocalDate.parse(entry.getKey());
            if (YearMonth.from(date).equals(currentMonth)) {
                totalUndisciplined += entry.getValue().undisciplineCount;
            }
        }
        Label countLabel = new Label(String.valueOf(totalUndisciplined));
        countLabel.getStyleClass().add("negative-text");
        countLabel.setStyle("-fx-font-size: 24px;");
        Label descriptionLabel = new Label("Undisciplined actions this month");
        box.getChildren().addAll(countLabel, descriptionLabel);
        return box;
    }

    private VBox createWidgetContainer(String title) {
        VBox box = new VBox(10);
        box.getStyleClass().add("glass-widget");
        box.setAlignment(Pos.CENTER);
        if (!title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("h3");
            titleLabel.setWrapText(true);
            box.getChildren().add(titleLabel);
        }
        return box;
    }

    @FXML private void handleAddWidgets() {
        if(welcomePane.isVisible()) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), welcomePane);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> mainApp.showAddWidgets());
            fadeOut.play();
        } else {
            mainApp.showAddWidgets();
        }
    }
}