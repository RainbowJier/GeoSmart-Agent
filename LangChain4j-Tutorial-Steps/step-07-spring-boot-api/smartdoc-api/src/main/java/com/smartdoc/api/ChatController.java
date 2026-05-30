package com.smartdoc.api;

import com.smartdoc.api.dto.req.ChatHistoryItemReq;
import com.smartdoc.api.dto.req.ChatReq;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import com.smartdoc.chat.ChatSessionManager;
import com.smartdoc.chat.DocAssistant;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final DocAssistant assistant;

    private final ChatSessionManager sessionManager;

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chat(@Valid @RequestBody ChatReq request) {
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
    public List<ChatHistoryItemReq> getHistory(@PathVariable String sessionId) {
        return sessionManager.getSessionHistory(sessionId).stream()
                .map(msg -> new ChatHistoryItemReq(
                        switch (msg.type()) {
                            case USER -> "user";
                            case AI -> "assistant";
                            default -> "system";
                        },
                        messageText(msg)
                ))
                .toList();
    }

    private static String messageText(ChatMessage msg) {
        if (msg instanceof AiMessage ai) return ai.text();
        if (msg instanceof UserMessage user) return user.singleText();
        if (msg instanceof SystemMessage sys) return sys.text();
        return msg.toString();
    }

    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        return Map.of("status", "cleared", "sessionId", sessionId);
    }
}
