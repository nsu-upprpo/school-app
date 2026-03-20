package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.UpdateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildResponse;
import com.github.nsu_upprpo.school_app.service.ChildService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/parents/me/children")
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    @PostMapping
    public ResponseEntity<ChildResponse> addChild(@Valid @RequestBody CreateChildRequest request) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(childService.addChild(parentId, request));
    }

    @GetMapping
    public ResponseEntity<List<ChildResponse>> getMyChildren() {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(childService.getChildrenByParent(parentId));
    }

    @GetMapping("/{childId}")
    public ResponseEntity<ChildResponse> getChild(@PathVariable UUID childId) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(childService.getChildById(parentId, childId));
    }

    @PutMapping("/{childId}")
    public ResponseEntity<ChildResponse> updateChild(
            @PathVariable UUID childId,
            @Valid @RequestBody UpdateChildRequest request) {
        UUID parentId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(childService.updateChild(parentId, childId, request));
    }
}
