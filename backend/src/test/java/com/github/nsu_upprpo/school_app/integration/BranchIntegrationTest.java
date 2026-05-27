package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.Branch;
import com.github.nsu_upprpo.school_app.model.entity.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class BranchIntegrationTest extends BaseIntegrationTest {

    @Test
    void parentGetAllBranches_returnsCreatedBranch() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/branches")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].id", hasItem(branch.getId().toString())));
    }

    @Test
    void parentGetBranchById_returns200() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        Branch branch = testDataFactory.createBranch(uniqueSuffix());
        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/branches/{id}", branch.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(branch.getId().toString()));
    }

    @Test
    void adminCreateUpdateDeleteBranch_flowWorks() throws Exception {
        User admin = testDataFactory.createAdmin(uniqueEmail("admin"));
        String token = loginAndGetAccessToken(admin.getEmail());

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/branches")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Левобережный " + uniqueSuffix(),
                                "city", "Новосибирск",
                                "address", "ул. Кирова, 50",
                                "phone", "+73839876543"
                        ))))
                .andReturn();

        assertStatusIn(createResult, 201, 200);
        JsonNode created = readJson(createResult);
        String branchId = created.get("id").asString();

        mockMvc.perform(put("/api/v1/admin/branches/{id}", branchId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Обновлённый филиал",
                                "city", "Новосибирск",
                                "address", "ул. Советская, 10",
                                "phone", "+73830000000"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Обновлённый филиал"));

        mockMvc.perform(delete("/api/v1/admin/branches/{id}", branchId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }
}
