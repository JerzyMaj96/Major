package com.jerzymaj.major.controllers;

import com.jerzymaj.major.models.WeeklySummary;
import com.jerzymaj.major.services.WeeklySummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/major/api/weekly-summary")
@RequiredArgsConstructor
public class WeeklySummaryController {

    private final WeeklySummaryService weeklySummaryService;

    @GetMapping("last-week")
    public WeeklySummary getWeeklySummary() {
        return weeklySummaryService.getLastWeeksSummary();
    }

    @GetMapping("all")
    public List<WeeklySummary> getAllWeeklySummary() {
        return weeklySummaryService.getAllWeeklySummary();
    }
}
