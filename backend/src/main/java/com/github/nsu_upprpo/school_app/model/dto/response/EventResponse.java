package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class EventResponse {
    private final UUID id;
    private final String name;
    private final String description;
    private final UUID branchId;
    private final String branchName;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String location;
}
