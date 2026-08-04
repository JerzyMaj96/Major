package com.jerzymaj.major.controllers;

import com.jerzymaj.major.Dtos.ActivityLogDto;
import com.jerzymaj.major.configuration.ApiRoutes;
import com.jerzymaj.major.mappers.ActivityLogMapper;
import com.jerzymaj.major.services.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(ApiRoutes.BASE_API)
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping("/activity-logs")
    public List<ActivityLogDto> retrieveAllActivityLogs() {
        return activityLogService.findAllActivityLogs().stream()
                .map(ActivityLogMapper::toDto)
                .toList();
    }
}
