package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.UpdateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupInfo;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Child;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.repository.ChildRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final ChildRepository childRepository;
    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final BranchService branchService;

    @Transactional
    public ChildResponse addChild(UUID parentId, CreateChildRequest request) {
        Branch branch = branchService.findById(request.getBranchId());

        Child child = Child.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .patronymic(request.getPatronymic())
                .birthDate(request.getBirthDate())
                .branch(branch)
                .build();
        child = childRepository.save(child);

        ParentChild link = new ParentChild(parentId, child.getId());
        parentChildRepository.save(link);

        return toResponse(child, Collections.emptyList());
    }

    public List<ChildResponse> getChildrenByParent(UUID parentId) {
        List<UUID> childIds = parentChildRepository.findByParentId(parentId).stream()
                .map(ParentChild::getChildId)
                .collect(Collectors.toList());

        if (childIds.isEmpty()) {
            return Collections.emptyList();
        }

        return childRepository.findAllById(childIds).stream()
                .filter(Child::isActive)
                .map(child -> {
                    List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(child.getId());
                    return toResponse(child, groupStudents);
                })
                .collect(Collectors.toList());
    }

    public ChildResponse getChildById(UUID parentId, UUID childId) {
        checkOwnership(parentId, childId);
        Child child = findById(childId);
        List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(childId);
        return toResponse(child, groupStudents);
    }

    @Transactional
    public ChildResponse updateChild(UUID parentId, UUID childId, UpdateChildRequest request) {
        checkOwnership(parentId, childId);
        Child child = findById(childId);
        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setPatronymic(request.getPatronymic());
        child.setBirthDate(request.getBirthDate());
        child = childRepository.save(child);

        List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(childId);
        return toResponse(child, groupStudents);
    }

    private void checkOwnership(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к данному ребёнку");
        }
    }

    public Child findById(UUID id) {
        return childRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ребёнок не найден"));
    }

    private ChildResponse toResponse(Child child, List<GroupStudent> groupStudents) {
        List<GroupInfo> groups = groupStudents.stream()
                .map(gs -> GroupInfo.builder()
                        .groupId(gs.getGroupId())
                        .build()) // TODO: возвращать полную информацию о группе
                .collect(Collectors.toList());

        return ChildResponse.builder()
                .id(child.getId())
                .firstName(child.getFirstName())
                .lastName(child.getLastName())
                .patronymic(child.getPatronymic())
                .birthDate(child.getBirthDate())
                .branchId(child.getBranch() != null ? child.getBranch().getId() : null)
                .branchName(child.getBranch() != null ? child.getBranch().getName() + ", " + child.getBranch().getAddress() : null)
                .groups(groups)
                .build();
    }

}
