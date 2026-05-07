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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogProcessingE2ETest {
    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    void processesSampleLogAndWritesAllOutputs() throws Exception {
        Path sample = Path.of("src", "test", "resources", "sample.log");
        Path copiedInput = tempDir.resolve("input.txt");
        Files.copy(sample, copiedInput);
        runService(copiedInput);

        Path apm = tempDir.resolve("apm.json");
        Path application = tempDir.resolve("application.json");
        Path request = tempDir.resolve("request.json");

        assertTrue(Files.exists(apm));
        assertTrue(Files.exists(application));
        assertTrue(Files.exists(request));

        JsonNode apmNode = mapper.readTree(apm.toFile());
        JsonNode appNode = mapper.readTree(application.toFile());
        JsonNode requestNode = mapper.readTree(request.toFile());

        assertEquals(20.0, apmNode.at("/cpu/min").asDouble(), 0.0001);
        assertEquals(34.5, apmNode.at("/cpu/median").asDouble(), 0.0001);
        assertEquals(45.5, apmNode.at("/cpu/max").asDouble(), 0.0001);
        assertEquals(2, appNode.at("/INFO").asInt());
        assertEquals(1, appNode.at("/ERROR").asInt());
        JsonNode usersRoute = requestNode.path("/users");
        assertEquals(100.0, usersRoute.at("/response_times/min").asDouble(), 0.0001);
        assertEquals(350.0, usersRoute.at("/response_times/95_percentile").asDouble(), 0.0001);
        assertEquals(2, usersRoute.at("/status_codes/2XX").asInt());
        assertEquals(1, usersRoute.at("/status_codes/5XX").asInt());
    }

    @Test
    void writesEmptyFilesWhenNoValidLines() throws Exception {
        Path input = tempDir.resolve("bad.txt");
        Files.writeString(input, "not a parsable line");
        runService(input);

        JsonNode apm = mapper.readTree(tempDir.resolve("apm.json").toFile());
        JsonNode app = mapper.readTree(tempDir.resolve("application.json").toFile());
        JsonNode req = mapper.readTree(tempDir.resolve("request.json").toFile());

        assertEquals(0, apm.size());
        assertEquals(0, app.size());
        assertEquals(0, req.size());
    }

    private void runService(Path input) throws IOException {
        LogHandler apm = new ApmLogHandler();
        LogHandler app = new ApplicationLogHandler();
        LogHandler request = new RequestLogHandler();
        apm.setNext(app);
        app.setNext(request);
        new LogProcessingService(new LogLineParser(), apm, new JsonOutputWriter(tempDir)).process(input);
    }
}
