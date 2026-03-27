package com.github.nsu_upprpo.school_app.controller;

import com.github.nsu_upprpo.school_app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<?> createPayment() {
        // TODO: создать платёж
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyPayments() {
        // TODO: мои платежи
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPayment(@PathVariable UUID id) {
        // TODO: детали платежа
        return ResponseEntity.ok().build();
    }

    @GetMapping("/prices")
    public ResponseEntity<?> getPrices(
            @RequestParam UUID courseId,
            @RequestParam UUID branchId) {
        // TODO: цены
        return ResponseEntity.ok().build();
    }
}
