package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.MarkAttendanceRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.AttendanceResponse;
import com.github.nsu_upprpo.school_app.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons/{lessonId}/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getByLesson(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(attendanceService.getByLesson(lessonId));
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> mark(@PathVariable UUID lessonId,
                                                    @Valid @RequestBody MarkAttendanceRequest request) {
        UUID teacherId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.mark(lessonId, request, teacherId));
    }
}

