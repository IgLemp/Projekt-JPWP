package com.transport.sim;

import java.util.*;
import lombok.Getter;

public class Simulator {
    @Getter private Company company;
    private Random rng = new Random();
    @Getter private int turn = 0;
    private List<Job> completedThisTurn = new ArrayList<>();
    @Getter private GameSettings settings; // Exposed getter for UI access
    @Getter private boolean gameOver = false;

    public Simulator(GameSettings settings) {
        this.settings = settings;
        this.company = new Company(settings.getDifficulty().getStartingCash());
        this.company.setFuelPriceMultiplier(settings.getDifficulty().getFuelCostMultiplier()); 
        initSampleData(); 
    }

    public List<Job> getCompletedThisTurn() { return new ArrayList<>(completedThisTurn); }

    public void initSampleData() {
        if(!company.getVehicles().isEmpty()) return;

        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 8000, 100, 0.12));
        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 10000, 90, 0.14));
        company.addVehicle(new Vehicle(Vehicle.generateRandomName(), 4000, 80, 0.08));

        company.addDriver(new Driver("Jan Kowalski", 50, 475.0));
        company.addDriver(new Driver("Anna Nowak", 75, 610.0));

        company.addJob(Job.randomJob());
        company.addJob(Job.randomJob());

        company.refreshCandidatePool();
        company.refreshVehicleMarket();
    }

    /**
     * Advances the simulation by one turn.
     * @return Log string of events.
     */
    public String nextTurn() {
        if (gameOver) return "GRA ZAKOŃCZONA. Zrestartuj aplikację.";

        turn++;
        completedThisTurn.clear();
        StringBuilder log = new StringBuilder();

        try {
            log.append(String.format("--- TURA %d ---\n", turn));

            // 1. Economy: Salary Deductions
            double totalSalaries = company.getDrivers().stream()
                    .mapToDouble(Driver::getSalary)
                    .sum();
            company.addCash(-totalSalaries);
            log.append(String.format("Finanse: Wypłacono pensje pracownikom: %.2f\n", totalSalaries));

            // Check bankruptcy immediately after salaries
            if (checkBankruptcy()) {
                log.append("\n!!! BANKRUCTWO !!!\nFirma utraciła płynność finansową.");
                return log.toString();
            }

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
                        v.damage(8); 
                        log.append(String.format("ALARM: Brak środków na serwis %s! Stan techniczny spada.\n", v.getName()));
                    }
                }
            }

            // 5. Process active jobs
            List<Job> toFinalize = new ArrayList<>();
            for (Job j : new ArrayList<>(company.getJobs())) {
                if (j.isAssigned() && !j.isCompleted()) {
                    Driver d = j.getAssignedDriver();
                    Vehicle v = j.getAssignedVehicle();

                    if (d == null || v == null || !company.getVehicles().contains(v)) {
                        j.setCompleted(true);
                        j.setResult("Anulowano: Zasób niedostępny");
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
            
            // Final bankruptcy check for the turn
            if (checkBankruptcy()) {
                log.append("\n!!! BANKRUCTWO !!!\nStan konta spadł poniżej krytycznego poziomu.");
            }

        } catch (Exception e) {
            log.append("BŁĄD KRYTYCZNY symulacji: ").append(e.getMessage());
            e.printStackTrace();
        }

        return log.toString();
    }

    private boolean checkBankruptcy() {
        double threshold = settings.getDifficulty().getBankruptcyLimit();
        if (company.getCash() < threshold) {
            this.gameOver = true;
            return true;
        }
        return false;
    }

    private void processJobCompletion(Job j, Driver d, Vehicle v, StringBuilder log) {
        double skillFactor = (d.getSkill() / 10.0) / 10.0; 
        double conditionFactor = v.getCondition() / 100.0;
        
        double successProb = 0.95 * (0.7 * skillFactor + 0.3 * conditionFactor);
        
        if ((d.getSkill() / 10) < j.getMinSkillRequired() + 2) {
            successProb -= 0.15; 
        }

        boolean success = rng.nextDouble() < Math.max(0.05, successProb);
        // Revenue logic with Difficulty Multiplier
        double revenueMult = settings.getDifficulty().getRevenueMultiplier();
        double fuelCost = j.getRouteLength() * v.getFuelConsumptionPerKm() * company.getFuelPrice();

        if (success) {
            double efficiencyBonus = 1.0 + (skillFactor * 0.2); 
            double grossRevenue = j.getReward() * efficiencyBonus * revenueMult;
            double profit = grossRevenue - fuelCost;
            
            company.addCash(profit);
            company.addReputation(j.getReputationGain()); 
            
            d.train(2); 
            v.addKm(j.getRouteLength());
            
            j.setCompleted(true);
            j.setResult(String.format("Sukces (Zysk: %.2f)", profit));
            log.append(String.format("Zlecenie: '%s' ZAKOŃCZONE. Zarobek: %.2f (+%.1f Rep)\n", 
                    j.getTitle(), profit, j.getReputationGain()));
        } else {
            double penalty = (j.getReward() * 0.25);
            company.addCash(-penalty);
            company.addReputation(-j.getReputationGain() * 0.5);
            
            v.damage(15 + rng.nextInt(15)); 
            
            j.setCompleted(true);
            j.setResult(String.format("Porażka (Kara: %.2f)", penalty));
            log.append(String.format("Zlecenie: '%s' NIEUDANE. Kara finansowa: %.2f. Reputacja spadła.\n", 
                    j.getTitle(), penalty));
        }
    }

    private void processRandomEvents(StringBuilder log) {
        if (rng.nextDouble() < 0.05 && !company.getVehicles().isEmpty()) {
            Vehicle v = company.getRandomVehicle();
            if (v != null) {
                log.append(String.format("ZDARZENIE: %s uległ drobnej kolizji.\n", v.getName()));
                v.damage(20);
                double suddenRepair = 500 * settings.getDifficulty().getMaintenanceCostMultiplier();
                if (company.getCash() >= suddenRepair) {
                    company.addCash(-suddenRepair);
                    v.repair(10);
                    log.append(String.format("ZDARZENIE: Opłacono ekspresową pomoc drogową (-%.2f).\n", suddenRepair));
                }
            }
        }
    }
}
