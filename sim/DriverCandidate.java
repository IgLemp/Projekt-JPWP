package com.transport.sim;

import java.util.*;
import lombok.Getter;

public class DriverCandidate {
    @Getter private String name;
    @Getter private int skill;
    @Getter private double hireCost;

    private static final String[] FIRST = {"Adam","Piotr","Marek","Krzysztof","Paweł","Jakub","Łukasz","Tomasz","Mateusz","Szymon"};
    private static final String[] LAST = {"Nowak","Kowalski","Wiśniewski","Wójcik","Kowalczyk","Kamiński","Lewandowski","Zieliński"};
    private static final Random rng = new Random();

    public DriverCandidate(String name, int skill, double hireCost) {
        this.name = name; this.skill = skill; this.hireCost = hireCost;
    }

    public static DriverCandidate randomCandidate() {
        String name = FIRST[rng.nextInt(FIRST.length)] + " " + LAST[rng.nextInt(LAST.length)];
        int skill = 30 + rng.nextInt(61); // 30-90
        double cost = 150 + rng.nextInt(301); // 150-450
        return new DriverCandidate(name, skill, cost);
    }
}

