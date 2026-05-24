
package com.github.nsu_upprpo.school_app.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.event.AttendanceMarkedEvent;
import com.github.nsu_upprpo.school_app.model.event.ProjectGradeCreatedEvent;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private ParentChildRepository parentChildRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener listener;

    @Test
    void onProjectGradeCreated_sendsGradeNotificationToParent() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(link));

        listener.onProjectGradeCreated(new ProjectGradeCreatedEvent(
                SchoolSeedData.GRADE_ID,
                SchoolSeedData.PROJECT_POSTER_ID,
                SchoolSeedData.STUDENT1_ID,
                SchoolSeedData.TEACHER_ID,
                92
        ));

        verify(notificationService).send(
                SchoolSeedData.PARENT_ID,
                NotificationType.GRADE,
                "У ребенка появилась новая оценка за проект: 92",
                SchoolSeedData.GRADE_ID,
                "PROJECT_GRADE"
        );
    }

    @Test
    void onAttendanceMarked_sendsAttendanceNotificationToParent() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(link));

        listener.onAttendanceMarked(new AttendanceMarkedEvent(
                SchoolSeedData.ATTENDANCE_PRESENT_ID,
                SchoolSeedData.LESSON_INTRO_ID,
                SchoolSeedData.STUDENT1_ID,
                SchoolSeedData.TEACHER_ID,
                "PRESENT"
        ));

        verify(notificationService).send(
                SchoolSeedData.PARENT_ID,
                NotificationType.GRADE,
                "Ребенок отмечен на занятии: PRESENT",
                SchoolSeedData.ATTENDANCE_PRESENT_ID,
                "ATTENDANCE"
        );
    }
}
