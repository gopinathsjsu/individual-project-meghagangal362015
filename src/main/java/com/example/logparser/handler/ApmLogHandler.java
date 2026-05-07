package com.example.logparser.handler;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;

import java.util.Map;

public class ApmLogHandler extends AbstractLogHandler {
    @Override
    public void handle(ParsedLogLine line, LogProcessingContext context) {
        Map<String, String> fields = line.fields();
        if (!fields.containsKey("metric") || !fields.containsKey("value")) {
            delegate(line, context);
            return;
        }
        if (!isDouble(fields.get("value"))) {
            delegate(line, context);
            return;
        }
        context.apmAggregator().add(fields.get("metric"), Double.parseDouble(fields.get("value")));
    }

    private boolean isDouble(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
