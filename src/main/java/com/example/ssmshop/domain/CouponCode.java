package com.example.ssmshop.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CouponCode {
    private Long id;
    private String code;
    private String title;
    private String rewardType;
    private Integer coinAmount;
    private String couponType;
    private String couponName;
    private Double discountRate;
    private BigDecimal thresholdAmount;
    private BigDecimal reduceAmount;
    private Integer couponExpiryDays;
    private Integer couponQuantity;
    private LocalDateTime expiresAt;
    private Integer totalQuota;
    private Integer redeemedCount;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getRewardType() { return rewardType; }
    public void setRewardType(String rewardType) { this.rewardType = rewardType; }
    public Integer getCoinAmount() { return coinAmount; }
    public void setCoinAmount(Integer coinAmount) { this.coinAmount = coinAmount; }
    public String getCouponType() { return couponType; }
    public void setCouponType(String couponType) { this.couponType = couponType; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public Double getDiscountRate() { return discountRate; }
    public void setDiscountRate(Double discountRate) { this.discountRate = discountRate; }
    public BigDecimal getThresholdAmount() { return thresholdAmount; }
    public void setThresholdAmount(BigDecimal thresholdAmount) { this.thresholdAmount = thresholdAmount; }
    public BigDecimal getReduceAmount() { return reduceAmount; }
    public void setReduceAmount(BigDecimal reduceAmount) { this.reduceAmount = reduceAmount; }
    public Integer getCouponExpiryDays() { return couponExpiryDays; }
    public void setCouponExpiryDays(Integer couponExpiryDays) { this.couponExpiryDays = couponExpiryDays; }
    public Integer getCouponQuantity() { return couponQuantity; }
    public void setCouponQuantity(Integer couponQuantity) { this.couponQuantity = couponQuantity; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public Integer getTotalQuota() { return totalQuota; }
    public void setTotalQuota(Integer totalQuota) { this.totalQuota = totalQuota; }
    public Integer getRedeemedCount() { return redeemedCount; }
    public void setRedeemedCount(Integer redeemedCount) { this.redeemedCount = redeemedCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    public String getStatusLabel() {
        if (!"ACTIVE".equals(status)) {
            return "已停用";
        }
        if (isExpired()) {
            return "已过期";
        }
        if (totalQuota != null && totalQuota > 0
                && redeemedCount != null && redeemedCount >= totalQuota) {
            return "已兑完";
        }
        return "可兑换";
    }

    public String getQuotaLabel() {
        int redeemed = redeemedCount == null ? 0 : redeemedCount;
        if (totalQuota == null || totalQuota <= 0) {
            return "不限量 · 已兑 " + redeemed;
        }
        return redeemed + " / " + totalQuota;
    }

    public String getRewardLabel() {
        if ("COIN".equals(rewardType)) {
            return "金币 " + (coinAmount == null ? 0 : coinAmount);
        }
        String count = " x" + (couponQuantity == null || couponQuantity < 1 ? 1 : couponQuantity);
        if ("AMOUNT_OFF".equals(couponType)) {
            String t = thresholdAmount == null ? "0" : thresholdAmount.stripTrailingZeros().toPlainString();
            String r = reduceAmount == null ? "0" : reduceAmount.stripTrailingZeros().toPlainString();
            return "满" + t + "减" + r + count;
        }
        double rate = discountRate == null ? 0.9 : discountRate;
        return BigDecimal.valueOf(rate * 10).stripTrailingZeros().toPlainString() + "折优惠券" + count;
    }
}
