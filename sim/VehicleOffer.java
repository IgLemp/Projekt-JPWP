package com.transport.sim;

import java.util.*;

public class VehicleOffer {
    private String name;
    private int condition;
    private double price;
    private double fuelConsumption;

    private static final String[] MODELS = {"Truck X","Truck Y","Van Z","Lorry 2000","Mover 7"};
    private static final Random rng = new Random();

    public VehicleOffer(String name, int condition, double price, double fuelConsumption) {
        this.name = name; this.condition = condition; this.price = price; this.fuelConsumption = fuelConsumption;
    }

    public String getName() { return name; }
    public int getCondition() { return condition; }
    public double getPrice() { return price; }
    public double getFuelConsumption() { return fuelConsumption; }

    public static VehicleOffer randomOffer() {
        String n = MODELS[rng.nextInt(MODELS.length)] + "-" + (100 + rng.nextInt(900));
        int cond = 40 + rng.nextInt(61); // 40-100
        double fc = 0.08 + rng.nextDouble() * 0.12; // 0.08 - 0.20
        double p = 2000 + rng.nextInt(9001); // 2000 - 11000
        p = p * (0.6 + cond/200.0); // scale by condition
        return new VehicleOffer(n, cond, Math.round(p), Math.round(fc*100.0)/100.0);
    }
}

