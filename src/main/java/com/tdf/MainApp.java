package com.tdf;

import com.tdf.controllers.AddExampleController;
import com.tdf.controllers.ExamplesViewController;
import com.tdf.controllers.MainViewController;
import com.tdf.controllers.SetupDetailController;
import com.tdf.data.DataManager;
import com.tdf.data.SetupSample;
import com.tdf.widgets.OnTopWidget;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

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
            Font.loadFont(getClass().getResourceAsStream("/fonts/FiraCode-Regular.ttf"), 10);
            Font.loadFont(getClass().getResourceAsStream("/fonts/FiraCode-Bold.ttf"), 10);
        } catch (Exception e) {
            System.err.println("CRITICAL ERROR: Could not load Fira Code font.");
            e.printStackTrace();
        }

        primaryStage.setTitle("Trading Wizard");
        primaryStage.initStyle(StageStyle.UNDECORATED);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/MainView.fxml"));
        Parent root = loader.load();

        mainViewController = loader.getController();
        mainViewController.setMainApp(this);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/tdf/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    // --- NEW NAVIGATION METHODS ---
    public void showSetupDetail(SetupSample setup) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/SetupDetailView.fxml"));
            Node view = loader.load();
            
            SetupDetailController controller = loader.getController();
            controller.setMainApp(this);
            controller.setSetup(setup);
            
            mainViewController.setContent(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showSetups() {
        if (mainViewController != null) {
            mainViewController.showSetups();
        }
    }
    
    public void cancelTradingFlow() {
        if (mainViewController != null) {
            mainViewController.showSidebarAndDashboard();
        }
    }
    
    public void showAddWidgets() {
        if (mainViewController != null) {
            mainViewController.showAddWidgets();
        }
    }
    
    public void showDashboard() {
        if (mainViewController != null) {
            mainViewController.showDashboard();
        }
    }
    
    public void showYearOverview() {
        if (mainViewController != null) {
            mainViewController.showYearOverview();
        }
    }

    public void showPnlCalendar() {
        if (mainViewController != null) {
            mainViewController.showCalendar();
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

    public void showExamplesView(SetupSample setup, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/ExamplesView.fxml"));
            Node view = loader.load();
            
            ExamplesViewController controller = loader.getController();
            controller.setMainApp(this);
            controller.setExamples(setup, type);
            
            mainViewController.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showAddExampleView(SetupSample setup, String type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/tdf/fxml/AddExampleView.fxml"));
            Node view = loader.load();
            
            AddExampleController controller = loader.getController();
            controller.setMainApp(this);
            controller.setContext(setup, type);
            
            mainViewController.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}