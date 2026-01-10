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
        // Apply multipliers to base logic
        this.company.setFuelPriceMultiplier(settings.getDifficulty().getFuelCostMultiplier()); 
        
        initSampleData(); 
    }

    public List<Job> getCompletedThisTurn() { return new ArrayList<>(completedThisTurn); }

    public void initSampleData() {
        if(!company.getVehicles().isEmpty()) return;

        // Using procedural name generation for starting fleet
        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 8000, 100, 0.12));
        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 10000, 90, 0.14));
        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 4000, 80, 0.08));

        // Initial drivers with randomized skills/salaries
        company.addDriver(new Driver("Jan Kowalski", 50, 475.0));
        company.addDriver(new Driver("Anna Nowak", 75, 610.0));

        company.addJob(Job.randomJob());
        company.addJob(Job.randomJob());

        company.refreshCandidatePool();
        company.refreshVehicleMarket();
    }

    public String nextTurn() {
        turn++;
        completedThisTurn.clear();
        StringBuilder log = new StringBuilder();

        try {
            log.append(String.format("--- TURA %d ---\n", turn));

            // 1. Economy: Salary Deductions
            // Every turn, the company must pay its workforce
            double totalSalaries = company.getDrivers().stream()
                    .mapToDouble(Driver::getSalary)
                    .sum();
            company.addCash(-totalSalaries);
            log.append(String.format("Finanse: Wypłacono pensje pracownikom: %.2f\n", totalSalaries));

            // 2. Fuel price fluctuation
            double fuelChange = (rng.nextDouble() - 0.5) * 0.2; // +/-10%
            double newFuelPrice = Math.max(0.5, company.getFuelPrice() * (1 + fuelChange));
            company.setFuelPrice(newFuelPrice);
            log.append(String.format("Rynek: Cena paliwa wynosi %.2f/u\n", newFuelPrice));

            // 3. Market Refresh
            company.refreshCandidatePool();
            company.refreshVehicleMarket();
            company.refreshJobMarket();

            // 4. Maintenance Logic
            for (Vehicle v : company.getVehicles()) {
                if (v.isMaintenanceDue()) {
                    double cost = v.getMaintenanceCost() * company.getFuelPriceMultiplier();
                    if (company.getCash() >= cost) {
                        company.addCash(-cost);
                        v.performMaintenance();
                        log.append(String.format("Serwis: %s przeszedł przegląd (Koszt: %.2f)\n", v.getName(), cost));
                    } else {
                        v.damage(8); // Increased penalty for neglected upkeep
                        log.append(String.format("ALARM: Brak środków na serwis %s! Stan techniczny spada.\n", v.getName()));
                    }
                }
            }

            // 5. Process active jobs with Skill and Assignment Logic
            List<Job> toFinalize = new ArrayList<>();
            for (Job j : new ArrayList<>(company.getJobs())) {
                if (j.isAssigned() && !j.isCompleted()) {
                    Driver d = j.getAssignedDriver();
                    Vehicle v = j.getAssignedVehicle();

                    // Validation check for resource consistency
                    if (d == null || v == null || !company.getVehicles().contains(v)) {
                        j.setCompleted(true);
                        j.setResult("Anulowano: Zasób niedostępny");
                        log.append(String.format("Błąd: Przerwano zlecenie '%s' (Brak pojazdu/kierowcy).\n", j.getTitle()));
                        toFinalize.add(j);
                        continue;
                    }

                    j.decrementTurnsRemaining();

                    if (j.getTurnsRemaining() <= 0) {
                        processJobCompletion(j, d, v, log);
                        toFinalize.add(j);
                    }
                }
            }

            for (Job j : toFinalize) {
                completedThisTurn.add(j);
                company.getJobs().remove(j);
            }

            // 6. Random Events
            processRandomEvents(log);

            company.recordTurn(turn);

        } catch (Exception e) {
            log.append("BŁĄD KRYTYCZNY symulacji: ").append(e.getMessage());
        }

        return log.toString();
    }

    private void processJobCompletion(Job j, Driver d, Vehicle v, StringBuilder log) {
        // Efficiency calculation: Skill (1-10) and Condition affect success
        double skillFactor = (d.getSkill() / 10.0) / 10.0; // Normalize 1-10 scale to 0.1-1.0
        double conditionFactor = v.getCondition() / 100.0;
        
        // Base success chance influenced by skill and vehicle health
        double successProb = 0.95 * (0.7 * skillFactor + 0.3 * conditionFactor);
        
        // Penalty for high-difficulty routes if skill is near the minimum requirement
        if ((d.getSkill() / 10) < j.getMinSkillRequired() + 2) {
            successProb -= 0.15; 
        }

        boolean success = rng.nextDouble() < Math.max(0.05, successProb);
        double fuelCost = j.getRouteLength() * v.getFuelConsumptionPerKm() * company.getFuelPrice();

        if (success) {
            // Skill-based revenue bonus
            double efficiencyBonus = 1.0 + (skillFactor * 0.2); 
            double profit = (j.getReward() * efficiencyBonus) - fuelCost;
            
            company.addCash(profit);
            // Reputation gain for successful premium assignments
            company.addReputation(j.getReputationGain()); 
            
            d.train(2); // Experience gain
            v.addKm(j.getRouteLength());
            
            j.setCompleted(true);
            j.setResult(String.format("Sukces (Zysk: %.2f)", profit));
            log.append(String.format("Zlecenie: '%s' ZAKOŃCZONE. Zarobek: %.2f (+%.1f Rep)\n", 
                    j.getTitle(), profit, j.getReputationGain()));
        } else {
            // Failure logic: penalties and reputation loss
            double penalty = j.getReward() * 0.25;
            company.addCash(-penalty);
            company.addReputation(-j.getReputationGain() * 0.5);
            
            v.damage(15 + rng.nextInt(15)); // Damage from "accidents"
            
            j.setCompleted(true);
            j.setResult(String.format("Porażka (Kara: %.2f)", penalty));
            log.append(String.format("Zlecenie: '%s' NIEUDANE. Kara finansowa: %.2f. Reputacja spadła.\n", 
                    j.getTitle(), penalty));
        }
    }

    private void processRandomEvents(StringBuilder log) {
        // 5% chance of a random event
        if (rng.nextDouble() < 0.05 && !company.getVehicles().isEmpty()) {
            Vehicle v = company.getRandomVehicle();
            if (v != null) {
                log.append(String.format("ZDARZENIE: %s uległ drobnej kolizji.\n", v.getName()));
                v.damage(20);
                double suddenRepair = 500;
                if (company.getCash() >= suddenRepair) {
                    company.addCash(-suddenRepair);
                    v.repair(10);
                    log.append("ZDARZENIE: Opłacono ekspresową pomoc drogową (-500.00).\n");
                }
            }
        }
    }
}
