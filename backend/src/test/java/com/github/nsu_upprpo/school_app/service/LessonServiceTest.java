
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.BadRequestException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateLessonRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.LessonResponse;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.Lesson;
import com.github.nsu_upprpo.school_app.model.entity.LessonStatus;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.LessonRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private GroupService groupService;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private LessonService lessonService;

    @Test
    void create_throwsBadRequest_whenStartIsAfterEnd() {
        CreateLessonRequest request = org.mockito.Mockito.mock(CreateLessonRequest.class);
        when(request.getGroupId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(request.getProjectId()).thenReturn(SchoolSeedData.PROJECT_POSTER_ID);
        when(request.getStartTime()).thenReturn(SchoolSeedData.LESSON1_END);
        when(request.getEndTime()).thenReturn(SchoolSeedData.LESSON1_START);
        when(groupService.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(org.mockito.Mockito.mock(Group.class));
        when(projectRepository.findById(SchoolSeedData.PROJECT_POSTER_ID))
                .thenReturn(Optional.of(org.mockito.Mockito.mock(Project.class)));

        assertThrows(BadRequestException.class, () -> lessonService.create(request));
    }

    @Test
    void cancel_setsCancelledStatusAndReason() {
        Lesson lesson = org.mockito.Mockito.mock(Lesson.class);
        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(lesson.getId()).thenReturn(SchoolSeedData.LESSON_INTRO_ID);
        when(lesson.getGroup()).thenReturn(group);
        when(lesson.getProject()).thenReturn(null);
        when(lesson.getStartTime()).thenReturn(SchoolSeedData.LESSON1_START);
        when(lesson.getEndTime()).thenReturn(SchoolSeedData.LESSON1_END);
        when(lesson.getTopic()).thenReturn("Введение в проект");
        when(lesson.getStatus()).thenReturn(LessonStatus.CANCELLED);
        when(lesson.getCancelReason()).thenReturn("Преподаватель заболел");
        when(lessonRepository.findById(SchoolSeedData.LESSON_INTRO_ID)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        LessonResponse response = lessonService.cancel(SchoolSeedData.LESSON_INTRO_ID, "Преподаватель заболел");

        org.mockito.Mockito.verify(lesson).setStatus(LessonStatus.CANCELLED);
        org.mockito.Mockito.verify(lesson).setCancelReason("Преподаватель заболел");
        assertEquals("CANCELLED", response.getStatus());
        assertEquals("Преподаватель заболел", response.getCancelReason());
    }

    @Test
    void complete_setsCompletedStatus() {
        Lesson lesson = org.mockito.Mockito.mock(Lesson.class);
        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(lesson.getId()).thenReturn(SchoolSeedData.LESSON_COLOR_ID);
        when(lesson.getGroup()).thenReturn(group);
        when(lesson.getProject()).thenReturn(null);
        when(lesson.getStartTime()).thenReturn(SchoolSeedData.LESSON2_START);
        when(lesson.getEndTime()).thenReturn(SchoolSeedData.LESSON2_END);
        when(lesson.getTopic()).thenReturn("Работа с цветом");
        when(lesson.getStatus()).thenReturn(LessonStatus.COMPLETED);
        when(lessonRepository.findById(SchoolSeedData.LESSON_COLOR_ID)).thenReturn(Optional.of(lesson));
        when(lessonRepository.save(lesson)).thenReturn(lesson);

        LessonResponse response = lessonService.complete(SchoolSeedData.LESSON_COLOR_ID);

        org.mockito.Mockito.verify(lesson).setStatus(LessonStatus.COMPLETED);
        assertEquals("COMPLETED", response.getStatus());
    }
}
