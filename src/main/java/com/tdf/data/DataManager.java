package com.tdf.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DataManager {

    private static final String BASE_DIR = System.getProperty("user.home") + "/.TradingDisciplineFramework";
    private static final Path SETTINGS_FILE = Paths.get(BASE_DIR, "settings.json");
    private static final Path NOTES_FILE = Paths.get(BASE_DIR, "notes.json");
    private static final Path PNL_FILE = Paths.get(BASE_DIR, "pnl.json");

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Settings settings;
    private List<Note> notes;
    private Map<String, PnlEntry> pnlData;

    public DataManager() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
        } catch (IOException e) {
            System.err.println("Could not create application data directory.");
            e.printStackTrace();
        }
        loadSettings();
        loadNotes();
        loadPnlData();
    }

    public Settings getSettings() { return settings; }

    public void saveSettings(Settings newSettings) {
        this.settings = newSettings;
        try (FileWriter writer = new FileWriter(SETTINGS_FILE.toFile())) {
            gson.toJson(settings, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadSettings() {
        if (Files.exists(SETTINGS_FILE)) {
            try (FileReader reader = new FileReader(SETTINGS_FILE.toFile())) {
                settings = gson.fromJson(reader, Settings.class);
                if (settings == null) settings = Settings.createDefault();
            } catch (IOException e) { settings = Settings.createDefault(); }
        } else {
            settings = Settings.createDefault();
            saveSettings(settings);
        }
    }

    public List<Note> getNotes() { return notes; }

    public void addNote(Note note) {
        notes.add(0, note);
        saveNotes();
    }

    private void loadNotes() {
        if (Files.exists(NOTES_FILE)) {
            try (FileReader reader = new FileReader(NOTES_FILE.toFile())) {
                Type listType = new TypeToken<ArrayList<Note>>(){}.getType();
                notes = gson.fromJson(reader, listType);
                if (notes == null) notes = new ArrayList<>();
            } catch (IOException e) { notes = new ArrayList<>(); }
        } else { notes = new ArrayList<>(); }
    }

    private void saveNotes() {
        try (FileWriter writer = new FileWriter(NOTES_FILE.toFile())) {
            gson.toJson(notes, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<String, PnlEntry> getPnlData() { return new TreeMap<>(pnlData); }

    public void addPnl(String dateStr, double amount, String noteOfDay) {
        PnlEntry entry = pnlData.getOrDefault(dateStr, new PnlEntry());
        entry.pnl += amount;
        if (noteOfDay != null && !noteOfDay.isBlank()) { entry.note = noteOfDay; }
        pnlData.put(dateStr, entry);
        savePnlData();
    }

    // New method for the counter
    public void incrementUndisciplineCount(String dateStr) {
        PnlEntry entry = pnlData.getOrDefault(dateStr, new PnlEntry());
        entry.undisciplineCount++;
        pnlData.put(dateStr, entry);
        savePnlData();
    }

    // New method for the reset button
    public void resetDate(String dateStr) {
        if (pnlData.containsKey(dateStr)) {
            pnlData.remove(dateStr);
            savePnlData();
        }
    }


    private void loadPnlData() {
        if (Files.exists(PNL_FILE)) {
            try (FileReader reader = new FileReader(PNL_FILE.toFile())) {
                Type mapType = new TypeToken<HashMap<String, PnlEntry>>(){}.getType();
                pnlData = gson.fromJson(reader, mapType);
                if(pnlData == null) pnlData = new HashMap<>();
            } catch (IOException e) { pnlData = new HashMap<>(); }
        } else { pnlData = new HashMap<>(); }
    }

    private void savePnlData() {
        try (FileWriter writer = new FileWriter(PNL_FILE.toFile())) {
            gson.toJson(pnlData, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
}