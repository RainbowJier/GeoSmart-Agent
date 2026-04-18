package com.geosmart.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;

public interface GeoSmartAssistant {

    @SystemMessage("""
            你是「国土空间规划智能助手」，专门帮助用户解答国土空间规划相关的政策法规问题，
            以及查询土地空间信息和业务办理状态。

            你的职责：
            1. 政策法规咨询：回答关于国土空间规划法律法规的问题，引用政策时需注明文件名称。
            2. 空间信息查询：查询指定区域的土地性质、规划用途、红线范围等信息。
            3. 业务办理查询：查询规划许可、用地审批等业务的办理进度。

            回答要求：
            - 专业准确，引用政策文件需注明文件名称和文号。
            - 如果不确定，请如实告知，不要编造信息。
            - 使用清晰、简洁的中文回答。
            """)
    TokenStream chat(@UserMessage String message, @MemoryId String sessionId);
}
