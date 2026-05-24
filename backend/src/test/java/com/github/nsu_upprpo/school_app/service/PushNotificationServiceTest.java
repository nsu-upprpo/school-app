
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PushNotificationServiceTest {

    @Test
    void service_canBeCreated() {
        assertNotNull(new PushNotificationService());
    }
}
