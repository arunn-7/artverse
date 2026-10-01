package com.artverse.dto;

import java.time.LocalDateTime;

public class CommentResponse {

    private Long id;
    private String text;
    private String userName;
    private LocalDateTime createdAt;

    public CommentResponse() {
    }

    public CommentResponse(Long id,
                           String text,
                           String userName,
                           LocalDateTime createdAt) {
        this.id = id;
        this.text = text;
        this.userName = userName;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}