package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    public PaymentResponse create(CreatePaymentRequest request, UUID parentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public List<PaymentResponse> getByParent(UUID parentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void confirmPayment(UUID paymentId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
