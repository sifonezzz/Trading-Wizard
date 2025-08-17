package com.tdf.dialogs;

import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Note;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class EndSessionController {

    @FXML private HBox titleBar;
    @FXML private TextField pnlField;
    @FXML private TextArea wrongNoteArea;
    @FXML private TextArea dayNoteArea;

    private Stage stage;
    private MainApp mainApp;
    private DataManager dataManager;
    private double xOffset = 0;
    private double yOffset = 0;

    // This method will be called to initialize the controller
    public void initialize(Stage stage, MainApp mainApp) {
        this.stage = stage;
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();

        // Make the custom window draggable
        titleBar.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });
        titleBar.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }

    @FXML
    private void handleFinish() {
        processResults(pnlField.getText(), wrongNoteArea.getText(), dayNoteArea.getText());
        mainApp.returnToMainMenu();
        stage.close();
    }
    
    @FXML
    private void handleClose() {
        mainApp.returnToMainMenu();
        stage.close();
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