package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.request.AddStudentRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateGroupRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupResponse;
import com.github.nsu_upprpo.school_app.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/groups")
@RequiredArgsConstructor
public class AdminGroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAll(
            @RequestParam(required = false) UUID branchId) {
        if (branchId != null) {
            return ResponseEntity.ok(groupService.getByBranch(branchId));
        }
        return ResponseEntity.ok(groupService.getAll());
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(groupService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CreateGroupRequest request) {
        return ResponseEntity.ok(groupService.update(id, request));
    }

    @PostMapping("/{id}/students")
    public ResponseEntity<Void> addStudent(
            @PathVariable UUID id,
            @Valid @RequestBody AddStudentRequest request) {
        groupService.addStudent(id, request.getChildId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/students/{childId}")
    public ResponseEntity<Void> removeStudent(
            @PathVariable UUID id,
            @PathVariable UUID childId) {
        groupService.removeStudent(id, childId);
        return ResponseEntity.noContent().build();
    }

}
