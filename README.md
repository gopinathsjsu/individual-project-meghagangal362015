# Log Parser CLI (Spring Boot + Java 17 + Maven)

A command-line log parser that reads a log file from `--file`, classifies each valid line with a **Chain of Responsibility**, aggregates APM/Application/Request metrics, ignores malformed or unsupported lines safely, and writes:

- `apm.json`
- `application.json`
- `request.json`

All output files are always written, even when empty.

## Part I — Design & class diagram

Part I (problem statement, patterns, trade-offs, and the **class diagram**) lives in **[`project-part-1/PART1_SUBMISSION.md`](project-part-1/PART1_SUBMISSION.md)**. Section **4** contains the Mermaid diagram; view that file on GitHub or in any Markdown preview **that supports Mermaid** so the diagram renders as a figure. **[`project-part-1/README.md`](project-part-1/README.md)** explains how the `.md`, `.txt`, and `.pdf` copies relate.

## Build

```bash
mvn clean test
mvn clean package
```

## Run

```bash
java -jar target/log-parser-cli-1.0.0.jar --file input.txt
```

To write output JSON files to a specific folder:

```bash
java -jar target/log-parser-cli-1.0.0.jar --file input.txt --log-parser.output-directory=./out
```

## Supported Log Types

- **APM log**: requires `metric` and numeric `value`
  - Aggregates `min`, `median`, `average`, `max` per metric
- **Application log**: requires `level` and `message`
  - Counts occurrences per severity (`INFO`, `ERROR`, etc.)
- **Request log**: requires `request_method`, `request_url`, `response_status`, `response_time_ms`
  - Aggregates `min`, `95_percentile`, `max` response times per route (`request_url`)
  - Counts response status classes per route (`2XX`, `4XX`, `5XX`)

## Parsing Notes

- Key-value format: `key=value`
- Quoted values are supported: `message="hello world"`
- Malformed lines are ignored safely

## Package Structure

- `cli` - CLI entry point and argument handling
- `parser` - line parser for key/value tokens
- `handler` - Chain of Responsibility handlers (`ApmLogHandler` → `ApplicationLogHandler` → `RequestLogHandler`)
- `aggregator` - aggregation logic per log type
- `service` - orchestration of parse -> handle -> write
- `writer` - JSON output via Jackson
- `model` - shared models

## Tests

- Unit tests for parser and handler chain
- End-to-end tests using `src/test/resources/sample.log`
- Empty-output behavior tests ensure all three JSON files are written
