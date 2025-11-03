package com.transport.sim;

public class Driver {
    private String name;
    private int skill; // 0-100
    private Vehicle assignedVehicle;

    public Driver(String name, int skill) { this.name = name; this.skill = skill; }
    public String getName() { return name; }
    public int getSkill() { return skill; }
    public void train(int delta) { skill = Math.min(100, skill + delta); }

    public Vehicle getAssignedVehicle() { return assignedVehicle; }
    public void setAssignedVehicle(Vehicle v) { this.assignedVehicle = v; }
    public boolean hasVehicle() { return assignedVehicle != null; }
}

