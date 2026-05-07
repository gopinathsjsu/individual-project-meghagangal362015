package com.example.logparser;

import com.example.logparser.handler.ApmLogHandler;
import com.example.logparser.handler.ApplicationLogHandler;
import com.example.logparser.handler.LogHandler;
import com.example.logparser.handler.RequestLogHandler;
import com.example.logparser.parser.LogLineParser;
import com.example.logparser.service.LogProcessingService;
import com.example.logparser.writer.JsonOutputWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Downloads a real log file from the public internet and runs the full pipeline.
 * <p>
 * Source: Elastic's example Apache access logs (combined log format), served from GitHub raw.
 * Those lines are not {@code key=value}, so the parser ignores them and aggregations stay empty,
 * but this still validates end-to-end behavior against an external file.
 */
class InternetDownloadedLogTest {
    private static final String ELASTIC_APACHE_LOG_URL =
            "https://raw.githubusercontent.com/elastic/examples/master/Common%20Data%20Formats/apache_logs/apache_logs";

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void downloadsPublicApacheSampleAndWritesJsonOutputs() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(ELASTIC_APACHE_LOG_URL))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Skipping: could not download sample log from the internet: " + e.getMessage());
            throw new IllegalStateException("unreachable");
        }

        Assumptions.assumeTrue(response.statusCode() == 200, () -> "Unexpected HTTP status: " + response.statusCode());
        String body = response.body();
        Assumptions.assumeTrue(body != null && !body.isBlank(), () -> "Downloaded body was empty");

        String limited = body.lines().limit(500).collect(Collectors.joining("\n"));
        Path input = tempDir.resolve("internet-apache.log");
        Files.writeString(input, limited);

        runService(input);

        assertTrue(Files.exists(tempDir.resolve("apm.json")));
        assertTrue(Files.exists(tempDir.resolve("application.json")));
        assertTrue(Files.exists(tempDir.resolve("request.json")));

        JsonNode apm = mapper.readTree(tempDir.resolve("apm.json").toFile());
        JsonNode app = mapper.readTree(tempDir.resolve("application.json").toFile());
        JsonNode req = mapper.readTree(tempDir.resolve("request.json").toFile());

        assertTrue(apm.isObject());
        assertTrue(app.isObject());
        assertTrue(req.isObject());

        // Apache combined log format is not space-separated key=value; lines are ignored safely.
        assertTrue(apm.isEmpty());
        assertTrue(app.isEmpty());
        assertTrue(req.isEmpty());
    }

    private void runService(Path input) throws Exception {
        LogHandler apm = new ApmLogHandler();
        LogHandler app = new ApplicationLogHandler();
        LogHandler request = new RequestLogHandler();
        apm.setNext(app);
        app.setNext(request);
        new LogProcessingService(new LogLineParser(), apm, new JsonOutputWriter(tempDir)).process(input);
    }
}
