package com.example.ssmshop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserCoupon {
    private Long id;
    private Long userId;
    private String couponType;
    private String couponName;
    private Double discountRate;
    private BigDecimal thresholdAmount;
    private BigDecimal reduceAmount;
    private LocalDateTime expiresAt;
    private String sourceType;
    private Long sourceRefId;
    private String status;
    private Integer quantity;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    private Long orderId;

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

    public String getCouponType() {
        return couponType;
    }

    public void setCouponType(String couponType) {
        this.couponType = couponType;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getThresholdAmount() {
        return thresholdAmount;
    }

    public void setThresholdAmount(BigDecimal thresholdAmount) {
        this.thresholdAmount = thresholdAmount;
    }

    public BigDecimal getReduceAmount() {
        return reduceAmount;
    }

    public void setReduceAmount(BigDecimal reduceAmount) {
        this.reduceAmount = reduceAmount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceRefId() {
        return sourceRefId;
    }

    public void setSourceRefId(Long sourceRefId) {
        this.sourceRefId = sourceRefId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isActive() {
        return "UNUSED".equals(status) && !isExpired() && getUsableQuantity() > 0;
    }

    public int getUsableQuantity() {
        return quantity == null || quantity < 1 ? 1 : quantity;
    }

    public String getDisplayName() {
        if (couponName != null && !couponName.isBlank()) {
            return couponName;
        }
        if ("AMOUNT_OFF".equals(couponType)) {
            return "满减券";
        }
        return "优惠券";
    }

    public String getRuleText() {
        if ("AMOUNT_OFF".equals(couponType)) {
            String threshold = thresholdAmount == null ? "0" : thresholdAmount.stripTrailingZeros().toPlainString();
            String reduce = reduceAmount == null ? "0" : reduceAmount.stripTrailingZeros().toPlainString();
            return "满" + threshold + "减" + reduce;
        }
        double rate = discountRate == null ? 0.9 : discountRate;
        return BigDecimal.valueOf(rate * 10).stripTrailingZeros().toPlainString() + "折";
    }

    public String getStatusText() {
        if ("USED".equals(status)) {
            return "已使用";
        }
        if (isExpired()) {
            return "已过期";
        }
        return getUsableQuantity() > 0 ? "可使用" : "已使用";
    }

    public String getSourceLabel() {
        if (sourceType == null || sourceType.isBlank()) {
            return "系统发放";
        }
        return switch (sourceType) {
            case "CHECKIN" -> "签到奖励";
            case "ACTIVITY" -> "活动领取";
            case "MAIL" -> "邮件领取";
            default -> sourceType;
        };
    }
}
