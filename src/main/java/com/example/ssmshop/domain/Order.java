package com.example.ssmshop.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String addressSnapshot;
    private BigDecimal totalAmount;
    private BigDecimal originalAmount;
    private Integer coinUsed;
    private BigDecimal coinDiscount;
    private Long couponId;
    private BigDecimal couponDiscount;
    private String status;
    private String paymentMethod;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private Integer estimatedDeliveryDays;
    private LocalDateTime autoShipAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddressSnapshot() {
        return addressSnapshot;
    }

    public void setAddressSnapshot(String addressSnapshot) {
        this.addressSnapshot = addressSnapshot;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public Integer getCoinUsed() {
        return coinUsed;
    }

    public void setCoinUsed(Integer coinUsed) {
        this.coinUsed = coinUsed;
    }

    public BigDecimal getCoinDiscount() {
        return coinDiscount;
    }

    public void setCoinDiscount(BigDecimal coinDiscount) {
        this.coinDiscount = coinDiscount;
    }

    public Long getCouponId() {
        return couponId;
    }

    public void setCouponId(Long couponId) {
        this.couponId = couponId;
    }

    public BigDecimal getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(BigDecimal couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentMethodLabel() {
        if ("WECHAT".equals(paymentMethod)) {
            return "微信支付";
        }
        if ("COD".equals(paymentMethod)) {
            return "货到付款";
        }
        if ("ONLINE".equals(paymentMethod)) {
            return "在线支付";
        }
        return paymentMethod == null ? "在线支付" : paymentMethod;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public Integer getEstimatedDeliveryDays() {
        return estimatedDeliveryDays;
    }

    public void setEstimatedDeliveryDays(Integer estimatedDeliveryDays) {
        this.estimatedDeliveryDays = estimatedDeliveryDays;
    }

    public LocalDateTime getAutoShipAt() {
        return autoShipAt;
    }

    public void setAutoShipAt(LocalDateTime autoShipAt) {
        this.autoShipAt = autoShipAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(LocalDateTime shippedAt) {
        this.shippedAt = shippedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getStatusLabel() {
        if ("PAID".equals(status)) {
            return "待发货";
        }
        if ("SHIPPED".equals(status)) {
            return "运输中";
        }
        if ("COMPLETED".equals(status)) {
            return "已送达";
        }
        if ("REFUND_REQUESTED".equals(status)) {
            return "退款处理中";
        }
        if ("REFUNDED".equals(status)) {
            return "已退款";
        }
        if ("CANCELLED".equals(status)) {
            return "已取消";
        }
        return status;
    }

    public String getDiscountText() {
        if (coinUsed != null && coinUsed > 0) {
            return "金币抵扣 " + coinUsed + " 金币，抵扣 ¥" + coinDiscount;
        }
        if (couponId != null) {
            return "优惠券抵扣 ¥" + couponDiscount;
        }
        return "未使用优惠";
    }

    public int getInitialDeliveryDays() {
        if (estimatedDeliveryDays != null && estimatedDeliveryDays > 0) {
            return estimatedDeliveryDays;
        }
        long seed = id == null ? 0L : id;
        return 3 + (int) Math.floorMod(seed, 5);
    }

    public int getRemainingDeliveryDays() {
        if ("COMPLETED".equals(status) || "REFUNDED".equals(status) || "CANCELLED".equals(status)) {
            return 0;
        }
        LocalDateTime baseTime = shippedAt != null ? shippedAt : createdAt;
        if (baseTime == null) {
            return getInitialDeliveryDays();
        }
        long elapsedDays = Math.max(0, Duration.between(baseTime, LocalDateTime.now()).toDays());
        return Math.max(0, getInitialDeliveryDays() - (int) elapsedDays);
    }

    public long getAutoShipHoursLeft() {
        if (!"PAID".equals(status) || autoShipAt == null || shippedAt != null) {
            return 0L;
        }
        long hours = Duration.between(LocalDateTime.now(), autoShipAt).toHours();
        return Math.max(0L, hours);
    }

    public String getDeliveryText() {
        if ("REFUND_REQUESTED".equals(status)) {
            return "退款处理中";
        }
        if ("REFUNDED".equals(status)) {
            return "已退款";
        }
        if ("CANCELLED".equals(status)) {
            return "订单已取消";
        }
        if ("PAID".equals(status) && shippedAt == null) {
            long hoursLeft = getAutoShipHoursLeft();
            return hoursLeft <= 0 ? "即将发货" : "预计 " + hoursLeft + " 小时内发货";
        }
        int remainingDays = getRemainingDeliveryDays();
        return remainingDays <= 0 ? "已送达" : "预计 " + remainingDays + " 天后送达";
    }

    public boolean isRefundable() {
        return "PAID".equals(status) || "SHIPPED".equals(status);
    }

    public boolean isReceivable() {
        return "SHIPPED".equals(status) && getRemainingDeliveryDays() <= 0;
    }
}
