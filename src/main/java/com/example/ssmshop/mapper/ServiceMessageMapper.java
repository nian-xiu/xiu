package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.ServiceMessage;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ServiceMessageMapper {
    int insert(ServiceMessage message);

    List<ServiceMessage> findByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);

    List<ServiceMessage> findByOrderId(@Param("orderId") Long orderId);

    List<ServiceMessage> findByUserId(@Param("userId") Long userId);

    List<ServiceMessage> findAll();

    int countAdminUnread();

    int countUserUnread(@Param("userId") Long userId);

    int markAdminRead(@Param("orderId") Long orderId);

    int markAllAdminRead();

    int markUserRead(@Param("userId") Long userId, @Param("orderId") Long orderId);

    int markAllUserRead(@Param("userId") Long userId);
}
