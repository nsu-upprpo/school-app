
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateChildRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.ChildResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChildServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ParentChildRepository parentChildRepository;
    @Mock
    private GroupStudentRepository groupStudentRepository;
    @Mock
    private GroupRepository groupRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ChildService childService;

    @Test
    void addChild_createsStudentAndParentLink() {
        CreateChildRequest request = org.mockito.Mockito.mock(CreateChildRequest.class);
        when(request.getFirstName()).thenReturn("Маша");
        when(request.getLastName()).thenReturn("Ученик");
        when(request.getPatronymic()).thenReturn(null);
        when(request.getBirthDate()).thenReturn(LocalDate.of(2015, 6, 15));
        when(request.getEmail()).thenReturn(SchoolSeedData.STUDENT1_EMAIL);
        when(request.getPassword()).thenReturn("12345678");

        when(passwordEncoder.encode("12345678")).thenReturn("encoded");

        User savedChild = org.mockito.Mockito.mock(User.class);
        when(savedChild.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(savedChild.getFirstName()).thenReturn("Маша");
        when(savedChild.getLastName()).thenReturn("Ученик");
        when(savedChild.getPatronymic()).thenReturn(null);
        when(savedChild.getBirthDate()).thenReturn(LocalDate.of(2015, 6, 15));
        when(userRepository.save(any(User.class))).thenReturn(savedChild);

        ChildResponse response = childService.addChild(SchoolSeedData.PARENT_ID, request);

        assertEquals(SchoolSeedData.STUDENT1_ID, response.getId());
        assertEquals("Маша", response.getFirstName());
        assertEquals(0, response.getGroups().size());

        ArgumentCaptor<ParentChild> linkCaptor = ArgumentCaptor.forClass(ParentChild.class);
        org.mockito.Mockito.verify(parentChildRepository).save(linkCaptor.capture());
        assertEquals(SchoolSeedData.PARENT_ID, linkCaptor.getValue().getParentId());
        assertEquals(SchoolSeedData.STUDENT1_ID, linkCaptor.getValue().getChildId());
    }

    @Test
    void getChildById_throwsForbiddenException_whenParentHasNoAccess() {
        when(parentChildRepository.existsByParentIdAndChildId(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID))
                .thenReturn(false);

        assertThrows(ForbiddenException.class,
                () -> childService.getChildById(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID));
    }

    @Test
    void getChildrenByParent_returnsOnlyActiveChildrenWithGroups() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByParentId(SchoolSeedData.PARENT_ID)).thenReturn(List.of(link));

        User child = org.mockito.Mockito.mock(User.class);
        when(child.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);
        when(child.getFirstName()).thenReturn("Маша");
        when(child.getLastName()).thenReturn("Ученик");
        when(child.getPatronymic()).thenReturn(null);
        when(child.getBirthDate()).thenReturn(LocalDate.of(2015, 6, 15));
        when(child.isActive()).thenReturn(true);
        when(userRepository.findAllById(List.of(SchoolSeedData.STUDENT1_ID))).thenReturn(List.of(child));

        GroupStudent groupStudent = new GroupStudent();
        groupStudent.setGroupId(SchoolSeedData.GROUP_GRAPHIC_ID);
        groupStudent.setChildId(SchoolSeedData.STUDENT1_ID);
        when(groupStudentRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(groupStudent));

        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);
        Group group = org.mockito.Mockito.mock(Group.class);
        when(group.getCourse()).thenReturn(course);
        when(groupRepository.findById(SchoolSeedData.GROUP_GRAPHIC_ID)).thenReturn(Optional.of(group));

        List<ChildResponse> response = childService.getChildrenByParent(SchoolSeedData.PARENT_ID);

        assertEquals(1, response.size());
        assertEquals("Маша", response.get(0).getFirstName());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.get(0).getGroups().get(0).getCourseName());
    }
}
