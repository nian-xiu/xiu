package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Product;
import com.example.ssmshop.dto.ProductFilter;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    List<Product> search(ProductFilter filter);

    List<Product> findFeatured(@Param("userId") Long userId);

    List<Product> findLatest(@Param("limit") int limit, @Param("userId") Long userId);

    Product findById(Long id);

    long countAll();

    long countByStatus(@Param("status") String status);

    long countLowStock(@Param("threshold") int threshold);

    int insert(Product product);

    int update(Product product);

    int delete(Long id);

    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    int increaseSales(@Param("productId") Long productId, @Param("quantity") int quantity);
}
