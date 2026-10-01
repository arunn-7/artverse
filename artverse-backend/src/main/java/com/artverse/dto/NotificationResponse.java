package com.artverse.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Long id;
    private String title;
    private String message;
    private String type;

    private String senderName;
    private String senderProfileImage;

    private boolean read;

    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id,
                                String title,
                                String message,
                                String type,
                                String senderName,
                                String senderProfileImage,
                                boolean read,
                                LocalDateTime createdAt) {

        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.senderName = senderName;
        this.senderProfileImage = senderProfileImage;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }

    public String getTitle() { return title; }

    public String getMessage() { return message; }

    public String getType() { return type; }

    public String getSenderName() { return senderName; }

    public String getSenderProfileImage() { return senderProfileImage; }

    public boolean isRead() { return read; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRead(boolean read) {
        this.read = read;
    }
}