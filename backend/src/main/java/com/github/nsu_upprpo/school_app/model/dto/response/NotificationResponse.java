package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class NotificationResponse {
    private final UUID id;
    private final String type;
    private final String messageText;
    private final UUID referenceId;
    private final String referenceType;
    private final boolean read;
    private final LocalDateTime createdAt;
}
