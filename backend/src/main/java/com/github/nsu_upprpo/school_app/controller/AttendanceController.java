package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<?> getAttendances(@PathVariable UUID lessonId) {
        // TODO: список учеников с отметками
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> markAttendance(@PathVariable UUID lessonId) {
        // TODO: массовая отметка посещаемости
        return ResponseEntity.ok().build();
    }
}
