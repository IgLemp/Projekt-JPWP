package com.transport.sim;

public class Job {
    private String title;
    private double reward;
    private int routeLength; // km
    private boolean assigned = false;
    private Driver assignedDriver;
    private Vehicle assignedVehicle;

    // deterministic turns remaining
    private int turnsRemaining = 0;
    private boolean completed = false;
    private String result = "";

    public Job(String title, double reward, int routeLength) {
        this.title = title; this.reward = reward; this.routeLength = routeLength;
    }
    public String getTitle() { return title; }
    public double getReward() { return reward; }
    public int getRouteLength() { return routeLength; }
    public boolean isAssigned() { return assigned; }
    public void setAssigned(boolean a) { assigned = a; }

    public void assign(Driver d, Vehicle v) { this.assignedDriver = d; this.assignedVehicle = v; this.assigned = true; this.turnsRemaining = (int)Math.ceil(routeLength / 100.0); }
    public Driver getAssignedDriver() { return assignedDriver; }
    public Vehicle getAssignedVehicle() { return assignedVehicle; }

    public int getTurnsRemaining() { return turnsRemaining; }
    public void decrementTurnsRemaining() { if (turnsRemaining>0) turnsRemaining--; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean c) { completed = c; }
    public String getResult() { return result; }
    public void setResult(String r) { result = r; }
}

