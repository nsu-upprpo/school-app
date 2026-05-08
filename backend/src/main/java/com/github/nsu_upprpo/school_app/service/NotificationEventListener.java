package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.event.AttendanceMarkedEvent;
import com.github.nsu_upprpo.school_app.model.event.PaymentConfirmedEvent;
import com.github.nsu_upprpo.school_app.model.event.ProjectGradeCreatedEvent;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final ParentChildRepository parentChildRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onProjectGradeCreated(ProjectGradeCreatedEvent event) {
        for (ParentChild parentChild : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    parentChild.getParentId(),
                    NotificationType.GRADE,
                    "У ребенка появилась новая оценка за проект: " + event.score(),
                    event.gradeId(),
                    "PROJECT_GRADE"
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttendanceMarked(AttendanceMarkedEvent event) {
        for (ParentChild parentChild : parentChildRepository.findByChildId(event.childId())) {
            notificationService.send(
                    parentChild.getParentId(),
                    NotificationType.GRADE,
                    "Ребенок отмечен на занятии: " + event.status(),
                    event.attendanceId(),
                    "ATTENDANCE"
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
                    "PAYMENT"
            );
        }
    }

}
