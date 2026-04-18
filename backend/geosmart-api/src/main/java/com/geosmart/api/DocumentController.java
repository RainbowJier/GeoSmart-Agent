package com.geosmart.api;

import com.geosmart.rag.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentIngestionService ingestionService;
    private final List<String> uploadedDocuments = new ArrayList<>();

    public DocumentController(DocumentIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(
            @RequestParam("file") MultipartFile file) {
        try {
            Path tempFile = Files.createTempFile("doc-", file.getOriginalFilename());
            file.transferTo(tempFile.toFile());
            ingestionService.ingestDocument(tempFile);
            Files.deleteIfExists(tempFile);

            uploadedDocuments.add(file.getOriginalFilename());
            log.info("Uploaded and ingested: {}", file.getOriginalFilename());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "filename", file.getOriginalFilename()
            ));
        } catch (Exception e) {
            log.error("Failed to upload document", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> listDocuments() {
        return ResponseEntity.ok(Map.of("documents", uploadedDocuments));
    }
}
