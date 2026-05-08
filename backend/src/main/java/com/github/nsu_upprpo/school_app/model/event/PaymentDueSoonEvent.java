package com.github.nsu_upprpo.school_app.model.event;

import java.time.LocalDate;
import java.util.UUID;

public record PaymentDueSoonEvent(
        UUID paymentId,
        UUID childId,
        LocalDate dueDate
) {
}
