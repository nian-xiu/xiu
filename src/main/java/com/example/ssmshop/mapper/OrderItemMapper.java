package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.OrderItem;

import java.util.List;

public interface OrderItemMapper {
    int insert(OrderItem item);

    List<OrderItem> findByOrderId(Long orderId);
}
