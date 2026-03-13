package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateBranchRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.BranchResponse;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;

    public List<BranchResponse> getAllBranches(String city) {
        List<Branch> branches;
        if (city == null || city.isEmpty()) {
            branches = branchRepository.findByActiveTrue();
        } else {
            branches = branchRepository.findByCityAndActiveTrue(city);
        }
        return branches.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public BranchResponse getById(UUID branchId) {
        return toResponse(findById(branchId));
    }

    @Transactional
    public BranchResponse create(CreateBranchRequest request) {
        Branch branch = Branch.builder()
                .name(request.getName())
                .city(request.getCity())
                .address(request.getAddress())
                .phone(request.getPhone())
                .build();
        branch = branchRepository.save(branch);
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(UUID branchId, CreateBranchRequest request) {
        Branch branch = findById(branchId);
        branch.setName(request.getName());
        branch.setCity(request.getCity());
        branch.setAddress(request.getAddress());
        branch.setPhone(request.getPhone());
        branch = branchRepository.save(branch);
        return toResponse(branch);
    }

    @Transactional
    public void delete(UUID id) {
        Branch branch = findById(id);
        branch.setActive(false);
        branchRepository.save(branch);
    }

    public Branch findById(UUID id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Филиал не найден"));
    }

    private BranchResponse toResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .city(branch.getCity())
                .address(branch.getAddress())
                .phone(branch.getPhone())
                .active(branch.isActive())
                .build();
    }
}
