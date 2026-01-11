package com.transport.sim;

import java.util.Random;
import lombok.Getter;
import lombok.Setter;

public class Job {
    @Getter private String title;
    @Getter private double reward;
    @Getter private int routeLength;
    @Getter private int minSkillRequired; // New field
    @Getter private double reputationGain; // New field
    @Setter private boolean assigned = false;
    @Getter private Driver assignedDriver;
    @Getter private Vehicle assignedVehicle;
    @Getter private int turnsRemaining = 0;
    @Setter private boolean completed = false;
    @Getter @Setter private String result = "";

    private static final Random RAND = new Random();

    public Job(String title, double reward, int routeLength, int minSkill) {
        this.title = title; 
        this.reward = reward; 
        this.routeLength = routeLength;
        this.minSkillRequired = minSkill;
        // Reputation gain scales with reward and skill difficulty
        this.reputationGain = (reward / 2000.0) + (minSkill * 0.5);
    }

    public boolean isAssigned() { return assigned; }
    
    public void assign(Driver d, Vehicle v) { 
        this.assignedDriver = d; 
        this.assignedVehicle = v; 
        this.assigned = true; 
        this.turnsRemaining = (int)Math.ceil(routeLength / 100.0); 
    }
    
    public void decrementTurnsRemaining() { if (turnsRemaining>0) turnsRemaining--; }
    public boolean isCompleted() { return completed; }

    // Constants for name generation...
    private static final String[] CARGO_TYPES = {"Ładunek", "Transport", "Dostawa", "Przewóz", "Spedycja"};
    private static final String[] GOODS = {"stali", "paliw", "części maszyn", "żywności", "leków"};
    private static final String[] DESTINATIONS = {"Warszawa", "Kraków", "Gdańsk", "Poznań", "Wrocław"};
    private static final String[] URGENCY = {"", " – pilne", " – ekspres", " – priorytet"};

    public static Job randomJob() {
        String type = CARGO_TYPES[RAND.nextInt(CARGO_TYPES.length)];
        String goods = GOODS[RAND.nextInt(GOODS.length)];
        String dest = DESTINATIONS[RAND.nextInt(DESTINATIONS.length)];
        String urgency = URGENCY[RAND.nextInt(URGENCY.length)];

        String name = type + " " + goods + " do " + dest + urgency; 
        int minSkill = RAND.nextInt(10) + 1;
        
        // FIXED: Declare and calculate these variables once
        double reward = (600 + RAND.nextDouble() * 10000) * (1 + (minSkill * 0.2));
        int routeLength = 50 + RAND.nextInt(500);
        
        return new Job(name, reward, routeLength, minSkill);
    }
}
