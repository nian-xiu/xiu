package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper {
    List<Product> findProductsByUserId(Long userId);

    int exists(@Param("userId") Long userId, @Param("productId") Long productId);

    int insert(@Param("userId") Long userId, @Param("productId") Long productId);

    int delete(@Param("userId") Long userId, @Param("productId") Long productId);
}
