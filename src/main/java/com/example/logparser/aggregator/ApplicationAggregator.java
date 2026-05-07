package com.example.logparser.aggregator;

import java.util.Map;
import java.util.TreeMap;

public class ApplicationAggregator {
    private final Map<String, Integer> severityCounts = new TreeMap<>();

    public void add(String level) {
        String normalized = level.toUpperCase();
        severityCounts.merge(normalized, 1, Integer::sum);
    }

    public Map<String, Object> toOutput() {
        return Map.copyOf(severityCounts);
    }
}
