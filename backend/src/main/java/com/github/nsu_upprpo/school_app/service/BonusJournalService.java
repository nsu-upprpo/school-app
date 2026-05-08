package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.response.BonusJournalResponse;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.model.entity.ProjectGrade;
import com.github.nsu_upprpo.school_app.repository.AttendanceRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.LessonRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectGradeRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BonusJournalService {

    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final ProjectRepository projectRepository;
    private final LessonRepository lessonRepository;
    private final AttendanceRepository attendanceRepository;
    private final ProjectGradeRepository projectGradeRepository;

    public List<BonusJournalResponse> getForParentChild(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к этому ребёнку");
        }

        return groupStudentRepository.findByChildIdAndLeftAtIsNull(childId).stream()
                .flatMap(groupStudent -> projectRepository
                        .findByGroupId(groupStudent.getGroupId())
                        .stream()
                )
                .sorted(Comparator.comparing(Project::getName))
                .map(project -> toResponse(project, childId))
                .toList();
    }

    private BonusJournalResponse toResponse(Project project, UUID childId) {
        long totalLessons = lessonRepository.countByProjectId(project.getId());

        long visitedLessons = attendanceRepository.countVisitedLessonsByChildAndProject(
                childId,
                project.getId()
        );

        ProjectGrade grade = projectGradeRepository
                .findByProjectIdAndChildId(project.getId(), childId)
                .orElse(null);

        int total = Math.toIntExact(totalLessons);
        int visited = Math.toIntExact(visitedLessons);

        return BonusJournalResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())

                .groupId(project.getGroup().getId())
                .courseName(project.getGroup().getCourse().getName())

                .visitedLessons(visited)
                .totalLessons(total)
                .missedLessons(Math.max(total - visited, 0))

                .score(grade != null ? grade.getScore() : null)
                .maxScore(project.getMaxScore())
                .gradeComment(grade != null ? grade.getComment() : null)

                .build();
    }
}