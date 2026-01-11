package com.transport.sim;

import lombok.Getter;
import lombok.Setter;

public class Driver {
    @Getter private String name;
    @Getter private int skill; 
    @Getter private double salary; // Added salary field
    @Getter @Setter private Vehicle assignedVehicle;

    public Driver(String name, int skill, double salary) { 
        this.name = name; 
        this.skill = skill; 
        this.salary = salary; // Updated constructor
    }

    public void train(int delta) { skill = Math.min(100, skill + delta); }

    public boolean hasVehicle() { return assignedVehicle != null; }
}
