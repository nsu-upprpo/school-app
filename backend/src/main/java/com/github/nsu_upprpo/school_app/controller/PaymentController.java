package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPaymentRequest(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentRequest(parentId, request));
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<List<PaymentResponse>> getByChild(@PathVariable UUID childId) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.getByChild(parentId, childId));
    }
}
