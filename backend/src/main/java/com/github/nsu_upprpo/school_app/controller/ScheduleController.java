package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.request.LessonPeriodRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import com.github.nsu_upprpo.school_app.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final LessonService lessonService;

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<LessonResponse>> getGroupSchedule(@PathVariable UUID groupId) {
        return ResponseEntity.ok(lessonService.getByGroup(groupId));
    }

    @GetMapping("/groups/{groupId}/period")
    public ResponseEntity<List<LessonResponse>> getByPeriod(@PathVariable UUID groupId, @RequestBody @Valid
                                                            LessonPeriodRequest request) {
        return ResponseEntity.ok(lessonService.getByPeriod(groupId, request));
    }
}