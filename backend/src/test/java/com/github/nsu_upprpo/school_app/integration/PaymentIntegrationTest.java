package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.Course;
import com.github.nsu_upprpo.school_app.model.entity.Group;
import com.github.nsu_upprpo.school_app.model.entity.User;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class PaymentIntegrationTest extends BaseIntegrationTest {

    @Test
    void parentGetAllPayments_returnsCreatedPayment() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        User teacher = testDataFactory.createTeacher(uniqueEmail("teacher"));
        User child = testDataFactory.createChild(uniqueEmail("child"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        Course course = testDataFactory.createCourse(uniqueSuffix());
        Group group = testDataFactory.createGroup(teacher, branch, course, uniqueSuffix());
        testDataFactory.linkParentAndChild(parent, child);
        testDataFactory.enroll(group, child);
        testDataFactory.createUnpaidPayment(child, group, LocalDate.now().plusDays(7));

        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/payments")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].childId", hasItem(child.getId().toString())));
    }

    @Test
    void adminCreateParentSubmitAndAdminConfirmPayment_flowWorks() throws Exception {
        User admin = testDataFactory.createAdmin(uniqueEmail("admin"));
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        User teacher = testDataFactory.createTeacher(uniqueEmail("teacher"));
        User child = testDataFactory.createChild(uniqueEmail("child"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        Course course = testDataFactory.createCourse(uniqueSuffix());
        Group group = testDataFactory.createGroup(teacher, branch, course, uniqueSuffix());
        testDataFactory.linkParentAndChild(parent, child);
        testDataFactory.enroll(group, child);

        String adminToken = loginAndGetAccessToken(admin.getEmail());
        String parentToken = loginAndGetAccessToken(parent.getEmail());

        Map<String, Object> createBody = new LinkedHashMap<>();
        createBody.put("childId", child.getId());
        createBody.put("groupId", group.getId());
        createBody.put("type", "MONTH");
        createBody.put("period", "2027-10");
        createBody.put("amount", 5500.0);
        createBody.put("coversFrom", "2027-10-01");
        createBody.put("coversTo", "2027-10-31");
        createBody.put("dueDate", LocalDate.now().plusDays(10).toString());

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/payments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andReturn();

        assertStatusIn(createResult, 201, 200);
        JsonNode created = readJson(createResult);
        String paymentId = created.get("id").asString();
        assertNotNull(paymentId);
        org.junit.jupiter.api.Assertions.assertEquals("UNPAID", created.get("status").asString());

        mockMvc.perform(post("/api/v1/payments/{paymentId}/submit", paymentId)
                        .header("Authorization", "Bearer " + parentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_CONFIRMATION"));

        mockMvc.perform(get("/api/v1/admin/payments")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "PENDING_CONFIRMATION"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/payments/{paymentId}/confirm", paymentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
