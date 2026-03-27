package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ScheduleController {

    private final LessonService lessonService;

    @GetMapping("/api/v1/children/{childId}/schedule")
    public ResponseEntity<?> getChildSchedule(
            @PathVariable UUID childId,
            @RequestParam String from,
            @RequestParam String to) {
        // TODO: расписание ребёнка
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/children/{childId}/schedule/upcoming")
    public ResponseEntity<?> getUpcomingLessons(
            @PathVariable UUID childId,
            @RequestParam(defaultValue = "3") int limit) {
        // TODO: ближайшие занятия
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/teachers/me/schedule")
    public ResponseEntity<?> getTeacherSchedule(
            @RequestParam String from,
            @RequestParam String to) {
        // TODO: расписание преподавателя
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/teachers/me/schedule/today")
    public ResponseEntity<?> getTeacherScheduleToday() {
        // TODO: расписание на сегодня
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/branches/{branchId}/schedule")
    public ResponseEntity<?> getBranchSchedule(
            @PathVariable UUID branchId,
            @RequestParam String from,
            @RequestParam String to) {
        // TODO: расписание филиала
        return ResponseEntity.ok().build();
    }
}
