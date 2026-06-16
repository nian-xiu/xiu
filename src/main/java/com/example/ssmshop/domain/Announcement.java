package com.example.ssmshop.domain;

import java.time.LocalDateTime;

public class Announcement {
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String category;
    private String status;
    private Boolean pinned;
    private Long relatedCampaignId;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getPinned() {
        return pinned;
    }

    public void setPinned(Boolean pinned) {
        this.pinned = pinned;
    }

    public Long getRelatedCampaignId() {
        return relatedCampaignId;
    }

    public void setRelatedCampaignId(Long relatedCampaignId) {
        this.relatedCampaignId = relatedCampaignId;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isPublished() {
        return "PUBLISHED".equals(status);
    }

    public boolean pinnedEnabled() {
        return Boolean.TRUE.equals(pinned);
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isVisible() {
        return isPublished()
                && !isExpired()
                && (publishedAt == null || !publishedAt.isAfter(LocalDateTime.now()));
    }

    public String getCategoryLabel() {
        return switch (category == null ? "GENERAL" : category) {
            case "BENEFIT" -> "福利预告";
            case "SYSTEM" -> "系统公告";
            default -> "一般公告";
        };
    }

    public String getStatusLabel() {
        if (isExpired()) {
            return "已过期";
        }
        return switch (status == null ? "DRAFT" : status) {
            case "PUBLISHED" -> "发布";
            case "ARCHIVED" -> "已归档";
            default -> "草稿";
        };
    }
}
