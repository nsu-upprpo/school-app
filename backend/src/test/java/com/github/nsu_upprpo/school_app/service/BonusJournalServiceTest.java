
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.response.BonusJournalResponse;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.Project;
import com.github.nsu_upprpo.school_app.model.entity.ProjectGrade;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.LessonParticipationRepository;
import com.github.nsu_upprpo.school_app.repository.LessonRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectGradeRepository;
import com.github.nsu_upprpo.school_app.repository.ProjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BonusJournalServiceTest {

    @Mock
    private ParentChildRepository parentChildRepository;
    @Mock
    private GroupStudentRepository groupStudentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private LessonParticipationRepository attendanceRepository;
    @Mock
    private ProjectGradeRepository projectGradeRepository;

    @InjectMocks
    private BonusJournalService bonusJournalService;

    @Test
    void getForParentChild_throwsForbiddenException_whenParentHasNoAccess() {
        when(parentChildRepository.existsByParentIdAndChildId(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> bonusJournalService.getForParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID));
    }

    @Test
    void getForParentChild_returnsAggregatedJournalRow() {
        when(parentChildRepository.existsByParentIdAndChildId(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(true);

        GroupStudent groupStudent = new GroupStudent();
        groupStudent.setGroupId(SchoolSeedData.GROUP_GRAPHIC_ID);
        groupStudent.setChildId(SchoolSeedData.STUDENT1_ID);
        groupStudent.setEnrolledAt(LocalDate.of(2025, 9, 1));
        when(groupStudentRepository.findByChildIdAndLeftAtIsNull(SchoolSeedData.STUDENT1_ID))
                .thenReturn(List.of(groupStudent));

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);

        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(group.getCourse()).thenReturn(course);

        Project project = org.mockito.Mockito.mock(Project.class);
        when(project.getId()).thenReturn(SchoolSeedData.PROJECT_POSTER_ID);
        when(project.getName()).thenReturn("Осенний постер");
        when(project.getGroup()).thenReturn(group);
        when(project.getMaxScore()).thenReturn(100);

        when(projectRepository.findByGroupId(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(List.of(project));
        when(lessonRepository.countByProjectId(SchoolSeedData.PROJECT_POSTER_ID)).thenReturn(4L);
        when(attendanceRepository.countVisitedLessonsByChildAndProject(
                SchoolSeedData.STUDENT1_ID, SchoolSeedData.PROJECT_POSTER_ID)).thenReturn(3L);

        ProjectGrade grade = org.mockito.Mockito.mock(ProjectGrade.class);
        when(grade.getScore()).thenReturn(92);
        when(grade.getComment()).thenReturn("Отличная композиция");
        when(projectGradeRepository.findByProjectIdAndChildId(
                SchoolSeedData.PROJECT_POSTER_ID, SchoolSeedData.STUDENT1_ID)).thenReturn(Optional.of(grade));

        List<BonusJournalResponse> response =
                bonusJournalService.getForParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);

        assertEquals(1, response.size());
        assertEquals("Осенний постер", response.get(0).getProjectName());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.get(0).getCourseName());
        assertEquals(3, response.get(0).getVisitedLessons());
        assertEquals(1, response.get(0).getMissedLessons());
        assertEquals(92, response.get(0).getScore());
    }
}
