
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.model.dto.request.CreateProjectRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectGradeResponse;
import com.github.nsu_upprpo.school_app.model.dto.response.ProjectResponse;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.model.entity.ProjectGrade;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.model.event.ProjectGradeCreatedEvent;
import com.github.nsu_upprpo.school_app.repository.ProjectGradeRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectGradeRepository projectGradeRepository;
    @Mock
    private UserService userService;
    @Mock
    private GroupService groupService;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void create_returnsProjectResponse_forAutumnPoster() {
        CreateProjectRequest request = org.mockito.Mockito.mock(CreateProjectRequest.class);
        when(request.getGroupId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(request.getName()).thenReturn("Осенний постер");
        when(request.getTotalLessons()).thenReturn(4);
        when(request.getMaxScore()).thenReturn(100);

        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(groupService.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(group);

        Project project = org.mockito.Mockito.mock(Project.class);
        when(project.getId()).thenReturn(SchoolSeedData.PROJECT_POSTER_ID);
        when(project.getGroup()).thenReturn(group);
        when(project.getName()).thenReturn("Осенний постер");
        when(project.getTotalLessons()).thenReturn(4);
        when(project.getMaxScore()).thenReturn(100);
        when(project.getStatus()).thenReturn("ACTIVE");
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.create(request);

        assertEquals(SchoolSeedData.PROJECT_POSTER_ID, response.getId());
        assertEquals("Осенний постер", response.getName());
        assertEquals(100, response.getMaxScore());
    }

    @Test
    void addGrade_savesGradeAndPublishesEvent() {
        Project project = org.mockito.Mockito.mock(Project.class);
        when(project.getId()).thenReturn(SchoolSeedData.PROJECT_POSTER_ID);
        when(projectRepository.findById(SchoolSeedData.PROJECT_POSTER_ID)).thenReturn(Optional.of(project));

        User child = org.mockito.Mockito.mock(User.class);
        when(child.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(child.getFullName()).thenReturn(SchoolSeedData.STUDENT1_FULL_NAME);

        User teacher = org.mockito.Mockito.mock(User.class);
        when(teacher.getId()).thenReturn(SchoolSeedData.TEACHER_ID);

        when(userService.findById(SchoolSeedData.STUDENT1_ID)).thenReturn(child);
        when(userService.findById(SchoolSeedData.TEACHER_ID)).thenReturn(teacher);

        ProjectGrade grade = org.mockito.Mockito.mock(ProjectGrade.class);
        when(grade.getId()).thenReturn(SchoolSeedData.GRADE_ID);
        when(grade.getProject()).thenReturn(project);
        when(grade.getChild()).thenReturn(child);
        when(grade.getTeacher()).thenReturn(teacher);
        when(grade.getScore()).thenReturn(92);
        when(grade.getComment()).thenReturn("Отличная композиция");
        when(projectGradeRepository.save(any(ProjectGrade.class))).thenReturn(grade);

        ProjectGradeResponse response = projectService.addGrade(
                SchoolSeedData.PROJECT_POSTER_ID, SchoolSeedData.STUDENT1_ID, SchoolSeedData.TEACHER_ID,
                92, "Отличная композиция");

        assertEquals(SchoolSeedData.GRADE_ID, response.getId());
        assertEquals(92, response.getScore());
        assertEquals("Отличная композиция", response.getComment());

        ArgumentCaptor<ProjectGradeCreatedEvent> captor = ArgumentCaptor.forClass(ProjectGradeCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertEquals(SchoolSeedData.PROJECT_POSTER_ID, captor.getValue().projectId());
        assertEquals(SchoolSeedData.STUDENT1_ID, captor.getValue().childId());
    }
}
