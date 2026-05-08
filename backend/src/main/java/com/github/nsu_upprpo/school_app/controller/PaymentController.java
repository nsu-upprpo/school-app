package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.common.util.SecurityUtils;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.model.entity.PaymentStatus;
import com.github.nsu_upprpo.school_app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> myPayments(
            @RequestParam(required = false) PaymentStatus status
    ) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.getForParent(parentId, status));
    }

    @GetMapping("/children/{childId}")
    public ResponseEntity<List<PaymentResponse>> childPayments(
            @PathVariable UUID childId,
            @RequestParam(required = false) PaymentStatus status
    ) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.getForParentChild(parentId, childId, status));
    }

    @PostMapping("/{paymentId}/submit")
    public ResponseEntity<PaymentResponse> submitPayment(@PathVariable UUID paymentId) {
        UUID parentId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(paymentService.submitByParent(parentId, paymentId));
    }
}
