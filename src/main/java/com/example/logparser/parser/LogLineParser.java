package com.example.logparser.parser;

import com.example.logparser.model.ParsedLogLine;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogLineParser {
    private static final Pattern TOKEN_PATTERN =
            Pattern.compile("(\\w+)=((\"(?:\\\\.|[^\"])*\")|\\S+)");

    public Optional<ParsedLogLine> parse(String line) {
        if (line == null || line.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = TOKEN_PATTERN.matcher(line);
        Map<String, String> fields = new HashMap<>();
        int cursor = 0;
        boolean matchedAny = false;

        while (matcher.find()) {
            String skipped = line.substring(cursor, matcher.start()).trim();
            if (!skipped.isEmpty()) {
                return Optional.empty();
            }

            String key = matcher.group(1);
            String rawValue = matcher.group(2);
            if (looksLikeUnterminatedQuote(rawValue)) {
                return Optional.empty();
            }
            fields.put(key, unquote(rawValue));
            cursor = matcher.end();
            matchedAny = true;
        }

        if (!matchedAny || !line.substring(cursor).trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new ParsedLogLine(fields));
    }

    private String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            String inner = value.substring(1, value.length() - 1);
            return inner.replace("\\\"", "\"");
        }
        return value;
    }

    private boolean looksLikeUnterminatedQuote(String value) {
        return value.startsWith("\"") && !value.endsWith("\"");
    }
}
