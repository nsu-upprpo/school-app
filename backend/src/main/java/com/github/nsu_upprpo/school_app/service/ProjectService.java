package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateProjectRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectGradeResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectResponse;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.model.entity.ProjectGrade;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.ProjectGradeRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectGradeRepository projectGradeRepository;
    private final UserService userService;
    private final GroupService groupService;

    public List<ProjectResponse> getByGroup(UUID groupId) {
        return projectRepository.findByGroupId(groupId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public ProjectResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        Group group = groupService.findById(request.getGroupId());
        Project project = Project.builder()
                .group(group)
                .name(request.getName())
                .totalLessons(request.getTotalLessons())
                .maxScore(request.getMaxScore())
                .status("ACTIVE")
                .build();
        project = projectRepository.save(project);
        return toResponse(project);
    }

    public List<ProjectGradeResponse> getGrades(UUID projectId) {
        return projectGradeRepository.findByProjectId(projectId).stream()
                .map(this::toGradeResponse).collect(Collectors.toList());
    }

    @Transactional
    public ProjectGradeResponse addGrade(UUID projectId, UUID childId, UUID teacherId, Integer score, String comment) {
        Project project = findById(projectId);
        User child = userService.findById(childId);
        User teacher = userService.findById(teacherId);

        ProjectGrade grade = ProjectGrade.builder()
                .project(project)
                .child(child)
                .teacher(teacher)
                .score(score)
                .comment(comment)
                .build();
        grade = projectGradeRepository.save(grade);
        return toGradeResponse(grade);
    }

    public Project findById(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Проект не найден"));
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .groupId(p.getGroup().getId())
                .name(p.getName())
                .totalLessons(p.getTotalLessons())
                .maxScore(p.getMaxScore())
                .status(p.getStatus())
                .build();
    }

    private ProjectGradeResponse toGradeResponse(ProjectGrade g) {
        return ProjectGradeResponse.builder()
                .id(g.getId())
                .projectId(g.getProject().getId())
                .childId(g.getChild().getId())
                .childName(g.getChild().getFullName())
                .teacherId(g.getTeacher().getId())
                .score(g.getScore())
                .comment(g.getComment())
                .build();
    }

}
