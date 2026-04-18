package com.geosmart.config;

import com.geosmart.rag.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService) {
        return args -> {
            Path sampleDir = Path.of("src/main/resources/sample-docs");
            if (Files.exists(sampleDir)) {
                try (var stream = Files.list(sampleDir)) {
                    stream.filter(Files::isRegularFile)
                          .forEach(file -> {
                              try {
                                  ingestionService.ingestDocument(file);
                                  log.info("Loaded sample document: {}", file.getFileName());
                              } catch (Exception e) {
                                  log.warn("Failed to load sample document: {}", file.getFileName(), e);
                              }
                          });
                }
            }
        };
    }
}
