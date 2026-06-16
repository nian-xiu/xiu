package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.CartItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    CartItem findByUserAndProduct(@Param("userId") Long userId, @Param("productId") Long productId);

    List<CartItem> findByUserId(Long userId);

    int insert(CartItem item);

    int updateQuantity(@Param("id") Long id, @Param("quantity") int quantity);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int clearByUserId(Long userId);
}
