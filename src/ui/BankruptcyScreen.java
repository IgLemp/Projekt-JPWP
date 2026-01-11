package com.transport.ui;

import com.transport.sim.Simulator;
import com.transport.score.ScoreService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class BankruptcyScreen {

    public static VBox create(Simulator sim, Runnable onRestart) {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 0);");
        root.setMaxSize(500, 550);

        Text title = new Text("BANKRUCTWO");
        title.setFont(Font.font("System", FontWeight.BOLD, 40));
        title.setFill(Color.DARKRED);

        ScoreService scoreService = new ScoreService();
        double finalScore = scoreService.calculateFinalScore(sim);

        VBox stats = new VBox(12);
        stats.setAlignment(Pos.CENTER);
        stats.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 20; -fx-background-radius: 10;");
        
        stats.getChildren().addAll(
            createStat("Przetrwano tur:", String.valueOf(sim.getTurn())),
            createStat("Reputacja firmy:", String.format("%.1f", sim.getCompany().getReputation())),
            createStat("WYNIK KOŃCOWY:", String.format("%.0f pkt", finalScore))
        );

        Button btnSave = new Button("ZAPISZ SWÓJ WYNIK");
        btnSave.setPrefWidth(220);
        btnSave.setStyle("-fx-base: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnSave.setOnAction(e -> SaveScoreDialog.show(sim, finalScore));

        Button btnRestart = new Button("POWRÓT DO MENU");
        btnRestart.setPrefWidth(220);
        btnRestart.setOnAction(e -> onRestart.run());

        root.getChildren().addAll(title, stats, btnSave, btnRestart);
        return root;
    }

    private static VBox createStat(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        box.getChildren().addAll(l, v);
        return box;
    }
}
