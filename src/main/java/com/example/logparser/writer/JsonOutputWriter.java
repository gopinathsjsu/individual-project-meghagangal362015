package com.example.logparser.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Component
public class JsonOutputWriter {
    private final ObjectMapper objectMapper;
    private final Path outputDirectory;

    @Autowired
    public JsonOutputWriter(@Value("${log-parser.output-directory:.}") String outputDirectory) {
        this(Path.of(outputDirectory));
    }

    public JsonOutputWriter(Path outputDirectory) {
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.outputDirectory = outputDirectory;
    }

    public void write(String fileName, Map<String, Object> payload) throws IOException {
        Files.createDirectories(outputDirectory);
        objectMapper.writeValue(outputDirectory.resolve(fileName).toFile(), payload);
    }
}
