package com.geosmart.api;

import com.geosmart.api.dto.ChatRequest;
import com.geosmart.chat.ChatSessionManager;
import com.geosmart.chat.GeoSmartAssistant;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final GeoSmartAssistant assistant;
    private final ChatSessionManager sessionManager;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(120_000L);
        String sessionId = request.sessionId() != null ? request.sessionId() : "default";

        executor.execute(() -> {
            try {
                TokenStream tokenStream = assistant.chat(request.message(), sessionId);
                tokenStream
                        .onPartialResponse(token -> {
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .onCompleteResponse(response -> emitter.complete())
                        .onError(error -> {
                            log.error("Chat streaming error", error);
                            emitter.completeWithError(error);
                        })
                        .start();
            } catch (Exception e) {
                log.error("Failed to start chat stream", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @GetMapping("/history/{sessionId}")
    public Map<String, Object> getHistory(@PathVariable String sessionId) {
        return Map.of("sessionId", sessionId);
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        return Map.of("status", "cleared", "sessionId", sessionId);
    }
}
