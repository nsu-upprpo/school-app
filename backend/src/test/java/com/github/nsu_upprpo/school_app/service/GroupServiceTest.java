
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.github.nsu_upprpo.school_app.common.exception.ConflictException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateGroupRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.GroupResponse;
import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.GroupStudent;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.GroupRepository;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;
    @Mock
    private GroupStudentRepository groupStudentRepository;
    @Mock
    private CourseService courseService;
    @Mock
    private BranchService branchService;
    @Mock
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ParentChildRepository parentChildRepository;

    @InjectMocks
    private GroupService groupService;

    @Test
    void create_returnsGroupResponse_forGraphicDesignGroup() {
        CreateGroupRequest request = org.mockito.Mockito.mock(CreateGroupRequest.class);
        when(request.getCourseId()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_ID);
        when(request.getBranchId()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_ID);
        when(request.getTeacherId()).thenReturn(SchoolSeedData.TEACHER_ID);
        when(request.getScheduleDescription()).thenReturn("ПН, СР 10:00-11:30");
        when(request.getMaxStudents()).thenReturn(12);

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getId()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_ID);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);

        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branch.getId()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_ID);
        when(branch.getName()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_NAME);

        User teacher = org.mockito.Mockito.mock(User.class);
        when(teacher.getId()).thenReturn(SchoolSeedData.TEACHER_ID);
        when(teacher.getFullName()).thenReturn(SchoolSeedData.TEACHER_FULL_NAME);

        when(courseService.findById(SchoolSeedData.COURSE_GRAPHIC_ID)).thenReturn(course);
        when(branchService.findById(SchoolSeedData.BRANCH_CENTRAL_ID)).thenReturn(branch);
        when(userService.findById(SchoolSeedData.TEACHER_ID)).thenReturn(teacher);

        Group saved = org.mockito.Mockito.mock(Group.class);
        when(saved.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(saved.getCourse()).thenReturn(course);
        when(saved.getBranch()).thenReturn(branch);
        when(saved.getTeacher()).thenReturn(teacher);
        when(saved.getScheduleDescription()).thenReturn("ПН, СР 10:00-11:30");
        when(saved.getMaxStudents()).thenReturn(12);
        when(saved.isActive()).thenReturn(true);
        when(groupRepository.save(any(Group.class))).thenReturn(saved);
        when(groupStudentRepository.countByGroupIdAndLeftAtIsNull(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(2L);

        GroupResponse response = groupService.create(request);

        assertEquals(SchoolSeedData.GROUP_GRAPHIC_ID, response.getId());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.getCourseName());
        assertEquals(SchoolSeedData.BRANCH_CENTRAL_NAME, response.getBranchName());
        assertEquals(2, response.getCurrentStudents());
    }

    @Test
    void addStudent_throwsConflict_whenStudentAlreadyInGroup() {
        Group group = org.mockito.Mockito.mock(Group.class);
        when(groupRepository.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(Optional.of(group));
        when(groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(
                SchoolSeedData.GROUP_GRAPHIC_ID, SchoolSeedData.STUDENT1_ID)).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> groupService.addStudent(SchoolSeedData.GROUP_GRAPHIC_ID, SchoolSeedData.STUDENT1_ID));
    }

    @Test
    void getByParent_returnsActiveGroupForParentChildren() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByParentId(SchoolSeedData.PARENT_ID)).thenReturn(List.of(link));

        GroupStudent gs = new GroupStudent();
        gs.setGroupId(SchoolSeedData.GROUP_GRAPHIC_ID);
        gs.setChildId(SchoolSeedData.STUDENT1_ID);
        gs.setEnrolledAt(LocalDate.of(2025, 9, 1));
        when(groupStudentRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(gs));

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getId()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_ID);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);

        Branch branch = org.mockito.Mockito.mock(Branch.class);
        when(branch.getId()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_ID);
        when(branch.getName()).thenReturn(SchoolSeedData.BRANCH_CENTRAL_NAME);

        User teacher = org.mockito.Mockito.mock(User.class);
        when(teacher.getId()).thenReturn(SchoolSeedData.TEACHER_ID);
        when(teacher.getFullName()).thenReturn(SchoolSeedData.TEACHER_FULL_NAME);

        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getId()).thenReturn(SchoolSeedData.GROUP_GRAPHIC_ID);
        when(group.getCourse()).thenReturn(course);
        when(group.getBranch()).thenReturn(branch);
        when(group.getTeacher()).thenReturn(teacher);
        when(group.getScheduleDescription()).thenReturn("ПН, СР 10:00-11:30");
        when(group.getMaxStudents()).thenReturn(12);
        when(group.isActive()).thenReturn(true);

        when(groupRepository.findAllById(List.of(SchoolSeedData.GROUP_GRAPHIC_ID))).thenReturn(List.of(group));
        when(groupStudentRepository.countByGroupIdAndLeftAtIsNull(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(2L);

        List<GroupResponse> response = groupService.getByParent(SchoolSeedData.PARENT_ID);

        assertEquals(1, response.size());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.get(0).getCourseName());
    }
}
