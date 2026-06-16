package com.example.ssmshop.dto;

public class ProductFilter {
    private String keyword;
    private Long categoryId;
    private String sort;
    private Integer minPrice;
    private Integer maxPrice;
    private Long userId;
    private Boolean includeBlacklisted = false;
    private String status;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Boolean getIncludeBlacklisted() {
        return includeBlacklisted;
    }

    public void setIncludeBlacklisted(Boolean includeBlacklisted) {
        this.includeBlacklisted = includeBlacklisted;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
