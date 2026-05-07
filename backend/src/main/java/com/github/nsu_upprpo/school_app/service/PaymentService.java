package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.model.entity.*;
import com.github.nsu_upprpo.school_app.model.event.PaymentConfirmedEvent;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public PaymentResponse createPaymentRequest(UUID parentId, CreatePaymentRequest request) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, request.getChildId())) {
            throw new ForbiddenException("Нет доступа к этому ребёнку");
        }

        if (!groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(
                request.getGroupId(),
                request.getChildId()
        )) {
            throw new ForbiddenException("Ребёнок не состоит в этой группе");
        }

        User child = userService.findById(request.getChildId());
        Group group = groupService.findById(request.getGroupId());

        Payment payment = Payment.builder()
                .child(child)
                .group(group)
                .period(request.getPeriod())
                .amount(request.getAmount())
                .coversFrom(request.getCoversFrom())
                .coversTo(request.getCoversTo())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);

        return toResponse(payment);
    }

    public List<PaymentResponse> getByChild(UUID parentId, UUID childId) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к этому ребёнку");
        }

        return paymentRepository.findByChildIdOrderByCreatedAtDesc(childId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentResponse confirm(UUID paymentId, UUID adminId) {
        Payment payment = findById(paymentId);

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        applicationEventPublisher.publishEvent(new PaymentConfirmedEvent(
                payment.getId(),
                payment.getChild().getId(),
                payment.getAmount()
        ));

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse cancel(UUID paymentId) {
        Payment payment = findById(paymentId);
        payment.setStatus(PaymentStatus.CANCELLED);
        payment = paymentRepository.save(payment);
        return toResponse(payment);
    }

    private Payment findById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Оплата не найдена"));
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .childId(p.getChild().getId())
                .childName(p.getChild().getFullName())
                .groupId(p.getGroup().getId())
                .groupName(p.getGroup().getCourse().getName())
                .period(p.getPeriod())
                .amount(p.getAmount())
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .coversFrom(p.getCoversFrom())
                .coversTo(p.getCoversTo())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
