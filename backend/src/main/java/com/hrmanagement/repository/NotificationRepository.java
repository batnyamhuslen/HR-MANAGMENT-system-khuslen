package com.hrmanagement.repository;

import com.hrmanagement.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);
    long countByRecipientUserIdAndIsReadFalse(Long recipientUserId);
    List<Notification> findByRecipientUserIdAndIsReadFalse(Long recipientUserId);
}
