
package com.github.nsu_upprpo.school_app.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.ParentChild;
import com.github.nsu_upprpo.school_app.model.event.PaymentConfirmedEvent;
import com.github.nsu_upprpo.school_app.model.event.PaymentCreatedEvent;
import com.github.nsu_upprpo.school_app.model.event.PaymentRejectedEvent;
import com.github.nsu_upprpo.school_app.repository.ParentChildRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaymentNotificationListenerTest {

    @Mock
    private ParentChildRepository parentChildRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PaymentNotificationListener listener;

    @Test
    void onPaymentCreated_sendsPaymentNotificationToParent() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(link));

        listener.onPaymentCreated(new PaymentCreatedEvent(
                SchoolSeedData.PAYMENT_ID,
                SchoolSeedData.STUDENT1_ID,
                new BigDecimal("4500.00"),
                LocalDate.of(2025, 9, 10)
        ));

        verify(notificationService).send(
                SchoolSeedData.PARENT_ID,
                NotificationType.PAYMENT,
                "Необходимо оплатить обучение: 4500.00 ₽",
                SchoolSeedData.PAYMENT_ID,
                "PAYMENT"
        );
    }

    @Test
    void onPaymentConfirmed_sendsConfirmedMessage() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(link));

        listener.onPaymentConfirmed(new PaymentConfirmedEvent(
                SchoolSeedData.PAYMENT_ID,
                SchoolSeedData.STUDENT1_ID,
                new BigDecimal("4500.00")
        ));

        verify(notificationService).send(
                SchoolSeedData.PARENT_ID,
                NotificationType.PAYMENT,
                "Оплата подтверждена: 4500.00 ₽",
                SchoolSeedData.PAYMENT_ID,
                "PAYMENT_CONFIRMED"
        );
    }

    @Test
    void onPaymentRejected_sendsRejectedMessage() {
        ParentChild link = new ParentChild(SchoolSeedData.PARENT_ID, SchoolSeedData.STUDENT1_ID);
        when(parentChildRepository.findByChildId(SchoolSeedData.STUDENT1_ID)).thenReturn(List.of(link));

        listener.onPaymentRejected(new PaymentRejectedEvent(
                SchoolSeedData.PAYMENT_ID,
                SchoolSeedData.STUDENT1_ID,
                "Чек нечитабелен"
        ));

        verify(notificationService).send(
                SchoolSeedData.PARENT_ID,
                NotificationType.PAYMENT,
                "Оплата не подтверждена: Чек нечитабелен",
                SchoolSeedData.PAYMENT_ID,
                "PAYMENT_REJECTED"
        );
    }
}
