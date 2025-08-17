package com.tdf.data;

public class Note {
    public String date; // ISO 8601 format
    public String text;

    public Note(String date, String text) {
        this.date = date;
        this.text = text;
    }
}