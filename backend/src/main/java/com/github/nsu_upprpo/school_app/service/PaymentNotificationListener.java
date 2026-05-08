package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.event.*;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import com.github.nsu_upprpo.school_app.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentNotificationListener {

    private final ParentChildRepository parentChildRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCreated(PaymentCreatedEvent event) {
        for (ParentChild link : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    link.getParentId(),
                    NotificationType.PAYMENT,
                    "Необходимо оплатить обучение: " + event.amount() + " ₽",
                    event.paymentId(),
                    "PAYMENT"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentDueSoon(PaymentDueSoonEvent event) {
        for (ParentChild link : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    link.getParentId(),
                    NotificationType.PAYMENT,
                    "Скоро срок оплаты. Оплатите до " + event.dueDate(),
                    event.paymentId(),
                    "PAYMENT_DUE_SOON"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentOverdue(PaymentOverdueEvent event) {
        for (ParentChild link : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    link.getParentId(),
                    NotificationType.PAYMENT,
                    "Оплата просрочена",
                    event.paymentId(),
                    "PAYMENT_OVERDUE"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentConfirmed(PaymentConfirmedEvent event) {
        for (ParentChild link : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    link.getParentId(),
                    NotificationType.PAYMENT,
                    "Оплата подтверждена: " + event.amount() + " ₽",
                    event.paymentId(),
                    "PAYMENT_CONFIRMED"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentRejected(PaymentRejectedEvent event) {
        for (ParentChild link : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    link.getParentId(),
                    NotificationType.PAYMENT,
                    "Оплата не подтверждена: " + event.reason(),
                    event.paymentId(),
                    "PAYMENT_REJECTED"
            );
        }
    }
}
