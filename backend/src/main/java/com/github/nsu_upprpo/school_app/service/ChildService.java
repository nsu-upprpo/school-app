package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.UpdateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupInfo;
import com.github.nsu_upprpo.school_app.model.entity.*;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildService {

    private final UserRepository userRepository;
    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ChildResponse addChild(UUID parentId, CreateChildRequest request) {
        User child = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .patronymic(request.getPatronymic())
                .birthDate(request.getBirthDate())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();
        child = userRepository.save(child);

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

        return userRepository.findAllById(childIds).stream()
                .filter(User::isActive)
                .map(child -> {
                    List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(child.getId());
                    return toResponse(child, groupStudents);
                })
                .collect(Collectors.toList());
    }

    public ChildResponse getChildById(UUID parentId, UUID childId) {
        checkOwnership(parentId, childId);
        User child = findChildById(childId);
        List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(childId);
        return toResponse(child, groupStudents);
    }

    @Transactional
    public ChildResponse updateChild(UUID parentId, UUID childId, UpdateChildRequest request) {
        checkOwnership(parentId, childId);
        User child = findChildById(childId);
        child.setFirstName(request.getFirstName());
        child.setLastName(request.getLastName());
        child.setPatronymic(request.getPatronymic());
        child.setBirthDate(request.getBirthDate());
        child = userRepository.save(child);

        List<GroupStudent> groupStudents = groupStudentRepository.findByChildId(childId);
        return toResponse(child, groupStudents);
    }

    private void checkOwnership(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к данному ребёнку");
        }
    }

    public User findChildById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ребёнок не найден"));
    }

    private ChildResponse toResponse(User child, List<GroupStudent> groupStudents) {
        List<GroupInfo> groups = groupStudents.stream()
                .map(gs -> {
                    Group group = groupRepository.findById(gs.getGroupId()).orElse(null);
                    return GroupInfo.builder()
                            .groupId(gs.getGroupId())
                            .groupName(group != null ? group.getCourse().getName() : null)
                            .courseName(group != null ? group.getCourse().getName() : null)
                            .build();
                })
                .collect(Collectors.toList());

        return ChildResponse.builder()
                .id(child.getId())
                .firstName(child.getFirstName())
                .lastName(child.getLastName())
                .patronymic(child.getPatronymic())
                .birthDate(child.getBirthDate())
                .groups(groups)
                .build();
    }
}