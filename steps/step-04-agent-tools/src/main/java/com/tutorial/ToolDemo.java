package com.tutorial;

import com.tutorial.tools.CalculatorTool;
import com.tutorial.tools.DateTimeTool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;

/**
 * Step 04: Agent Tool Invocation — Let LLM call your Java methods.
 * <p>
 * Core concepts:
 * - @Tool: Mark a method as a "tool" that LLM can call
 * - @P: Describe parameter meaning, help LLM pass parameters correctly
 * - LLM automatically decides when to call which tool (Function Calling)
 * <p>
 * Workflow:
 * 1. User asks "What is 123 * 456"
 * 2. LLM determines it needs to call calculate tool, outputs: { "a": 123, "operator": "*", "b": 456 }
 * 3. LangChain4j framework automatically calls CalculatorTool.calculate(123, "*", 456)
 * 4. Return value "123.00 * 456.00 = 56088.00" is sent back to LLM
 * 5. LLM generates natural language answer: "123 multiplied by 456 equals 56088"
 */
public class ToolDemo {

    @SystemMessage("""
            You are an intelligent assistant that can help users with calculations and time queries.
            When users ask calculation questions, use the calculator tool.
            When users ask time-related questions, use the date time tool.
            For normal questions, answer directly.
            """)
    interface ToolAssistant {
        String chat(String message);
    }

    public static void main(String[] args) {
         ChatModel chatModel = createChatModel();
         System.out.println("ChatModel created successfully\n");

         // Create tool instances
         CalculatorTool calcTool = new CalculatorTool();
         DateTimeTool dateTimeTool = new DateTimeTool();

         // Assemble AiServices (key: register tools)
         ToolAssistant assistant = AiServices.builder(ToolAssistant.class)
                 .chatModel(chatModel)
                 .tools(calcTool, dateTimeTool)   // ← Register tools
                 .build();

         // Demo 1: Calculator tool
         System.out.println("[Demo 1] Trigger calculator tool");
         String answer1 = assistant.chat("What is 123 * 456?");
         System.out.println("Answer: " + answer1);
         System.out.println();

         // Demo 2: Date time tool
         System.out.println("[Demo 2] Trigger date time tool");
         String answer2 = assistant.chat("What time is it now?");
         System.out.println("Answer: " + answer2);
         System.out.println();

         // Demo 3: No tool triggered (normal conversation)
         System.out.println("[Demo 3] No tool triggered (normal conversation)");
         String answer3 = assistant.chat("Hello, introduce yourself");
         System.out.println("Answer: " + answer3);
         System.out.println();
    }

    private static ChatModel createChatModel() {
        Map<String, Object> config = loadConfig();
        @SuppressWarnings("unchecked")
        Map<String, Object> llmConfig = (Map<String, Object>) config.get("llm");
        String provider = (String) llmConfig.get("provider");
        @SuppressWarnings("unchecked")
        Map<String, String> providerConfig = (Map<String, String>) llmConfig.get(provider);

         String apiKey = providerConfig.get("api-key");
         if (apiKey == null || apiKey.contains("your-api-key-here")) {
             System.err.println("Please configure API Key first! See step-00-setup");
             System.exit(1);
         }

        return OpenAiChatModel.builder()
                .baseUrl(providerConfig.get("base-url"))
                .apiKey(apiKey)
                .modelName(providerConfig.get("model-name"))
                .build();
    }

    private static Map<String, Object> loadConfig() {
        Yaml yaml = new Yaml();
        InputStream is = ToolDemo.class.getClassLoader()
                .getResourceAsStream("application.yml");
        if (is == null) {
            throw new IllegalStateException("application.yml not found");
        }
        return yaml.load(is);
    }
}
