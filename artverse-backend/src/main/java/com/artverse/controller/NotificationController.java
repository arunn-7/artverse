package com.artverse.controller;

import com.artverse.dto.NotificationResponse;
import com.artverse.dto.UnreadCountResponse;
import com.artverse.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(
            Authentication authentication) {
        return notificationService.getMyNotifications(authentication);
    }

    @PutMapping("/{id}/read")
    public String markAsRead(
            @PathVariable String id,
            Authentication authentication) {
        return notificationService.markAsRead(id, authentication);
    }

    @PutMapping("/read-all")
    public String markAllAsRead(Authentication authentication) {
        return notificationService.markAllAsRead(authentication);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(
            Authentication authentication) {
        return notificationService.getUnreadCount(authentication);
    }
}