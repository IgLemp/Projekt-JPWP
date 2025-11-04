package com.transport.sim;

import lombok.Getter;
import lombok.Setter;

public class Driver {
    @Getter private String name;
    @Getter private int skill; // 0-100
    @Getter @Setter private Vehicle assignedVehicle;

    public Driver(String name, int skill) { this.name = name; this.skill = skill; }
    public void train(int delta) { skill = Math.min(100, skill + delta); }

    public boolean hasVehicle() { return assignedVehicle != null; }
}

