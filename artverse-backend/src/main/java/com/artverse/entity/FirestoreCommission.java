package com.artverse.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FirestoreCommission {

    private String id;
    private String clientUid;
    private String clientEmail;
    private String clientName;
    private String title;
    private String description;
    private String category;
    private BigDecimal budget;
    private Integer requiredDays;
    private LocalDateTime deadline;
    private CommissionStatus status = CommissionStatus.OPEN;
    private CommissionPaymentStatus paymentStatus =
            CommissionPaymentStatus.PENDING;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FirestoreCommission() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClientUid() { return clientUid; }
    public void setClientUid(String clientUid) { this.clientUid = clientUid; }
    public String getClientEmail() { return clientEmail; }
    public void setClientEmail(String clientEmail) { this.clientEmail = clientEmail; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getBudget() { return budget; }
    public void setBudget(BigDecimal budget) { this.budget = budget; }
    public Integer getRequiredDays() { return requiredDays; }
    public void setRequiredDays(Integer requiredDays) { this.requiredDays = requiredDays; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public CommissionStatus getStatus() { return status; }
    public void setStatus(CommissionStatus status) { this.status = status; }
    public CommissionPaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(CommissionPaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }
    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}