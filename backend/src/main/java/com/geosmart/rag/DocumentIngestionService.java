package com.geosmart.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionService.class);

    private final EmbeddingStoreIngestor ingestor;

    public DocumentIngestionService(EmbeddingStore<TextSegment> store,
                                    EmbeddingModel embeddingModel,
                                    @Value("${rag.chunk-size:500}") int chunkSize,
                                    @Value("${rag.chunk-overlap:100}") int chunkOverlap) {
        this.ingestor = EmbeddingStoreIngestor.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .documentSplitter(DocumentSplitters.recursive(chunkSize, chunkOverlap))
                .build();
    }

    public void ingestDocument(Path documentPath) {
        String fileName = documentPath.getFileName().toString();
        DocumentParser parser = resolveParser(fileName);
        Document document = FileSystemDocumentLoader.loadDocument(documentPath, parser);
        ingestor.ingest(document);
        log.info("Ingested document: {} ({} chars)", fileName, document.text().length());
    }

    private DocumentParser resolveParser(String fileName) {
        if (fileName.endsWith(".pdf")) {
            return new ApachePdfBoxDocumentParser();
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return new ApachePoiDocumentParser();
        } else {
            return new TextDocumentParser();
        }
    }
}
