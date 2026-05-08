package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RejectPaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import com.github.nsu_upprpo.school_app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createByAdmin(request));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) UUID groupId
    ) {
        return ResponseEntity.ok(paymentService.getForAdmin(status, groupId));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirm(@PathVariable UUID paymentId) {
        UUID adminId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.confirmByAdmin(paymentId, adminId));
    }

    @PostMapping("/{paymentId}/reject")
    public ResponseEntity<PaymentResponse> reject(
            @PathVariable UUID paymentId,
            @Valid @RequestBody RejectPaymentRequest request
    ) {
        return ResponseEntity.ok(paymentService.rejectByAdmin(paymentId, request));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancel(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(paymentService.cancelByAdmin(paymentId));
    }
}