package com.example.logparser.aggregator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApmAggregator {
    private final Map<String, List<Double>> valuesByMetric = new HashMap<>();

    public void add(String metric, double value) {
        valuesByMetric.computeIfAbsent(metric, ignored -> new ArrayList<>()).add(value);
    }

    public Map<String, Object> toOutput() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : valuesByMetric.entrySet()) {
            List<Double> values = new ArrayList<>(entry.getValue());
            values.sort(Comparator.naturalOrder());

            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("min", values.get(0));
            stats.put("median", median(values));
            stats.put("average", values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0));
            stats.put("max", values.get(values.size() - 1));
            metrics.put(entry.getKey(), stats);
        }
        return metrics;
    }

    private double median(List<Double> sortedValues) {
        int size = sortedValues.size();
        if (size % 2 == 1) {
            return sortedValues.get(size / 2);
        }
        return (sortedValues.get(size / 2 - 1) + sortedValues.get(size / 2)) / 2.0;
    }
}
