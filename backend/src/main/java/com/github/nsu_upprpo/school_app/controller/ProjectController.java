package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/api/v1/groups/{groupId}/projects")
    public ResponseEntity<?> createProject(@PathVariable UUID groupId) {
        // TODO: создать проект
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/api/v1/groups/{groupId}/projects")
    public ResponseEntity<?> getGroupProjects(@PathVariable UUID groupId) {
        // TODO: список проектов группы
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/v1/projects/{projectId}/grades")
    public ResponseEntity<?> gradeProject(@PathVariable UUID projectId) {
        // TODO: выставить оценки
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/v1/children/{childId}/bonus-journal")
    public ResponseEntity<?> getBonusJournal(
            @PathVariable UUID childId,
            @RequestParam(required = false) UUID groupId) {
        // TODO: бонусный журнал ребёнка
        return ResponseEntity.ok().build();
    }
}
