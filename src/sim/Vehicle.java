package com.transport.sim;

import lombok.Getter;
import java.util.Random;

public class Vehicle {
    @Getter private String name;
    @Getter private double value;
    @Getter private int condition; // 0-100
    @Getter private int maintenanceIntervalKm;
    @Getter private int kmSinceMaintenance = 0;
    @Getter private double fuelConsumptionPerKm;

    private static final String[] PREFIX = {"Swift", "Reliant", "Iron", "Global", "Arctic", "Nomad", "Apex"};
    private static final String[] SUFFIX = {"Hauler", "Voyager", "Carrier", "Transport", "Link", "Titan"};
    private static final Random RNG = new Random();

    public Vehicle(String name, double value, int condition, double fuelConsumptionPerKm) {
        // Procedural naming if no name is provided
        this.name = (name == null || name.isEmpty()) ? generateRandomName() : name; 
        this.value = value; 
        this.condition = condition; 
        this.fuelConsumptionPerKm = fuelConsumptionPerKm;
        this.maintenanceIntervalKm = 500;
    }

    public static String generateRandomName() {
        return PREFIX[RNG.nextInt(PREFIX.length)] + " " + SUFFIX[RNG.nextInt(SUFFIX.length)] + "-" + (100 + RNG.nextInt(899));
    }

    public void repair(int amount) { condition = Math.min(100, condition + amount); }
    public void damage(int amount) { condition = Math.max(0, condition - amount); }
    public void addKm(int km) { kmSinceMaintenance += km; }
    public boolean isMaintenanceDue() { return kmSinceMaintenance >= maintenanceIntervalKm || condition < 50; }
    
    public double getMaintenanceCost() { 
        return 200 + (100 * (100 - condition) / 100.0); 
    }

    public void performMaintenance() { 
        kmSinceMaintenance = 0; 
        repair(20); 
    }
}
