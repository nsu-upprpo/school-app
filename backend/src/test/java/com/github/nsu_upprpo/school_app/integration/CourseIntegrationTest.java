package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class CourseIntegrationTest extends BaseIntegrationTest {

    @Test
    void parentGetAllCourses_returnsCreatedCourse() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        Course course = testDataFactory.createCourse(uniqueSuffix());
        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/courses")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].id", hasItem(course.getId().toString())));
    }

    @Test
    void parentGetCourseById_returns200() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        Course course = testDataFactory.createCourse(uniqueSuffix());
        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/courses/{id}", course.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(course.getId().toString()));
    }

    @Test
    void adminCreateDeleteCourse_flowWorks() throws Exception {
        User admin = testDataFactory.createAdmin(uniqueEmail("admin"));
        String token = loginAndGetAccessToken(admin.getEmail());

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/courses")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Архитектурный дизайн " + uniqueSuffix(),
                                "description", "Макетирование и пространственное мышление",
                                "minAge", 10,
                                "maxAge", 16
                        ))))
                .andReturn();

        assertStatusIn(createResult, 201, 200);
        JsonNode created = readJson(createResult);
        String courseId = created.get("id").asText();

        mockMvc.perform(delete("/api/v1/admin/courses/{id}", courseId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
