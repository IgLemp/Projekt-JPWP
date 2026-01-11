package com.transport.score;

import com.transport.sim.Simulator;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScoreService {

    public double calculateFinalScore(Simulator sim) {
        double cash = sim.getCompany().getCash();
        double reputation = sim.getCompany().getReputation();
        
        double assetValue = sim.getCompany().getVehicles().stream()
                .mapToDouble(v -> v.getValue() * (v.getCondition() / 100.0))
                .sum();

        // Formuła: Gotówka + (75% wartości floty) + (Punkty reputacji * 150)
        return Math.max(0, cash + (assetValue * 0.75) + (reputation * 150));
    }

    public void saveScore(ScoreRecord record, File file) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("{");
            out.printf("  \"playerName\": \"%s\",%n", record.getPlayerName());
            out.printf("  \"finalScore\": %.2f,%n", record.getFinalScore());
            out.printf("  \"timestamp\": \"%s\",%n", record.getTimestamp());
            out.printf("  \"totalCash\": %.2f,%n", record.getTotalCash());
            out.printf("  \"reputation\": %.2f,%n", record.getReputation());
            out.printf("  \"vehiclesOwned\": %d,%n", record.getVehiclesOwned());
            out.printf("  \"turnsReached\": %d%n", record.getTurnsReached());
            out.println("}");
        }
    }

    public List<ScoreRecord> loadScores(File directory) {
        List<ScoreRecord> scores = new ArrayList<>();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".score"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    scores.add(parseManualJson(file));
                } catch (Exception ignored) {}
            }
        }
        scores.sort(Comparator.comparingDouble(ScoreRecord::getFinalScore).reversed());
        return scores.size() > 10 ? scores.subList(0, 10) : scores;
    }

    private ScoreRecord parseManualJson(File file) throws Exception {
        String content = new Scanner(file).useDelimiter("\\Z").next();
        return ScoreRecord.builder()
                .playerName(extract(content, "playerName"))
                .finalScore(Double.parseDouble(extract(content, "finalScore")))
                .timestamp(extract(content, "timestamp"))
                .totalCash(Double.parseDouble(extract(content, "totalCash")))
                .reputation(Double.parseDouble(extract(content, "reputation")))
                .vehiclesOwned(Integer.parseInt(extract(content, "vehiclesOwned")))
                .turnsReached(Integer.parseInt(extract(content, "turnsReached")))
                .build();
    }

    private String extract(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\":\\s*\"?([^,\"}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1).trim() : "";
    }
}
