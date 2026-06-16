package com.example.ssmshop.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityCampaign {
    private Long id;
    private String title;
    private String description;
    private String rewardType;
    private Integer coinAmount;
    private String couponType;
    private String couponName;
    private Double discountRate;
    private BigDecimal thresholdAmount;
    private BigDecimal reduceAmount;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private Integer quotaLimit;
    private Integer couponQuantity;
    private Integer couponExpiryDays;
    private Integer claimedCount;
    private String status;
    private LocalDateTime createdAt;
    private Boolean claimedByUser;
    private Integer remainingQuota;
    private Boolean claimableNow;
    private Boolean flashSale;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public Integer getQuotaLimit() {
        return quotaLimit;
    }

    public void setQuotaLimit(Integer quotaLimit) {
        this.quotaLimit = quotaLimit;
    }

    public Integer getCouponExpiryDays() {
        return couponExpiryDays;
    }

    public void setCouponExpiryDays(Integer couponExpiryDays) {
        this.couponExpiryDays = couponExpiryDays;
    }

    public Integer getCouponQuantity() {
        return couponQuantity;
    }

    public void setCouponQuantity(Integer couponQuantity) {
        this.couponQuantity = couponQuantity;
    }

    public Integer getClaimedCount() {
        return claimedCount;
    }

    public void setClaimedCount(Integer claimedCount) {
        this.claimedCount = claimedCount;
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

    public Boolean getClaimedByUser() {
        return claimedByUser;
    }

    public void setClaimedByUser(Boolean claimedByUser) {
        this.claimedByUser = claimedByUser;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(Integer remainingQuota) {
        this.remainingQuota = remainingQuota;
    }

    public Boolean getClaimableNow() {
        return claimableNow;
    }

    public void setClaimableNow(Boolean claimableNow) {
        this.claimableNow = claimableNow;
    }

    public Boolean getFlashSale() {
        return flashSale;
    }

    public void setFlashSale(Boolean flashSale) {
        this.flashSale = flashSale;
    }

    public boolean isFlashSaleEnabled() {
        return Boolean.TRUE.equals(flashSale);
    }

    public boolean isCoinReward() {
        return "COIN".equals(rewardType);
    }

    public boolean isCouponReward() {
        return "COUPON".equals(rewardType);
    }

    public boolean isActiveWindow() {
        LocalDateTime now = LocalDateTime.now();
        if (startAt != null && now.isBefore(startAt)) {
            return false;
        }
        if (endAt != null && now.isAfter(endAt)) {
            return false;
        }
        return true;
    }

    public String getRewardLabel() {
        if (isCoinReward()) {
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

    public String getWindowLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        if (startAt == null && endAt == null) {
            return "长期有效";
        }
        if (startAt != null && endAt != null) {
            return startAt.format(formatter) + " 至 " + endAt.format(formatter);
        }
        if (startAt != null) {
            return "从 " + startAt.format(formatter) + " 开始";
        }
        return "截至 " + endAt.format(formatter);
    }

    public String getQuotaLabel() {
        if (quotaLimit == null || quotaLimit <= 0) {
            return "不限量";
        }
        int claimed = claimedCount == null ? 0 : claimedCount;
        return claimed + " / " + quotaLimit;
    }

    public String getStatusLabel() {
        if (!"ACTIVE".equals(status)) {
            return "已停用";
        }
        if (!isActiveWindow()) {
            return "未到时间";
        }
        if (Boolean.TRUE.equals(claimedByUser)) {
            return "已领取";
        }
        if (remainingQuota != null && remainingQuota <= 0) {
            return "已领完";
        }
        return "可领取";
    }

    public String getRemainingTimeLabel() {
        if (endAt == null) {
            return "暂无结束时间";
        }
        long hours = Math.max(0, Duration.between(LocalDateTime.now(), endAt).toHours());
        if (hours < 24) {
            return "还剩 " + hours + " 小时";
        }
        return "还剩 " + Math.max(0, hours / 24) + " 天";
    }

    public Long getCountdownEpochMillis() {
        if (endAt == null) {
            return null;
        }
        return endAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
