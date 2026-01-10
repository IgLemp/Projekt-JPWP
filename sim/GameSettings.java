package com.transport.sim;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class GameSettings {

    @Getter
    @RequiredArgsConstructor
    public enum Difficulty {
        CASUAL("Niedzielny Kierowca", "Dla relaksu. Dużo gotówki, tanie paliwo.", 
               50000.0, 0.8, 0.5, 1.2, "#4caf50"),
        
        ENTREPRENEUR("Przedsiębiorca", "Standardowa rozgrywka. Zbalansowana ekonomia.", 
                     15000.0, 1.0, 1.0, 1.0, "#2196f3"),
        
        TYCOON("Magnat", "Wyzwanie. Wysokie koszty, wymagający rynek.", 
               5000.0, 1.3, 1.5, 0.9, "#ff9800"),
        
        LEGEND("Legenda", "Brutalny realizm. Każda pomyłka kosztuje.", 
               2000.0, 2.0, 2.5, 0.8, "#9c27b0");

        private final String label;
        private final String description;
        private final double startingCash;
        private final double fuelCostMultiplier; // Higher is worse
        private final double maintenanceCostMultiplier; // Higher is worse
        private final double revenueMultiplier; // Lower is worse
        private final String colorCode;
    }

    @Getter private String companyName;
    @Getter private Difficulty difficulty;

    public GameSettings(String companyName, Difficulty difficulty) {
        this.companyName = companyName;
        this.difficulty = difficulty;
    }
}
