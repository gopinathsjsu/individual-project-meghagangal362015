package com.example.logparser;

import com.example.logparser.aggregator.ApmAggregator;
import com.example.logparser.aggregator.ApplicationAggregator;
import com.example.logparser.aggregator.RequestAggregator;
import com.example.logparser.handler.ApmLogHandler;
import com.example.logparser.handler.ApplicationLogHandler;
import com.example.logparser.handler.LogHandler;
import com.example.logparser.handler.RequestLogHandler;
import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HandlerChainTest {
    @Test
    void routesToCorrectAggregators() {
        LogHandler apm = new ApmLogHandler();
        LogHandler app = new ApplicationLogHandler();
        LogHandler request = new RequestLogHandler();
        apm.setNext(app);
        app.setNext(request);

        LogProcessingContext context = new LogProcessingContext(
                new ApmAggregator(),
                new ApplicationAggregator(),
                new RequestAggregator()
        );

        apm.handle(new ParsedLogLine(Map.of("metric", "cpu", "value", "10")), context);
        apm.handle(new ParsedLogLine(Map.of("level", "warn", "message", "disk high")), context);
        apm.handle(new ParsedLogLine(Map.of(
                "request_method", "GET",
                "request_url", "/health",
                "response_status", "200",
                "response_time_ms", "15"
        )), context);

        Map<String, Object> apmOutput = context.apmAggregator().toOutput();
        Map<String, Object> appOutput = context.applicationAggregator().toOutput();
        Map<String, Object> requestOutput = context.requestAggregator().toOutput();

        assertEquals(1, apmOutput.size());
        assertEquals(1, appOutput.size());
        assertEquals(1, requestOutput.size());
    }

    @Test
    void unknownParsedLineIsIgnoredWhenNoHandlerMatches() {
        LogHandler apm = new ApmLogHandler();
        LogHandler app = new ApplicationLogHandler();
        LogHandler request = new RequestLogHandler();
        apm.setNext(app);
        app.setNext(request);

        LogProcessingContext context = new LogProcessingContext(
                new ApmAggregator(),
                new ApplicationAggregator(),
                new RequestAggregator()
        );

        apm.handle(new ParsedLogLine(Map.of("foo", "bar", "baz", "qux")), context);

        assertEquals(0, context.apmAggregator().toOutput().size());
        assertEquals(0, context.applicationAggregator().toOutput().size());
        assertEquals(0, context.requestAggregator().toOutput().size());
    }
}
