package com.geosmart.rag;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RetrievalService {

    private final ContentRetriever contentRetriever;

    public RetrievalService(EmbeddingStore<TextSegment> store,
                            EmbeddingModel embeddingModel,
                            @Value("${rag.max-results:5}") int maxResults) {
        this.contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .build();
    }

    public ContentRetriever getContentRetriever() {
        return contentRetriever;
    }
}
