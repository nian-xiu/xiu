package com.example.ssmshop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RewardMail {
    private Long id;
    private Long userId;
    private Long senderId;
    private String senderName;
    private String title;
    private String message;
    private String rewardType;
    private Integer coinAmount;
    private String couponType;
    private String couponName;
    private Double discountRate;
    private BigDecimal thresholdAmount;
    private BigDecimal reduceAmount;
    private Integer couponQuantity;
    private LocalDateTime expiresAt;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime viewedAt;
    private LocalDateTime claimedAt;

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

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
    }

    public Integer getCoinAmount() {
        return coinAmount;
    }

    public void setCoinAmount(Integer coinAmount) {
        this.coinAmount = coinAmount;
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

    public Integer getCouponQuantity() {
        return couponQuantity;
    }

    public void setCouponQuantity(Integer couponQuantity) {
        this.couponQuantity = couponQuantity;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getViewedAt() {
        return viewedAt;
    }

    public void setViewedAt(LocalDateTime viewedAt) {
        this.viewedAt = viewedAt;
    }

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
    }

    public boolean isUnread() {
        return "UNREAD".equals(status);
    }

    public boolean isClaimed() {
        return "CLAIMED".equals(status);
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public boolean isClaimable() {
        return !isClaimed() && !isExpired();
    }

    public String getRewardLabel() {
        if ("COIN".equals(rewardType)) {
            return "金币 " + (coinAmount == null ? 0 : coinAmount);
        }
        String count = " x" + (couponQuantity == null || couponQuantity < 1 ? 1 : couponQuantity);
        if ("AMOUNT_OFF".equals(couponType)) {
            String threshold = thresholdAmount == null ? "0" : thresholdAmount.stripTrailingZeros().toPlainString();
            String reduce = reduceAmount == null ? "0" : reduceAmount.stripTrailingZeros().toPlainString();
            return "满" + threshold + "减" + reduce + count;
        }
        double rate = discountRate == null ? 0.9 : discountRate;
        return BigDecimal.valueOf(rate * 10).stripTrailingZeros().toPlainString() + "折优惠券" + count;
    }

    public String getStatusLabel() {
        if (isClaimed()) {
            return "已领取";
        }
        if (isExpired()) {
            return "已过期";
        }
        if (isUnread()) {
            return "未读";
        }
        return "已读";
    }
}
