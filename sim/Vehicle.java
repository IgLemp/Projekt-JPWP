package com.transport.sim;

public class Vehicle {
    private String name;
    private double value; // purchase price
    private int condition; // 0-100
    private int maintenanceIntervalKm; // km between maintenance
    private int kmSinceMaintenance = 0;
    private double fuelConsumptionPerKm; // units per km

    public Vehicle(String name, double value, int condition, double fuelConsumptionPerKm) {
        this.name = name; this.value = value; this.condition = condition; this.fuelConsumptionPerKm = fuelConsumptionPerKm;
        this.maintenanceIntervalKm = 500; // default
    }
    public String getName() { return name; }
    public double getValue() { return value; }
    public int getCondition() { return condition; }
    public void repair(int amount) { condition = Math.min(100, condition + amount); }
    public void damage(int amount) { condition = Math.max(0, condition - amount); }
    public int getKmSinceMaintenance() { return kmSinceMaintenance; }
    public void addKm(int km) { kmSinceMaintenance += km; }
    public boolean isMaintenanceDue() { return kmSinceMaintenance >= maintenanceIntervalKm || condition < 50; }
    public double getMaintenanceCost() { return 200 + (100 * (100 - condition)/100.0); }
    public void performMaintenance() { kmSinceMaintenance = 0; repair(20); }
    public double getFuelConsumptionPerKm() { return fuelConsumptionPerKm; }
}

