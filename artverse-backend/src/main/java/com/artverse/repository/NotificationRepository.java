package com.artverse.repository;

import com.artverse.entity.Notification;
import com.artverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);

    List<Notification> findByReceiverAndIsReadFalse(User receiver);

    long countByReceiverAndIsReadFalse(User receiver);
}