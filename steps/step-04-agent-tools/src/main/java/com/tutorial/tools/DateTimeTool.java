package com.tutorial.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date time tool — Solve the problem that LLM doesn't know current time.
 * <p>
 * LLM's training data has a cutoff date, it doesn't know what time "now" is.
 * Through @Tool, let LLM proactively query current time when needed.
 */
public class DateTimeTool {

    @Tool("Get current date and time. Use this when user asks 'what time is it now', 'what day is today' etc.")
    public String getCurrentDateTime(
            @P("Date format, such as 'yyyy-MM-dd' or 'yyyy-MM-dd HH:mm:ss', defaults to full format") String format) {
        String pattern =
                (format == null || format.isBlank())
                ? "yyyy-MM-dd HH:mm:ss"
                : format;
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }
}
