package com.transport;

import com.transport.ui.UIFactory;
import com.transport.ui.StartScreen;
import com.transport.sim.Simulator;
import com.transport.sim.Vehicle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Simulator simulator;

    @Override
    public void start(Stage primaryStage) {
    // Load CSS
    // Application.setUserAgentStylesheet(getClass().getResource("./styles.css").toExternalForm());

    StartScreen startScreen = new StartScreen();

    // The callback determines what happens when user clicks "Start"
    Scene startScene = new Scene(startScreen.createContent(settings -> {
        // 1. User clicked start
        System.out.println("Starting game: " + settings.getDifficulty());

        // 2. Initialize Simulator with settings
        Simulator sim = new Simulator(settings);

        // 3. Create Main Game UI
        BorderPane gameRoot = UIFactory.createMainUI(sim);
        
        // 4. Switch Scene
        Scene gameScene = new Scene(gameRoot, 1024, 768);
        
        // Apply CSS to game scene as well if needed
        // gameScene.getStylesheets().add(...);
        
        primaryStage.setScene(gameScene);
        primaryStage.centerOnScreen();
    }), 900, 600);
    
    // Attempt to load CSS if file exists
    try {
        startScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    } catch (Exception e) {
        System.out.println("CSS file not found, using default styles");
    }

    primaryStage.setTitle("Transport Manager 2026");
    primaryStage.setScene(startScene);
    primaryStage.show();
}
    public static void main(String[] args) {
        launch();
    }
}

