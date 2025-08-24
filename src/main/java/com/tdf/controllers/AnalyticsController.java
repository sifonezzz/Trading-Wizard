package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.PnlEntry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsController implements Controller {

    @FXML private ComboBox<String> timeframeComboBox;
    @FXML private Label totalPnlLabel, profitFactorLabel, winRateLabel, winLossDaysLabel, avgPnlLabel, avgWinLossLabel, maxDrawdownLabel, totalUdLabel;
    @FXML private ScatterChart<Number, Number> disciplineChart;
    
    private MainApp mainApp;
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        
        timeframeComboBox.setItems(FXCollections.observableArrayList("This Month", "Last 30 Days", "This Year", "All Time"));
        timeframeComboBox.getSelectionModel().select("All Time");
        timeframeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateAnalytics());
        
        updateAnalytics();
    }

    private void updateAnalytics() {
        Map<String, PnlEntry> pnlData = dataManager.getPnlData();
        List<Map.Entry<String, PnlEntry>> filteredData = filterDataByTimeframe(pnlData);

        if (filteredData.isEmpty()) {
            resetUI();
            return;
        }

        calculatePerformanceMetrics(filteredData);
        populateDisciplineChart(filteredData);
    }
    
    private List<Map.Entry<String, PnlEntry>> filterDataByTimeframe(Map<String, PnlEntry> pnlData) {
        String timeframe = timeframeComboBox.getValue();
        LocalDate now = LocalDate.now();

        return pnlData.entrySet().stream()
            .filter(entry -> {
                LocalDate date = LocalDate.parse(entry.getKey());
                return switch (timeframe) {
                    case "This Month" -> YearMonth.from(date).equals(YearMonth.from(now));
                    case "Last 30 Days" -> !date.isBefore(now.minusDays(30));
                    case "This Year" -> date.getYear() == now.getYear();
                    default -> true; // All Time
                };
            })
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors.toList());
    }

    private void calculatePerformanceMetrics(List<Map.Entry<String, PnlEntry>> data) {
        double totalPnl = 0;
        double grossProfit = 0;
        double grossLoss = 0;
        int greenDays = 0;
        int redDays = 0;
        int totalUd = 0;

        for (Map.Entry<String, PnlEntry> entry : data) {
            double pnl = entry.getValue().pnl;
            totalPnl += pnl;
            totalUd += entry.getValue().undisciplineCount;
            if (pnl > 0) {
                greenDays++;
                grossProfit += pnl;
            } else if (pnl < 0) {
                redDays++;
                grossLoss += Math.abs(pnl);
            }
        }

        double profitFactor = (grossLoss == 0) ? Double.POSITIVE_INFINITY : grossProfit / grossLoss;
        double winRate = (greenDays + redDays == 0) ? 0 : (double) greenDays / (greenDays + redDays) * 100;
        double avgPnl = data.isEmpty() ? 0 : totalPnl / data.size();
        double avgWin = (greenDays == 0) ? 0 : grossProfit / greenDays;
        double avgLoss = (redDays == 0) ? 0 : -grossLoss / redDays;
        
        double maxDrawdown = 0;
        double peak = Double.NEGATIVE_INFINITY;
        double cumulativePnl = 0;
        for (Map.Entry<String, PnlEntry> entry : data) {
            cumulativePnl += entry.getValue().pnl;
            if (cumulativePnl > peak) {
                peak = cumulativePnl;
            }
            double drawdown = peak - cumulativePnl;
            if (drawdown > maxDrawdown) {
                maxDrawdown = drawdown;
            }
        }
        
        totalPnlLabel.setText(String.format("$%.2f", totalPnl));
        profitFactorLabel.setText(profitFactor == Double.POSITIVE_INFINITY ? "Infinity" : String.format("%.2f", profitFactor));
        winRateLabel.setText(String.format("%.2f%%", winRate));
        winLossDaysLabel.setText(String.format("%d Green / %d Red", greenDays, redDays));
        avgPnlLabel.setText(String.format("$%.2f", avgPnl));
        avgWinLossLabel.setText(String.format("Avg Win: $%.2f / Avg Loss: $%.2f", avgWin, avgLoss));
        maxDrawdownLabel.setText(String.format("-$%.2f", maxDrawdown));
        totalUdLabel.setText(String.valueOf(totalUd));
    }

    private void populateDisciplineChart(List<Map.Entry<String, PnlEntry>> data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Trading Days");
        for (Map.Entry<String, PnlEntry> entry : data) {
            series.getData().add(new XYChart.Data<>(entry.getValue().undisciplineCount, entry.getValue().pnl));
        }
        disciplineChart.getData().setAll(series);
    }
    
    @FXML
    private void handleExportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PNL Data");
        fileChooser.setInitialFileName("pnl_export_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(mainApp.getPrimaryStage());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Date,PNL,Undisciplined Actions,Note\n");
                for (Map.Entry<String, PnlEntry> entry : dataManager.getPnlData().entrySet()) {
                    String note = entry.getValue().note != null ? entry.getValue().note.replace("\"", "\"\"") : "";
                    writer.append(String.format("%s,%.2f,%d,\"%s\"\n",
                        entry.getKey(),
                        entry.getValue().pnl,
                        entry.getValue().undisciplineCount,
                        note
                    ));
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "PNL data has been saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Export Failed", "An error occurred while saving the file.");
                e.printStackTrace();
            }
        }
    }

    private void resetUI() {
        totalPnlLabel.setText("$0.00");
        profitFactorLabel.setText("0.00");
        winRateLabel.setText("0.00%");
        winLossDaysLabel.setText("0 Green / 0 Red");
        avgPnlLabel.setText("$0.00");
        avgWinLossLabel.setText("Avg Win: $0.00 / Avg Loss: $0.00");
        maxDrawdownLabel.setText("-$0.00");
        totalUdLabel.setText("0");
        disciplineChart.getData().clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}