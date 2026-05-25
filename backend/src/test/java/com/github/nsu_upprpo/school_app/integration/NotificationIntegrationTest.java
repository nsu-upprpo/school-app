package com.github.nsu_upprpo.school_app.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.nsu_upprpo.school_app.model.entity.Notification;
import com.github.nsu_upprpo.school_app.model.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

class NotificationIntegrationTest extends BaseIntegrationTest {

    @Test
    void noAuthNotifications_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void parentGetAndMarkOwnNotification_flowWorks() throws Exception {
        User parent = testDataFactory.createParent(uniqueEmail("parent"));
        Notification notification = testDataFactory.createUnreadNotification(parent, "Оплата за сентябрь подтверждена");
        String token = loginAndGetAccessToken(parent.getEmail());

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$[*].id", hasItem(notification.getId().toString())));

        mockMvc.perform(get("/api/v1/notifications/unread")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(notification.getId().toString())));

        MvcResult markResult = mockMvc.perform(post("/api/v1/notifications/{id}/read", notification.getId())
                        .header("Authorization", "Bearer " + token))
                .andReturn();

        assertStatusIn(markResult, 200, 204);
    }
}
