
package com.github.nsu_upprpo.school_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.model.dto.response.NotificationResponse;
import com.github.nsu_upprpo.school_app.model.entity.Notification;
import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.NotificationRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getUnreadByUser_mapsUnreadNotification() {
        Notification notification = org.mockito.Mockito.mock(Notification.class);
        when(notification.getId()).thenReturn(SchoolSeedData.NOTIFICATION_ID);
        when(notification.getType()).thenReturn(NotificationType.PAYMENT);
        when(notification.getMessageText()).thenReturn("Оплата за сентябрь подтверждена");
        when(notification.getReferenceId()).thenReturn(SchoolSeedData.PAYMENT_ID);
        when(notification.getReferenceType()).thenReturn("PAYMENT_CONFIRMED");
        when(notification.isRead()).thenReturn(false);
        when(notification.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 9, 1, 8, 30));

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(SchoolSeedData.PARENT_ID))
                .thenReturn(List.of(notification));

        List<NotificationResponse> response = notificationService.getUnreadByUser(SchoolSeedData.PARENT_ID);

        assertEquals(1, response.size());
        assertEquals("Оплата за сентябрь подтверждена", response.get(0).getMessageText());
        assertEquals(false, response.get(0).isRead());
    }

    @Test
    void markAsRead_throwsForbidden_whenNotificationBelongsToAnotherUser() {
        User otherUser = org.mockito.Mockito.mock(User.class);
        when(otherUser.getId()).thenReturn(SchoolSeedData.STUDENT1_ID);

        Notification notification = org.mockito.Mockito.mock(Notification.class);
        when(notification.getUser()).thenReturn(otherUser);
        when(notificationRepository.findById(SchoolSeedData.NOTIFICATION_ID)).thenReturn(Optional.of(notification));

        assertThrows(ForbiddenException.class,
                () -> notificationService.markAsRead(SchoolSeedData.NOTIFICATION_ID, SchoolSeedData.PARENT_ID));
    }

    @Test
    void send_savesNotificationForParent() {
        User parent = org.mockito.Mockito.mock(User.class);
        when(userService.findById(SchoolSeedData.PARENT_ID)).thenReturn(parent);

        notificationService.send(
                SchoolSeedData.PARENT_ID,
                NotificationType.PAYMENT,
                "Оплата за сентябрь подтверждена",
                SchoolSeedData.PAYMENT_ID,
                "PAYMENT_CONFIRMED"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertEquals("Оплата за сентябрь подтверждена", captor.getValue().getMessageText());
        assertEquals("PAYMENT_CONFIRMED", captor.getValue().getReferenceType());
    }
}
