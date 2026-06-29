package com.example.ssmshop.service;

import com.example.ssmshop.dto.DashboardStats;
import com.example.ssmshop.mapper.OrderMapper;
import com.example.ssmshop.mapper.ProductMapper;
import com.example.ssmshop.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class AdminService {
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public AdminService(ProductMapper productMapper, OrderMapper orderMapper, UserMapper userMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    public DashboardStats stats() {
        DashboardStats stats = new DashboardStats();
        stats.setProductCount(productMapper.countAll());
        stats.setActiveProductCount(productMapper.countByStatus("ON_SALE"));
        stats.setLowStockCount(productMapper.countLowStock(30));
        stats.setOrderCount(orderMapper.countAll());
        stats.setPaidOrderCount(orderMapper.countByStatus("PAID"));
        stats.setShippedOrderCount(orderMapper.countByStatus("SHIPPED"));
        stats.setCompletedOrderCount(orderMapper.countByStatus("COMPLETED"));
        stats.setConfirmedReceiptCount(orderMapper.countConfirmedReceipts());
        stats.setUserCount(userMapper.countCustomers());
        BigDecimal revenue = orderMapper.sumRevenue();
        stats.setRevenue(revenue == null ? BigDecimal.ZERO : revenue);
        BigDecimal pendingSettlement = orderMapper.sumPendingSettlement();
        stats.setPendingSettlement(pendingSettlement == null ? BigDecimal.ZERO : pendingSettlement);
        if (stats.getConfirmedReceiptCount() > 0) {
            stats.setAverageOrderAmount(stats.getRevenue().divide(BigDecimal.valueOf(stats.getConfirmedReceiptCount()), 2, RoundingMode.HALF_UP));
        }
        return stats;
    }
}
