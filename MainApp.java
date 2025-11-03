package com.transport;

import com.transport.ui.UIFactory;
import com.transport.sim.Simulator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {
    private Simulator simulator;

    @Override
    public void start(Stage stage) {
        simulator = new Simulator();
        simulator.initSampleData();

        BorderPane root = UIFactory.createMainUI(simulator);
        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Transport Manager - Demo");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}

