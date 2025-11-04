package com.transport.sim;

import lombok.Getter;
import lombok.Setter;

public class Job {
    @Getter private String title;
    @Getter private double reward;
    @Getter private int routeLength; // km
    @Setter private boolean assigned = false;
    @Getter private Driver assignedDriver;
    @Getter private Vehicle assignedVehicle;

    // deterministic turns remaining
    @Getter private int turnsRemaining = 0;
    @Setter private boolean completed = false;
    @Getter @Setter private String result = "";

    public Job(String title, double reward, int routeLength) {
        this.title = title; this.reward = reward; this.routeLength = routeLength;
    }
    public boolean isAssigned() { return assigned; }

    public void assign(Driver d, Vehicle v) { this.assignedDriver = d; this.assignedVehicle = v; this.assigned = true; this.turnsRemaining = (int)Math.ceil(routeLength / 100.0); }

    public void decrementTurnsRemaining() { if (turnsRemaining>0) turnsRemaining--; }

    public boolean isCompleted() { return completed; }
}

