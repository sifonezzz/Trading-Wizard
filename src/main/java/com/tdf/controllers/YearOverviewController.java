package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.PnlEntry;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class YearOverviewController implements Controller {
    
    @FXML private Label yearLabel;
    @FXML private Label totalPnlLabel;
    @FXML private GridPane yearGrid;
    
    private MainApp mainApp;
    private int year = LocalDate.now().getYear();

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        Map<String, PnlEntry> pnlData = MainApp.getDataManager().getPnlData();
        yearLabel.setText(year + " PNL Overview");
        drawYear(pnlData);
    }
    
    private void drawYear(Map<String, PnlEntry> pnlData) {
        double yearTotalPnl = 0;
        
        for (int month = 1; month <= 12; month++) {
            VBox monthBox = createMonthBox(year, month, pnlData);
            int row = (month - 1) / 4;
            int col = (month - 1) % 4;
            yearGrid.add(monthBox, col, row);

            for(Map.Entry<String, PnlEntry> entry : pnlData.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());
                if (date.getYear() == year && date.getMonthValue() == month) {
                    yearTotalPnl += entry.getValue().pnl;
                }
            }
        }
        
        totalPnlLabel.setText(String.format("Total PNL: $%.2f", yearTotalPnl));
        if (yearTotalPnl >= 0) {
            totalPnlLabel.getStyleClass().add("positive-text");
        } else {
            totalPnlLabel.getStyleClass().add("negative-text");
        }
    }
    
    private VBox createMonthBox(int year, int month, Map<String, PnlEntry> pnlData) {
        VBox monthBox = new VBox(5);
        monthBox.setAlignment(Pos.CENTER);
        monthBox.getStyleClass().add("note-box");
        
        YearMonth yearMonth = YearMonth.of(year, month);
        Label monthNameLabel = new Label(yearMonth.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH)));
        monthNameLabel.getStyleClass().add("h3");
        
        GridPane miniCalendar = new GridPane();
        miniCalendar.setAlignment(Pos.CENTER);
        miniCalendar.setHgap(4);
        miniCalendar.setVgap(4);
        
        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for(int i=0; i < days.length; i++) {
            miniCalendar.add(new Label(days[i]), i, 0);
        }
        
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        
        double monthTotalPnl = 0;

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            int row = (day + firstDayOfWeek - 2) / 7 + 1;
            int col = (day + firstDayOfWeek - 2) % 7;
            
            Circle dayCircle = new Circle(8);
            PnlEntry entry = pnlData.get(date.toString());
            
            if (entry != null) {
                monthTotalPnl += entry.pnl;
                if(entry.pnl >= 0) {
                    dayCircle.getStyleClass().add("positive-day-circle");
                } else {
                    dayCircle.getStyleClass().add("negative-day-circle");
                }
            } else {
                dayCircle.getStyleClass().add("no-pnl-day-circle");
            }
            miniCalendar.add(dayCircle, col, row);
        }
        
        Label monthTotalLabel = new Label(String.format("$%.2f", monthTotalPnl));
        if(monthTotalPnl >= 0) monthTotalLabel.getStyleClass().add("positive-text");
        else monthTotalLabel.getStyleClass().add("negative-text");

        monthBox.getChildren().addAll(monthNameLabel, miniCalendar, monthTotalLabel);
        return monthBox;
    }
    
    @FXML
    private void handleBackToMenu() {
        mainApp.showPnlCalendar();
    }

} // <-- This was the missing curly brace