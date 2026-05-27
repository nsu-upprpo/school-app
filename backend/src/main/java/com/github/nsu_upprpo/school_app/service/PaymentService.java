package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.request.CreatePaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.request.RejectPaymentRequest;
import com.github.nsu_upprpo.school_app.model.dto.response.PaymentResponse;
import com.github.nsu_upprpo.school_app.model.entity.*;
import com.github.nsu_upprpo.school_app.model.event.*;
import com.github.nsu_upprpo.school_app.repository.GroupStudentRepository;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final ParentChildRepository parentChildRepository;
    private final GroupStudentRepository groupStudentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public PaymentResponse createByAdmin(CreatePaymentRequest request) {
        User child = userService.findById(request.getChildId());
        Group group = groupService.findById(request.getGroupId());

        if (!groupStudentRepository.existsByGroupIdAndChildIdAndLeftAtIsNull(
                request.getGroupId(),
                request.getChildId()
        )) {
            log.warn("Payment creation rejected: child not in group [childId={}, groupId={}]",
                    request.getChildId(), request.getGroupId());
            throw new ForbiddenException("Ребёнок не состоит в этой группе");
        }

        Payment payment = Payment.builder()
                .child(child)
                .group(group)
                .type(request.getType())
                .period(request.getPeriod())
                .amount(request.getAmount())
                .status(PaymentStatus.UNPAID)
                .coversFrom(request.getCoversFrom())
                .coversTo(request.getCoversTo())
                .dueDate(request.getDueDate())
                .lessonsCount(request.getLessonsCount())
                .build();

        payment = paymentRepository.save(payment);
        log.info("Payment created [paymentId={}, childId={}, groupId={}, amount={}, dueDate={}]",
                payment.getId(), child.getId(), group.getId(), payment.getAmount(), payment.getDueDate());

        eventPublisher.publishEvent(new PaymentCreatedEvent(
                payment.getId(),
                payment.getChild().getId(),
                payment.getAmount(),
                payment.getDueDate()
        ));

        return toResponse(payment);
    }

    public List<PaymentResponse> getForParent(UUID parentId, PaymentStatus status) {
        List<Payment> payments = parentChildRepository.findByParentId(parentId).stream()
                .flatMap(link -> {
                    if (status == null) {
                        return paymentRepository
                                .findByChildIdOrderByCreatedAtDesc(link.getChildId())
                                .stream();
                    }

                    return paymentRepository
                            .findByChildIdAndStatusOrderByCreatedAtDesc(link.getChildId(), status)
                            .stream();
                })
                .toList();

        return payments.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PaymentResponse> getForParentChild(UUID parentId, UUID childId, PaymentStatus status) {
        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            throw new ForbiddenException("Нет доступа к этому ребёнку");
        }

        List<Payment> payments = status == null
                ? paymentRepository.findByChildIdOrderByCreatedAtDesc(childId)
                : paymentRepository.findByChildIdAndStatusOrderByCreatedAtDesc(childId, status);

        return payments.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<PaymentResponse> getForAdmin(PaymentStatus status, UUID groupId) {
        List<Payment> payments;

        if (groupId != null && status != null) {
            payments = paymentRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, status);
        } else if (groupId != null) {
            payments = paymentRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        } else if (status != null) {
            payments = paymentRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            payments = paymentRepository.findAll();
        }

        return payments.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public PaymentResponse submitByParent(UUID parentId, UUID paymentId) {
        Payment payment = findById(paymentId);

        UUID childId = payment.getChild().getId();

        if (!parentChildRepository.existsByParentIdAndChildId(parentId, childId)) {
            log.warn("Payment submission rejected: parent has no access [parentId={}, paymentId={}, childId={}]",
                    parentId, paymentId, childId);
            throw new ForbiddenException("Нет доступа к этой оплате");
        }

        if (payment.getStatus() != PaymentStatus.UNPAID
                && payment.getStatus() != PaymentStatus.OVERDUE
                && payment.getStatus() != PaymentStatus.REJECTED) {
            log.warn("Payment submission rejected: illegal status transition [paymentId={}, currentStatus={}]",
                    paymentId, payment.getStatus());
            throw new IllegalStateException("Эту оплату нельзя отправить на подтверждение");
        }

        payment.setStatus(PaymentStatus.PENDING_CONFIRMATION);
        payment.setSubmittedAt(LocalDateTime.now());
        payment.setRejectionReason(null);

        payment = paymentRepository.save(payment);
        log.info("Payment submitted for confirmation [paymentId={}, parentId={}]", paymentId, parentId);

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse confirmByAdmin(UUID paymentId, UUID adminId) {
        Payment payment = findById(paymentId);
        User admin = userService.findById(adminId);

        if (payment.getStatus() != PaymentStatus.PENDING_CONFIRMATION) {
            log.warn("Payment confirmation rejected: illegal status [paymentId={}, currentStatus={}]",
                    paymentId, payment.getStatus());
            throw new IllegalStateException("Подтвердить можно только оплату в статусе PENDING_CONFIRMATION");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setConfirmedAt(LocalDateTime.now());
        payment.setConfirmedBy(admin);
        payment.setRejectionReason(null);

        payment = paymentRepository.save(payment);
        log.info("Payment confirmed [paymentId={}, adminId={}, amount={}]",
                paymentId, adminId, payment.getAmount());

        eventPublisher.publishEvent(new PaymentConfirmedEvent(
                payment.getId(),
                payment.getChild().getId(),
                payment.getAmount()
        ));

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse rejectByAdmin(UUID paymentId, RejectPaymentRequest request) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING_CONFIRMATION) {
            log.warn("Payment rejection refused: illegal status [paymentId={}, currentStatus={}]",
                    paymentId, payment.getStatus());
            throw new IllegalStateException("Отклонить можно только оплату в статусе PENDING_CONFIRMATION");
        }

        payment.setStatus(PaymentStatus.REJECTED);
        payment.setRejectionReason(request.getReason());

        payment = paymentRepository.save(payment);
        log.info("Payment rejected [paymentId={}, reason={}]", paymentId, request.getReason());

        eventPublisher.publishEvent(new PaymentRejectedEvent(
                payment.getId(),
                payment.getChild().getId(),
                request.getReason()
        ));

        return toResponse(payment);
    }

    @Transactional
    public PaymentResponse cancelByAdmin(UUID paymentId) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("Payment cancellation refused: payment already paid [paymentId={}]", paymentId);
            throw new IllegalStateException("Оплаченное начисление нельзя отменить");
        }

        payment.setStatus(PaymentStatus.CANCELLED);

        payment = paymentRepository.save(payment);
        log.info("Payment cancelled [paymentId={}]", paymentId);

        return toResponse(payment);
    }

    @Transactional
    public void notifyDueSoonPayments() {
        LocalDate targetDate = LocalDate.now().plusDays(3);

        List<Payment> payments = paymentRepository
                .findByStatusAndDueDateOrderByCreatedAtDesc(PaymentStatus.UNPAID, targetDate);

        int notified = 0;
        for (Payment payment : payments) {
            if (payment.getDueSoonNotifiedAt() != null) {
                continue;
            }

            payment.setDueSoonNotifiedAt(LocalDateTime.now());

            eventPublisher.publishEvent(new PaymentDueSoonEvent(
                    payment.getId(),
                    payment.getChild().getId(),
                    payment.getDueDate()
            ));
            notified++;
        }
        if (notified > 0) {
            log.info("Due-soon payment notifications dispatched [count={}, dueDate={}]", notified, targetDate);
        }
    }

    @Transactional
    public void markAndNotifyOverduePayments() {
        List<Payment> payments = paymentRepository
                .findByStatusAndDueDateBeforeOrderByCreatedAtDesc(PaymentStatus.UNPAID, LocalDate.now());

        int marked = 0;
        for (Payment payment : payments) {
            if (payment.getOverdueNotifiedAt() != null) {
                continue;
            }

            payment.setStatus(PaymentStatus.OVERDUE);
            payment.setOverdueNotifiedAt(LocalDateTime.now());

            eventPublisher.publishEvent(new PaymentOverdueEvent(
                    payment.getId(),
                    payment.getChild().getId()
            ));
            marked++;
        }
        if (marked > 0) {
            log.info("Payments marked overdue [count={}]", marked);
        }
    }

    private Payment findById(UUID paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Оплата не найдена"));
    }

    private PaymentResponse toResponse(Payment payment) {
        User confirmedBy = payment.getConfirmedBy();

        return PaymentResponse.builder()
                .id(payment.getId())

                .childId(payment.getChild().getId())
                .childName(payment.getChild().getFullName())

                .groupId(payment.getGroup().getId())
                .groupName(payment.getGroup().getCourse().getName())

                .type(payment.getType())
                .period(payment.getPeriod())
                .amount(payment.getAmount())
                .status(payment.getStatus())

                .coversFrom(payment.getCoversFrom())
                .coversTo(payment.getCoversTo())
                .dueDate(payment.getDueDate())
                .lessonsCount(payment.getLessonsCount())

                .submittedAt(payment.getSubmittedAt())
                .confirmedAt(payment.getConfirmedAt())

                .confirmedById(confirmedBy != null ? confirmedBy.getId() : null)
                .confirmedByName(confirmedBy != null ? confirmedBy.getFullName() : null)

                .rejectionReason(payment.getRejectionReason())

                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}