package com.example.ssmshop.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RewardCampaignForm {
    @NotBlank(message = "请填写活动标题")
    private String title;

    private String description;

    @NotBlank(message = "请选择奖励类型")
    private String rewardType = "COIN";

    @Min(value = 1, message = "金币数量必须大于 0")
    private Integer coinAmount;

    @NotBlank(message = "请选择优惠券类型")
    private String couponType = "DISCOUNT_RATE";

    @DecimalMin(value = "0.10", message = "折扣必须大于 0")
    private Double discountRate;

    @DecimalMin(value = "0.01", message = "门槛金额必须大于 0")
    private BigDecimal thresholdAmount;

    @DecimalMin(value = "0.01", message = "减免金额必须大于 0")
    private BigDecimal reduceAmount;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;

    @Min(value = 0, message = "名额不能小于 0")
    private Integer quotaLimit;

    @Min(value = 1, message = "优惠券数量必须至少为 1")
    private Integer couponQuantity = 1;

    @Min(value = 0, message = "有效期天数不能小于 0")
    private Integer expiryDays;

    private boolean flashSale;

    public boolean isFlashSale() {
        return flashSale;
    }

    public void setFlashSale(boolean flashSale) {
        this.flashSale = flashSale;
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

    public Integer getCouponQuantity() {
        return couponQuantity;
    }

    public void setCouponQuantity(Integer couponQuantity) {
        this.couponQuantity = couponQuantity;
    }

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }
}
