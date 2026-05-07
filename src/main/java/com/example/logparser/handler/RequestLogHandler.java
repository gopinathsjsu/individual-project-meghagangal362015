package com.example.logparser.handler;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;

import java.util.Map;

public class RequestLogHandler extends AbstractLogHandler {
    @Override
    public void handle(ParsedLogLine line, LogProcessingContext context) {
        Map<String, String> fields = line.fields();
        boolean canHandle = fields.containsKey("request_method")
                && fields.containsKey("request_url")
                && fields.containsKey("response_status")
                && fields.containsKey("response_time_ms")
                && isInteger(fields.get("response_status"))
                && isDouble(fields.get("response_time_ms"));
        if (!canHandle) {
            delegate(line, context);
            return;
        }
        context.requestAggregator().add(
                fields.get("request_method"),
                fields.get("request_url"),
                Integer.parseInt(fields.get("response_status")),
                Double.parseDouble(fields.get("response_time_ms"))
        );
    }

    private boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
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
