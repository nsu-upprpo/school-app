package com.github.nsu_upprpo.school_app.model.dto.request;

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

    @NotNull(message = "ID ребёнка обязателен")
    private UUID childId;

    @NotNull(message = "ID группы обязателен")
    private UUID groupId;

    private String period;

    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    private BigDecimal amount;

    private LocalDate coversFrom;

    private LocalDate coversTo;
}
