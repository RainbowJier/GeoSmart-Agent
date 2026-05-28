package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Calculator tool — LLM discovers and calls this method through @Tool annotation.
 * <p>
 * Working principle:
 * 1. LangChain4j reads the description in @Tool annotation, tells LLM "you have a calculator available"
 * 2. When user asks "What is 123 * 456", LLM determines it needs to call this tool
 * 3. LLM outputs structured data (function name + parameters), framework automatically calls this Java method
 * 4. Method return value is sent back to LLM, LLM generates natural language answer based on the result
 * <p>
 * Key annotations:
 * - @Tool("description"): Method description, for LLM to see, helps it decide when to call
 * - @P("description"): Parameter description, helps LLM pass parameters correctly
 */
public class CalculatorTool {

    @Tool("Calculate the result of four arithmetic operations on two numbers. Use this tool when user needs to do mathematical calculations.")
    public String calculate(
            @P("The first number") double a,
            @P("Operator: one of +, -, *, /") String operator,
            @P("The second number") double b) {

        double result = switch (operator) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            default -> throw new IllegalArgumentException("Unsupported operator: " + operator);
        };

        return String.format("%.2f %s %.2f = %.2f", a, operator, b, result);
    }
}
