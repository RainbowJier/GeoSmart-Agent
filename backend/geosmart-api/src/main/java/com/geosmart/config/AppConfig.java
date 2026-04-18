package com.geosmart.config;

import com.geosmart.rag.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class AppConfig {

    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);
    private static final String SAMPLE_DOCS_PATH = "classpath:sample-docs/";

    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService, ResourceLoader resourceLoader) {
        return args -> {
            try {
                org.springframework.core.io.Resource[] resources =
                    new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                        .getResources(SAMPLE_DOCS_PATH + "*");

                for (org.springframework.core.io.Resource resource : resources) {
                    if (resource.exists() && resource.isFile()) {
                        try {
                            Path file = Path.of(resource.getURI());
                            ingestionService.ingestDocument(file);
                            log.info("Loaded sample document: {}", file.getFileName());
                        } catch (Exception e) {
                            log.warn("Failed to load sample document: {}", resource.getFilename(), e);
                        }
                    }
                }
            } catch (IOException e) {
                log.warn("Failed to access sample documents directory", e);
            }
        };
    }
}
