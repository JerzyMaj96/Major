package com.jerzymaj.major.controllers;

import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.services.GptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GptController {

    private final GptService gptService;

    @PostMapping(ApiRoutes.BASE_API + "/gpt/generate-description")
    public ResponseEntity<String> generateTaskDescription(String title) {

        String description = gptService.generateTaskDescription(title);

        return ResponseEntity.ok(description);
    }
}
