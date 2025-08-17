package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Note;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NotesViewController implements Controller {
    @FXML private VBox notesContainer;
    @FXML private TextField newNoteField;
    @FXML private Button addNoteButton;
    
    private MainApp mainApp;
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
        this.dataManager = MainApp.getDataManager();
        loadNotes();
    }

    private void loadNotes() {
        notesContainer.getChildren().clear();
        if (dataManager.getNotes().isEmpty()) {
            Label noNotesLabel = new Label("No notes saved yet.");
            noNotesLabel.getStyleClass().add("h3");
            notesContainer.getChildren().add(noNotesLabel);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy", Locale.ENGLISH);
            for (Note note : dataManager.getNotes()) {
                VBox noteBox = new VBox(5);
                noteBox.getStyleClass().add("note-box");
                noteBox.setPadding(new Insets(10));

                ZonedDateTime zdt = ZonedDateTime.parse(note.date);
                Label dateLabel = new Label(zdt.format(formatter));
                dateLabel.getStyleClass().add("note-date");

                Label textLabel = new Label(note.text);
                textLabel.getStyleClass().add("note-text");
                textLabel.setWrapText(true);

                noteBox.getChildren().addAll(dateLabel, textLabel);
                notesContainer.getChildren().add(noteBox);
            }
        }
    }

    @FXML
    private void handleAddNote() {
        String noteText = newNoteField.getText();
        if (noteText != null && !noteText.isBlank()) {
            Note newNote = new Note(ZonedDateTime.now().toString(), noteText);
            dataManager.addNote(newNote);
            newNoteField.clear();
            loadNotes(); // Refresh the view
        }
    }
}