package com.geosmart.api;

import com.geosmart.chat.ChatSessionManager;
import com.geosmart.chat.GeoSmartAssistant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
@ActiveProfiles("dev")
@Import(ChatSessionManager.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GeoSmartAssistant assistant;

    @Test
    void shouldReturnHistoryForSession() throws Exception {
        mockMvc.perform(get("/api/chat/history/test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("test-session"));
    }

    @Test
    void shouldClearSession() throws Exception {
        mockMvc.perform(delete("/api/chat/session/test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cleared"))
                .andExpect(jsonPath("$.sessionId").value("test-session"));
    }
}
