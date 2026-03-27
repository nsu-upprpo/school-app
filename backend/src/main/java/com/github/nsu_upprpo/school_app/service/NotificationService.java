package com.github.nsu_upprpo.school_app.service;

import com.github.nsu_upprpo.school_app.model.entity.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    public Object getByUser(UUID userId, int page, int size) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public long getUnreadCount(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAsRead(UUID notificationId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void markAllAsRead(UUID userId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void create(UUID userId, NotificationType type, String title, String message) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
