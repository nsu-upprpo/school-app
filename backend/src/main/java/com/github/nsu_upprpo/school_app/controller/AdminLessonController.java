package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateLessonRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import com.github.nsu_upprpo.school_app.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lessons")
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonService lessonService;

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getByGroup(@RequestParam UUID groupId) {
        return ResponseEntity.ok(lessonService.getByGroup(groupId));
    }

    @PostMapping
    public ResponseEntity<LessonResponse> create(@Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<LessonResponse> cancel(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(lessonService.cancel(id, reason));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<LessonResponse> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonService.complete(id));
    }
}
