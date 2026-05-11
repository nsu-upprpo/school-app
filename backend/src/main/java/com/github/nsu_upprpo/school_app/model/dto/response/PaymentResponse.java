package com.github.nsu_upprpo.school_app.model.dto.response;

import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import com.github.nsu_upprpo.school_app.model.entity.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PaymentResponse {

    private UUID id;

    private UUID childId;
    private String childName;

    private UUID groupId;
    private String groupName;

    private PaymentType type;
    private String period;
    private BigDecimal amount;
    private PaymentStatus status;

    private LocalDate coversFrom;
    private LocalDate coversTo;
    private LocalDate dueDate;

    private Integer lessonsCount;

    private LocalDateTime submittedAt;
    private LocalDateTime confirmedAt;

    private UUID confirmedById;
    private String confirmedByName;

    private String rejectionReason;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}