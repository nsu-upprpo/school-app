package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateBranchRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.BranchResponse;
import com.github.nsu_upprpo.school_app.service.BranchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/branches")
@RequiredArgsConstructor
public class AdminBranchController {

    private final BranchService branchService;

    @PostMapping
    public ResponseEntity<BranchResponse> create(@Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(branchService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BranchResponse> update(@PathVariable UUID id,
                                                 @Valid @RequestBody CreateBranchRequest request) {
        return ResponseEntity.ok(branchService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BranchResponse> delete(@PathVariable UUID id) {
        branchService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
