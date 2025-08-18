package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.PnlEntry;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class PnlCalendarController implements Controller {

    @FXML private Label monthLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Button prevButton;
    @FXML private Button nextButton;
    @FXML private LineChart<String, Number> equityChart;

    private MainApp mainApp;
    private DataManager dataManager;
    private YearMonth currentMonth;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        this.currentMonth = YearMonth.now();
        drawAll();
    }
    
    private void drawAll() {
        drawCalendar();
        drawEquityCurve("All");
    }

    private void drawCalendar() {
        monthLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)));
        calendarGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        LocalDate firstDayOfMonth = currentMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();
        int daysInMonth = currentMonth.lengthOfMonth();
        int day = 1;
        
        double[] weeklyPnls = new double[6];
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = new VBox(2);
                dayCell.setAlignment(Pos.CENTER);
                dayCell.getStyleClass().add("calendar-day-cell");

                if (!((row == 1 && col < firstDayOfWeek -1) || day > daysInMonth)) {
                    Label dayLabel = new Label(String.valueOf(day));
                    dayLabel.getStyleClass().add("day-label");
                    dayCell.getChildren().add(dayLabel);
                    
                    String dateStr = currentMonth.atDay(day).toString();
                    PnlEntry pnlEntry = dataManager.getPnlData().get(dateStr);
                    if (pnlEntry != null) {
                        double pnl = pnlEntry.pnl;
                        weeklyPnls[row-1] += pnl;
                        
                        Label pnlLabel = new Label(String.format("$%.2f", pnl));
                        dayCell.getChildren().add(pnlLabel);
                        if (pnlEntry.undisciplineCount > 0) {
                            Label udLabel = new Label("UD: " + pnlEntry.undisciplineCount);
                            dayCell.getChildren().add(udLabel);
                        }
                        
                        if (pnlEntry.exceededMaxLoss) {
                            Label maxLossLabel = new Label("PAST MAX LOSS");
                            maxLossLabel.getStyleClass().add("max-loss-label");
                            // FIX: Add a 3px bottom margin to the label
                            VBox.setMargin(maxLossLabel, new javafx.geometry.Insets(0, 0, 3, 0));
                            dayCell.getChildren().add(maxLossLabel);
                        }

                        if (pnl >= 0) {
                            dayCell.getStyleClass().add("positive-day");
                        } else {
                            dayCell.getStyleClass().add("negative-day");
                        }
                        
                        if (pnlEntry.note != null && !pnlEntry.note.isBlank()) {
                            Tooltip.install(dayCell, new Tooltip(pnlEntry.note));
                        }
                    }
                    day++;
                }
                
                calendarGrid.add(dayCell, col, row);
            }
        }
        updateNavButtons();
        updateTotals(weeklyPnls);
    }
    
    private void updateTotals(double[] weeklyPnls) {
        double monthlyTotal = 0;

        for(int i = 0; i < weeklyPnls.length; i++) {
            double pnl = weeklyPnls[i];
            
            VBox weeklyTotalCell = new VBox();
            weeklyTotalCell.setAlignment(Pos.CENTER);
            weeklyTotalCell.getStyleClass().add("totals-cell");
            
            Label weeklyLabel = new Label(String.format("$%.2f", pnl));
            if (pnl > 0) weeklyLabel.getStyleClass().add("positive-text");
            else if (pnl < 0) weeklyLabel.getStyleClass().add("negative-text");
            
            weeklyTotalCell.getChildren().add(weeklyLabel);
            
            calendarGrid.add(weeklyTotalCell, 8, i + 1);
            monthlyTotal += pnl;
        }
        
        VBox finalTotalCell = new VBox();
        finalTotalCell.setAlignment(Pos.CENTER);
        finalTotalCell.getStyleClass().add("totals-cell");
        
        Label totalLabel = new Label(String.format("$%.2f", monthlyTotal));
        totalLabel.getStyleClass().add("total-pnl-label");
        if (monthlyTotal > 0) totalLabel.getStyleClass().add("positive-text");
        else if (monthlyTotal < 0) totalLabel.getStyleClass().add("negative-text");
        
        finalTotalCell.getChildren().add(totalLabel);

        calendarGrid.add(finalTotalCell, 8, 7);
    }

    private void updateNavButtons() {
        nextButton.setDisable(currentMonth.isAfter(YearMonth.now()) || currentMonth.equals(YearMonth.now()));
        prevButton.setDisable(false);
    }

    @FXML private void handlePrevMonth() {
        currentMonth = currentMonth.minusMonths(1);
        drawAll();
    }

    @FXML private void handleNextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        drawAll();
    }
    
    @FXML
    private void showYearOverview() {
        mainApp.showYearOverview();
    }

    @FXML private void filterAll() { drawEquityCurve("All"); }
    @FXML private void filterYear() { drawEquityCurve("Year"); }
    @FXML private void filterMonth() { drawEquityCurve("Month"); }

    private void drawEquityCurve(String timeframe) {
        equityChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        Map<String, PnlEntry> pnlData = dataManager.getPnlData();
        Map<String, PnlEntry> filteredData = new TreeMap<>();
        LocalDate now = LocalDate.now();

        for(Map.Entry<String, PnlEntry> entry : pnlData.entrySet()) {
            LocalDate entryDate = LocalDate.parse(entry.getKey());
            boolean include = switch (timeframe) {
                case "Month" -> YearMonth.from(entryDate).equals(YearMonth.from(now));
                case "Year" -> entryDate.getYear() == now.getYear();
                default -> true;
            };
            if(include) {
                filteredData.put(entry.getKey(), entry.getValue());
            }
        }

        if(filteredData.isEmpty()) {
            return;
        }

        NumberAxis yAxis = (NumberAxis) equityChart.getYAxis();
        yAxis.setForceZeroInRange(true);

        double cumulativePnl = 0;
        for (Map.Entry<String, PnlEntry> entry : filteredData.entrySet()) {
            cumulativePnl += entry.getValue().pnl;
            series.getData().add(new XYChart.Data<>(entry.getKey(), cumulativePnl));
        }

        equityChart.getData().add(series);
    }
}