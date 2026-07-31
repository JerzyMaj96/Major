package com.jerzymaj.major.services;

import com.jerzymaj.major.exceptions.AiGenerationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

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

}
