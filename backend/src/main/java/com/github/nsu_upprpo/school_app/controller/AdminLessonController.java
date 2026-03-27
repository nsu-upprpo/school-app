package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/lessons")
@RequiredArgsConstructor
public class AdminLessonController {

    private final LessonService lessonService;

    @PostMapping
    public ResponseEntity<?> create() { return ResponseEntity.status(HttpStatus.CREATED).build(); }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id) { return ResponseEntity.ok().build(); }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) { return ResponseEntity.ok().build(); }

}
