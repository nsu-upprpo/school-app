package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.model.dto.request.MarkAttendanceRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.AttendanceResponse;
import com.github.nsu_upprpo.school_app.model.entity.Attendance;
import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonService lessonService;
    private final UserService userService;

    public List<AttendanceResponse> getByLesson(UUID lessonId) {
        return attendanceRepository.findByLessonId(lessonId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<AttendanceResponse> getByChild(UUID childId) {
        return attendanceRepository.findByChildId(childId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public AttendanceResponse mark(UUID lessonId, MarkAttendanceRequest request, UUID teacherId) {
        Lesson lesson = lessonService.findById(lessonId);
        User child = userService.findById(request.getChildId());
        User teacher = userService.findById(teacherId);

        if (attendanceRepository.existsByLessonIdAndChildId(lessonId, request.getChildId())) {
            throw new ConflictException("Посещаемость уже отмечена для этого ученика");
        }

        Attendance attendance = Attendance.builder()
                .lesson(lesson)
                .child(child)
                .markedBy(teacher)
                .markedAt(LocalDateTime.now())
                .status(request.getStatus())
                .build();
        attendance = attendanceRepository.save(attendance);
        return toResponse(attendance);
    }

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .lessonId(a.getLesson().getId())
                .rescheduledLessonId(a.getRescheduledLesson() != null ? a.getRescheduledLesson().getId() : null)
                .childId(a.getChild().getId())
                .childName(a.getChild().getFullName())
                .markedById(a.getMarkedBy() != null ? a.getMarkedBy().getId() : null)
                .markedAt(a.getMarkedAt())
                .status(a.getStatus())
                .build();
    }

}
