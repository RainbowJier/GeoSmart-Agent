package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessStatusToolTest {
    @Test
    void shouldReturnBusinessStatus() {
        BusinessStatusTool tool = new BusinessStatusTool();
        String result = tool.queryBusinessStatus("GH-2024-001");
        assertThat(result).contains("GH-2024-001");
    }

    @Test
    void shouldContainStatusField() {
        BusinessStatusTool tool = new BusinessStatusTool();
        String result = tool.queryBusinessStatus("GH-2024-001");
        assertThat(result).contains("status");
    }
}
