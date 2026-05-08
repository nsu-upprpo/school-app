package com.github.nsu_upprpo.school_app.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectPaymentRequest {

    @NotBlank
    private String reason;
}