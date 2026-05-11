package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateProjectRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectGradeResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectResponse;
import com.github.nsu_upprpo.school_app.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getByGroup(@RequestParam UUID groupId) {
        return ResponseEntity.ok(projectService.getByGroup(groupId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(request));
    }

    @GetMapping("/{id}/grades")
    public ResponseEntity<List<ProjectGradeResponse>> getGrades(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getGrades(id));
    }

    @PostMapping("/{id}/grades")
    public ResponseEntity<ProjectGradeResponse> addGrade(
            @PathVariable UUID id,
            @RequestParam UUID childId,
            @RequestParam Integer score,
            @RequestParam(required = false) String comment) {
        UUID teacherId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.addGrade(id, childId, teacherId, score, comment));
    }
}
