package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.BadRequestException;
import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.MarkAttendanceRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.AttendanceResponse;
import com.github.nsu_upprpo.school_app.model.entity.*;
import com.github.nsu_upprpo.school_app.model.event.AttendanceMarkedEvent;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.LessonParticipationRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LessonParticipationService {

    private final LessonParticipationRepository participationRepository;
    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final LessonService lessonService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public List<AttendanceResponse> getAttendanceByLesson(UUID lessonId) {
        return participationRepository.findByLessonId(lessonId).stream()
                .filter(p -> p.getStatus() != null && p.getStatus().isTeacherMark())
                .map(this::toAttendanceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse markByTeacher(UUID lessonId, MarkAttendanceRequest request, UUID teacherId) {
        Lesson lesson = lessonService.findById(lessonId);
        User child = userService.findById(request.getChildId());
        User teacher = userService.findById(teacherId);

        ParticipationStatus status = parseTeacherStatus(request.getStatus());

        Optional<LessonParticipation> existing =
                participationRepository.findByLessonIdAndChildId(lessonId, request.getChildId());

        LessonParticipation p = existing.orElseGet(LessonParticipation::new);
        p.setLesson(lesson);
        p.setChild(child);
        p.setStatus(status);
        p.setChangedBy(teacher);
        p.setChangedAt(LocalDateTime.now());

        p = participationRepository.save(p);
        log.info("Attendance marked by teacher [lessonId={}, childId={}, teacherId={}, status={}]",
                lesson.getId(), child.getId(), teacher.getId(), status);

        eventPublisher.publishEvent(new AttendanceMarkedEvent(
                p.getId(),
                lesson.getId(),
                child.getId(),
                teacher.getId(),
                status.name()
        ));

        return toAttendanceResponse(p);
    }

    @Transactional
    public void cancelByParent(UUID parentId, UUID childId, UUID lessonId, String reason) {
        checkOwnership(parentId, childId);
        Lesson lesson = lessonService.findById(lessonId);
        ensureLessonModifiable(lesson);
        ensureChildInGroup(childId, lesson.getGroup().getId());

        Optional<LessonParticipation> existing =
                participationRepository.findByLessonIdAndChildId(lessonId, childId);
        if (existing.isPresent()) {
            ParticipationStatus s = existing.get().getStatus();
            if (s == ParticipationStatus.CANCELLED_BY_PARENT) {
                throw new ConflictException("Занятие уже отменено для этого ребёнка");
            }
            if (s == ParticipationStatus.RESCHEDULED_OUT) {
                throw new ConflictException("Занятие уже перенесено — сначала отмените перенос");
            }
            if (s == ParticipationStatus.RESCHEDULED_IN) {
                throw new ConflictException("Ребёнок переведён на это занятие — сначала отмените перенос");
            }
            if (s.isTeacherMark()) {
                throw new ConflictException("Занятие уже прошло, посещение отмечено учителем");
            }
        }

        User child = userService.findById(childId);
        User parent = userService.findById(parentId);

        LessonParticipation p = LessonParticipation.builder()
                .lesson(lesson)
                .child(child)
                .status(ParticipationStatus.CANCELLED_BY_PARENT)
                .reason(reason)
                .changedBy(parent)
                .changedAt(LocalDateTime.now())
                .build();
        participationRepository.save(p);
        log.info("Lesson cancelled by parent [parentId={}, childId={}, lessonId={}]",
                parentId, childId, lessonId);

        notifyTeacher(lesson, child,
                "Родитель отменил занятие для ребёнка " + child.getFullName());
    }

    @Transactional
    public void restoreByParent(UUID parentId, UUID childId, UUID lessonId) {
        checkOwnership(parentId, childId);
        Lesson lesson = lessonService.findById(lessonId);
        ensureLessonModifiable(lesson);

        LessonParticipation existing = participationRepository
                .findByLessonIdAndChildId(lessonId, childId)
                .orElseThrow(() -> new NotFoundException(
                        "Нечего восстанавливать — ребёнок участвует в занятии штатно"));

        ParticipationStatus s = existing.getStatus();

        if (s == ParticipationStatus.CANCELLED_BY_PARENT) {
            participationRepository.delete(existing);
            log.info("Lesson cancellation restored by parent [parentId={}, childId={}, lessonId={}]",
                    parentId, childId, lessonId);
            notifyTeacher(lesson, existing.getChild(),
                    "Родитель восстановил занятие для ребёнка " + existing.getChild().getFullName());
            return;
        }

        if (s == ParticipationStatus.RESCHEDULED_OUT) {
            // Сносим парную RESCHEDULED_IN на целевом занятии
            Lesson target = existing.getRescheduledTo();
            if (target != null) {
                participationRepository.findByLessonIdAndChildId(target.getId(), childId)
                        .filter(p -> p.getStatus() == ParticipationStatus.RESCHEDULED_IN)
                        .ifPresent(participationRepository::delete);
            }
            participationRepository.delete(existing);
            log.info("Lesson reschedule undone by parent [parentId={}, childId={}, sourceLessonId={}]",
                    parentId, childId, lessonId);
            notifyTeacher(lesson, existing.getChild(),
                    "Родитель отменил перенос занятия для ребёнка " + existing.getChild().getFullName());
            return;
        }

        if (s == ParticipationStatus.RESCHEDULED_IN) {
            throw new ConflictException("Это занятие — цель переноса. Отмените перенос с исходного занятия.");
        }

        // Учительские отметки восстановлению родителем не подлежат.
        throw new ConflictException("Нельзя восстановить занятие в статусе " + s);
    }

    @Transactional
    public void rescheduleByParent(UUID parentId, UUID childId, UUID sourceLessonId, UUID targetLessonId,
                                   String reason) {
        if (sourceLessonId.equals(targetLessonId)) {
            throw new BadRequestException("Целевое занятие совпадает с исходным");
        }
        checkOwnership(parentId, childId);

        Lesson source = lessonService.findById(sourceLessonId);
        Lesson target = lessonService.findById(targetLessonId);
        ensureLessonModifiable(source);
        ensureLessonModifiable(target);
        ensureChildInGroup(childId, source.getGroup().getId());

        if (!source.getGroup().getCourse().getId().equals(target.getGroup().getCourse().getId())) {
            throw new BadRequestException("Перенос возможен только между занятиями одного курса");
        }

        // Проверки занятости обоих слотов.
        participationRepository.findByLessonIdAndChildId(sourceLessonId, childId)
                .ifPresent(p -> {
                    throw new ConflictException("У ребёнка уже есть статус на исходном занятии: " + p.getStatus());
                });
        participationRepository.findByLessonIdAndChildId(targetLessonId, childId)
                .ifPresent(p -> {
                    throw new ConflictException(
                            "У ребёнка уже есть статус на целевом занятии: " + p.getStatus());
                });

        ensureTargetHasCapacity(target);

        User child = userService.findById(childId);
        User parent = userService.findById(parentId);
        LocalDateTime now = LocalDateTime.now();

        LessonParticipation out = LessonParticipation.builder()
                .lesson(source)
                .child(child)
                .status(ParticipationStatus.RESCHEDULED_OUT)
                .rescheduledTo(target)
                .reason(reason)
                .changedBy(parent)
                .changedAt(now)
                .build();

        LessonParticipation in = LessonParticipation.builder()
                .lesson(target)
                .child(child)
                .status(ParticipationStatus.RESCHEDULED_IN)
                .rescheduledFrom(source)
                .reason(reason)
                .changedBy(parent)
                .changedAt(now)
                .build();

        participationRepository.save(out);
        participationRepository.save(in);
        log.info("Lesson rescheduled by parent [parentId={}, childId={}, fromLessonId={}, toLessonId={}]",
                parentId, childId, sourceLessonId, targetLessonId);

        notifyTeacher(source, child,
                "Родитель перенёс ребёнка " + child.getFullName() + " с этого занятия");
        notifyTeacher(target, child,
                "Родитель перевёл на это занятие ребёнка " + child.getFullName());
    }

    private void checkOwnership(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к данному ребёнку");
        }
    }

    private void ensureChildInGroup(UUID childId, UUID groupId) {
        if (!groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(groupId, childId)) {
            throw new ForbiddenException("Ребёнок не состоит в группе этого занятия");
        }
    }

    private void ensureLessonModifiable(Lesson lesson) {
        if (lesson.getStatus() != null && lesson.getStatus() != LessonStatus.PLANNED) {
            throw new ConflictException(
                    "Изменения возможны только для запланированных занятий (статус: "
                            + lesson.getStatus() + ")");
        }
        if (!lesson.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Нельзя изменять прошедшие или текущие занятия");
        }
    }

    private void ensureTargetHasCapacity(Lesson target) {
        Integer max = target.getGroup().getMaxStudents();
        if (max == null) return;

        long baseStudents = groupStudentRepository.countByGroupIdAndLeftAtIsNull(target.getGroup().getId());
        List<LessonParticipation> existing = participationRepository.findByLessonId(target.getId());
        long incoming = existing.stream()
                .filter(p -> p.getStatus() == ParticipationStatus.RESCHEDULED_IN).count();
        long outgoing = existing.stream()
                .filter(p -> p.getStatus() == ParticipationStatus.RESCHEDULED_OUT
                        || p.getStatus() == ParticipationStatus.CANCELLED_BY_PARENT).count();

        long projected = baseStudents - outgoing + incoming;
        if (projected >= max) {
            throw new ConflictException("На целевом занятии нет свободных мест (макс. " + max + ")");
        }
    }

    private ParticipationStatus parseTeacherStatus(String raw) {
        try {
            ParticipationStatus s = ParticipationStatus.valueOf(raw);
            if (!s.isTeacherMark()) {
                throw new BadRequestException("Статус " + raw + " недопустим для отметки посещения");
            }
            return s;
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Допустимые значения статуса: PRESENT, ABSENT, LATE, EXCUSED");
        }
    }

    private void notifyTeacher(Lesson lesson, User child, String message) {
        UUID teacherId = lesson.getGroup().getTeacher().getId();
        notificationService.send(
                teacherId,
                NotificationType.LESSON,
                message,
                lesson.getId(),
                "LESSON"
        );
    }

    private AttendanceResponse toAttendanceResponse(LessonParticipation p) {
        return AttendanceResponse.builder()
                .id(p.getId())
                .lessonId(p.getLesson().getId())
                .rescheduledLessonId(p.getRescheduledTo() != null ? p.getRescheduledTo().getId() : null)
                .childId(p.getChild().getId())
                .childName(p.getChild().getFullName())
                .markedById(p.getChangedBy() != null ? p.getChangedBy().getId() : null)
                .markedAt(p.getChangedAt())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .build();
    }
}
