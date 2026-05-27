package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.common.exception.ForbiddenException;
import com.github.nsu_upprpo.school_app.common.exception.NotFoundException;
import com.github.nsu_upprpo.school_app.model.dto.response.NotificationResponse;
import com.github.nsu_upprpo.school_app.model.entity.Notification;
import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import com.github.nsu_upprpo.school_app.model.entity.User;
import com.github.nsu_upprpo.school_app.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public List<NotificationResponse> getByUser(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadByUser(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Уведомление не найдено"));

        if (!n.getUser().getId().equals(userId)) {
            log.warn("Notification access denied [notificationId={}, requestUserId={}, ownerUserId={}]",
                    notificationId, userId, n.getUser().getId());
            throw new ForbiddenException("Нет доступа к этому уведомлению");
        }

        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void send(UUID userId, NotificationType type, String message, UUID referenceId, String referenceType) {
        User user = userService.findById(userId);
        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .messageText(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .build();
        notificationRepository.save(n);
        log.debug("Notification sent [userId={}, type={}, referenceType={}, referenceId={}]",
                userId, type, referenceType, referenceId);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType() != null ? n.getType().name() : null)
                .messageText(n.getMessageText())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}

