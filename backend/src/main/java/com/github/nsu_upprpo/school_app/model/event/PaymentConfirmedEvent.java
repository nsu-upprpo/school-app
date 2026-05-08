package com.github.nsu_upprpo.school_app.model.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID paymentId,
        UUID childId,
        BigDecimal amount
) {
}
