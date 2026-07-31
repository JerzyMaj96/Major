package com.jerzymaj.major.services;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GptService {

    private final ChatClient chatClient;

    public String generateTaskDescription(String taskTitle) {

        return chatClient.prompt()
                .system("""
                        You are an assistant for a software development team. 
                        Based on the task title, generate a short, technical description (2-3 sentences)
                        explaining what needs to be done. 
                        Respond with only the description, without any additional comments.
                        """)
                .user(taskTitle)
                .options(OpenAiChatOptions.builder()
                        .model("gpt-4o-mini-2024-07-18")
                        .maxTokens(150)
                        .build())
                .call()
                .content();
    }

}
