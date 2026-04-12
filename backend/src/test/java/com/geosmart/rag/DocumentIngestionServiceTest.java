package com.geosmart.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatNoException;

class DocumentIngestionServiceTest {

    private DocumentIngestionService service;

    @BeforeEach
    void setUp() {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        service = new DocumentIngestionService(store, embeddingModel, 500, 100);
    }

    @Test
    void shouldIngestTextDocumentWithoutError(@TempDir Path tempDir) throws Exception {
        Path docPath = tempDir.resolve("test-regulation.txt");
        java.nio.file.Files.writeString(docPath, """
                国土空间规划管理办法
                第一条 为了规范国土空间规划管理，制定本办法。
                第二条 国土空间规划包括总体规划和详细规划。
                """);

        assertThatNoException().isThrownBy(() -> service.ingestDocument(docPath));
    }
}
