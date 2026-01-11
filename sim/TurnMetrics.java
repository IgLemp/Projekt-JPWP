package com.transport.sim;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TurnMetrics {
    private final int turn;
    private final double cashBalance;
    private final double fuelCostPerKm;
}
