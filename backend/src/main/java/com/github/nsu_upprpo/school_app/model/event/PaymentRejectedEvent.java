package com.github.nsu_upprpo.school_app.model.event;

import java.util.UUID;

public record PaymentRejectedEvent(
        UUID paymentId,
        UUID childId,
        String reason
) {
}
