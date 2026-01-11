package com.transport.ui;

import com.transport.sim.Simulator;
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
        root.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0);");
        root.setMaxSize(500, 400);

        Text title = new Text("BANKRUCTWO");
        title.setFont(Font.font("System", FontWeight.BOLD, 36));
        title.setFill(Color.DARKRED);

        Label subTitle = new Label("Twoja firma utraciła płynność finansową.");
        subTitle.setFont(Font.font(16));

        VBox stats = new VBox(10);
        stats.setAlignment(Pos.CENTER);
        stats.setStyle("-fx-background-color: #f8f8f8; -fx-padding: 20; -fx-background-radius: 5;");
        
        stats.getChildren().addAll(
            createStat("Przetrwano tur:", String.valueOf(sim.getTurn())),
            createStat("Końcowe saldo:", String.format("$%.2f", sim.getCompany().getCash())),
            createStat("Limit bankructwa:", String.format("$%.0f", sim.getSettings().getDifficulty().getBankruptcyLimit())),
            createStat("Zdobyta reputacja:", String.format("%.1f", sim.getCompany().getReputation()))
        );

        Button btnRestart = new Button("WRÓĆ DO MENU");
        btnRestart.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        btnRestart.setPrefWidth(200);
        btnRestart.setPrefHeight(40);
        btnRestart.setOnAction(e -> onRestart.run());

        root.getChildren().addAll(title, subTitle, stats, btnRestart);
        return root;
    }

    private static VBox createStat(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #666; font-size: 12px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        box.getChildren().addAll(l, v);
        return box;
    }
}

