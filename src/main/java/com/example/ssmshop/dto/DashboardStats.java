package com.example.ssmshop.dto;

import java.math.BigDecimal;

public class DashboardStats {
    private long productCount;
    private long activeProductCount;
    private long lowStockCount;
    private long orderCount;
    private long paidOrderCount;
    private long shippedOrderCount;
    private long completedOrderCount;
    private long userCount;
    private BigDecimal revenue = BigDecimal.ZERO;
    private BigDecimal averageOrderAmount = BigDecimal.ZERO;

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }

    public long getActiveProductCount() {
        return activeProductCount;
    }

    public void setActiveProductCount(long activeProductCount) {
        this.activeProductCount = activeProductCount;
    }

    public long getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(long lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public long getPaidOrderCount() {
        return paidOrderCount;
    }

    public void setPaidOrderCount(long paidOrderCount) {
        this.paidOrderCount = paidOrderCount;
    }

    public long getShippedOrderCount() {
        return shippedOrderCount;
    }

    public void setShippedOrderCount(long shippedOrderCount) {
        this.shippedOrderCount = shippedOrderCount;
    }

    public long getCompletedOrderCount() {
        return completedOrderCount;
    }

    public void setCompletedOrderCount(long completedOrderCount) {
        this.completedOrderCount = completedOrderCount;
    }

    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    public BigDecimal getAverageOrderAmount() {
        return averageOrderAmount;
    }

    public void setAverageOrderAmount(BigDecimal averageOrderAmount) {
        this.averageOrderAmount = averageOrderAmount;
    }
}
