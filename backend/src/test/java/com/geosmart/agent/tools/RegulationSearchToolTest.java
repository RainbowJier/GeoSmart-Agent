package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class RegulationSearchToolTest {
    @Test
    void shouldReturnRegulationResultsForKeyword() {
        RegulationSearchTool tool = new RegulationSearchTool();
        String result = tool.searchRegulations("国土空间规划");
        assertThat(result).contains("国土空间规划");
    }

    @Test
    void shouldReturnNonEmptyResult() {
        RegulationSearchTool tool = new RegulationSearchTool();
        String result = tool.searchRegulations("用地审批");
        assertThat(result).isNotBlank();
    }
}
