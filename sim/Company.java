package com.transport.sim;

import java.util.*;
import lombok.Getter;
import lombok.Setter;

public class Company {
    @Getter private double cash;
    @Getter @Setter private double fuelPrice = 1.8; // per unit
    private List<Vehicle> vehicles = new ArrayList<>();
    @Getter private List<Driver> drivers = new ArrayList<>();
    @Getter private List<Job> jobs = new ArrayList<>();
    private Map<Integer, Double> history = new HashMap<>();
    private Random rng = new Random();
    @Getter @Setter private double fuelPriceMultiplier;

    // candidate pool for hires
    @Getter private List<DriverCandidate> candidates = new ArrayList<>();
    // vehicle market
    @Getter private List<VehicleOffer> vehicleMarket = new ArrayList<>();

    public Company(double initialCash) { this.cash = initialCash; }
    public void addCash(double delta) { cash += delta; }

    public void addVehicle(Vehicle v) { vehicles.add(v); }
    public void removeVehicle(Vehicle v) { vehicles.remove(v); }
    public List<Vehicle> getVehicles() { return vehicles; }
    
    public Vehicle getRandomVehicle() { 
        if (vehicles.isEmpty()) return null; 
        return vehicles.get(rng.nextInt(vehicles.size())); 
    }

    public void addDriver(Driver d) { drivers.add(d); }
    public void removeDriver(Driver d) { drivers.remove(d); }
    public Driver getRandomDriver() { if (drivers.isEmpty()) return null; return drivers.get(rng.nextInt(drivers.size())); }

    public void addJob(Job j) { jobs.add(j); }

    public void recordTurn(int turn) { history.put(turn, cash); }
    public Map<Integer, Double> getHistory() { return history; }

    // --- LOGIC HELPERS ---
    
    public boolean isDriverBusy(Driver d) {
        return jobs.stream().anyMatch(j -> j.isAssigned() && !j.isCompleted() && j.getAssignedDriver() == d);
    }

    public boolean isVehicleBusy(Vehicle v) {
        return jobs.stream().anyMatch(j -> j.isAssigned() && !j.isCompleted() && j.getAssignedVehicle() == v);
    }

    public Driver getDriverForVehicle(Vehicle v) {
        return drivers.stream().filter(d -> d.getAssignedVehicle() == v).findFirst().orElse(null);
    }

    // ---------------------

    // purchase vehicle if enough cash
    public boolean purchaseVehicle(Vehicle v) {
        if (cash >= v.getValue()) {
            addCash(-v.getValue());
            addVehicle(v);
            return true;
        }
        return false;
    }

    public double sellVehicle(Vehicle v) {
        if (!vehicles.contains(v)) return 0.0;
        double price = v.getValue() * (0.4 + 0.6 * (v.getCondition()/100.0));
        removeVehicle(v);
        // Also remove assignment from driver if exists
        Driver d = getDriverForVehicle(v);
        if(d != null) d.setAssignedVehicle(null);
        
        addCash(price);
        return price;
    }

    // Candidate pool logic
    public void refreshCandidatePool() {
        candidates.clear();
        int count = 3 + rng.nextInt(3); // 3-5 candidates
        for (int i = 0; i < count; i++) {
            candidates.add(DriverCandidate.randomCandidate());
        }
    }

    public boolean hireCandidate(DriverCandidate c) {
        if (cash >= c.getHireCost()) {
            addDriver(new Driver(c.getName(), c.getSkill()));
            addCash(-c.getHireCost());
            candidates.remove(c);
            return true;
        }
        return false;
    }

    // Vehicle market
    public void refreshVehicleMarket() {
        vehicleMarket.clear();
        int count = 3 + rng.nextInt(3); // 3-5 offers
        for (int i = 0; i < count; i++) vehicleMarket.add(VehicleOffer.randomOffer());
    }

    public boolean buyOffer(VehicleOffer offer) {
        if (cash >= offer.getPrice()) {
            addCash(-offer.getPrice());
            addVehicle(new Vehicle(offer.getName(), offer.getPrice(), offer.getCondition(), offer.getFuelConsumption()));
            vehicleMarket.remove(offer);
            return true;
        }
        return false;
    }

    // Job market
    public void refreshJobMarket() {
        // Keep unassigned jobs? Or flush? 
        // Logic choice: flush unassigned, keep assigned
        List<Job> active = new ArrayList<>();
        for(Job j : jobs) if(j.isAssigned() && !j.isCompleted()) active.add(j);
        jobs = active;
        
        int count = 3 + rng.nextInt(3); 
        for (int i = 0; i < count; i++) {
            jobs.add(Job.randomJob());
        }
    }
}
