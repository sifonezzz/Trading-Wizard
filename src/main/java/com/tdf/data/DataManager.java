package com.tdf.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataManager {

    private static final String BASE_DIR_NAME = ".TradingDisciplineFramework";
    private static final String BASE_DIR = System.getProperty("user.home") + File.separator + BASE_DIR_NAME;
    private static final Path SETTINGS_FILE = Paths.get(BASE_DIR, "settings.json");
    private static final Path NOTES_FILE = Paths.get(BASE_DIR, "notes.json");
    private static final Path PNL_FILE = Paths.get(BASE_DIR, "pnl.json");
    private static final Path SETUP_SAMPLES_FILE = Paths.get(BASE_DIR, "setup_samples.json");
    private static final Path GOALS_FILE = Paths.get(BASE_DIR, "goals.json");
    private static final String[] ALL_FILES = {"settings.json", "notes.json", "pnl.json", "setup_samples.json", "goals.json"};
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Settings settings;
    private List<Note> notes;
    private Map<String, PnlEntry> pnlData;
    private List<SetupSample> setupSamples;
    private List<Goal> goals;

    public DataManager() {
        try {
            Files.createDirectories(Paths.get(BASE_DIR));
        } catch (IOException e) {
            System.err.println("Could not create application data directory.");
            e.printStackTrace();
        }
        loadAllData();
    }
    
    public void loadAllData() {
        loadSettings();
        loadNotes();
        loadPnlData();
        loadSetupSamples();
        loadGoals();
    }

    public String getBaseDir() {
        return BASE_DIR;
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
        if (settings.maxLoss > 0 && entry.pnl < -settings.maxLoss) {
            entry.exceededMaxLoss = true;
        }
        pnlData.put(dateStr, entry);
        savePnlData();
    }

    public void incrementUndisciplineCount(String dateStr) {
        PnlEntry entry = pnlData.getOrDefault(dateStr, new PnlEntry());
        entry.undisciplineCount++;
        pnlData.put(dateStr, entry);
        savePnlData();
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
    
    public List<SetupSample> getSetupSamples() { return setupSamples; }

    public void saveSetupSamples() {
        try (FileWriter writer = new FileWriter(SETUP_SAMPLES_FILE.toFile())) {
            gson.toJson(setupSamples, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }
    
    private void loadSetupSamples() {
        if (Files.exists(SETUP_SAMPLES_FILE)) {
            try (FileReader reader = new FileReader(SETUP_SAMPLES_FILE.toFile())) {
                Type listType = new TypeToken<ArrayList<SetupSample>>(){}.getType();
                setupSamples = gson.fromJson(reader, listType);
                if (setupSamples == null) {
                    setupSamples = new ArrayList<>();
                }
            } catch (IOException e) {
                setupSamples = new ArrayList<>();
            }
        } else {
            setupSamples = new ArrayList<>();
        }
    }
    
    public List<Goal> getGoals() { return goals; }

    public void saveGoals() {
        try (FileWriter writer = new FileWriter(GOALS_FILE.toFile())) {
            gson.toJson(goals, writer);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadGoals() {
        if (Files.exists(GOALS_FILE)) {
            try (FileReader reader = new FileReader(GOALS_FILE.toFile())) {
                Type listType = new TypeToken<ArrayList<Goal>>(){}.getType();
                goals = gson.fromJson(reader, listType);
                if (goals == null) goals = new ArrayList<>();
            } catch (IOException e) { goals = new ArrayList<>(); }
        } else { goals = new ArrayList<>(); }
    }
    
    public boolean createBackup(File targetZipFile) {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetZipFile))) {
            for (String fileName : ALL_FILES) {
                File fileToBackup = new File(BASE_DIR, fileName);
                if (fileToBackup.exists()) {
                    zos.putNextEntry(new ZipEntry(fileName));
                    Files.copy(fileToBackup.toPath(), zos);
                    zos.closeEntry();
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreFromBackup(File sourceZipFile) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceZipFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(BASE_DIR, zipEntry.getName());
                Files.copy(zis, newFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            loadAllData();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}