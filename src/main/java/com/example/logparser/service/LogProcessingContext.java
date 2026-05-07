package com.example.logparser.service;

import com.example.logparser.aggregator.ApmAggregator;
import com.example.logparser.aggregator.ApplicationAggregator;
import com.example.logparser.aggregator.RequestAggregator;

public record LogProcessingContext(
        ApmAggregator apmAggregator,
        ApplicationAggregator applicationAggregator,
        RequestAggregator requestAggregator
) {
}
