package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class GroupIntegrationTest extends BaseIntegrationTest {

    @Test
    void teacherGetGroups_returnsAssignedGroup() throws Exception {
        User teacher = testDataFactory.createTeacher(uniqueEmail("teacher"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        Course course = testDataFactory.createCourse(uniqueSuffix());
        Group group = testDataFactory.createGroup(teacher, branch, course, uniqueSuffix());
        String token = loginAndGetAccessToken(teacher.getEmail());

        mockMvc.perform(get("/api/v1/teachers/me/groups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].id", hasItem(group.getId().toString())));
    }

    @Test
    void adminCreateGroup_returnsCreatedGroup() throws Exception {
        User admin = testDataFactory.createAdmin(uniqueEmail("admin"));
        User teacher = testDataFactory.createTeacher(uniqueEmail("teacher"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        Course course = testDataFactory.createCourse(uniqueSuffix());
        String token = loginAndGetAccessToken(admin.getEmail());

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/groups")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "teacherId", teacher.getId(),
                                "branchId", branch.getId(),
                                "courseId", course.getId(),
                                "scheduleDescription", "ВТ, ЧТ 16:00-17:30",
                                "maxStudents", 10
                        ))))
                .andReturn();

        assertStatusIn(createResult, 201, 200);
        JsonNode created = readJson(createResult);
        String groupId = created.get("id").asText();

        mockMvc.perform(get("/api/v1/admin/groups")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(groupId)));
    }
}
