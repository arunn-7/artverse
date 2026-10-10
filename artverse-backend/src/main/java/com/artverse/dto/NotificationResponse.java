package com.artverse.dto;

import java.time.LocalDateTime;

public class NotificationResponse {

    private String id;
    private String title;
    private String message;
    private String type;
    private String senderName;
    private String senderProfileImage;
    private boolean read;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(
            String id,
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderProfileImage() {
        return senderProfileImage;
    }

    public void setSenderProfileImage(String senderProfileImage) {
        this.senderProfileImage = senderProfileImage;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}