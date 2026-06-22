package com.hrmanagement.service;

import com.hrmanagement.dto.notification.NotificationDto;
import com.hrmanagement.entity.Notification;
import com.hrmanagement.enums.NotificationType;
import com.hrmanagement.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(Long recipientUserId, NotificationType type,
                                    String title, String message,
                                    String linkPath, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLinkPath(linkPath);
        notification.setRelatedEntityId(relatedEntityId);
        notificationRepository.save(notification);
    }

    @Transactional
    public void createNotificationForUsers(List<Long> recipientUserIds, NotificationType type,
                                            String title, String message,
                                            String linkPath, Long relatedEntityId) {
        for (Long userId : recipientUserIds) {
            createNotification(userId, type, title, message, linkPath, relatedEntityId);
        }
    }

    public Page<NotificationDto> getNotificationsForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDto);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long requestingUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!notification.getRecipientUserId().equals(requestingUserId)) {
            throw new AccessDeniedException("Cannot mark another user's notification as read");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByRecipientUserIdAndIsReadFalse(userId);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    private NotificationDto toDto(Notification n) {
        NotificationDto dto = new NotificationDto();
        dto.setId(n.getId());
        dto.setType(n.getType().name());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setLinkPath(n.getLinkPath());
        dto.setRelatedEntityId(n.getRelatedEntityId());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}
