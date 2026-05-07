package com.example.logparser.cli;

import com.example.logparser.handler.LogHandler;
import com.example.logparser.service.LogProcessingService;
import org.springframework.boot.Banner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;

@SpringBootApplication(scanBasePackages = "com.example.logparser")
public class LogParserCliApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(LogParserCliApplication.class);
        application.setWebApplicationType(WebApplicationType.NONE);
        application.setBannerMode(Banner.Mode.OFF);
        application.run(args);
    }

    @Bean
    CommandLineRunner commandLineRunner(LogProcessingService service, LogHandler rootHandler) {
        return args -> {
            String inputFile = parseFileArgument(args);
            if (inputFile == null) {
                System.err.println("Usage: java -jar target/log-parser-cli-1.0.0.jar --file input.txt");
                System.exit(1);
                return;
            }

            Path inputPath = Path.of(inputFile);
            if (!Files.isRegularFile(inputPath)) {
                System.err.println("Input file not found (or not a regular file): " + inputPath.toAbsolutePath());
                System.exit(2);
                return;
            }

            service.process(inputPath, rootHandler);
        };
    }

    private static String parseFileArgument(String[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        for (int i = 0; i < args.length - 1; i++) {
            if ("--file".equals(args[i])) {
                return args[i + 1];
            }
        }
        return null;
    }
}
