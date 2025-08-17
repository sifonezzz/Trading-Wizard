package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Note;
import com.tdf.data.PnlEntry;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardController implements Controller {

    @FXML private VBox welcomePane;
    @FXML private FlowPane widgetPane;
    @FXML private Button smallAddButton;
    
    private MainApp mainApp;
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        buildDashboard();
    }
    
    private void buildDashboard() {
        List<String> activeWidgets = dataManager.getSettings().activeWidgets;
        
        if (activeWidgets == null || activeWidgets.isEmpty()) {
            welcomePane.setVisible(true);
            welcomePane.setManaged(true);
            smallAddButton.setVisible(false);
            smallAddButton.setManaged(false);
        } else {
            welcomePane.setVisible(false);
            welcomePane.setManaged(false);
            smallAddButton.setVisible(true);
            smallAddButton.setManaged(true);
            
            for (String widgetName : activeWidgets) {
                VBox widget = createWidget(widgetName);
                if (widget != null) {
                    widgetPane.getChildren().add(widget);
                }
            }
        }
    }
    
    private VBox createWidget(String widgetName) {
        return switch (widgetName) {
            case "Latest Journal Note" -> createLatestNoteWidget();
            case "Previous Day PNL" -> createPrevDayPnlWidget();
            case "Current Month Calendar" -> createCurrentMonthWidget();
            case "Monthly Undiscipline Counter" -> createUdCounterWidget();
            default -> null;
        };
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
        // --- THIS IS THE FIX ---
        countLabel.getStyleClass().add("negative-text"); // Apply the red color style
        countLabel.setStyle("-fx-font-size: 24px;"); // Keep bold style implicitly from class
        
        Label descriptionLabel = new Label("Undisciplined actions this month");
        
        box.getChildren().addAll(countLabel, descriptionLabel);
        return box;
    }

    private VBox createWidgetContainer(String title) {
        VBox box = new VBox(10);
        box.getStyleClass().add("note-box");
        box.setPrefWidth(280);
        box.setAlignment(Pos.CENTER);
        if (!title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("h3");
            box.getChildren().add(titleLabel);
        }
        return box;
    }
    
    @FXML
    private void handleAddWidgets() {
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