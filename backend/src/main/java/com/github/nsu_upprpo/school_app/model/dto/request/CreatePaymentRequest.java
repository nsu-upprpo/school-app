package com.github.nsu_upprpo.school_app.model.dto.request;

import com.github.nsu_upprpo.school_app.model.entity.PaymentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreatePaymentRequest {

    @NotNull
    private UUID childId;

    @NotNull
    private UUID groupId;

    @NotNull
    private PaymentType type;

    private String period;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private LocalDate coversFrom;

    private LocalDate coversTo;

    private LocalDate dueDate;

    private Integer lessonsCount;
}
