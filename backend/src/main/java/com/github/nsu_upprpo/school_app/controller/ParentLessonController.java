package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.CancelLessonRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RescheduleLessonRequest;
import com.github.nsu_upprpo.school_app.service.LessonParticipationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parents/me/children/{childId}/lessons")
@RequiredArgsConstructor
public class ParentLessonController {

    private final LessonParticipationService participationService;

    @PostMapping("/{lessonId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable UUID childId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody(required = false) CancelLessonRequest request) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        String reason = request != null ? request.getReason() : null;
        participationService.cancelByParent(parentId, childId, lessonId, reason);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/restore")
    public ResponseEntity<Void> restore(
            @PathVariable UUID childId,
            @PathVariable UUID lessonId) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        participationService.restoreByParent(parentId, childId, lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/reschedule")
    public ResponseEntity<Void> reschedule(
            @PathVariable UUID childId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody RescheduleLessonRequest request) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        participationService.rescheduleByParent(
                parentId, childId, lessonId, request.getTargetLessonId(), request.getReason());
        return ResponseEntity.noContent().build();
    }
}
