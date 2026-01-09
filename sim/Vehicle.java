package com.transport.sim;

import lombok.Getter;
import lombok.Setter;

public class Vehicle {
    @Getter private String name;
    @Getter private double value; // purchase price
    @Getter private int condition; // 0-100
    
    // FIX: Added @Getter here so UIFactory can read the interval
    @Getter private int maintenanceIntervalKm; // km between maintenance
    
    @Getter private int kmSinceMaintenance = 0;
    @Getter private double fuelConsumptionPerKm; // units per km

    public Vehicle(String name, double value, int condition, double fuelConsumptionPerKm) {
        this.name = name; 
        this.value = value; 
        this.condition = condition; 
        this.fuelConsumptionPerKm = fuelConsumptionPerKm;
        this.maintenanceIntervalKm = 500; // default interval
    }

    public void repair(int amount) { 
        condition = Math.min(100, condition + amount); 
    }

    public void damage(int amount) { 
        condition = Math.max(0, condition - amount); 
    }

    public void addKm(int km) { 
        kmSinceMaintenance += km; 
    }

    public boolean isMaintenanceDue() { 
        return kmSinceMaintenance >= maintenanceIntervalKm || condition < 50; 
    }

    public double getMaintenanceCost() { 
        return 200 + (100 * (100 - condition)/100.0); 
    }

    public void performMaintenance() { 
        kmSinceMaintenance = 0; 
        repair(20); 
    }
}
