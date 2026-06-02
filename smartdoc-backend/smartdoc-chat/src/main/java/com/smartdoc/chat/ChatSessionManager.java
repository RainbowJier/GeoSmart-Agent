package com.smartdoc.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatSessionManager {

    private final int maxMessages;
    private final long sessionTimeoutMs;
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastAccessTime = new ConcurrentHashMap<>();

    public ChatSessionManager(
            @Value("${chat.max-memory-messages:20}") int maxMessages,
            @Value("${chat.session-timeout-minutes:30}") int sessionTimeoutMinutes) {
        this.maxMessages = maxMessages;
        this.sessionTimeoutMs = sessionTimeoutMinutes * 60_000L;
    }

    public ChatMemory getOrCreate(String sessionId) {
        lastAccessTime.put(sessionId, System.currentTimeMillis());
        return sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public List<ChatMessage> getSessionHistory(String sessionId) {
        ChatMemory memory = sessions.get(sessionId);
        if (memory == null) {
            return List.of();
        }
        return memory.messages().stream()
                .filter(m -> m.type() != ChatMessageType.SYSTEM)
                .toList();
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        lastAccessTime.remove(sessionId);
    }

    public Map<String, ChatMemory> getAllSessions() {
        return sessions;
    }

    @Scheduled(fixedDelayString = "${chat.eviction-interval-ms:300000}")
    public void evictExpiredSessions() {
        long now = System.currentTimeMillis();
        lastAccessTime.entrySet().removeIf(entry -> {
            if (now - entry.getValue() > sessionTimeoutMs) {
                sessions.remove(entry.getKey());
                log.debug("Evicted expired session: {}", entry.getKey());
                return true;
            }
            return false;
        });
    }
}
