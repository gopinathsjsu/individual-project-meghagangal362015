package com.example.logparser.model;

import java.util.Map;

public record ParsedLogLine(Map<String, String> fields) {
}
