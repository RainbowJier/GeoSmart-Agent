# Step 04: Agent Tool Invoke

## Table of Contents

- [Learning Target](#learning-target)
- [Prerequisites](#prerequisites)
- [Core Concepts](#core-concepts)
  - [Function Calling Workflow](#function-calling-workflow)
  - [@Tool and @P Annotations](#tool-and-p-annotations)
  - [Register Tools](#register-tools)
- [How to Run](#how-to-run)
- [What You Will See](#what-you-will-see)
- [Key APIs](#key-apis)
- [Difference from Previous Step](#difference-from-previous-step)
- [How the Framework Knows to Call Tools (Under the Hood)](#how-the-framework-knows-to-call-tools-under-the-hood)
  - [The Complete Workflow](#the-complete-workflow)
  - [Why Does LLM Know the Structure?](#why-does-llm-know-the-structure)
  - [Key Insight](#key-insight)
- [Exercises](#exercises)
- [Next Step](#next-step)

---

## Learning Target

- Understand `@Tool` annotation — Let LLM call your Java methods
- Understand `@P` annotation — Describe parameter meaning
- Understand Function Calling mechanism — LLM automatically decides when to call which tool
- Learn to create custom tools and register them to AiServices

---

## Prerequisites

- Complete [Step 01: Hello LLM](../step-01-hello-llm/)

---

## Core Concepts

### Function Calling Workflow

```
User: "What is 123 * 456?"
  ↓
LLM analysis: This is a calculation problem → Need to call calculate tool
  ↓
LLM outputs: { "a": 123, "operator": "*", "b": 456 }
  ↓
Framework calls: CalculatorTool.calculate(123, "*", 456)
  ↓
Method returns: "123.00 * 456.00 = 56088.00"
  ↓
LLM generates: "123 multiplied by 456 equals 56088"
```

### @Tool and @P Annotations

```java
@Tool("Calculate the result of four arithmetic operations on two numbers. Use this when the user needs to perform mathematical calculations.")
public String calculate(
        @P("The first number") double a,
        @P("Operator: +, -, *, /") String operator,
        @P("The second number") double b) {
    // ...
}
```

- The description in `@Tool` is for **LLM to see**, not for humans. The clearer the description, the better the LLM can call it correctly
- The description in `@P` helps LLM understand what value should be passed for each parameter
- The method returns String, and LLM will understand the returned content

### Register Tools

```java
AiServices.builder(ToolAssistant.class)
    .chatModel(chatModel)
    .tools(calcTool, dateTimeTool)   // ← Register tools
    .build();
```

---

## How to Run

```bash
cd steps/step-04-agent-tools
mvn compile exec:java
```

---

## What You Will See

```
[Demo 1] Trigger calculator tool
Answer: 123 multiplied by 456 equals 56088.

[Demo 2] Trigger date time tool
Answer: Current time is May 11, 2026 15:30:45.

[Demo 3] No tool triggered (normal conversation)
Answer: Hello! I am an intelligent assistant...
```

## Key APIs

| API | Purpose |
|-----|---------|
| `@Tool("description")` | Mark method as callable tool |
| `@P("description")` | Describe parameter meaning |
| `AiServices.builder().tools(...)` | Register tool instances |

## Difference from Previous Step

- Step 03: `.contentRetriever(retriever)` — LLM passively receives knowledge
- **Step 04**: `.tools(calcTool, dateTimeTool)` — LLM actively calls methods
--- 
## How the Framework Knows to Call Tools (Under the Hood)

### The Complete Workflow

1. **Tool Registration** (Your Code)
   ```java
   .tools(calcTool, dateTimeTool)
   ```

2. **Tool Specification Generation** (LangChain4j)
   - Framework inspects `@Tool` methods
   - Converts them to `ToolSpecification` objects
   - Generates JSON schema for parameters

3. **Send to LLM**
   ```json
   {
     "messages": ["What is 123 * 456?"],
     "tools": [
       {
         "name": "calculate",
         "description": "Calculate the result of four arithmetic operations...",
         "parameters": {
           "type": "object",
           "properties": {
             "a": {"type": "number"},
             "operator": {"type": "string"},
             "b": {"type": "number"}
           }
         }
       }
     ]
   }
   ```

4. **LLM Decision** (Model Trained for Function Calling)
   - LLM analyzes the request and tool descriptions
   - Decides to call a tool based on `semantic`[语义] matching
   - Outputs structured response:
   ```json
   {
     "tool_calls": [
       {"name": "calculate", "arguments": "{\"a\":123,\"operator\":\"*\",\"b\":456}"}
     ]
   }
   ```

5. **Tool Execution** (LangChain4j Intercepts)
   - Parses tool name and arguments
   - Maps `"calculate"` → `CalculatorTool.calculate()` method
   - Invokes method with provided arguments
   - Captures return value: `"123.00 * 456.00 = 56088.00"`

6. **Feedback to LLM**
   ```json
   {
     "messages": [
       {"role": "user", "content": "What is 123 * 456?"},
       {"role": "assistant", "tool_calls": [...]},
       {"role": "tool", "content": "123.00 * 456.00 = 56088.00"}
     ]
   }
   ```

7. **Final Response** (LLM Generates Natural Language)
   - "123 multiplied by 456 equals 56088"

---

### Why Does LLM Know the Structure?

**LLM providers train their models specifically for function calling:**

- Models are fine-tuned on millions of examples showing:
  - Input: User message + tool specifications
  - Expected output: Structured tool calls in provider-specific format
  
- Each provider defines their own format (OpenAI, Anthropic, Google have different schemas)
- Models learn:
  - When to output tool_calls vs plain text
  - How to match user intent to tool descriptions
  - How to generate valid JSON arguments matching parameter schemas

**LangChain4j's Role:**
- Converts Java `@Tool` annotations to provider-specific formats
- Parses provider-specific responses back to Java method calls
- Abstracts away provider differences

---

### Key Insight

The structure is **hardcoded into the model during training** — LangChain4j doesn't teach the LLM at runtime! That's why `@Tool` and `@P` descriptions are critical: they directly influence the LLM's decision-making.

---

## Exercises

- [ ] Create a `WeatherTool` that simulates returning city weather (no real API needed, just return hardcoded data)
- [ ] Intentionally make the `@Tool` description vague (e.g., "Calculate things"), observe if LLM can still call it correctly
- [ ] Think: What types can tool methods return? Try returning a JSON string

## Next Step

[Step 05: Session Memory](../step-05-memory-session/) — Let LLM remember conversation history