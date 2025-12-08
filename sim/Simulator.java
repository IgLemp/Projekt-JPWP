package com.transport.sim;

import java.util.*;

import lombok.Getter;
import lombok.Setter;

public class Simulator {
    @Getter private Company company;
    private Random rng = new Random();
    @Getter private int turn = 0;
    private List<Job> completedThisTurn = new ArrayList<>();

    public Simulator() {
        this.company = new Company(15000.0); // start budget
    }

    public List<Job> getCompletedThisTurn() { return new ArrayList<>(completedThisTurn); }

    public void initSampleData() {
        company.addVehicle(new Vehicle("Truck A", 8000, 100, 0.12));
        company.addVehicle(new Vehicle("Truck B", 10000, 90, 0.14));
        company.addVehicle(new Vehicle("Van C", 4000, 80, 0.08));

        company.addDriver(new Driver("Jan Kowalski", 50));
        company.addDriver(new Driver("Anna Nowak", 70));

        company.addJob(new Job("Local delivery", 300, 30));
        company.addJob(new Job("Regional haul", 1200, 200));
        company.addJob(new Job("Express parcel", 200, 20));

        company.refreshCandidatePool();
        company.refreshVehicleMarket();
    }

    // deterministic ETA: ceil(routeLength/100)
    // Only assigned driver+vehicle pairs work
    public String nextTurn() {
        turn++;
        completedThisTurn.clear();
        StringBuilder log = new StringBuilder();

        // Fuel price fluctuation
        double fuelChange = (rng.nextDouble() - 0.5) * 0.2; // +/-10%
        company.setFuelPrice(company.getFuelPrice() * (1 + fuelChange));
        log.append(String.format("Tura %d: zmiana ceny paliwa %.1f%%\n", turn, fuelChange*100));

        // Refresh candidate pool and vehicle market each turn
        company.refreshCandidatePool();
        company.refreshVehicleMarket();
        company.refreshJobMarket();
        log.append("Odświeżono pulę kandydatów i rynek pojazdów.\n");

        // Scheduled maintenance
        for (Vehicle v : company.getVehicles()) {
            if (v.isMaintenanceDue()) {
                double cost = v.getMaintenanceCost();
                company.addCash(-cost);
                v.performMaintenance();
                log.append(String.format("Konserwacja: %s serwisowany za %.2f\n", v.getName(), cost));
            }
        }

        // Process active jobs: decrement ETA and finalize finished jobs
        List<Job> toFinalize = new ArrayList<>();
        for (Job j : new ArrayList<>(company.getJobs())) {
            if (j.isAssigned() && !j.isCompleted()) {
                // ensure driver has assigned vehicle and it's the same
                Driver d = j.getAssignedDriver();
                Vehicle v = j.getAssignedVehicle();
                if (d == null || v == null || d.getAssignedVehicle() != v) {
                    // invalid assignment, skip
                    continue;
                }
                // decrement remaining turns
                j.decrementTurnsRemaining();
                if (j.getTurnsRemaining() <= 0) {
                    // compute success
                    double skillFactor = d.getSkill() / 100.0;
                    double conditionFactor = v.getCondition() / 100.0;
                    double routeDifficulty = 1.0 + j.getRouteLength()/300.0;
                    double baseSuccess = 0.9;
                    double successProb = baseSuccess * (0.6*skillFactor + 0.4*conditionFactor) / routeDifficulty;
                    successProb = Math.min(1.0, Math.max(0.05, successProb));
                    boolean success = rng.nextDouble() < successProb;
                    double fuelCost = j.getRouteLength() * v.getFuelConsumptionPerKm() * company.getFuelPrice();
                    if (success) {
                        double efficiency = (0.5*skillFactor + 0.5*conditionFactor);
                        double profit = j.getReward() * efficiency - fuelCost;
                        company.addCash(profit);
                        j.setCompleted(true);
                        j.setResult(String.format("Sukces (zysk %.2f)", profit));
                        log.append(String.format("Zakończono: '%s' - sukces, zysk %.2f\n", j.getTitle(), profit));
                    } else {
                        double loss = - (fuelCost * 0.5 + 100);
                        company.addCash(loss);
                        v.damage(10 + rng.nextInt(20));
                        j.setCompleted(true);
                        j.setResult(String.format("Niepowodzenie (strata %.2f)", loss));
                        log.append(String.format("Zakończono: '%s' - niepowodzenie, strata %.2f\n", j.getTitle(), loss));
                    }
                    toFinalize.add(j);
                }
            }
        }

        // move completed jobs to completedThisTurn and remove from active list
        for (Job j : toFinalize) {
            completedThisTurn.add(j);
            company.getJobs().remove(j);
        }

        // Random vehicle breakdown unrelated to jobs
        if (rng.nextDouble() < 0.08) {
            Vehicle v = company.getRandomVehicle();
            if (v != null) {
                v.damage(20);
                double repairCost = 300 + rng.nextInt(500);
                company.addCash(-repairCost);
                log.append(String.format("Wydarzenie losowe: %s awaria. Koszt naprawy %.2f. Stan %d\n", v.getName(), repairCost, v.getCondition()));
            }
        }

        company.recordTurn(turn);
        return log.toString();
    }
}
