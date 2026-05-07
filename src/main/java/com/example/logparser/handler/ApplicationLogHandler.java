package com.example.logparser.handler;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;

import java.util.Map;

public class ApplicationLogHandler extends AbstractLogHandler {
    @Override
    public void handle(ParsedLogLine line, LogProcessingContext context) {
        Map<String, String> fields = line.fields();
        if (!fields.containsKey("level") || !fields.containsKey("message")) {
            delegate(line, context);
            return;
        }
        context.applicationAggregator().add(line.fields().get("level"));
    }
}
