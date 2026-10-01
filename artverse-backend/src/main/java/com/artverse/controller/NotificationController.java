package com.artverse.controller;

import com.artverse.dto.NotificationResponse;
import com.artverse.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.artverse.dto.UnreadCountResponse;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> getNotifications(Authentication authentication) {

        return notificationService.getMyNotifications(authentication);
    }
    @PutMapping("/{id}/read")
    public String markAsRead(@PathVariable Long id) {

        return notificationService.markAsRead(id);
    }
    @PutMapping("/read-all")
    public String markAllAsRead(Authentication authentication) {

        return notificationService.markAllAsRead(authentication);
    }
    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(Authentication authentication) {

        return notificationService.getUnreadCount(authentication);
    }
}