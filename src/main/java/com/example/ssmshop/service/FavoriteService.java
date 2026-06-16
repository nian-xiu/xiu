package com.example.ssmshop.service;

import com.example.ssmshop.domain.Product;
import com.example.ssmshop.mapper.FavoriteMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    public List<Product> list(Long userId) {
        return favoriteMapper.findProductsByUserId(userId);
    }

    public boolean exists(Long userId, Long productId) {
        return favoriteMapper.exists(userId, productId) > 0;
    }

    public void toggle(Long userId, Long productId) {
        if (exists(userId, productId)) {
            favoriteMapper.delete(userId, productId);
        } else {
            favoriteMapper.insert(userId, productId);
        }
    }
}
