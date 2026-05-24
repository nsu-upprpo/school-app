package com.github.nsu_upprpo.school_app.integration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class AuthIntegrationTest extends BaseIntegrationTest {

    @Test
    void register_returns201AndTokens() throws Exception {
        String email = uniqueEmail("parent");
        String phone = uniquePhone();

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Тест",
                                "lastName", "Регистрация",
                                "patronymic", "Постманович",
                                "email", email,
                                "phone", phone,
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = readJson(result);
        assertNotNull(json.get("accessToken"));
        assertNotNull(json.get("refreshToken"));
    }

    @Test
    void login_registeredParent_returns200AndTokens() throws Exception {
        String email = uniqueEmail("registered");
        String phone = uniquePhone();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Ирина",
                                "lastName", "Родитель",
                                "email", email,
                                "phone", phone,
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = readJson(loginResult);
        assertNotNull(json.get("accessToken"));
        assertNotNull(json.get("refreshToken"));
    }

    @Test
    void refresh_registeredParent_returns200() throws Exception {
        String email = uniqueEmail("refresh");
        String phone = uniquePhone();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Ирина",
                                "lastName", "Родитель",
                                "email", email,
                                "phone", phone,
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated());

        String refreshToken = loginAndGetRefreshToken(email);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "refreshToken", refreshToken
                        ))))
                .andExpect(status().isOk());
    }

    @Test
    void login_wrongPassword_returns4xx() throws Exception {
        String email = uniqueEmail("badlogin");
        String phone = uniquePhone();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "firstName", "Ирина",
                                "lastName", "Родитель",
                                "email", email,
                                "phone", phone,
                                "password", TEST_PASSWORD
                        ))))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "wrong-password"
                        ))))
                .andReturn();

        assertStatusIn(result, 400, 401, 403);
    }
}
