package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.BadRequestException;
import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateGroupRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupDetailedResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupResponse;
import com.github.nsu_upprpo.school_app.model.entity.*;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.time.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final CourseService courseService;
    private final BranchService branchService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ParentChildRepository parentChildRepository;

    public List<GroupResponse> getAll() {
        return groupRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getByBranch(UUID branchId) {
        return groupRepository.findByBranchIdAndActiveTrue(branchId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getByTeacher(UUID teacherId) {
        return groupRepository.findByTeacherIdAndActiveTrue(teacherId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getByParent(UUID parentId) {
        List<UUID> childIds = parentChildRepository.findByParentId(parentId).stream()
                .map(ParentChild::getChildId)
                .collect(Collectors.toList());

        if (childIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> groupIds = childIds.stream()
                .flatMap(childId -> groupStudentRepository.findByChildId(childId).stream())
                .filter(gs -> gs.getLeftAt() == null)
                .map(GroupStudent::getGroupId)
                .distinct()
                .collect(Collectors.toList());

        return groupRepository.findAllById(groupIds).stream()
                .filter(Group::isActive)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public GroupDetailedResponse getById(UUID id) {
        Group group = findById(id);
        List<GroupStudent> students = groupStudentRepository.findByGroupIdAndLeftAtIsNull(id);

        List<GroupDetailedResponse.StudentInfo> studentInfos = students.stream()
                .map(gs -> {
                    User child = userRepository.findById(gs.getChildId()).orElse(null);
                    if (child == null) return null;
                    return GroupDetailedResponse.StudentInfo.builder()
                            .childId(child.getId())
                            .firstName(child.getFirstName())
                            .lastName(child.getLastName())
                            .enrolledAt(gs.getEnrolledAt())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return GroupDetailedResponse.builder()
                .id(group.getId())
                .courseName(group.getCourse().getName())
                .branchName(group.getBranch().getName())
                .teacherName(group.getTeacher().getFullName())
                .scheduleDescription(group.getScheduleDescription())
                .maxStudents(group.getMaxStudents())
                .students(studentInfos)
                .build();
    }

    @Transactional
    public GroupResponse create(CreateGroupRequest request) {
        Course course = courseService.findById(request.getCourseId());
        Branch branch = branchService.findById(request.getBranchId());
        User teacher = userService.findById(request.getTeacherId());

        Group group = Group.builder()
                .course(course)
                .branch(branch)
                .teacher(teacher)
                .scheduleDescription(request.getScheduleDescription())
                .maxStudents(request.getMaxStudents())
                .build();
        group = groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public GroupResponse update(UUID id, CreateGroupRequest request) {
        Group group = findById(id);
        Course course = courseService.findById(request.getCourseId());
        Branch branch = branchService.findById(request.getBranchId());
        User teacher = userService.findById(request.getTeacherId());

        group.setCourse(course);
        group.setBranch(branch);
        group.setTeacher(teacher);
        group.setScheduleDescription(request.getScheduleDescription());
        group.setMaxStudents(request.getMaxStudents());
        group = groupRepository.save(group);
        return toResponse(group);
    }

    @Transactional
    public void addStudent(UUID groupId, UUID childId) {
        Group group = findById(groupId);

        if (groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(groupId, childId)) {
            throw new ConflictException("Ребёнок уже в этой группе");
        }

        if (group.getMaxStudents() != null) {
            long currentCount = groupStudentRepository.countByGroupIdAndLeftAtIsNull(groupId);
            if (currentCount >= group.getMaxStudents()) {
                throw new BadRequestException("Группа заполнена (макс. " + group.getMaxStudents() + ")");
            }
        }

        if (!userRepository.existsById(childId)) {
            throw new NotFoundException("Ребёнок не найден");
        }

        GroupStudent gs = new GroupStudent();
        gs.setGroupId(groupId);
        gs.setChildId(childId);
        gs.setEnrolledAt(LocalDate.now());
        groupStudentRepository.save(gs);
    }

    @Transactional
    public void removeStudent(UUID groupId, UUID childId) {
        GroupStudent.GroupStudentId gsId = new GroupStudent.GroupStudentId(groupId, childId);
        GroupStudent gs = groupStudentRepository.findById(gsId)
                .orElseThrow(() -> new NotFoundException("Ребёнок не в этой группе"));
        gs.setLeftAt(LocalDate.now());
        groupStudentRepository.save(gs);
    }

    public Group findById(UUID id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Группа не найдена"));
    }

    private GroupResponse toResponse(Group group) {
        long studentCount = groupStudentRepository.countByGroupIdAndLeftAtIsNull(group.getId());

        return GroupResponse.builder()
                .id(group.getId())
                .courseId(group.getCourse().getId())
                .courseName(group.getCourse().getName())
                .branchId(group.getBranch().getId())
                .branchName(group.getBranch().getName())
                .teacherId(group.getTeacher().getId())
                .teacherName(group.getTeacher().getFullName())
                .scheduleDescription(group.getScheduleDescription())
                .maxStudents(group.getMaxStudents())
                .currentStudents((int) studentCount)
                .active(group.isActive())
                .build();
    }
}
