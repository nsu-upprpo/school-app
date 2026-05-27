package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.LessonPeriodRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildScheduleLessonResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import com.github.nsu_upprpo.school_app.service.ChildScheduleService;
import com.github.nsu_upprpo.school_app.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final LessonService lessonService;
    private final ChildScheduleService childScheduleService;

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<List<LessonResponse>> getGroupSchedule(@PathVariable UUID groupId) {
        return ResponseEntity.ok(lessonService.getByGroup(groupId));
    }

    @GetMapping("/groups/{groupId}/period")
    public ResponseEntity<List<LessonResponse>> getByPeriod(@PathVariable UUID groupId,
                                                            @RequestBody @Valid LessonPeriodRequest request) {
        return ResponseEntity.ok(lessonService.getByPeriod(groupId, request));
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<List<ChildScheduleLessonResponse>> getChildSchedule(
            @PathVariable UUID childId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(childScheduleService.getForChild(parentId, childId, from, to));
    }

    @GetMapping("/children/{childId}/upcoming")
    public ResponseEntity<List<ChildScheduleLessonResponse>> getUpcomingForChild(
            @PathVariable UUID childId,
            @RequestParam(defaultValue = "3") int limit) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(
                childScheduleService.getUpcomingForChild(parentId, childId, limit)
        );
    }
}
