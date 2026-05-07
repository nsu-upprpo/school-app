package com.github.nsu_upprpo.school_app.model.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private final UUID id;

    private final UUID childId;
    private final String childName;

    private final UUID groupId;
    private final String groupName;

    private final String period;
    private final BigDecimal amount;
    private final String status;

    private final LocalDate coversFrom;
    private final LocalDate coversTo;

    private final LocalDateTime paidAt;
    private final LocalDateTime createdAt;

}
