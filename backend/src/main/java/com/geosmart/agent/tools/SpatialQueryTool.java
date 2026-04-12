package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class SpatialQueryTool {

    @Tool("查询指定区域或地块的空间规划信息，包括土地性质、规划用途、红线范围等")
    public String querySpatialInfo(@P("区域名称或地块编号，如：滨江新城、A-01地块") String location) {
        return """
                {
                  "location": "%s",
                  "spatialInfo": {
                    "landUseType": "城镇建设用地",
                    "planningPurpose": "商住混合用地",
                    "redlineStatus": "未涉及生态红线",
                    "zoning": "城市更新改造区",
                    "floorAreaRatio": "≤3.5",
                    "buildingDensity": "≤35%%",
                    "greeningRate": "≥30%%",
                    "restrictions": ["需进行环境影响评价", "符合城市总体规划要求"]
                  }
                }
                """.formatted(location);
    }
}
