package com.geosmart.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

@Component
public class BusinessStatusTool {

    @Tool("查询业务办理状态，如规划许可证、用地审批、建设工程规划许可等")
    public String queryBusinessStatus(@P("业务编号，如：GH-2024-001") String businessId) {
        return """
                {
                  "businessId": "%s",
                  "businessType": "建设工程规划许可证",
                  "status": "审核中",
                  "currentStep": "规划审查",
                  "totalSteps": 5,
                  "completedSteps": 3,
                  "applicant": "XX开发有限公司",
                  "submitDate": "2024-03-15",
                  "estimatedCompletionDate": "2024-05-01",
                  "nextStep": "公示阶段",
                  "remarks": "已通过专家评审，待公示"
                }
                """.formatted(businessId);
    }
}
