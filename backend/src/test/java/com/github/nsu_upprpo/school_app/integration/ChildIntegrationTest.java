package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.User;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ChildIntegrationTest extends BaseIntegrationTest {

    @Test
    void parentAddAndGetChild_flowWorks() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        String token = loginAndGetAccessToken(parent.getEmail());

        MvcResult createResult = mockMvc.perform(post("/api/v1/parents/me/children")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Ребёнок",
                                "lastName", "Тестовый",
                                "patronymic", "Иванович",
                                "birthDate", "2016-01-01",
                                "email", uniqueEmail("child"),
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String childId = readJson(createResult).get("id").asString();

        mockMvc.perform(get("/api/v1/parents/me/children")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].id", hasItem(childId)));

        mockMvc.perform(get("/api/v1/parents/me/children/{childId}", childId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(childId));
    }

    @Test
    void parentUpdateChild_returns200() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        String token = loginAndGetAccessToken(parent.getEmail());

        MvcResult createResult = mockMvc.perform(post("/api/v1/parents/me/children")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "СтароеИмя",
                                "lastName", "Ребёнок",
                                "patronymic", "Тестович",
                                "birthDate", "2016-01-01",
                                "email", uniqueEmail("child"),
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        String childId = readJson(createResult).get("id").asString();

        mockMvc.perform(put("/api/v1/parents/me/children/{childId}", childId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Обновлённый",
                                "lastName", "Ребёнок",
                                "patronymic", "Тестович",
                                "birthDate", "2016-01-01"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Обновлённый"));
    }
}
