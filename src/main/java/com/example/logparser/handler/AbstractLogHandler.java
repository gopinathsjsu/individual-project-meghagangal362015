package com.example.logparser.handler;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.service.LogProcessingContext;

public abstract class AbstractLogHandler implements LogHandler {
    private LogHandler next;

    @Override
    public void setNext(LogHandler next) {
        this.next = next;
    }

    protected void delegate(ParsedLogLine line, LogProcessingContext context) {
        if (next != null) {
            next.handle(line, context);
        }
    }
}
