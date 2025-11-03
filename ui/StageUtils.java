package com.transport.ui;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ProgressIndicator;

public class StageUtils {
    private static StackPane overlayPane;

    public static void showTemporaryOverlay(StackPane parent, Node node) {
        if (overlayPane == null) overlayPane = new StackPane();
        overlayPane.getChildren().clear();
        overlayPane.getChildren().add(node);
        overlayPane.setStyle("-fx-background-color: rgba(0,0,0,0.25);");
        overlayPane.setPrefSize(parent.getWidth(), parent.getHeight());
        parent.getChildren().add(overlayPane);
    }

    public static void hideTemporaryOverlay(StackPane parent) {
        parent.getChildren().remove(overlayPane);
    }
}

