package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class GroupInfo {

    private final UUID groupId;
    private final String groupName;
    private final String courseName;

}
