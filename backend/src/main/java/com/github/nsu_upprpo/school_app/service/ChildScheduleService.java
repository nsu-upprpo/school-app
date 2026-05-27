package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildScheduleLessonResponse;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import com.github.nsu_upprpo.school_app.model.entity.LessonParticipation;
import com.github.nsu_upprpo.school_app.model.entity.ParticipationStatus;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.LessonParticipationRepository;
import com.github.nsu_upprpo.school_app.repository.LessonRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ChildScheduleService {

    private static final int UPCOMING_HORIZON_DAYS = 90;

    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final LessonRepository lessonRepository;
    private final LessonParticipationRepository participationRepository;

    public List<ChildScheduleLessonResponse> getForChild(
            UUID parentId, UUID childId, LocalDateTime from, LocalDateTime to) {

        checkOwnership(parentId, childId);

        // 1. Активные группы ребёнка
        List<GroupStudent> memberships =
                groupStudentRepository.findByChildIdAndLeftAtIsNull(childId);
        if (memberships.isEmpty()) {
            return List.of();
        }
        Map<UUID, GroupStudent> membershipByGroup = memberships.stream()
                .collect(Collectors.toMap(GroupStudent::getGroupId, gs -> gs));

        // 2. Базовые уроки групп в окне + фильтр по периоду членства
        List<UUID> groupIds = new ArrayList<>(membershipByGroup.keySet());
        List<Lesson> baseLessons = lessonRepository
                .findByGroupIdInAndStartTimeBetween(groupIds, from, to).stream()
                .filter(l -> isWithinMembership(l, membershipByGroup.get(l.getGroup().getId())))
                .toList();

        // 3. Все участия ребёнка в окне
        List<LessonParticipation> participations =
                participationRepository.findByChildIdAndPeriod(childId, from, to);
        Map<UUID, LessonParticipation> overlayByLesson = participations.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p, (a, b) -> a));

        // 4. RESCHEDULED_IN на чужие занятия - их нет среди baseLessons
        Set<UUID> baseIds = baseLessons.stream().map(Lesson::getId).collect(Collectors.toSet());
        List<UUID> incomingLessonIds = participations.stream()
                .filter(p -> p.getStatus() == ParticipationStatus.RESCHEDULED_IN)
                .map(p -> p.getLesson().getId())
                .filter(id -> !baseIds.contains(id))
                .toList();
        List<Lesson> incomingLessons = incomingLessonIds.isEmpty()
                ? List.of()
                : lessonRepository.findByIdIn(incomingLessonIds);

        // 5. Склейка + сортировка
        return Stream.concat(baseLessons.stream(), incomingLessons.stream())
                .map(lesson -> toResponse(lesson, overlayByLesson.get(lesson.getId())))
                .sorted(Comparator.comparing(ChildScheduleLessonResponse::getStartTime))
                .toList();
    }

    public List<ChildScheduleLessonResponse> getUpcomingForChild(
            UUID parentId, UUID childId, int limit) {

        if (limit <= 0) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime horizon = now.plusDays(UPCOMING_HORIZON_DAYS);

        return getForChild(parentId, childId, now, horizon).stream()
                .filter(this::isActuallyAttending)
                .limit(limit)
                .toList();
    }

    private boolean isActuallyAttending(ChildScheduleLessonResponse r) {
        String s = r.getChildStatus();
        // s == null означает штатное участие
        return s == null
                || (!s.equals(ParticipationStatus.CANCELLED_BY_PARENT.name())
                && !s.equals(ParticipationStatus.RESCHEDULED_OUT.name())
                && !s.equals(ParticipationStatus.ABSENT.name()));
    }

    private boolean isWithinMembership(Lesson lesson, GroupStudent gs) {
        if (gs == null) {
            return false;
        }
        LocalDate day = lesson.getStartTime().toLocalDate();
        if (gs.getEnrolledAt() != null && day.isBefore(gs.getEnrolledAt())) {
            return false;
        }
        if (gs.getLeftAt() != null && day.isAfter(gs.getLeftAt())) {
            return false;
        }
        return true;
    }

    private void checkOwnership(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к данному ребёнку");
        }
    }

    private ChildScheduleLessonResponse toResponse(Lesson lesson, LessonParticipation overlay) {
        return ChildScheduleLessonResponse.builder()
                .lessonId(lesson.getId())
                .groupId(lesson.getGroup().getId())
                .projectId(lesson.getProject() != null ? lesson.getProject().getId() : null)
                .startTime(lesson.getStartTime())
                .endTime(lesson.getEndTime())
                .topic(lesson.getTopic())
                .lessonStatus(lesson.getStatus() != null ? lesson.getStatus().name() : null)
                .childStatus(overlay != null && overlay.getStatus() != null
                        ? overlay.getStatus().name() : null)
                .rescheduledToLessonId(overlay != null && overlay.getRescheduledTo() != null
                        ? overlay.getRescheduledTo().getId() : null)
                .rescheduledFromLessonId(overlay != null && overlay.getRescheduledFrom() != null
                        ? overlay.getRescheduledFrom().getId() : null)
                .reason(overlay != null ? overlay.getReason() : null)
                .build();
    }
}
