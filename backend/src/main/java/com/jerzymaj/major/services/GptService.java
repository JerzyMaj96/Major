package com.jerzymaj.major.services;

import com.jerzymaj.major.exceptions.AiGenerationException;
import com.jerzymaj.major.models.ActivityLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GptService {

    private final ChatClient chatClient;

    public String generateTaskDescription(String taskTitle) {

        try {
            return chatClient.prompt()
                    .system("""
                            You are an assistant for a software development team.
                            Based on the task title, generate a short, technical description (2-3 sentences)
                            explaining what needs to be done. Respond with only the description,
                            without any additional comments.
                            
                            """)
                    .user(taskTitle)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o-mini-2024-07-18")
                            .maxTokens(150)
                    )
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Failed to generate task description for title: {}", taskTitle, ex);
            throw new AiGenerationException("Failed to generate task description", ex);
        }
    }

    public String generateWeeklySummary(List<ActivityLog> activityLogs) {
        String formattedLogs = activityLogs.stream()
                .map(log -> String.format("[%s] Task '%s': %s changed from '%s' to '%s' by %s",
                        log.getCreatedAt(),
                        log.getTask().getTitle(),
                        log.getChangeType(),
                        log.getOldValue(),
                        log.getNewValue(),
                        log.getChangedBy()))
                .collect(Collectors.joining("\n"));

        try {
            return chatClient.prompt()
                    .system("""
                            You are an assistant for a software development team.
                            Based on the activity logs, generate a medium size, technical summary (10 sentences)
                            explaining the work completed during the week. Respond with only the summary,
                            without any additional comments.
                            """)
                    .user(formattedLogs)
                    .options(OpenAiChatOptions.builder()
                            .model("gpt-4o-mini-2024-07-18")
                            .maxTokens(400)
                    )
                    .call()
                    .content();
        } catch (Exception ex) {
            log.error("Failed to generate weekly summary", ex);
            throw new AiGenerationException("Failed to generate weekly summary", ex);
        }
    }
}
