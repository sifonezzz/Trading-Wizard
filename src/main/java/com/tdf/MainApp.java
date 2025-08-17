package com.tdf;

import com.tdf.controllers.MainViewController;
import com.tdf.data.DataManager;
import com.tdf.widgets.OnTopWidget;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Objects;

public class MainApp extends Application {

    private Stage primaryStage;
    private OnTopWidget onTopWidget;
    private static DataManager dataManager;
    private MainViewController mainViewController;

    public static DataManager getDataManager() {
        return dataManager;
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        dataManager = new DataManager();
        
        try {
            Font.loadFont(getClass().getResource("/fonts/FiraCode-Regular.ttf").toExternalForm(), 10);
            Font.loadFont(getClass().getResource("/fonts/FiraCode-Bold.ttf").toExternalForm(), 10);
        } catch (Exception e) {
            System.err.println("Could not load fonts: " + e.getMessage());
        }

        primaryStage.setTitle("Trading Wizard");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/MainView.fxml"));
        Parent root = loader.load();

        mainViewController = loader.getController();
        mainViewController.setMainApp(this);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // --- FIX: Re-added this method to prevent the crash ---
    public void showYearOverview() {
        if (mainViewController != null) {
            mainViewController.showYearOverview();
        }
    }
    
    public void startOnTopWidget() {
        primaryStage.hide();
        if (onTopWidget == null) {
            onTopWidget = new OnTopWidget(this);
        }
        onTopWidget.show();
    }
    
    public void returnToMainMenu() {
        if(onTopWidget != null && onTopWidget.isShowing()) {
            onTopWidget.close();
        }
        primaryStage.show();
        mainViewController.showSidebarAndDashboard();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}