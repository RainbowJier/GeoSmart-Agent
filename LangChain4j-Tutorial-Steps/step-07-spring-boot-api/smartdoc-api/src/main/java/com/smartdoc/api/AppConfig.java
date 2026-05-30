package com.smartdoc.api;

import com.smartdoc.rag.DocumentIngestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@Configuration
public class AppConfig {

    private static final String SAMPLE_DOCS_PATH = "classpath:sample-docs/";

    @Bean
    public CommandLineRunner loadSampleDocuments(DocumentIngestionService ingestionService) {
        return args -> {
            try {
                Resource[] resources = new PathMatchingResourcePatternResolver()
                        .getResources(SAMPLE_DOCS_PATH + "*");

                for (Resource resource : resources) {
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
                log.info("Sample documents loading completed");
            } catch (IOException e) {
                log.warn("Failed to access sample documents directory (this is OK if no sample docs exist)", e);
            }
        };
    }
}
