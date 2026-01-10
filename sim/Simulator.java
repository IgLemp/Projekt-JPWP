package com.transport.sim;

import java.util.*;
import lombok.Getter;

public class Simulator {
    @Getter private Company company;
    private Random rng = new Random();
    @Getter private int turn = 0;
    private List<Job> completedThisTurn = new ArrayList<>();
    private GameSettings settings;

    public Simulator(GameSettings settings) {
        this.settings = settings;
        this.company = new Company(settings.getDifficulty().getStartingCash());
        // Apply multipliers to base logic if needed, e.g., store modifiers in Company
        this.company.setFuelPriceMultiplier(settings.getDifficulty().getFuelCostMultiplier()); 
        
        // Ensure you call init here
        initSampleData(); 
    }

    public List<Job> getCompletedThisTurn() { return new ArrayList<>(completedThisTurn); }

    public void initSampleData() {
        // Prevent duplicate init if called multiple times, though constructor handles it now
        if(!company.getVehicles().isEmpty()) return;

        company.addVehicle(new Vehicle("Truck A", 8000, 100, 0.12));
        company.addVehicle(new Vehicle("Truck B", 10000, 90, 0.14));
        company.addVehicle(new Vehicle("Van C", 4000, 80, 0.08));

        company.addDriver(new Driver("Jan Kowalski", 50));
        company.addDriver(new Driver("Anna Nowak", 70));

        company.addJob(new Job("Local delivery", 300, 30));
        company.addJob(new Job("Regional haul", 1200, 200));

        company.refreshCandidatePool();
        company.refreshVehicleMarket();
    }

    public String nextTurn() {
        turn++;
        completedThisTurn.clear();
        StringBuilder log = new StringBuilder();

        try {
            // 1. Fuel price fluctuation
            double fuelChange = (rng.nextDouble() - 0.5) * 0.2; // +/-10%
            double newFuelPrice = Math.max(0.5, company.getFuelPrice() * (1 + fuelChange)); // Cap minimum price
            company.setFuelPrice(newFuelPrice);
            log.append(String.format("Tura %d: Cena paliwa %.2f/u (%.1f%%)\n", turn, newFuelPrice, fuelChange*100));

            // 2. Markets
            company.refreshCandidatePool();
            company.refreshVehicleMarket();
            company.refreshJobMarket();

            // 3. Maintenance (Only if affordable)
            for (Vehicle v : company.getVehicles()) {
                if (v.isMaintenanceDue()) {
                    double cost = v.getMaintenanceCost();
                    if (company.getCash() >= cost) {
                        company.addCash(-cost);
                        v.performMaintenance();
                        log.append(String.format("Serwis: %s naprawiony za %.2f\n", v.getName(), cost));
                    } else {
                        v.damage(5); // Penalty for skipping maintenance
                        log.append(String.format("Ostrzeżenie: Brak środków na serwis %s. Stan pogorszony.\n", v.getName()));
                    }
                }
            }

            // 4. Process active jobs
            List<Job> toFinalize = new ArrayList<>();
            for (Job j : new ArrayList<>(company.getJobs())) {
                if (j.isAssigned() && !j.isCompleted()) {
                    Driver d = j.getAssignedDriver();
                    Vehicle v = j.getAssignedVehicle();

                    // CRITICAL FIX: Handle broken links (e.g., vehicle sold or unassigned while job active)
                    if (d == null || v == null || d.getAssignedVehicle() != v || !company.getVehicles().contains(v)) {
                        j.setCompleted(true);
                        j.setResult("Anulowano: Błąd przypisania (pojazd/kierowca zmieniony)");
                        log.append(String.format("BŁĄD: Zlecenie '%s' anulowane - niespójne dane.\n", j.getTitle()));
                        toFinalize.add(j);
                        continue;
                    }

                    j.decrementTurnsRemaining();

                    if (j.getTurnsRemaining() <= 0) {
                        // Job finished
                        processJobCompletion(j, d, v, log);
                        toFinalize.add(j);
                    }
                }
            }

            for (Job j : toFinalize) {
                completedThisTurn.add(j);
                company.getJobs().remove(j);
            }

            // 5. Random Events
            processRandomEvents(log);

            company.recordTurn(turn);

        } catch (Exception e) {
            e.printStackTrace();
            log.append("CRITICAL ERROR in simulation: ").append(e.getMessage());
        }

        return log.toString();
    }

    private void processJobCompletion(Job j, Driver d, Vehicle v, StringBuilder log) {
        double skillFactor = d.getSkill() / 100.0;
        double conditionFactor = v.getCondition() / 100.0;
        double routeDifficulty = 1.0 + j.getRouteLength()/300.0;

        // Formula tweak: Ensure minimal success chance
        double successProb = 0.9 * (0.6*skillFactor + 0.4*conditionFactor) / routeDifficulty;
        successProb = Math.min(0.99, Math.max(0.10, successProb));

        boolean success = rng.nextDouble() < successProb;
        double fuelCost = j.getRouteLength() * v.getFuelConsumptionPerKm() * company.getFuelPrice();

        if (success) {
            double efficiency = (0.5*skillFactor + 0.5*conditionFactor);
            double profit = j.getReward() * efficiency - fuelCost;
            company.addCash(profit);
            
            d.train(1 + rng.nextInt(3)); // Small skill gain
            v.addKm(j.getRouteLength()); // Add mileage
            
            j.setCompleted(true);
            j.setResult(String.format("Sukces (Zysk: %.2f)", profit));
            log.append(String.format("Zakończono: '%s' - SUKCES. Zysk: %.2f. (Paliwo: %.2f)\n", j.getTitle(), profit, fuelCost));
        } else {
            double penalty = 100.0;
            double loss = -(fuelCost * 0.5 + penalty); // Lose fuel sunk cost + penalty
            company.addCash(loss);
            
            v.damage(10 + rng.nextInt(20));
            
            j.setCompleted(true);
            j.setResult(String.format("Awaria/Opóźnienie (Strata: %.2f)", loss));
            log.append(String.format("Zakończono: '%s' - PORAŻKA. Strata: %.2f.\n", j.getTitle(), loss));
        }
    }

    private void processRandomEvents(StringBuilder log) {
        if (rng.nextDouble() < 0.05 && !company.getVehicles().isEmpty()) {
            Vehicle v = company.getRandomVehicle();
            if (v != null) {
                v.damage(15);
                double repairCost = 200 + rng.nextInt(300);
                if (company.getCash() >= repairCost) {
                    company.addCash(-repairCost);
                    log.append(String.format("Wydarzenie: Awaria %s w trasie. Szybka naprawa: %.2f\n", v.getName(), repairCost));
                } else {
                    log.append(String.format("Wydarzenie: Awaria %s. Brak środków na naprawę, stan drastycznie spada.\n", v.getName()));
                    v.damage(20);
                }
            }
        }
    }
}
