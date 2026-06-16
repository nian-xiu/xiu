package com.example.ssmshop.domain;

import java.time.LocalDateTime;

public class ServiceMessage {
    private Long id;
    private Long userId;
    private Long orderId;
    private String message;
    private String reply;
    private String status;
    private String senderRole;
    private Boolean userUnread;
    private Boolean adminUnread;
    private LocalDateTime createdAt;
    private LocalDateTime repliedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public void setSenderRole(String senderRole) {
        this.senderRole = senderRole;
    }

    public Boolean getUserUnread() {
        return userUnread;
    }

    public void setUserUnread(Boolean userUnread) {
        this.userUnread = userUnread;
    }

    public Boolean getAdminUnread() {
        return adminUnread;
    }

    public void setAdminUnread(Boolean adminUnread) {
        this.adminUnread = adminUnread;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public void setRepliedAt(LocalDateTime repliedAt) {
        this.repliedAt = repliedAt;
    }
}
