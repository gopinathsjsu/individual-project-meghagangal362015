package com.example.logparser.aggregator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RequestAggregator {
    private final Map<String, RouteStats> routes = new HashMap<>();

    public void add(String requestMethod, String requestUrl, int responseStatus, double responseTimeMs) {
        String route = requestUrl;
        routes.computeIfAbsent(route, ignored -> new RouteStats())
                .add(responseStatus, responseTimeMs);
    }

    public Map<String, Object> toOutput() {
        Map<String, Object> outputRoutes = new LinkedHashMap<>();
        for (Map.Entry<String, RouteStats> entry : routes.entrySet()) {
            outputRoutes.put(entry.getKey(), entry.getValue().toOutput());
        }
        return outputRoutes;
    }

    private static class RouteStats {
        private final List<Double> responseTimes = new ArrayList<>();
        private int count2xx;
        private int count4xx;
        private int count5xx;

        void add(int responseStatus, double responseTimeMs) {
            responseTimes.add(responseTimeMs);
            if (responseStatus >= 200 && responseStatus < 300) {
                count2xx++;
            } else if (responseStatus >= 400 && responseStatus < 500) {
                count4xx++;
            } else if (responseStatus >= 500 && responseStatus < 600) {
                count5xx++;
            }
        }

        Map<String, Object> toOutput() {
            List<Double> sorted = new ArrayList<>(responseTimes);
            sorted.sort(Comparator.naturalOrder());
            Map<String, Object> responseTimesOutput = new LinkedHashMap<>();
            responseTimesOutput.put("min", sorted.get(0));
            responseTimesOutput.put("95_percentile", percentile95(sorted));
            responseTimesOutput.put("max", sorted.get(sorted.size() - 1));

            Map<String, Object> statusCodesOutput = new LinkedHashMap<>();
            statusCodesOutput.put("2XX", count2xx);
            statusCodesOutput.put("4XX", count4xx);
            statusCodesOutput.put("5XX", count5xx);

            Map<String, Object> routeOutput = new LinkedHashMap<>();
            routeOutput.put("response_times", responseTimesOutput);
            routeOutput.put("status_codes", statusCodesOutput);
            return routeOutput;
        }

        private double percentile95(List<Double> sorted) {
            int n = sorted.size();
            int index = (int) Math.ceil(0.95 * n) - 1;
            index = Math.max(0, Math.min(index, n - 1));
            return sorted.get(index);
        }
    }
}
