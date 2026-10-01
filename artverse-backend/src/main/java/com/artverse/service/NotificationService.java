package com.artverse.service;

import com.artverse.dto.NotificationResponse;
import com.artverse.entity.Notification;
import com.artverse.entity.NotificationType;
import com.artverse.entity.User;
import com.artverse.exception.UserNotFoundException;
import com.artverse.repository.NotificationRepository;
import com.artverse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.artverse.dto.UnreadCountResponse;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void createNotification(User receiver,
                                   User sender,
                                   String title,
                                   String message,
                                   NotificationType type) {

        Notification notification = new Notification();

        notification.setReceiver(receiver);
        notification.setSender(sender);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);

        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getMyNotifications(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user)
                .stream()
                .map(notification -> new NotificationResponse(
                        notification.getId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getType().name(),
                        notification.getSender() != null
                                ? notification.getSender().getFullName()
                                : "ArtVerse",
                        notification.getSender() != null
                                ? notification.getSender().getProfileImageUrl()
                                : null,
                        notification.isRead(),
                        notification.getCreatedAt()
                ))
                .toList();
    }
    public String markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setRead(true);

        notificationRepository.save(notification);

        return "Notification marked as read";
    }
    public String markAllAsRead(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Notification> notifications =
                notificationRepository.findByReceiverAndIsReadFalse(user);

        for (Notification notification : notifications) {
            notification.setRead(true);
        }

        notificationRepository.saveAll(notifications);

        return "All notifications marked as read";
    }
    public UnreadCountResponse getUnreadCount(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        long count = notificationRepository.countByReceiverAndIsReadFalse(user);

        return new UnreadCountResponse(count);
    }
}