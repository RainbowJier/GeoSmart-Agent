package com.geosmart.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatSessionManager {

    private final int maxMessages;
    private final Map<String, ChatMemory> sessions = new ConcurrentHashMap<>();

    public ChatSessionManager(@Value("${chat.max-memory-messages:20}") int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public ChatMemory getOrCreate(String sessionId) {
        return sessions.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
    }

    public Map<String, ChatMemory> getAllSessions() {
        return sessions;
    }
}
