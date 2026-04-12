package com.geosmart.agent.tools;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SpatialQueryToolTest {
    @Test
    void shouldReturnSpatialInfoForLocation() {
        SpatialQueryTool tool = new SpatialQueryTool();
        String result = tool.querySpatialInfo("滨江新城");
        assertThat(result).contains("滨江新城");
    }

    @Test
    void shouldContainLandUseInfo() {
        SpatialQueryTool tool = new SpatialQueryTool();
        String result = tool.querySpatialInfo("A-01地块");
        assertThat(result).contains("planningPurpose");
    }
}
