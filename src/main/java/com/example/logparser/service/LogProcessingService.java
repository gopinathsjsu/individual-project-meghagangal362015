package com.example.logparser.service;

import com.example.logparser.handler.LogHandler;
import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.parser.LogLineParser;
import com.example.logparser.writer.JsonOutputWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class LogProcessingService {
    private final LogLineParser parser;
    private final LogHandler rootHandler;
    private final JsonOutputWriter jsonOutputWriter;

    @Autowired
    public LogProcessingService(LogLineParser parser, JsonOutputWriter jsonOutputWriter) {
        this.parser = parser;
        this.rootHandler = null;
        this.jsonOutputWriter = jsonOutputWriter;
    }

    public LogProcessingService(LogLineParser parser, LogHandler rootHandler, JsonOutputWriter jsonOutputWriter) {
        this.parser = parser;
        this.rootHandler = rootHandler;
        this.jsonOutputWriter = jsonOutputWriter;
    }

    public void process(Path inputFile, LogHandler rootHandler) throws IOException {
        LogProcessingContext context = new LogProcessingContext(
                new com.example.logparser.aggregator.ApmAggregator(),
                new com.example.logparser.aggregator.ApplicationAggregator(),
                new com.example.logparser.aggregator.RequestAggregator()
        );

        List<String> lines = Files.readAllLines(inputFile);
        for (String line : lines) {
            Optional<ParsedLogLine> parsed = parser.parse(line);
            parsed.ifPresent(parsedLogLine -> rootHandler.handle(parsedLogLine, context));
        }

        jsonOutputWriter.write("apm.json", context.apmAggregator().toOutput());
        jsonOutputWriter.write("application.json", context.applicationAggregator().toOutput());
        jsonOutputWriter.write("request.json", context.requestAggregator().toOutput());
    }

    public void process(Path inputFile) throws IOException {
        if (rootHandler == null) {
            throw new IllegalStateException("Root handler is not configured for this LogProcessingService instance");
        }
        process(inputFile, rootHandler);
    }
}
