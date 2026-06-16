package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.UserCoupon;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserCouponMapper {
    int insert(UserCoupon coupon);

    List<UserCoupon> findUnusedByUserId(Long userId);

    List<UserCoupon> findByUserId(Long userId);

    UserCoupon findUnusedByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int markUsed(@Param("id") Long id, @Param("userId") Long userId, @Param("orderId") Long orderId);

    int deleteInactiveByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int deleteInactiveByUserId(Long userId);
}
