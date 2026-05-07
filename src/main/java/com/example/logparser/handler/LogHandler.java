package com.example.logparser.handler;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;

public interface LogHandler {
    void setNext(LogHandler next);

    void handle(ParsedLogLine line, LogProcessingContext context);
}
