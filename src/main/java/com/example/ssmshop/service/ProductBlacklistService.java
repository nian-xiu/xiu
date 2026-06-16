package com.example.ssmshop.service;

import com.example.ssmshop.domain.Product;
import com.example.ssmshop.mapper.FavoriteMapper;
import com.example.ssmshop.mapper.ProductBlacklistMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductBlacklistService {
    private final ProductBlacklistMapper productBlacklistMapper;
    private final FavoriteMapper favoriteMapper;

    public ProductBlacklistService(ProductBlacklistMapper productBlacklistMapper, FavoriteMapper favoriteMapper) {
        this.productBlacklistMapper = productBlacklistMapper;
        this.favoriteMapper = favoriteMapper;
    }

    public List<Product> list(Long userId) {
        return productBlacklistMapper.findProductsByUserId(userId);
    }

    public boolean exists(Long userId, Long productId) {
        return productBlacklistMapper.exists(userId, productId) > 0;
    }

    @Transactional
    public void add(Long userId, Long productId) {
        favoriteMapper.delete(userId, productId);
        productBlacklistMapper.insert(userId, productId);
    }

    public void remove(Long userId, Long productId) {
        productBlacklistMapper.delete(userId, productId);
    }
}
