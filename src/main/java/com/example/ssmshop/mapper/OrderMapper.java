package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Order;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderMapper {
    int insert(Order order);

    Order findById(Long id);

    Order findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    List<Order> findByUserId(Long userId);

    List<Order> findAll(@Param("status") String status);

    long countAll();

    long countByStatus(@Param("status") String status);

    BigDecimal sumRevenue();

    List<Order> findPendingAutoShip();

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int updateStatusByUserId(@Param("id") Long id, @Param("userId") Long userId, @Param("status") String status);
}
