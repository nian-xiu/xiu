package com.example.ssmshop.dto;

import com.example.ssmshop.domain.Order;
import com.example.ssmshop.domain.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class OrderDetail {
    private Order order;
    private List<OrderItem> items = new ArrayList<>();

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
