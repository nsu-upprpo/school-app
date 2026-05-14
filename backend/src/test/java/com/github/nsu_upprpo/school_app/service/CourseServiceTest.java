
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateCourseRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.CourseResponse;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private CourseService courseService;

    @Test
    void getAll_returnsGraphicDesignAndArchitectureDesign() {
        Course graphic = mockCourse(SchoolSeedData.COURSE_GRAPHIC_ID,
                SchoolSeedData.COURSE_GRAPHIC_NAME, 7, 14, true);
        Course architecture = mockCourse(SchoolSeedData.COURSE_ARCH_ID,
                SchoolSeedData.COURSE_ARCH_NAME, 10, 16, true);
        when(courseRepository.findByActiveTrue()).thenReturn(List.of(graphic, architecture));

        List<CourseResponse> response = courseService.getAll();

        assertEquals(2, response.size());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.get(0).getName());
        assertEquals(SchoolSeedData.COURSE_ARCH_NAME, response.get(1).getName());
    }

    @Test
    void getById_throwsNotFoundException_whenCourseDoesNotExist() {
        when(courseRepository.findById(SchoolSeedData.COURSE_GRAPHIC_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> courseService.getById(SchoolSeedData.COURSE_GRAPHIC_ID));
    }

    @Test
    void create_returnsSavedCourseResponse() {
        CreateCourseRequest request = org.mockito.Mockito.mock(CreateCourseRequest.class);
        when(request.getName()).thenReturn(SchoolSeedData.COURSE_GRAPHIC_NAME);
        when(request.getDescription()).thenReturn("Основы графики, композиции и цвета");
        when(request.getMinAge()).thenReturn(7);
        when(request.getMaxAge()).thenReturn(14);

        Course savedCourse = mockCourse(SchoolSeedData.COURSE_GRAPHIC_ID,
                SchoolSeedData.COURSE_GRAPHIC_NAME, 7, 14, true);
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);

        CourseResponse response = courseService.create(request);

        assertEquals(SchoolSeedData.COURSE_GRAPHIC_ID, response.getId());
        assertEquals(SchoolSeedData.COURSE_GRAPHIC_NAME, response.getName());
        verify(courseRepository).save(any(Course.class));
    }

    private Course mockCourse(java.util.UUID id, String name, Integer minAge, Integer maxAge, boolean active) {
        Course course = org.mockito.Mockito.mock(Course.class);
        when(course.getId()).thenReturn(id);
        when(course.getName()).thenReturn(name);
        when(course.getDescription()).thenReturn("Описание " + name);
        when(course.getMinAge()).thenReturn(minAge);
        when(course.getMaxAge()).thenReturn(maxAge);
        when(course.isActive()).thenReturn(active);
        return course;
    }
}
