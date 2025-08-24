// FILE: src/main/java/com/tdf/data/Note.java
package com.tdf.data;

import java.util.ArrayList;
import java.util.List;

public class Note {
    public String date; // ISO 8601 format
    public String text;
    public List<String> tags; // NEW FIELD

    public Note(String date, String text) {
        this.date = date;
        this.text = text;
        this.tags = new ArrayList<>(); // NEW INITIALIZATION
    }
}