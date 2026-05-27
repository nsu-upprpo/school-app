package com.github.nsu_upprpo.school_app.model.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID paymentId,
        UUID childId,
        BigDecimal amount,
        LocalDate dueDate
) {
}
