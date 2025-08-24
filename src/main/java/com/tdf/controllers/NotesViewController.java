package com.tdf.controllers;

import com.tdf.Controller;
import com.tdf.MainApp;

public class NotesViewController implements Controller {
    
    private MainApp mainApp;
    
    @Override
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
}