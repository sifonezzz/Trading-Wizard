package com.tdf.dialogs;

import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Note;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class StopTradingDialog extends Dialog<Void> {

    private DataManager dataManager;

    public StopTradingDialog(MainApp mainApp) {
        this.dataManager = MainApp.getDataManager();
        
        setTitle("End of Session");
        
        // FIX: Apply stylesheet and custom header
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(Objects.requireNonNull(mainApp.getClass().getResource("styles.css")).toExternalForm());
        dialogPane.getStyleClass().add("main-view");
        
        dialogPane.setHeaderText("Log your session details before finishing.");

        ButtonType finishButtonType = new ButtonType("Finish Session", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(finishButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField pnlField = new TextField();
        pnlField.setPromptText("e.g., 50.25 or -25.50");
        TextArea wrongNoteArea = new TextArea();
        wrongNoteArea.setPromptText("What did you do wrong?");
        TextArea dayNoteArea = new TextArea();
        dayNoteArea.setPromptText("General note for the calendar.");

        grid.add(new Label("Today's PNL:"), 0, 0);
        grid.add(pnlField, 1, 0);
        grid.add(new Label("What I did wrong:"), 0, 1);
        grid.add(wrongNoteArea, 1, 1);
        grid.add(new Label("Note of the day:"), 0, 2);
        grid.add(dayNoteArea, 1, 2);

        dialogPane.setContent(grid);
        
        setResultConverter(dialogButton -> {
            if (dialogButton == finishButtonType) {
                processResults(pnlField.getText(), wrongNoteArea.getText(), dayNoteArea.getText());
            }
            return null;
        });
    }
    
    private void processResults(String pnl, String wrongNote, String dayNote) {
        if (wrongNote != null && !wrongNote.isBlank()) {
            Note note = new Note(ZonedDateTime.now().toString(), wrongNote);
            dataManager.addNote(note);
        }
        
        try {
            if (pnl != null && !pnl.isBlank()) {
                double pnlAmount = Double.parseDouble(pnl);
                String todayStr = ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                dataManager.addPnl(todayStr, pnlAmount, dayNote);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid PNL format");
        }
    }
}