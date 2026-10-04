package com.artverse.dto;

import com.artverse.entity.CommissionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private String category;
    private BigDecimal budget;
    private Integer requiredDays;
    private LocalDateTime deadline;
    private CommissionStatus status;
    private LocalDateTime createdAt;

    public CommissionResponse(
            Long id,
            Long clientId,
            String clientName,
            String title,
            String description,
            String category,
            BigDecimal budget,
            Integer requiredDays,
            LocalDateTime deadline,
            CommissionStatus status,
            LocalDateTime createdAt) {

        this.id = id;
        this.clientId = clientId;
        this.clientName = clientName;
        this.title = title;
        this.description = description;
        this.category = category;
        this.budget = budget;
        this.requiredDays = requiredDays;
        this.deadline = deadline;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getClientId() {
        return clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public Integer getRequiredDays() {
        return requiredDays;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}