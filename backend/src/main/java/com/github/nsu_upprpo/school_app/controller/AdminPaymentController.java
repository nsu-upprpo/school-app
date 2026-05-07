package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPending() {
        return ResponseEntity.ok(paymentService.getPendingPayments());
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<PaymentResponse> confirm(@PathVariable UUID id) {
        UUID adminId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.confirm(id, adminId));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable UUID id) {
        return ResponseEntity.ok(paymentService.cancel(id));
    }
}
