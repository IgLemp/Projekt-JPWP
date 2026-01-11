package com.transport.ui;

import com.transport.sim.GameSettings;
import com.transport.sim.GameSettings.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.function.Consumer;

public class StartScreen {

    private Difficulty selectedDifficulty = Difficulty.ENTREPRENEUR;
    private TextField nameField;
    private VBox root;

    public Region createContent(Consumer<GameSettings> onStartGame) {
        root = new VBox(30);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));
        root.getStyleClass().add("start-screen-root");

        // 1. Title Section
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        Text title = new Text("TRANSPORT MANAGER");
        title.getStyleClass().add("game-title");
        Text subtitle = new Text("Edycja JavaFX");
        subtitle.getStyleClass().add("game-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        // 2. Input Section
        VBox inputBox = new VBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setMaxWidth(400);
        Label nameLabel = new Label("Nazwa Firmy:");
        nameLabel.getStyleClass().add("section-label");
        nameField = new TextField("Trans-Pol Logistyka");
        nameField.getStyleClass().add("company-input");
        inputBox.getChildren().addAll(nameLabel, nameField);

        // 3. Difficulty Section
        Label diffLabel = new Label("Wybierz Poziom Trudności:");
        diffLabel.getStyleClass().add("section-label");
        
        HBox cardsContainer = new HBox(15);
        cardsContainer.setAlignment(Pos.CENTER);
        
        // Create cards
        for (Difficulty diff : Difficulty.values()) {
            cardsContainer.getChildren().add(createDifficultyCard(diff));
        }

        // 4. Action Section
        Button startButton = new Button("ROZPOCZNIJ GRĘ");
        startButton.getStyleClass().add("start-button");
        startButton.setPrefWidth(300);
        startButton.setOnAction(e -> {
            String name = nameField.getText();
            if (name == null || name.trim().isEmpty()) {
                nameField.setStyle("-fx-border-color: red;");
                return;
            }
            onStartGame.accept(new GameSettings(name, selectedDifficulty));
        });

        root.getChildren().addAll(titleBox, inputBox, diffLabel, cardsContainer, startButton);
        
        // Initial selection highlight
        updateSelectionVisuals();
        
        return root;
    }

    private VBox createDifficultyCard(Difficulty diff) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setPrefWidth(220);
        card.setPrefHeight(280);
        card.getStyleClass().add("difficulty-card");
        card.setCursor(Cursor.HAND);
        
        // ID for selection logic
        card.setUserData(diff);

        Label lblTitle = new Label(diff.getLabel());
        lblTitle.getStyleClass().add("diff-title");
        lblTitle.setStyle("-fx-text-fill: " + diff.getColorCode() + ";");

        Label lblDesc = new Label(diff.getDescription());
        lblDesc.setWrapText(true);
        lblDesc.getStyleClass().add("diff-desc");

        // Stats Box
        VBox stats = new VBox(5);
        stats.getStyleClass().add("diff-stats");
        stats.getChildren().add(createStatRow("Gotówka", String.format("$%.0f", diff.getStartingCash())));
        stats.getChildren().add(createStatRow("Koszty", String.format("x%.1f", diff.getMaintenanceCostMultiplier())));
        stats.getChildren().add(createStatRow("Przychód", String.format("x%.1f", diff.getRevenueMultiplier())));

        card.getChildren().addAll(lblTitle, lblDesc, stats);

        // Click Event
        card.setOnMouseClicked(e -> {
            selectedDifficulty = diff;
            updateSelectionVisuals();
        });

        return card;
    }

    private HBox createStatRow(String label, String value) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setStyle("-fx-text-fill: #777; -fx-font-size: 11px;");
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");
        row.getChildren().addAll(l, r, v);
        return row;
    }

    private void updateSelectionVisuals() {
        if (root == null) return;
        HBox container = (HBox) root.getChildren().get(3); // Index of cards container
        
        for (javafx.scene.Node node : container.getChildren()) {
            VBox card = (VBox) node;
            Difficulty cardDiff = (Difficulty) card.getUserData();
            
            card.getStyleClass().remove("selected-card");
            card.setEffect(null);
            
            if (cardDiff == selectedDifficulty) {
                card.getStyleClass().add("selected-card");
                // Add a glow effect matching the difficulty color
                DropShadow glow = new DropShadow();
                glow.setColor(Color.web(cardDiff.getColorCode()));
                glow.setWidth(30);
                glow.setHeight(30);
                card.setEffect(glow);
            }
        }
    }
}
