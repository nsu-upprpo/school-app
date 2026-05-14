
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.model.dto.request.MarkAttendanceRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.AttendanceResponse;
import com.github.nsu_upprpo.school_app.model.entity.Attendance;
import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.model.event.AttendanceMarkedEvent;
import com.github.nsu_upprpo.school_app.repository.AttendanceRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private LessonService lessonService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void mark_throwsConflictException_whenAttendanceAlreadyExists() {
        MarkAttendanceRequest request = org.mockito.Mockito.mock(MarkAttendanceRequest.class);
        when(request.getChildId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(attendanceRepository.existsByLessonIdAndChildId(SchoolSeedData.LESSON_INTRO_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(true);

        Lesson lesson = org.mockito.Mockito.mock(Lesson.class);
        when(lessonService.findById(SchoolSeedData.LESSON_INTRO_ID)).thenReturn(lesson);
        when(userService.findById(SchoolSeedData.STUDENT1_ID)).thenReturn(org.mockito.Mockito.mock(User.class));
        when(userService.findById(SchoolSeedData.TEACHER_ID)).thenReturn(org.mockito.Mockito.mock(User.class));

        assertThrows(ConflictException.class,
                () -> attendanceService.mark(SchoolSeedData.LESSON_INTRO_ID, request, SchoolSeedData.TEACHER_ID));
    }

    @Test
    void mark_savesAttendanceAndPublishesEvent() {
        MarkAttendanceRequest request = org.mockito.Mockito.mock(MarkAttendanceRequest.class);
        when(request.getChildId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(request.getStatus()).thenReturn("PRESENT");

        Lesson lesson = org.mockito.Mockito.mock(Lesson.class);
        when(lesson.getId()).thenReturn(SchoolSeedData.LESSON_INTRO_ID);
        when(lessonService.findById(SchoolSeedData.LESSON_INTRO_ID)).thenReturn(lesson);

        User child = org.mockito.Mockito.mock(User.class);
        when(child.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(child.getFullName()).thenReturn(SchoolSeedData.STUDENT1_FULL_NAME);

        User teacher = org.mockito.Mockito.mock(User.class);
        when(teacher.getId()).thenReturn(SchoolSeedData.TEACHER_ID);

        when(userService.findById(SchoolSeedData.STUDENT1_ID)).thenReturn(child);
        when(userService.findById(SchoolSeedData.TEACHER_ID)).thenReturn(teacher);
        when(attendanceRepository.existsByLessonIdAndChildId(SchoolSeedData.LESSON_INTRO_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(false);

        Attendance saved = org.mockito.Mockito.mock(Attendance.class);
        when(saved.getId()).thenReturn(SchoolSeedData.ATTENDANCE_PRESENT_ID);
        when(saved.getLesson()).thenReturn(lesson);
        when(saved.getRescheduledLesson()).thenReturn(null);
        when(saved.getChild()).thenReturn(child);
        when(saved.getMarkedBy()).thenReturn(teacher);
        when(saved.getStatus()).thenReturn("PRESENT");
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(saved);

        AttendanceResponse response = attendanceService.mark(
                SchoolSeedData.LESSON_INTRO_ID, request, SchoolSeedData.TEACHER_ID);

        assertEquals(SchoolSeedData.ATTENDANCE_PRESENT_ID, response.getId());
        assertEquals("PRESENT", response.getStatus());

        ArgumentCaptor<AttendanceMarkedEvent> captor = ArgumentCaptor.forClass(AttendanceMarkedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertEquals(SchoolSeedData.LESSON_INTRO_ID, captor.getValue().lessonId());
        assertEquals(SchoolSeedData.STUDENT1_ID, captor.getValue().childId());
    }

    private static <T> T any(Class<T> clazz) {
        return org.mockito.ArgumentMatchers.any(clazz);
    }
}
