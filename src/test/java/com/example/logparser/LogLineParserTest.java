package com.example.logparser;

import com.example.logparser.model.ParsedLogLine;
import com.example.logparser.parser.LogLineParser;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogLineParserTest {
    private final LogLineParser parser = new LogLineParser();

    @Test
    void parsesQuotedValues() {
        Optional<ParsedLogLine> parsed = parser.parse("level=INFO message=\"hello world\"");
        assertTrue(parsed.isPresent());
        assertEquals("INFO", parsed.get().fields().get("level"));
        assertEquals("hello world", parsed.get().fields().get("message"));
    }

    @Test
    void rejectsMalformedLine() {
        Optional<ParsedLogLine> parsed = parser.parse("level=INFO message=\"unterminated");
        assertTrue(parsed.isEmpty());
    }
}
