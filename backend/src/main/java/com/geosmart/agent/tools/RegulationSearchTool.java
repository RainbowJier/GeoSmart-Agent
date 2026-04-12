package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class RegulationSearchTool {

    @Tool("根据关键词检索国土空间规划相关法规政策文件，返回匹配的法规名称、文号和摘要")
    public String searchRegulations(@P("搜索关键词，如：国土空间规划、用地审批、生态红线") String keyword) {
        return """
                {
                  "total": 2,
                  "regulations": [
                    {
                      "name": "《国土空间规划法》",
                      "docNumber": "国发〔2024〕XX号",
                      "summary": "规定了国土空间规划的编制、审批、实施和监督管理，明确了各类空间用途管制要求。与「%s」高度相关。",
                      "effectiveDate": "2024-01-01"
                    },
                    {
                      "name": "《建设用地审批管理办法》",
                      "docNumber": "自然资发〔2023〕XX号",
                      "summary": "规范了建设用地的申请、审查、批准程序，涉及农用地转用、土地征收等事项。包含「%s」相关条款。",
                      "effectiveDate": "2023-06-01"
                    }
                  ]
                }
                """.formatted(keyword, keyword);
    }
}
