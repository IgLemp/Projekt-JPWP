package com.transport.score;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ScoreRecord {
    String playerName;
    double finalScore;
    String timestamp;
    double totalCash;
    double reputation;
    int vehiclesOwned;
    int turnsReached;
}
