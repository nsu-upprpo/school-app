package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreateCourseRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.CourseResponse;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<CourseResponse> getAll() {
        return courseRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getById(UUID id) {
        return toResponse(findById(id));
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        Course course = Course.builder()
                .name(request.getName())
                .description(request.getDescription())
                .minAge(request.getMinAge())
                .maxAge(request.getMaxAge())
                .build();
        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public CourseResponse update(UUID id, CreateCourseRequest request) {
        Course course = findById(id);
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setMinAge(request.getMinAge());
        course.setMaxAge(request.getMaxAge());
        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Transactional
    public void delete(UUID id) {
        Course course = findById(id);
        course.setActive(false);
        courseRepository.save(course);
    }

    public Course findById(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Курс не найден"));
    }

    private CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .description(course.getDescription())
                .minAge(course.getMinAge())
                .maxAge(course.getMaxAge())
                .active(course.isActive())
                .build();
    }

}
