package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;
import com.tdf.data.DataManager;
import com.tdf.data.Note;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class NotesViewController implements Controller {
    @FXML private VBox notesContainer;
    @FXML private TextField newNoteField;
    @FXML private Button addNoteButton;
    
    private DataManager dataManager;

    @Override
    public void setMainApp(MainApp mainApp) {
        this.dataManager = MainApp.getDataManager();
        loadNotes();
    }

    private void loadNotes() {
        notesContainer.getChildren().clear();
        if (dataManager.getNotes().isEmpty()) {
            Label noNotesLabel = new Label("No journal notes saved yet.");
            noNotesLabel.getStyleClass().add("h3");
            notesContainer.getChildren().add(noNotesLabel);
        } else {
            DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd", Locale.ENGLISH);
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
            
            for (int i = 0; i < dataManager.getNotes().size(); i++) {
                Note note = dataManager.getNotes().get(i);
                HBox timelineEntry = createTimelineEntry(note, i, dayFormatter, monthFormatter);
                notesContainer.getChildren().add(timelineEntry);
            }
        }
    }

    private HBox createTimelineEntry(Note note, int index, DateTimeFormatter dayFormatter, DateTimeFormatter monthFormatter) {
        // Date side
        ZonedDateTime zdt = ZonedDateTime.parse(note.date);
        Label dayLabel = new Label(zdt.format(dayFormatter));
        dayLabel.getStyleClass().add("timeline-day");
        Label monthLabel = new Label(zdt.format(monthFormatter).toUpperCase());
        monthLabel.getStyleClass().add("timeline-month");
        VBox dateBox = new VBox(-5, dayLabel, monthLabel);
        dateBox.setAlignment(Pos.CENTER);

        // Center spine
        FontIcon dotIcon = new FontIcon("fas-circle");
        dotIcon.getStyleClass().add("timeline-dot");
        Region line = new Region();
        line.getStyleClass().add("timeline-spine");
        VBox spineBox = new VBox(dotIcon, line);
        spineBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(line, javafx.scene.layout.Priority.ALWAYS);

        // Note card side
        Label textLabel = new Label(note.text);
        textLabel.getStyleClass().add("note-text");
        textLabel.setWrapText(true);
        VBox noteCard = new VBox(textLabel);
        noteCard.getStyleClass().add("note-box");
        noteCard.setAlignment(Pos.CENTER_LEFT);

        HBox entryBox;
        // Alternate sides
        if (index % 2 == 0) {
            entryBox = new HBox(10, dateBox, spineBox, noteCard);
            noteCard.getStyleClass().add("timeline-card-right");
        } else {
            HBox.setHgrow(noteCard, javafx.scene.layout.Priority.ALWAYS);
            entryBox = new HBox(10, noteCard, spineBox, dateBox);
            noteCard.getStyleClass().add("timeline-card-left");
        }
        entryBox.setAlignment(Pos.TOP_CENTER);
        return entryBox;
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