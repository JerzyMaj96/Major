package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.WeeklySummaryDto;
import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.mappers.WeeklySummaryMapper;
import com.jerzymaj.major.services.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(ApiRoutes.BASE_API + "/weekly-summary")
@RequiredArgsConstructor
public class WeeklySummaryController {

    private final WeeklySummaryService weeklySummaryService;

    @GetMapping("/last-week")
    public ResponseEntity<WeeklySummaryDto> getWeeklySummary() {
        return ResponseEntity.ok(WeeklySummaryMapper.toDto(weeklySummaryService.getLastWeeksSummary()));
    }

    @GetMapping("/all")
    public ResponseEntity<List<WeeklySummaryDto>> getAllWeeklySummary() {
        return ResponseEntity.ok(weeklySummaryService.getAllWeeklySummary().stream()
                .map(WeeklySummaryMapper::toDto)
                .toList());
    }

    @PostMapping("/generate")
    public CompletableFuture<ResponseEntity<WeeklySummaryDto>> generateWeeklySummary() {
        return weeklySummaryService.generateWeeklySummary()
                .thenApply(weeklySummary -> ResponseEntity.ok(WeeklySummaryMapper.toDto(weeklySummary)));
    }
}
