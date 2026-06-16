package com.example.ssmshop.service;

import com.example.ssmshop.domain.CartItem;
import com.example.ssmshop.domain.Product;
import com.example.ssmshop.dto.CartLine;
import com.example.ssmshop.dto.CartSummary;
import com.example.ssmshop.mapper.CartMapper;
import com.example.ssmshop.mapper.ProductMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;

    public CartService(CartMapper cartMapper, ProductMapper productMapper) {
        this.cartMapper = cartMapper;
        this.productMapper = productMapper;
    }

    @Transactional
    public void add(Long userId, Long productId, int quantity) {
        Product product = productMapper.findById(productId);
        if (product == null || !"ON_SALE".equals(product.getStatus())) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        int safeQuantity = Math.max(1, Math.min(quantity, product.getStock()));
        CartItem existing = cartMapper.findByUserAndProduct(userId, productId);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(productId);
            item.setQuantity(safeQuantity);
            cartMapper.insert(item);
        } else {
            cartMapper.updateQuantity(existing.getId(), Math.min(product.getStock(), existing.getQuantity() + safeQuantity));
        }
    }

    public CartSummary summary(Long userId) {
        List<CartItem> items = cartMapper.findByUserId(userId);
        CartSummary summary = new CartSummary();
        for (CartItem item : items) {
            Product product = productMapper.findById(item.getProductId());
            if (product == null) {
                continue;
            }
            int quantity = Math.min(item.getQuantity(), product.getStock());
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            CartLine line = new CartLine();
            line.setCartItemId(item.getId());
            line.setProduct(product);
            line.setQuantity(quantity);
            line.setSubtotal(subtotal);
            summary.getLines().add(line);
            summary.setTotalQuantity(summary.getTotalQuantity() + quantity);
            summary.setTotalAmount(summary.getTotalAmount().add(subtotal));
        }
        return summary;
    }

    public void update(Long cartItemId, Long userId, int quantity) {
        com.example.ssmshop.domain.CartItem item = cartMapper.findByUserId(userId).stream()
                .filter(it -> it.getId().equals(cartItemId))
                .findFirst()
                .orElse(null);
        if (item == null) {
            return;
        }
        Product product = productMapper.findById(item.getProductId());
        int safeQuantity = Math.max(1, quantity);
        if (product != null) {
            safeQuantity = Math.min(safeQuantity, product.getStock());
        }
        cartMapper.updateQuantity(cartItemId, Math.max(1, safeQuantity));
    }

    public void remove(Long cartItemId, Long userId) {
        cartMapper.deleteByIdAndUserId(cartItemId, userId);
    }
}
