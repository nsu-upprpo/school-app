package com.github.nsu_upprpo.school_app.integration;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    protected static final String TEST_PASSWORD = "password123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestDataFactory testDataFactory;

    protected String loginAndGetAccessToken(String email) throws Exception {
        return loginAndGetAccessToken(email, TEST_PASSWORD);
    }

    protected String loginAndGetAccessToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = readJson(result);
        assertTrue(json.hasNonNull("accessToken"), "Response must contain accessToken");
        return json.get("accessToken").asString();
    }

    protected String loginAndGetRefreshToken(String email) throws Exception {
        return loginAndGetRefreshToken(email, TEST_PASSWORD);
    }

    protected String loginAndGetRefreshToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", password
                        ))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = readJson(result);
        assertTrue(json.hasNonNull("refreshToken"), "Response must contain refreshToken");
        return json.get("refreshToken").asString();
    }

    protected JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    protected void assertStatusIn(MvcResult result, int... statuses) {
        int actual = result.getResponse().getStatus();
        for (int status : statuses) {
            if (actual == status) {
                return;
            }
        }
        throw new AssertionError("Unexpected status: " + actual);
    }

    protected String uniqueEmail(String prefix) {
        return prefix + "_" + System.nanoTime() + "@school.test";
    }

    protected String uniquePhone() {
        return "+7999" + ThreadLocalRandom.current().nextInt(100000, 999999);
    }

    protected String uniqueSuffix() {
        return String.valueOf(System.nanoTime());
    }

    protected String futureTimestamp(int year, int month, int day, int hour, int minute) {
        return LocalDateTime.of(year, month, day, hour, minute).toString();
    }
}
