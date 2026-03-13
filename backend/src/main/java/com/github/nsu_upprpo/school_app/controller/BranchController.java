package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.model.dto.response.BranchResponse;
import com.github.nsu_upprpo.school_app.service.BranchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public ResponseEntity<List<BranchResponse>> getAllBranches(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(branchService.getAllBranches(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BranchResponse> getBranchById(@PathVariable UUID id) {
        return ResponseEntity.ok(branchService.getById(id));
    }

}
