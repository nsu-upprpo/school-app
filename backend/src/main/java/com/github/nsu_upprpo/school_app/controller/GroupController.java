package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupDetailedResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupResponse;
import com.github.nsu_upprpo.school_app.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/api/v1/teachers/me/groups")
    public ResponseEntity<List<GroupResponse>> getTeacherGroups() {
        UUID teacherId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(groupService.getByTeacher(teacherId));
    }

    @GetMapping("/api/v1/parents/me/groups")
    public ResponseEntity<List<GroupResponse>> getParentGroups() {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(groupService.getByParent(parentId));
    }

    @GetMapping("/api/v1/groups/{id}")
    public ResponseEntity<GroupDetailedResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(groupService.getById(id));
    }

}
