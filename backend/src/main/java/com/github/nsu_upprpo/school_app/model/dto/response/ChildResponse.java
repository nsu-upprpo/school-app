package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ChildResponse {

    private final UUID id;
    private final String firstName;
    private final String lastName;
    private final String patronymic;
    private final LocalDate birthDate;
    private final List<GroupInfo> groups;

}
