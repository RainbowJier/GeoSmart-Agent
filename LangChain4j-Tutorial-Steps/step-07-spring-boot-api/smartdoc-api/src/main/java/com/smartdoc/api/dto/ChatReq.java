package com.smartdoc.api.dto;

/**
 * 聊天请求 DTO（数据传输对象）
 *
 * 【Step 06 对照】Step 06 中用户输入通过 Scanner 从控制台读取：
 *
 *   // Step 06 的写法：
 *   Scanner scanner = new Scanner(System.in);
 *   String userInput = scanner.nextLine();
 *
 * Spring Boot 方式：用户输入通过 HTTP POST 请求的 JSON Body 传入，
 * Spring MVC 自动将 JSON 反序列化为这个 record 对象。
 *
 * 使用 Java 16+ 的 record 类型，自动生成 getter、equals、hashCode、toString。
 * 等价于 Lombok 的 @Data，但更轻量（不可变）。
 *
 * JSON 示例：
 *   {
 *     "message": "什么是国土空间规划？",
 *     "sessionId": "user-001"
 *   }
 */
public record ChatReq(
        /** 用户发送的消息内容 */
        String message,
        /** 会话 ID，用于区分不同用户的对话；为 null 时使用 "default" */
        String sessionId
) {}
