
package com.artverse.dto;

import com.artverse.entity.CommissionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CommissionResponse {

    private String id;
    private String clientId;
    private String clientName;
    private String title;
    private String description;
    private String category;
    private BigDecimal budget;
    private Integer requiredDays;
    private LocalDateTime deadline;
    private CommissionStatus status;
    private LocalDateTime createdAt;

    public CommissionResponse() {
    }

    public CommissionResponse(
            String id,
            String clientId,
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public Integer getRequiredDays() {
        return requiredDays;
    }

    public void setRequiredDays(Integer requiredDays) {
        this.requiredDays = requiredDays;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }

    public CommissionStatus getStatus() {
        return status;
    }

    public void setStatus(CommissionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
