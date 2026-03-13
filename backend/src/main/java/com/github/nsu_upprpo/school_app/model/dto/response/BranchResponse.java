package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class BranchResponse {

    private final UUID id;
    private final String name;
    private final String city;
    private final String address;
    private final String phone;
    private final boolean active;

}
