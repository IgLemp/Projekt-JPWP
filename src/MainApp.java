package com.transport;

import com.transport.sim.GameSettings;
import com.transport.sim.Simulator;
import com.transport.ui.StartScreen;
import com.transport.ui.UIFactory;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Transport Manager Tycoon");
        showStartScreen();
    }

    /**
     * Displays the initial Start/Configuration screen.
     */
    private void showStartScreen() {
        StartScreen startScreen = new StartScreen();
        
        // callback: when user clicks "Start Game", run startGame() with their settings
        Region content = startScreen.createContent(this::startGame);
        
        Scene scene = new Scene(content, 900, 700);
        // Load CSS if available (optional)
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Initializes the Simulator and switches to the Main Game UI.
     */
    private void startGame(GameSettings settings) {
        Simulator sim = new Simulator(settings);
        
        // FIX: Passed 'this::showStartScreen' as the second argument.
        // This is the Runnable that the BankruptcyScreen will execute to restart the game.
        BorderPane gameRoot = UIFactory.createMainUI(sim, this::showStartScreen);
        
        Scene scene = new Scene(gameRoot, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
