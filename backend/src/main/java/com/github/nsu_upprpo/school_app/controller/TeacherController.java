package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonTemplateResponse;
import com.github.nsu_upprpo.school_app.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/teachers/me")
@RequiredArgsConstructor
public class TeacherController {

    private final LessonService lessonService;

    @GetMapping("/lesson-template")
    public ResponseEntity<LessonTemplateResponse> getLessonTemplate() {
        UUID teacherId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(lessonService.getTemplateForTeacher(teacherId));
    }
}

