package com.example.ssmshop.service;

import com.example.ssmshop.domain.CartItem;
import com.example.ssmshop.domain.Product;
import com.example.ssmshop.dto.CartSummary;
import com.example.ssmshop.mapper.CartMapper;
import com.example.ssmshop.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private CartService cartService;

    @Test
    void addShouldRejectZeroQuantity() {
        Product product = onSaleProduct(1L, 10);
        when(productMapper.findById(1L)).thenReturn(product);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.add(7L, 1L, 0));
        assertEquals("商品数量必须大于 0", ex.getMessage());
    }

    @Test
    void addShouldRejectOutOfStockProduct() {
        Product product = onSaleProduct(1L, 0);
        when(productMapper.findById(1L)).thenReturn(product);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cartService.add(7L, 1L, 2));
        assertEquals("商品库存不足", ex.getMessage());
    }

    @Test
    void addShouldCapQuantityByStock() {
        Product product = onSaleProduct(1L, 3);
        when(productMapper.findById(1L)).thenReturn(product);
        when(cartMapper.findByUserAndProduct(7L, 1L)).thenReturn(null);

        cartService.add(7L, 1L, 8);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartMapper).insert(captor.capture());
        assertEquals(3, captor.getValue().getQuantity());
    }

    @Test
    void summaryShouldClampToCurrentStock() {
        CartItem item = new CartItem();
        item.setId(11L);
        item.setProductId(1L);
        item.setQuantity(8);

        Product product = onSaleProduct(1L, 3);
        product.setPrice(new BigDecimal("12.50"));

        when(cartMapper.findByUserId(7L)).thenReturn(List.of(item));
        when(productMapper.findById(1L)).thenReturn(product);

        CartSummary summary = cartService.summary(7L);
        assertEquals(3, summary.getTotalQuantity());
        assertEquals(0, new BigDecimal("37.50").compareTo(summary.getTotalAmount()));
    }

    private Product onSaleProduct(Long id, int stock) {
        Product product = new Product();
        product.setId(id);
        product.setStatus("ON_SALE");
        product.setStock(stock);
        product.setPrice(new BigDecimal("10.00"));
        return product;
    }
}
