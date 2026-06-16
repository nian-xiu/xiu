package com.example.ssmshop.mapper;

import com.example.ssmshop.domain.Category;

import java.util.List;

public interface CategoryMapper {
    List<Category> findEnabled();

    List<Category> findAll();

    Category findById(Long id);
}
