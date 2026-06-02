package com.smartdoc.api;

import com.smartdoc.api.dto.resp.ChatHistoryItemResp;
import com.smartdoc.api.dto.req.ChatReq;
import com.smartdoc.common.entity.AjaxResult;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import com.smartdoc.chat.ChatSessionManager;
import com.smartdoc.chat.SpringCodeAssistant;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ExecutorService executor = new ThreadPoolExecutor(
            4, 20, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(200),
            r -> { Thread t = new Thread(r, "sse-chat"); t.setDaemon(true); return t; },
            new ThreadPoolExecutor.CallerRunsPolicy());

    private final SpringCodeAssistant assistant;

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
        AtomicBoolean cancelled = new AtomicBoolean(false);

        Future<?> future = executor.submit(() -> {
            try {
                TokenStream tokenStream = assistant.chat(request.message(), sessionId);

                tokenStream
                        .onPartialResponse(token -> {
                            if (cancelled.get()) return;
                            try {
                                emitter.send(SseEmitter.event().data(token));
                            } catch (IOException e) {
                                cancelled.set(true);
                                emitter.completeWithError(e);
                            }
                        })
                        .onCompleteResponse(response -> {
                            if (cancelled.get()) return;
                            try {
                                emitter.send(SseEmitter.event().data("[DONE]"));
                            } catch (IOException e) {
                                log.warn("Failed to send [DONE] event", e);
                            } finally {
                                emitter.complete();
                            }
                        })
                        .onError(error -> {
                            if (cancelled.get()) return;
                            log.error("Chat streaming error", error);
                            emitter.completeWithError(error);
                        })
                        .start();
            } catch (Exception e) {
                log.error("Failed to start chat stream", e);
                emitter.completeWithError(e);
            }
        });

        emitter.onTimeout(() -> {
            cancelled.set(true);
            future.cancel(true);
            log.warn("SSE connection timed out for session: {}", sessionId);
        });

        emitter.onCompletion(() -> {
            cancelled.set(true);
            future.cancel(true);
        });

        return emitter;
    }

    @GetMapping("/history/{sessionId}")
    public AjaxResult<List<ChatHistoryItemResp>> getHistory(@PathVariable String sessionId) {
        List<ChatHistoryItemResp> history = sessionManager.getSessionHistory(sessionId).stream()
                .map(msg -> new ChatHistoryItemResp(
                        switch (msg.type()) {
                            case USER -> "user";
                            case AI -> "assistant";
                            default -> "system";
                        },
                        messageText(msg)
                ))
                .toList();
        return AjaxResult.success(history);
    }

    private static String messageText(ChatMessage msg) {
        if (msg instanceof AiMessage ai) return ai.text();
        if (msg instanceof UserMessage user) return user.singleText();
        if (msg instanceof SystemMessage sys) return sys.text();
        return msg.toString();
    }

    @DeleteMapping("/session/{sessionId}")
    public AjaxResult<Void> clearSession(@PathVariable String sessionId) {
        sessionManager.clearSession(sessionId);
        return AjaxResult.success();
    }
}
