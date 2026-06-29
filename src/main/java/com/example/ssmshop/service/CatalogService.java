package com.example.ssmshop.service;

import com.example.ssmshop.domain.Category;
import com.example.ssmshop.domain.Product;
import com.example.ssmshop.dto.ProductFilter;
import com.example.ssmshop.form.ProductForm;
import com.example.ssmshop.mapper.CategoryMapper;
import com.example.ssmshop.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CatalogService {
    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;
    private final ProductImageStorageService productImageStorageService;

    public CatalogService(CategoryMapper categoryMapper, ProductMapper productMapper, ProductImageStorageService productImageStorageService) {
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
        this.productImageStorageService = productImageStorageService;
    }

    public List<Category> categories() {
        return categoryMapper.findEnabled();
    }

    public List<Category> allCategories() {
        return categoryMapper.findAll();
    }

    public List<Product> search(ProductFilter filter) {
        return productMapper.search(filter);
    }

    public List<Product> search(ProductFilter filter, Long userId) {
        filter.setUserId(userId);
        return productMapper.search(filter);
    }

    public List<Product> featured() {
        return productMapper.findFeatured(null);
    }

    public List<Product> featured(Long userId) {
        return productMapper.findFeatured(userId);
    }

    public List<Product> latest(int limit) {
        return productMapper.findLatest(limit, null);
    }

    public List<Product> latest(int limit, Long userId) {
        return productMapper.findLatest(limit, userId);
    }

    public Product findProduct(Long id) {
        return productMapper.findById(id);
    }

    @Transactional
    public void saveProduct(ProductForm form, MultipartFile coverImage) {
        String uploadedCoverUrl = productImageStorageService.store(coverImage);
        if (uploadedCoverUrl != null) {
            form.setCoverUrl(uploadedCoverUrl);
        }

        Product product = new Product();
        product.setId(form.getId());
        product.setCategoryId(form.getCategoryId());
        product.setName(normalizeProductName(form.getName()));
        product.setSubtitle(form.getSubtitle());
        product.setDescription(form.getDescription());
        product.setPrice(form.getPrice());
        product.setOriginalPrice(form.getOriginalPrice());
        product.setStock(form.getStock());
        product.setCoverUrl(form.getCoverUrl());
        product.setStatus(form.getStatus());
        product.setFeatured(Boolean.TRUE.equals(form.getFeatured()));
        if (product.getId() == null) {
            productMapper.insert(product);
        } else {
            productMapper.update(product);
        }
    }

    private String normalizeProductName(String name) {
        return name == null ? null : name.replaceAll("\\s+", " ").trim();
    }

    public ProductForm toForm(Product product) {
        ProductForm form = new ProductForm();
        form.setId(product.getId());
        form.setCategoryId(product.getCategoryId());
        form.setName(product.getName());
        form.setSubtitle(product.getSubtitle());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setOriginalPrice(product.getOriginalPrice());
        form.setStock(product.getStock());
        form.setCoverUrl(product.getCoverUrl());
        form.setStatus(product.getStatus());
        form.setFeatured(product.getFeatured());
        return form;
    }

    public void deleteProduct(Long id) {
        productMapper.delete(id);
    }
}
