package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.BadRequestException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateLessonRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.LessonPeriodRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonTemplateResponse;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import com.github.nsu_upprpo.school_app.model.entity.LessonStatus;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.LessonRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonService {

    private static final int RECENT_LESSONS_LIMIT = 3;

    private final LessonRepository lessonRepository;
    private final GroupService groupService;
    private final ProjectRepository projectRepository;
    private final GroupRepository groupRepository;

    public List<LessonResponse> getByGroup(UUID groupId) {
        return lessonRepository.findByGroupIdOrderByStartTimeAsc(groupId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<LessonResponse> getByPeriod(UUID groupId, LessonPeriodRequest request) {
        LocalDateTime from = request.getFromDate();
        LocalDateTime to = request.getToDate();
        return lessonRepository.findByGroupIdAndStartTimeBetween(groupId, from, to).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public LessonResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public LessonResponse create(CreateLessonRequest request) {
        Group group = groupService.findById(request.getGroupId());
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new NotFoundException("Проект не найден"));
        }
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Время начала должно быть раньше времени окончания");
        }

        Lesson lesson = Lesson.builder()
                .group(group)
                .project(project)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .topic(request.getTopic())
                .status(LessonStatus.PLANNED)
                .build();
        lesson = lessonRepository.save(lesson);
        return toResponse(lesson);
    }

    @Transactional
    public LessonResponse cancel(UUID id, String reason) {
        Lesson lesson = findById(id);
        lesson.setStatus(LessonStatus.CANCELLED);
        lesson.setCancelReason(reason);
        lesson = lessonRepository.save(lesson);
        return toResponse(lesson);
    }

    @Transactional
    public LessonResponse complete(UUID id) {
        Lesson lesson = findById(id);
        lesson.setStatus(LessonStatus.COMPLETED);
        lesson = lessonRepository.save(lesson);
        return toResponse(lesson);
    }

    public Lesson findById(UUID id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Урок не найден"));
    }

    public LessonTemplateResponse getTemplateForTeacher(UUID teacherId) {
        List<Group> groups = groupRepository.findByTeacherIdAndActiveTrue(teacherId);

        List<LessonTemplateResponse.GroupTemplate> groupTemplates = groups.stream()
                .map(this::buildGroupTemplate)
                .collect(Collectors.toList());

        return LessonTemplateResponse.builder()
                .groups(groupTemplates)
                .build();
    }

    private LessonTemplateResponse.GroupTemplate buildGroupTemplate(Group group) {
        List<Lesson> recentLessons = lessonRepository.findByGroupId(group.getId()).stream()
                .sorted(Comparator.comparing(Lesson::getStartTime).reversed())
                .limit(RECENT_LESSONS_LIMIT)
                .collect(Collectors.toList());

        List<LessonTemplateResponse.RecentLessonTime> recentTimes = recentLessons.stream()
                .map(l -> LessonTemplateResponse.RecentLessonTime.builder()
                        .startTime(l.getStartTime().plusWeeks(1))
                        .endTime(l.getEndTime().plusWeeks(1))
                        .build())
                .collect(Collectors.toList());

        List<String> recentTopics = recentLessons.stream()
                .map(Lesson::getTopic)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .collect(Collectors.toList());

        List<LessonTemplateResponse.ProjectOption> projects = projectRepository.findByGroupId(group.getId()).stream()
                .map(p -> LessonTemplateResponse.ProjectOption.builder()
                        .projectId(p.getId())
                        .name(p.getName())
                        .status(p.getStatus())
                        .build())
                .collect(Collectors.toList());

        return LessonTemplateResponse.GroupTemplate.builder()
                .groupId(group.getId())
                .courseName(group.getCourse().getName())
                .scheduleDescription(group.getScheduleDescription())
                .recentLessonTimes(recentTimes)
                .projects(projects)
                .recentTopics(recentTopics)
                .build();
    }

    private LessonResponse toResponse(Lesson lesson) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .projectId(lesson.getProject() != null ? lesson.getProject().getId() : null)
                .groupId(lesson.getGroup().getId())
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .topic(lesson.getTopic())
                .status(lesson.getStatus() != null ? lesson.getStatus().name() : null)
                .cancelReason(lesson.getCancelReason())
                .build();
    }
}
