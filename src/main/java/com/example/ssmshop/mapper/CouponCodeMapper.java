package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.CouponCode;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponCodeMapper {
    int insert(CouponCode code);

    CouponCode findByCode(@Param("code") String code);

    List<CouponCode> findAll();

    int increaseRedeemedCount(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") String status);

    int delete(@Param("id") Long id);

    int existsRedemption(@Param("codeId") Long codeId, @Param("userId") Long userId);

    int insertRedemption(@Param("codeId") Long codeId, @Param("userId") Long userId);
}
