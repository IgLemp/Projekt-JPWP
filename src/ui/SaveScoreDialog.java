package com.transport.ui;

import com.transport.score.ScoreRecord;
import com.transport.score.ScoreService;
import com.transport.sim.Simulator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SaveScoreDialog {
    public static void show(Simulator sim, double score) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Zapisz Wynik");

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f4;");

        Label scoreLabel = new Label("TWÓJ WYNIK: " + String.format("%.0f pkt", score));
        scoreLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Imię gracza (max 20 znaków)");
        nameField.setMaxWidth(250);
        
        nameField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal.length() > 20) nameField.setText(old);
        });

        Button btnSave = new Button("WYBIERZ MIEJSCE I ZAPISZ");
        btnSave.setPrefWidth(250);
        btnSave.setStyle("-fx-base: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");

        btnSave.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Imię nie może być puste!").show();
                return;
            }

            FileChooser fc = new FileChooser();
            String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            fc.setInitialFileName("Transport_Score_" + ts + ".score");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Pliki wyników", "*.score"));
            
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try {
                    ScoreRecord record = ScoreRecord.builder()
                            .playerName(name)
                            .finalScore(score)
                            .timestamp(LocalDateTime.now().toString())
                            .totalCash(sim.getCompany().getCash())
                            .reputation(sim.getCompany().getReputation())
                            .vehiclesOwned(sim.getCompany().getVehicles().size())
                            .turnsReached(sim.getTurn())
                            .build();
                    new ScoreService().saveScore(record, file);
                    stage.close();
                } catch (Exception ex) {
                    new Alert(Alert.AlertType.ERROR, "Błąd: " + ex.getMessage()).show();
                }
            }
        });

        layout.getChildren().addAll(scoreLabel, new Label("Podaj imię:"), nameField, btnSave);
        stage.setScene(new Scene(layout, 350, 250));
        stage.showAndWait();
    }
}
