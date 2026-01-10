package com.transport.sim;

import java.util.*;
import lombok.Getter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DriverCandidate {
    @Getter private String name;
    @Getter private int skill;
    @Getter private double hireCost;
    @Getter private double salary; // Added salary field

    private static final String[] FIRST = {"Adam","Piotr","Marek","Krzysztof","Paweł","Jakub","Łukasz","Tomasz","Mateusz","Szymon"};
    private static final String[] LAST = {"Nowak","Kowalski","Wiśniewski","Wójcik","Kowalczyk","Kamiński","Lewandowski","Zieliński"};
    private static final Random rng = new Random();

    public static DriverCandidate randomCandidate() {
        String name = FIRST[rng.nextInt(FIRST.length)] + " " + LAST[rng.nextInt(LAST.length)];
        int skill = 30 + rng.nextInt(61); 
        double cost = 150 + rng.nextInt(301); 
        // Salary calculation based on skill
        double salary = 400 + (skill * 5.0); 
        return new DriverCandidate(name, skill, cost, salary); // Correctly passing 4 arguments
    }
}
